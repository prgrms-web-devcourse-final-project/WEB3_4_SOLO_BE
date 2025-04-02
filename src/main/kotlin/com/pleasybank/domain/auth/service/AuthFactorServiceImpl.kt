package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.dto.*
import com.pleasybank.domain.auth.entity.UserAuthentication
import com.pleasybank.domain.auth.repository.UserAuthenticationRepository
import com.pleasybank.core.security.jwt.JwtTokenProvider
import com.pleasybank.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 인증 요소 서비스 구현체
 * PIN 및 생체인증 같은 2차 인증 요소를 관리합니다.
 */
@Service
class AuthFactorServiceImpl(
    private val userRepository: UserRepository,
    private val userAuthRepository: UserAuthenticationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : AuthFactorService {
    private val logger = LoggerFactory.getLogger(AuthFactorServiceImpl::class.java)
    
    /**
     * PIN 설정
     */
    @Transactional
    override fun setupPin(userId: Long, request: PinSetupRequest): PinSetupResponse {
        // 사용자 확인
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        
        // PIN 유효성 검사
        if (request.pin.length < 4 || request.pin.length > 6) {
            throw IllegalArgumentException("PIN은 4-6자리 숫자여야 합니다.")
        }
        
        // 기존 PIN이 있으면 비활성화
        val existingPins = userAuthRepository.findByUserIdAndType(userId, "PIN")
        if (existingPins.isPresent) {
            val existingPin = existingPins.get()
            val updatedPin = UserAuthentication(
                id = existingPin.id,
                user = existingPin.user,
                authType = existingPin.authType,
                authValue = existingPin.authValue,
                isEnabled = false,
                createdAt = existingPin.createdAt,
                updatedAt = LocalDateTime.now()
            )
            userAuthRepository.save(updatedPin)
        }
        
        // 새 PIN 저장
        val encodedPin = passwordEncoder.encode(request.pin)
        val pinAuth = UserAuthentication(
            user = user,
            authType = "PIN",
            authValue = encodedPin,
            isEnabled = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userAuthRepository.save(pinAuth)
        
        return PinSetupResponse(
            message = "PIN이 성공적으로 설정되었습니다."
        )
    }
    
    /**
     * PIN 확인
     */
    @Transactional
    override fun verifyPin(request: PinVerifyRequest): LoginResponse {
        // 사용자 확인
        val user = userRepository.findById(request.userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: ${request.userId}") }
        
        // PIN 인증 정보 조회
        val pinAuth = userAuthRepository.findByUserIdAndAuthTypeAndIsEnabledTrue(request.userId, "PIN")
            .orElseThrow { IllegalArgumentException("PIN이 설정되어 있지 않습니다.") }
        
        // PIN 검증
        if (!passwordEncoder.matches(request.pin, pinAuth.authValue)) {
            throw IllegalArgumentException("잘못된 PIN입니다.")
        }
        
        // 토큰 생성
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
    
    /**
     * 생체인증 설정
     */
    @Transactional
    override fun setupBiometric(userId: Long, request: BiometricSetupRequest): BiometricSetupResponse {
        // 사용자 확인
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        
        // 기존 생체인증이 있으면 비활성화
        val existingBiometrics = userAuthRepository.findByUserIdAndType(userId, "BIOMETRIC")
        if (existingBiometrics.isPresent) {
            val existingBiometric = existingBiometrics.get()
            val updatedBiometric = UserAuthentication(
                id = existingBiometric.id,
                user = existingBiometric.user,
                authType = existingBiometric.authType,
                authValue = existingBiometric.authValue,
                isEnabled = false,
                createdAt = existingBiometric.createdAt,
                updatedAt = LocalDateTime.now()
            )
            userAuthRepository.save(updatedBiometric)
        }
        
        // 새 생체인증 정보 저장
        val biometricAuth = UserAuthentication(
            user = user,
            authType = "BIOMETRIC",
            authValue = request.publicKey,
            isEnabled = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userAuthRepository.save(biometricAuth)
        
        return BiometricSetupResponse(
            message = "생체인증이 성공적으로 설정되었습니다."
        )
    }
} 