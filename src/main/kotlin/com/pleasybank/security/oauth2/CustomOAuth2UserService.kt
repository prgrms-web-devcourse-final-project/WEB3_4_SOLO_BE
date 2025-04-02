package com.pleasybank.security.oauth2

import com.pleasybank.user.entity.User
import com.pleasybank.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import org.slf4j.LoggerFactory

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : DefaultOAuth2UserService() {
    
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        logger.info("OAuth2 사용자 정보 로드 시작: provider=${userRequest.clientRegistration.registrationId}")
        
        try {
            val oAuth2User = super.loadUser(userRequest)
            
            // 사용자 정보 로깅 (민감 정보 일부만)
            logger.info("OAuth2 사용자 속성: ${oAuth2User.attributes.keys}")
            if (oAuth2User.attributes.containsKey("id")) {
                logger.info("OAuth2 사용자 ID: ${oAuth2User.attributes["id"]}")
            }
            
            return processOAuth2User(userRequest, oAuth2User)
        } catch (e: Exception) {
            logger.error("OAuth2 사용자 정보 로드 중 오류 발생", e)
            throw e
        }
    }
    
    @Transactional
    protected fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        logger.info("OAuth2 사용자 정보 처리 시작: provider=${userRequest.clientRegistration.registrationId}")
        
        try {
            val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                userRequest.clientRegistration.registrationId,
                oAuth2User.attributes
            )

            logger.info("OAuth2 사용자 정보: id=${oAuth2UserInfo.getId()}, name=${oAuth2UserInfo.getName()}, hasEmail=${oAuth2UserInfo.getEmail() != null}")

            // 카카오의 경우 특별 처리
            if (userRequest.clientRegistration.registrationId == "kakao") {
                // 카카오는 email이 없을 수 있으므로 ID를 이용해서 사용자 검색
                val kakaoId = oAuth2UserInfo.getId()
                val email = oAuth2UserInfo.getEmail() ?: "kakao_${kakaoId}@example.com"
                
                // 이미 가입된 사용자인지 확인
                var user = userRepository.findByProviderAndProviderId("kakao", kakaoId).orElse(null)
                
                if (user != null) {
                    logger.info("기존 카카오 사용자 발견: id=${user.id}")
                    user = updateExistingUser(user, oAuth2UserInfo)
                } else {
                    // 이메일로 한번 더 체크
                    user = userRepository.findByEmail(email).orElse(null)
                    
                    if (user != null) {
                        logger.info("이메일로 기존 사용자 발견: id=${user.id}")
                        // 기존 사용자이지만 카카오 정보가 없는 경우 업데이트
                        user = user.copy(
                            provider = "kakao",
                            providerId = kakaoId,
                            updatedAt = LocalDateTime.now()
                        )
                        user = userRepository.save(user)
                    } else {
                        logger.info("새 카카오 사용자 등록")
                        user = registerNewUser(userRequest, oAuth2UserInfo, email)
                    }
                }
                
                val attributes = mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "name" to user.name,
                    "provider" to user.provider
                )
                
                logger.info("카카오 사용자 인증 완료: id=${user.id}")
                
                return DefaultOAuth2User(
                    Collections.singleton(SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "id"
                )
            } else {
                // 기존 로직: 이메일 기반 처리
                if (oAuth2UserInfo.getEmail().isNullOrBlank()) {
                    throw IllegalArgumentException("이메일을 찾을 수 없습니다.")
                }

                val email = oAuth2UserInfo.getEmail() ?: throw IllegalArgumentException("이메일이 필요합니다.")
                
                var user = userRepository.findByEmail(email).orElse(null)

                if (user != null) {
                    user = updateExistingUser(user, oAuth2UserInfo)
                } else {
                    user = registerNewUser(userRequest, oAuth2UserInfo, email)
                }

                val attributes = mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "name" to user.name,
                    "provider" to user.provider
                )

                return DefaultOAuth2User(
                    Collections.singleton(SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "id"
                )
            }
        } catch (e: Exception) {
            logger.error("OAuth2 사용자 정보 처리 중 오류 발생", e)
            throw e
        }
    }
    
    @Transactional
    protected fun registerNewUser(
        userRequest: OAuth2UserRequest, 
        oAuth2UserInfo: OAuth2UserInfo,
        email: String
    ): User {
        val now = LocalDateTime.now()
        val imageUrl = oAuth2UserInfo.getImageUrl() ?: ""
        
        logger.info("새 사용자 등록: email=$email, provider=${userRequest.clientRegistration.registrationId}")
        
        val user = User(
            email = email,
            password = passwordEncoder.encode(""), // OAuth2 사용자는 비밀번호 없음
            name = oAuth2UserInfo.getName() ?: "사용자",
            phoneNumber = "",
            profileImageUrl = imageUrl,
            provider = userRequest.clientRegistration.registrationId,
            providerId = oAuth2UserInfo.getId(),
            createdAt = now,
            updatedAt = now,
            status = "ACTIVE"
        )

        return userRepository.save(user)
    }
    
    @Transactional
    protected fun updateExistingUser(user: User, oAuth2UserInfo: OAuth2UserInfo): User {
        val imageUrl = oAuth2UserInfo.getImageUrl() ?: user.profileImageUrl
        
        logger.info("기존 사용자 정보 업데이트: id=${user.id}")
        
        val updatedUser = user.copy(
            name = oAuth2UserInfo.getName() ?: user.name,
            profileImageUrl = imageUrl,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }
} 