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

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : DefaultOAuth2UserService() {
    
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        
        return processOAuth2User(userRequest, oAuth2User)
    }
    
    @Transactional
    protected fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            userRequest.clientRegistration.registrationId,
            oAuth2User.attributes
        )

        if (oAuth2UserInfo.getEmail().isNullOrBlank()) {
            throw IllegalArgumentException("이메일을 찾을 수 없습니다.")
        }

        val email = oAuth2UserInfo.getEmail() ?: throw IllegalArgumentException("이메일이 필요합니다.")
        
        var user = userRepository.findByEmail(email).orElse(null)

        if (user != null) {
            user = updateExistingUser(user, oAuth2UserInfo)
        } else {
            user = registerNewUser(userRequest, oAuth2UserInfo)
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
    
    @Transactional
    protected fun registerNewUser(userRequest: OAuth2UserRequest, oAuth2UserInfo: OAuth2UserInfo): User {
        val now = LocalDateTime.now()
        val email = oAuth2UserInfo.getEmail() ?: throw IllegalArgumentException("이메일이 필요합니다.")
        val imageUrl = oAuth2UserInfo.getImageUrl() ?: ""
        
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
        val updatedUser = user.copy(
            name = oAuth2UserInfo.getName() ?: user.name,
            profileImageUrl = imageUrl,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }
} 