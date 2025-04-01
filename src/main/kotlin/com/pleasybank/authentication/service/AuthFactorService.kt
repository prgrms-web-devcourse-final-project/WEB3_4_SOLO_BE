package com.pleasybank.authentication.service

import com.pleasybank.authentication.dto.*
import com.pleasybank.authentication.entity.UserAuthentication
import com.pleasybank.authentication.repository.UserAuthenticationRepository
import com.pleasybank.security.jwt.JwtTokenProvider
import com.pleasybank.user.repository.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthFactorService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    
    @Transactional
    fun setupPin(userId: Long, request: PinSetupRequest): PinSetupResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 기존 PIN이 있다면 업데이트, 없다면 새로 생성
        val userAuth = userAuthenticationRepository.findByUserIdAndAuthType(userId, "PIN")
            .orElse(
                UserAuthentication(
                    user = user,
                    authType = "PIN",
                    authValue = "",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    isEnabled = false
                )
            )
        
        // copy를 사용하여 새 인스턴스 생성 (불변 객체 패턴)
        val updatedAuth = userAuth.copy(
            authValue = passwordEncoder.encode(request.pinCode),
            updatedAt = LocalDateTime.now(),
            isEnabled = true
        )
        
        userAuthenticationRepository.save(updatedAuth)
        
        return PinSetupResponse(message = "PIN 코드가 성공적으로 설정되었습니다.")
    }
    
    @Transactional
    fun verifyPin(request: PinVerifyRequest): LoginResponse {
        val user = userRepository.findByEmail(request.username)
            .orElseThrow { BadCredentialsException("유효하지 않은 사용자입니다.") }
        
        val pinAuth = userAuthenticationRepository.findByUserIdAndAuthTypeAndIsEnabledTrue(user.id!!, "PIN")
            .orElseThrow { BadCredentialsException("PIN이 설정되지 않았습니다.") }
        
        if (!passwordEncoder.matches(request.pinCode, pinAuth.authValue)) {
            throw BadCredentialsException("유효하지 않은 PIN 코드입니다.")
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
            expiresIn = 3600 // 1시간
        )
    }
    
    @Transactional
    fun setupBiometric(userId: Long, request: BiometricSetupRequest): BiometricSetupResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 기존 생체인증이 있다면 업데이트, 없다면 새로 생성
        val userAuth = userAuthenticationRepository.findByUserIdAndAuthType(userId, request.biometricType)
            .orElse(
                UserAuthentication(
                    user = user,
                    authType = request.biometricType,
                    authValue = "",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    isEnabled = false
                )
            )
        
        // copy를 사용하여 새 인스턴스 생성 (불변 객체 패턴)
        val updatedAuth = userAuth.copy(
            authValue = request.publicKeyCredential,
            updatedAt = LocalDateTime.now(),
            isEnabled = true
        )
        
        userAuthenticationRepository.save(updatedAuth)
        
        return BiometricSetupResponse(message = "생체인증이 성공적으로 설정되었습니다.")
    }
} 