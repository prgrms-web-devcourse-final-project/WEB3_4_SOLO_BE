package com.pleasybank.security.oauth2

import com.pleasybank.entity.oauthprovider.OAuthProvider
import com.pleasybank.entity.user.User
import com.pleasybank.entity.useroauth.UserOAuth
import com.pleasybank.repository.OAuthProviderRepository
import com.pleasybank.repository.UserOAuthRepository
import com.pleasybank.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val oAuthProviderRepository: OAuthProviderRepository,
    private val userOAuthRepository: UserOAuthRepository,
    private val passwordEncoder: PasswordEncoder
) : DefaultOAuth2UserService() {
    
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        
        val registrationId = userRequest.clientRegistration.registrationId
        val provider = getOrCreateProvider(registrationId)
        
        val extractedData = when (registrationId.toLowerCase()) {
            "kakao" -> processKakaoUser(oAuth2User)
            // 다른 OAuth 제공자 추가 가능 (네이버, 구글 등)
            else -> throw OAuth2AuthenticationException("Unsupported OAuth provider: $registrationId")
        }
        
        val oauthUserId = extractedData.id
        
        // OAuth 연결된 사용자 찾기
        val userOAuth = userOAuthRepository.findByProviderIdAndOauthUserId(provider.id!!, oauthUserId)
            .orElseGet {
                // 연결된 사용자가 없으면 새로 생성
                val email = extractedData.email
                
                val user = userRepository.findByEmail(email).orElseGet {
                    User(
                        email = email,
                        password = passwordEncoder.encode(UUID.randomUUID().toString()),
                        name = extractedData.name,
                        phoneNumber = "", // OAuth에서 획득 불가능한 경우 빈 값 설정
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        status = "ACTIVE"
                    )
                }
                
                if (user.id == null) {
                    userRepository.save(user)
                }
                
                val newUserOAuth = UserOAuth(
                    user = user,
                    provider = provider,
                    oauthUserId = oauthUserId,
                    accessToken = userRequest.accessToken.tokenValue,
                    tokenExpiresAt = LocalDateTime.now().plusSeconds(userRequest.accessToken.expiresAt?.epochSecond?.minus(System.currentTimeMillis() / 1000) ?: 0),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                userOAuthRepository.save(newUserOAuth)
            }
        
        // 사용자 로그인 시간 업데이트
        userOAuth.user.lastLoginAt = LocalDateTime.now()
        userRepository.save(userOAuth.user)
        
        // OAuth 정보 업데이트
        userOAuth.accessToken = userRequest.accessToken.tokenValue
        userOAuth.tokenExpiresAt = LocalDateTime.now().plusSeconds(userRequest.accessToken.expiresAt?.epochSecond?.minus(System.currentTimeMillis() / 1000) ?: 0)
        userOAuth.updatedAt = LocalDateTime.now()
        userOAuthRepository.save(userOAuth)
        
        val attributes: Map<String, Any> = mapOf(
            "id" to userOAuth.user.id!!,
            "email" to userOAuth.user.email,
            "name" to userOAuth.user.name,
            "oauthId" to userOAuth.oauthUserId,
            "provider" to provider.providerName
        )
        
        val authorities = Collections.singleton(SimpleGrantedAuthority("ROLE_USER"))
        
        return DefaultOAuth2User(authorities, attributes, "email")
    }
    
    private fun getOrCreateProvider(providerName: String): OAuthProvider {
        return oAuthProviderRepository.findByProviderName(providerName.toUpperCase())
            .orElseGet {
                val newProvider = OAuthProvider(
                    providerName = providerName.toUpperCase(),
                    isActive = true
                )
                oAuthProviderRepository.save(newProvider)
            }
    }
    
    private fun processKakaoUser(oAuth2User: OAuth2User): OAuthUserData {
        val attributes = oAuth2User.attributes
        
        val id = attributes["id"].toString()
        val kakaoAccount = attributes["kakao_account"] as Map<*, *>
        val profile = kakaoAccount["profile"] as Map<*, *>
        
        val email = kakaoAccount["email"] as? String ?: "$id@kakao.user"
        val name = profile["nickname"] as? String ?: "사용자"
        
        return OAuthUserData(id, email, name)
    }
}

data class OAuthUserData(
    val id: String,
    val email: String,
    val name: String
) 