package com.pleasybank.authentication.service

import com.pleasybank.authentication.dto.*
import com.pleasybank.authentication.entity.PasswordReset
import com.pleasybank.authentication.repository.PasswordResetRepository
import com.pleasybank.security.jwt.JwtTokenProvider
import com.pleasybank.user.entity.User
import com.pleasybank.user.repository.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordResetRepository: PasswordResetRepository
) {
    
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
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
            username = savedUser.email,  // username 대신 email 사용
            email = savedUser.email,
            name = savedUser.name,
            createdAt = savedUser.createdAt
        )
    }
    
    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.username)
            .orElseThrow { BadCredentialsException("Invalid credentials") }
        
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid credentials")
        }
        
        // 마지막 로그인 시간 업데이트
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)
        
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
    fun requestPasswordReset(request: PasswordResetRequest): PasswordResetResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다.") }
        
        // 기존 만료되지 않은 토큰이 있다면 만료 처리
        val now = LocalDateTime.now()
        val activeResets = passwordResetRepository.findByUserIdAndIsUsedFalseAndExpiresAtAfter(user.id!!, now)
        activeResets.forEach { it.isUsed = true }
        passwordResetRepository.saveAll(activeResets)
        
        // 새 토큰 생성
        val token = UUID.randomUUID().toString()
        val expiresAt = now.plusHours(24) // 24시간 유효
        
        val passwordReset = PasswordReset(
            user = user,
            token = token,
            expiresAt = expiresAt,
            createdAt = now
        )
        
        passwordResetRepository.save(passwordReset)
        
        // 실제 구현에서는 이메일 발송 로직 추가
        
        return PasswordResetResponse(message = "비밀번호 재설정 이메일이 발송되었습니다.")
    }
    
    @Transactional
    fun resetPassword(request: NewPasswordRequest): NewPasswordResponse {
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
        user.password = passwordEncoder.encode(request.newPassword)
        user.updatedAt = now
        userRepository.save(user)
        
        // 토큰 사용 처리
        passwordReset.isUsed = true
        passwordResetRepository.save(passwordReset)
        
        return NewPasswordResponse(message = "비밀번호가 성공적으로 변경되었습니다.")
    }
} 