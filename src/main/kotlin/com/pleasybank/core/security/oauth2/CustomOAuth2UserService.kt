package com.pleasybank.core.security.oauth2

import com.pleasybank.domain.user.entity.User
import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * OAuth2 인증 사용자 정보를 처리하는 서비스
 */
@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        
        val provider = userRequest.clientRegistration.registrationId
        val providerId = oAuth2User.getAttribute<String>("sub") ?: oAuth2User.getAttribute<String>("id")?.toString()
        val name = oAuth2User.getAttribute<String>("name") 
            ?: oAuth2User.getAttribute<Map<String, Any>>("properties")?.get("nickname")?.toString()
            ?: "사용자"
        
        if (providerId == null) {
            throw IllegalArgumentException("OAuth2 사용자 ID를 가져올 수 없습니다")
        }
        
        // 기존에 providerId로 등록된 사용자 확인
        val userByProvider = userRepository.findByProviderAndProviderId(provider, providerId)
        
        val email = oAuth2User.getAttribute<String>("email") 
            ?: userByProvider?.email
            ?: "${providerId}@${provider.lowercase()}.pleasybank.com" // 임시 이메일 형식

        val user = if (userByProvider != null) {
            // 기존 사용자 정보 업데이트
            userByProvider.also {
                it.lastLoginAt = LocalDateTime.now()
                it.updatedAt = LocalDateTime.now()
                userRepository.save(it)
            }
        } else {
            // 이메일로 사용자 조회 시도
            val userByEmail = userRepository.findByEmail(email)
            
            if (userByEmail != null) {
                // 기존 이메일 사용자에 OAuth 정보 추가
                userByEmail.also {
                    it.provider = provider
                    it.providerId = providerId
                    it.lastLoginAt = LocalDateTime.now()
                    it.updatedAt = LocalDateTime.now()
                    userRepository.save(it)
                }
            } else {
                // 새 사용자 생성
                val newUser = User(
                    email = email,
                    name = name,
                    // OAuth 로그인이므로 임의의 비밀번호 설정
                    password = UUID.randomUUID().toString(), 
                    provider = provider,
                    providerId = providerId,
                    lastLoginAt = LocalDateTime.now(),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                userRepository.save(newUser)
            }
        }
        
        val userAttributes = oAuth2User.attributes
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        
        // nameAttributeKey를 id로 설정 (카카오의 경우)
        val nameAttributeKey = if (provider == "kakao") "id" else "sub"
        
        return DefaultOAuth2User(
            authorities,
            userAttributes,
            nameAttributeKey
        )
    }
} 