package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.dto.*
import com.pleasybank.domain.auth.entity.PasswordReset
import com.pleasybank.domain.auth.entity.UserOAuth
import com.pleasybank.domain.auth.repository.OAuthProviderRepository
import com.pleasybank.domain.auth.repository.PasswordResetRepository
import com.pleasybank.domain.auth.repository.UserOAuthRepository
import com.pleasybank.core.security.jwt.JwtTokenProvider
import com.pleasybank.domain.user.entity.User
import com.pleasybank.domain.user.repository.UserRepository
import com.pleasybank.integration.kakao.dto.KakaoTokenResponse
import com.pleasybank.integration.kakao.dto.KakaoUserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.*

/**
 * 인증 서비스 구현체
 * 사용자 가입, 로그인, 비밀번호 재설정, 소셜 로그인 처리를 담당합니다.
 */
@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordResetRepository: PasswordResetRepository,
    private val userOAuthRepository: UserOAuthRepository,
    private val oAuthProviderRepository: OAuthProviderRepository,
    private val restTemplate: RestTemplate
) : AuthService {
    private val logger = LoggerFactory.getLogger(AuthServiceImpl::class.java)
    
    // 기본값 제공
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id:be56a79b5d2ef5456c6c2cf55d89dd38}")
    private lateinit var kakaoClientId: String
    
    @Value("\${spring.security.oauth2.client.registration.kakao.client-secret:your-client-secret}")
    private lateinit var kakaoClientSecret: String
    
    @Transactional
    override fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }
        
        if (request.password != request.confirmPassword) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
        }
        
        val now = LocalDateTime.now()
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            phoneNumber = request.phone,
            createdAt = now,
            updatedAt = now,
            status = "ACTIVE"
        )
        
        val savedUser = userRepository.save(user)
        
        return SignupResponse(
            id = savedUser.id!!,
            username = savedUser.email,
            email = savedUser.email,
            name = savedUser.name,
            createdAt = savedUser.createdAt
        )
    }
    
    @Transactional
    override fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { BadCredentialsException("Invalid credentials") }
        
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid credentials")
        }
        
        // 마지막 로그인 시간 업데이트
        val updatedUser = user.copy(lastLoginAt = LocalDateTime.now())
        userRepository.save(updatedUser)
        
        val accessToken = jwtTokenProvider.createToken(user.email, "ROLE_USER")
        val refreshToken = jwtTokenProvider.createRefreshToken(user.email)
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = 3600,
            user = UserDto(
                id = user.id!!,
                email = user.email,
                name = user.name,
                profileImageUrl = user.profileImageUrl
            )
        )
    }
    
    @Transactional
    override fun processKakaoAuth(code: String, redirectUri: String): LoginResponse {
        logger.info("카카오 인증 코드 처리 시작: code=${code.take(10)}..., redirectUri=${redirectUri}")
        
        // 카카오 개발자 콘솔에 등록된 정확한 리다이렉트 URI 사용
        val hardcodedRedirectUri = "http://localhost:3000/auth/callback/kakao"
        logger.info("원래 리다이렉트 URI: $redirectUri → 사용할 URI: $hardcodedRedirectUri")
        
        try {
            // 1. 카카오 API로 인증 코드를 사용하여 액세스 토큰 요청
            logger.info("카카오 API에 액세스 토큰 요청 중...")
            val kakaoTokenResponse = getKakaoAccessToken(code, hardcodedRedirectUri)
            logger.info("카카오 토큰 응답 수신 성공: access_token=${kakaoTokenResponse.access_token.take(10)}...")
            
            // 2. 카카오 액세스 토큰으로 사용자 정보 요청
            logger.info("카카오 액세스 토큰으로 사용자 정보 요청 중...")
            val kakaoUserInfo = getKakaoUserInfo(kakaoTokenResponse.access_token)
            val kakaoId = kakaoUserInfo.id
            logger.info("카카오 사용자 정보 조회 성공: id=$kakaoId")
            
            // 3. 사용자 찾기 또는 생성
            val provider = oAuthProviderRepository.findByProviderName("KAKAO")
                .orElseThrow { IllegalStateException("KAKAO 제공자가 등록되어 있지 않습니다.") }
            
            // 기존 사용자 OAuth 정보 찾기
            val userOAuthOpt = userOAuthRepository.findByProviderIdAndOauthUserId(provider.id!!, kakaoId.toString())
            val user: User
            
            if (userOAuthOpt.isPresent) {
                // 기존 사용자
                user = userOAuthOpt.get().user
                logger.info("기존 사용자 발견: id=${user.id}, email=${user.email}")
            } else {
                // 신규 사용자 생성
                val email = "kakao_${kakaoId}@pleasybank.com" // 임시 이메일
                val name = kakaoUserInfo.properties?.nickname ?: "사용자"
                val profileImage = kakaoUserInfo.properties?.profile_image
                
                user = User(
                    email = email,
                    password = passwordEncoder.encode(UUID.randomUUID().toString()), // 랜덤 비밀번호
                    name = name,
                    profileImageUrl = profileImage,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    status = "ACTIVE"
                )
                
                val savedUser = userRepository.save(user)
                
                // OAuth 연결 정보 저장
                val userOAuth = UserOAuth(
                    user = savedUser,
                    provider = provider,
                    oauthUserId = kakaoId.toString(),
                    accessToken = kakaoTokenResponse.access_token,
                    refreshToken = kakaoTokenResponse.refresh_token,
                    tokenExpiresAt = LocalDateTime.now().plusSeconds(kakaoTokenResponse.expires_in.toLong()),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                userOAuthRepository.save(userOAuth)
                logger.info("신규 사용자 생성: id=${savedUser.id}, email=${savedUser.email}")
            }
            
            // 4. JWT 토큰 생성
            logger.info("JWT 토큰 생성 중...")
            val accessToken = jwtTokenProvider.createToken(user.email, "ROLE_USER")
            val refreshToken = jwtTokenProvider.createRefreshToken(user.email)
            logger.info("JWT 토큰 생성 완료")
            
            // 5. 로그인 응답 생성
            return LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = "Bearer",
                expiresIn = 3600,
                user = UserDto(
                    id = user.id!!,
                    email = user.email,
                    name = user.name,
                    profileImageUrl = user.profileImageUrl
                )
            )
        } catch (e: Exception) {
            logger.error("카카오 인증 처리 중 예외 발생", e)
            throw e
        }
    }
    
    @Transactional
    override fun requestPasswordReset(request: PasswordResetRequest): PasswordResetResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다.") }
        
        // 기존 만료되지 않은 토큰이 있다면 만료 처리
        val now = LocalDateTime.now()
        val activeResets = passwordResetRepository.findByUserIdAndIsUsedFalseAndExpiresAtAfter(user.id!!, now)
        
        // 각 객체마다 isUsed 필드를 업데이트하여 새 목록 생성
        val updatedResets = activeResets.map { reset ->
            PasswordReset(
                id = reset.id,
                user = reset.user,
                token = reset.token,
                expiresAt = reset.expiresAt,
                createdAt = reset.createdAt,
                isUsed = true  // 사용됨으로 표시
            )
        }
        
        passwordResetRepository.saveAll(updatedResets)
        
        // 새 토큰 생성
        val token = UUID.randomUUID().toString()
        val expiresAt = now.plusHours(24) // 24시간 유효
        
        val passwordReset = PasswordReset(
            user = user,
            token = token,
            expiresAt = expiresAt,
            createdAt = now,
            isUsed = false
        )
        
        passwordResetRepository.save(passwordReset)
        
        // 실제 구현에서는 이메일 발송 로직 추가
        
        return PasswordResetResponse(message = "비밀번호 재설정 이메일이 발송되었습니다.")
    }
    
    @Transactional
    override fun resetPassword(request: NewPasswordRequest): NewPasswordResponse {
        if (request.newPassword != request.confirmPassword) {
            throw IllegalArgumentException("새 비밀번호가 일치하지 않습니다.")
        }
        
        val now = LocalDateTime.now()
        val passwordReset = passwordResetRepository.findByToken(request.token)
            .orElseThrow { IllegalArgumentException("유효하지 않은 토큰입니다.") }
        
        if (passwordReset.isUsed) {
            throw IllegalArgumentException("이미 사용된 토큰입니다.")
        }
        
        if (passwordReset.expiresAt.isBefore(now)) {
            throw IllegalArgumentException("만료된 토큰입니다.")
        }
        
        // 비밀번호 업데이트
        val user = passwordReset.user
        val updatedUser = user.copy(
            password = passwordEncoder.encode(request.newPassword),
            updatedAt = now
        )
        userRepository.save(updatedUser)
        
        // 토큰 사용 처리
        val updatedReset = PasswordReset(
            id = passwordReset.id,
            user = passwordReset.user,
            token = passwordReset.token,
            expiresAt = passwordReset.expiresAt,
            createdAt = passwordReset.createdAt,
            isUsed = true  // 사용됨으로 표시
        )
        passwordResetRepository.save(updatedReset)
        
        return NewPasswordResponse(message = "비밀번호가 성공적으로 변경되었습니다.")
    }
    
    /**
     * 카카오 인증 코드로 액세스 토큰 요청
     */
    private fun getKakaoAccessToken(code: String, redirectUri: String): KakaoTokenResponse {
        logger.info("카카오 토큰 요청 시작 - 인증코드: ${code.take(10)}..., 리다이렉트 URI: $redirectUri")
        
        try {
            // 카카오 API 요청을 위한 폼 파라미터 구성
            val formParams = LinkedMultiValueMap<String, String>()
            formParams.add("grant_type", "authorization_code")
            formParams.add("client_id", kakaoClientId)
            // 클라이언트 시크릿이 비어있는 경우 처리
            if (kakaoClientSecret.isNotBlank() && kakaoClientSecret != "your-client-secret") {
                formParams.add("client_secret", kakaoClientSecret)
            }
            formParams.add("redirect_uri", redirectUri)
            formParams.add("code", code)
            
            // HTTP 헤더 설정
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            headers.set("User-Agent", "PleasyBank/1.0")
            
            val request = HttpEntity(formParams, headers)
            
            // 요청 실행
            val response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", 
                request, 
                String::class.java
            )
            
            // 응답 처리
            if (response.statusCode.is2xxSuccessful) {
                val mapper = com.fasterxml.jackson.databind.ObjectMapper()
                val tokenResponse = mapper.readValue(response.body, KakaoTokenResponse::class.java)
                return tokenResponse
            } else {
                throw IllegalStateException("카카오 토큰 요청에 실패했습니다: ${response.statusCode}")
            }
        } catch (e: HttpClientErrorException) {
            val responseBody = e.responseBodyAsString
            logger.error("카카오 토큰 요청 중 HTTP 오류: ${e.statusCode}, 응답: $responseBody", e)
            throw IllegalStateException("카카오 토큰 요청에 실패했습니다: ${e.statusCode} : $responseBody")
        } catch (e: Exception) {
            logger.error("카카오 토큰 요청 중 예외 발생", e)
            throw IllegalStateException("카카오 토큰 요청에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 카카오 액세스 토큰으로 사용자 정보 요청
     */
    private fun getKakaoUserInfo(accessToken: String): KakaoUserInfoResponse {
        val url = "https://kapi.kakao.com/v2/user/me"
        
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        
        val request = HttpEntity<String>(headers)
        
        try {
            val response = restTemplate.postForEntity(url, request, KakaoUserInfoResponse::class.java)
            return response.body ?: throw IllegalStateException("카카오 사용자 정보 응답이 null입니다.")
        } catch (e: Exception) {
            logger.error("카카오 사용자 정보 요청 실패: ${e.message}", e)
            throw IllegalStateException("카카오 사용자 정보 요청에 실패했습니다: ${e.message}")
        }
    }
} 