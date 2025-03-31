package com.pleasybank.authentication.controller

import com.pleasybank.authentication.dto.*
import com.pleasybank.authentication.service.AuthFactorService
import com.pleasybank.authentication.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val authFactorService: AuthFactorService
) {
    
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<SignupResponse> {
        val response = authService.signup(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/reset-password-request")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): ResponseEntity<PasswordResetResponse> {
        val response = authService.requestPasswordReset(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: NewPasswordRequest): ResponseEntity<NewPasswordResponse> {
        val response = authService.resetPassword(request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/oauth2/authorization/{provider}")
    fun oauth2Authorization(@PathVariable provider: String, request: HttpServletRequest): ResponseEntity<Void> {
        // OAuth2 인증 시작은 Spring Security의 필터가 처리
        // 이 메서드는 API 명세서와의 일관성을 위해 존재
        return ResponseEntity.ok().build()
    }
    
    @GetMapping("/oauth2/callback/{provider}")
    fun oauth2Callback(@PathVariable provider: String, request: HttpServletRequest): ResponseEntity<Void> {
        // OAuth2 콜백은 OAuth2SuccessHandler에서 처리
        // 이 메서드는 API 명세서와의 일관성을 위해 존재
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/pin/setup")
    fun setupPin(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: PinSetupRequest
    ): ResponseEntity<PinSetupResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
        val response = authFactorService.setupPin(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/pin/verify")
    fun verifyPin(@Valid @RequestBody request: PinVerifyRequest): ResponseEntity<LoginResponse> {
        val response = authFactorService.verifyPin(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/biometric/setup")
    fun setupBiometric(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: BiometricSetupRequest
    ): ResponseEntity<BiometricSetupResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
        val response = authFactorService.setupBiometric(userId, request)
        return ResponseEntity.ok(response)
    }
} 