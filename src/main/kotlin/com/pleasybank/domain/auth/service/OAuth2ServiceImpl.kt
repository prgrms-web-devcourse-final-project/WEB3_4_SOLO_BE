package com.pleasybank.domain.auth.service

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.domain.auth.dto.TokenResponse
import com.pleasybank.core.security.JwtTokenProvider
import com.pleasybank.domain.user.entity.User
import com.pleasybank.domain.user.repository.UserRepository
import com.pleasybank.integration.kakao.KakaoApiClient
import com.pleasybank.integration.kakao.dto.KakaoUserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class OAuth2ServiceImpl(
    private val kakaoApiClient: KakaoApiClient,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) : OAuth2Service {
    
    private val logger = LoggerFactory.getLogger(OAuth2ServiceImpl::class.java)
    
    @Transactional
    override fun processKakaoLogin(code: String, redirectUri: String): TokenResponse {
        logger.info("카카오 로그인 처리 시작")
        
        // 1. 카카오 인증 코드로 액세스 토큰 요청
        val kakaoToken = kakaoApiClient.getAccessToken(code, redirectUri)
        logger.info("카카오 토큰 획득 성공")
        
        // 2. 카카오 액세스 토큰으로 사용자 정보 요청
        val kakaoUserInfo = kakaoApiClient.getUserInfo(kakaoToken.accessToken)
        logger.info("카카오 사용자 정보 획득 성공: id=${kakaoUserInfo.id}")
        
        // 3. 이메일 정보가 없는 경우 예외 처리
        val email = kakaoUserInfo.kakaoAccount.email
            ?: throw ResourceNotFoundException("카카오 계정에서 이메일 정보를 가져올 수 없습니다. 이메일 제공에 동의해주세요.")
        
        // 4. 해당 providerId로 가입된 사용자가 있는지 확인
        val kakaoId = kakaoUserInfo.id.toString()
        val existingUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId)
        
        val user = if (existingUser != null) {
            // 5-1. 기존 사용자인 경우 로그인 시간 업데이트
            existingUser.lastLoginAt = LocalDateTime.now()
            existingUser.updatedAt = LocalDateTime.now()
            existingUser.profileImageUrl = updateProfileIfNeeded(existingUser.profileImageUrl, kakaoUserInfo)
            userRepository.save(existingUser)
            existingUser
        } else {
            // 5-2. 신규 사용자인 경우 회원 가입 처리
            val nickname = kakaoUserInfo.kakaoAccount.profile.nickname ?: "카카오 사용자"
            val profileImage = kakaoUserInfo.kakaoAccount.profile.profileImageUrl
            
            // 랜덤 비밀번호 생성 (소셜 로그인 사용자는 비밀번호 사용 안 함)
            val randomPassword = UUID.randomUUID().toString()
            
            val newUser = User(
                email = email,
                password = passwordEncoder.encode(randomPassword),
                name = nickname,
                phoneNumber = null,
                profileImageUrl = profileImage,
                lastLoginAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                status = "ACTIVE",
                provider = "KAKAO",
                providerId = kakaoId
            )
            
            userRepository.save(newUser)
        }
        
        // 6. JWT 토큰 생성 및 반환
        val accessToken = jwtTokenProvider.createToken(user.id!!, listOf("ROLE_USER"))
        val refreshToken = UUID.randomUUID().toString()
        
        logger.info("카카오 로그인 처리 완료: ${user.email}")
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer"
        )
    }
    
    /**
     * 프로필 이미지가 변경되었는지 확인하고 업데이트
     */
    private fun updateProfileIfNeeded(
        currentProfileUrl: String?, 
        kakaoUserInfo: KakaoUserInfoResponse
    ): String? {
        val newProfileUrl = kakaoUserInfo.kakaoAccount.profile.profileImageUrl
        
        // 새 프로필 이미지가 있고, 기존 이미지와 다른 경우에만 업데이트
        return if (newProfileUrl != null && newProfileUrl != currentProfileUrl) {
            newProfileUrl
        } else {
            currentProfileUrl
        }
    }
} 