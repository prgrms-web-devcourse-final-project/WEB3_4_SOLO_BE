package com.pleasybank.domain.auth.service

import com.pleasybank.core.exception.DuplicateResourceException
import com.pleasybank.core.exception.InvalidCredentialsException
import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.core.security.JwtTokenProvider
import com.pleasybank.core.service.CacheService
import com.pleasybank.domain.auth.dto.LoginRequest
import com.pleasybank.domain.auth.dto.SignupRequest
import com.pleasybank.domain.auth.dto.TokenResponse
import com.pleasybank.domain.user.entity.User
import com.pleasybank.domain.user.repository.UserRepository
import com.pleasybank.integration.kakao.KakaoApiClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val cacheService: CacheService,
    private val kakaoApiClient: KakaoApiClient,
    
    @Value("\${app.oauth2.kakao.client-id}")
    private val kakaoClientId: String,
    
    @Value("\${app.oauth2.kakao.redirect-uri}")
    private val kakaoRedirectUri: String
) : AuthService {

    companion object {
        private const val REFRESH_TOKEN_TTL = 604800L // 7일 (초 단위)
        private const val TOKEN_BLACKLIST_TTL = 86400L // 1일 (초 단위)
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun signup(request: SignupRequest): TokenResponse {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("이미 사용 중인 이메일입니다: ${request.email}")
        }

        // 비밀번호 확인 체크
        if (request.password != request.confirmPassword) {
            throw IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
        }

        // 사용자 생성
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            phoneNumber = request.phoneNumber,
            profileImageUrl = null,
            lastLoginAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = "ACTIVE",
            provider = "LOCAL",
            providerId = null
        )

        val savedUser = userRepository.save(user)

        // JWT 토큰 발급
        val accessToken = jwtTokenProvider.createToken(savedUser.id!!, listOf("ROLE_USER"))
        val refreshToken = UUID.randomUUID().toString()

        // 리프레시 토큰 저장
        cacheService.setValue("refresh_token:$refreshToken", savedUser.id, REFRESH_TOKEN_TTL)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer"
        )
    }

    @Transactional
    override fun login(request: LoginRequest): TokenResponse {
        try {
            // 인증 시도
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )

            // 사용자 정보 가져오기
            val user = userRepository.findByEmail(request.email)
                ?: throw InvalidCredentialsException("유효하지 않은 이메일 또는 비밀번호입니다.")

            // 로그인 시간 업데이트
            user.lastLoginAt = LocalDateTime.now()
            user.updatedAt = LocalDateTime.now()
            userRepository.save(user)

            // JWT 토큰 발급
            val accessToken = jwtTokenProvider.createToken(user.id!!, listOf("ROLE_USER"))
            val refreshToken = UUID.randomUUID().toString()

            // 리프레시 토큰 저장
            cacheService.setValue("refresh_token:$refreshToken", user.id, REFRESH_TOKEN_TTL)

            return TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = "Bearer"
            )
        } catch (e: AuthenticationException) {
            throw InvalidCredentialsException("유효하지 않은 이메일 또는 비밀번호입니다.")
        }
    }

    @Transactional
    override fun refreshToken(refreshToken: String): TokenResponse {
        // 리프레시 토큰 검증
        val userId = cacheService.getValue("refresh_token:$refreshToken") as? Long
            ?: throw InvalidCredentialsException("유효하지 않은 리프레시 토큰입니다.")

        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw InvalidCredentialsException("사용자 정보를 찾을 수 없습니다.")
        }

        // 새 토큰 발급
        val accessToken = jwtTokenProvider.createToken(userId, listOf("ROLE_USER"))
        val newRefreshToken = UUID.randomUUID().toString()

        // 기존 리프레시 토큰 삭제 및 새 토큰 저장
        cacheService.delete("refresh_token:$refreshToken")
        cacheService.setValue("refresh_token:$newRefreshToken", userId, REFRESH_TOKEN_TTL)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            tokenType = "Bearer"
        )
    }

    @Transactional
    override fun logout(token: String) {
        // 토큰 블랙리스트에 추가
        cacheService.setValue("token_blacklist:$token", true, TOKEN_BLACKLIST_TTL)

        // 추가 로직 (필요시)
        try {
            // 필요한 경우 토큰에서 사용자 ID를 추출하여 추가 처리
            // jwtTokenProvider.getUserIdFromToken(token)
        } catch (e: Exception) {
            // 토큰 파싱 실패는 무시
        }
    }

    @Transactional
    override fun processKakaoLogin(code: String): TokenResponse {
        // 카카오 토큰 교환
        val kakaoToken = kakaoApiClient.getAccessToken(code, kakaoRedirectUri)
        
        // 카카오 사용자 정보 조회
        val userInfo = kakaoApiClient.getUserInfo(kakaoToken.accessToken)
        
        // 카카오 계정 정보로 사용자 조회 또는 생성
        val kakaoAccount = userInfo.kakaoAccount
        val kakaoId = userInfo.id.toString()
        
        // 기존 사용자 확인 (providerId로 검색)
        val existingUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId)
        
        // 저장될 사용자와 ID
        val savedUser: User
        
        if (existingUser == null) {
            // 카카오 ID 기반으로 임시 이메일 생성
            val tempEmail = "kakao_${kakaoId}@pleasybank.com"
            
            // 새 사용자 생성
            val newUser = User(
                email = tempEmail,
                name = kakaoAccount.profile.nickname ?: "카카오사용자",
                // OAuth 로그인이므로 임의의 비밀번호 설정
                password = passwordEncoder.encode(UUID.randomUUID().toString()),
                profileImageUrl = kakaoAccount.profile.profileImageUrl,
                lastLoginAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = "ACTIVE",
                provider = "KAKAO",
                providerId = kakaoId
            )
            savedUser = userRepository.save(newUser)
        } else {
            // 기존 사용자 업데이트
            existingUser.lastLoginAt = LocalDateTime.now()
            existingUser.updatedAt = LocalDateTime.now()
            // 프로필 정보 업데이트 (선택적)
            kakaoAccount.profile.nickname?.let { existingUser.name = it }
            kakaoAccount.profile.profileImageUrl?.let { existingUser.profileImageUrl = it }
            
            savedUser = userRepository.save(existingUser)
        }
        
        // JWT 토큰 발급
        val accessToken = jwtTokenProvider.createToken(savedUser.id!!, listOf("ROLE_USER"))
        val refreshToken = UUID.randomUUID().toString()
        
        // 리프레시 토큰 저장
        cacheService.setValue("refresh_token:$refreshToken", savedUser.id!!, REFRESH_TOKEN_TTL)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer"
        )
    }

    @Transactional
    override fun processExternalLogin(provider: String, providerId: String, name: String): TokenResponse {
        logger.debug("외부 로그인 처리 - provider: $provider, providerId: $providerId, name: $name")
        
        // 기존 사용자 조회 (providerId로 검색)
        val existingUser = userRepository.findByProviderAndProviderId(provider, providerId)
        
        // 저장될 사용자
        val savedUser = if (existingUser == null) {
            // 임시 이메일 생성
            val tempEmail = "${provider.lowercase()}_${providerId}@pleasybank.com"
            
            // 새 사용자 생성
            val newUser = User(
                email = tempEmail,
                name = name,
                // OAuth 로그인이므로 임의의 비밀번호 설정
                password = passwordEncoder.encode(java.util.UUID.randomUUID().toString()),
                provider = provider,
                providerId = providerId,
                status = "ACTIVE",
                lastLoginAt = java.time.LocalDateTime.now(),
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
            
            logger.debug("새 사용자 생성: $tempEmail ($name)")
            userRepository.save(newUser)
        } else {
            // 기존 사용자 업데이트
            existingUser.apply {
                this.lastLoginAt = java.time.LocalDateTime.now()
                this.updatedAt = java.time.LocalDateTime.now()
                // 필요한 경우 이름 업데이트
                if (this.name != name && name.isNotBlank()) {
                    this.name = name
                }
            }
            
            logger.debug("기존 사용자 로그인: ${existingUser.email} (${existingUser.name})")
            userRepository.save(existingUser)
        }
        
        // JWT 토큰 발급
        val accessToken = jwtTokenProvider.createToken(savedUser.id!!, listOf("ROLE_USER"))
        val refreshToken = java.util.UUID.randomUUID().toString()
        
        // 리프레시 토큰 저장
        cacheService.setValue("refresh_token:$refreshToken", savedUser.id!!, REFRESH_TOKEN_TTL)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer"
        )
    }
} 