package com.pleasybank.domain.auth.controller

import com.pleasybank.core.util.StringUtils
import com.pleasybank.domain.auth.dto.*
import com.pleasybank.domain.auth.service.AuthFactorService
import com.pleasybank.domain.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API 컨트롤러
 * 로그인, 회원가입, 카카오 인증 등의 엔드포인트를 제공합니다.
 */
@Controller
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "인증 관리 API")
class AuthController(
    private val authService: AuthService,
    private val authFactorService: AuthFactorService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    @PostMapping("/signup")
    @ResponseBody
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<SignupResponse> {
        val response = authService.signup(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/login")
    @ResponseBody
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/kakao")
    @ResponseBody
    @CrossOrigin(origins = ["http://localhost:3000"], allowCredentials = "true")
    @Operation(summary = "카카오 인증 코드 처리", description = "카카오 로그인 후 받은 인증 코드를 처리하여 JWT 토큰을 발급합니다.")
    fun handleKakaoAuth(@RequestBody request: KakaoLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("카카오 인증 코드 처리 요청: code=${request.code.take(10)}... redirectUri=${request.redirectUri}")
        
        try {
            val response = authService.processKakaoAuth(request.code, request.redirectUri)
            logger.info("카카오 인증 처리 성공: 토큰 생성됨 (액세스 토큰 길이: ${response.accessToken.length})")
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("카카오 인증 처리 중 오류 발생: ${e.message}", e)
            throw e
        }
    }
    
    @PostMapping("/reset-password-request")
    @ResponseBody
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정을 위한 이메일을 발송합니다.")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): ResponseEntity<PasswordResetResponse> {
        val response = authService.requestPasswordReset(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/reset-password")
    @ResponseBody
    @Operation(summary = "비밀번호 재설정", description = "사용자의 비밀번호를 재설정합니다.")
    fun resetPassword(@Valid @RequestBody request: NewPasswordRequest): ResponseEntity<NewPasswordResponse> {
        val response = authService.resetPassword(request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/oauth2/authorization/{provider}")
    @ResponseBody
    @Operation(summary = "OAuth2 인증 요청", description = "지정된 제공자(kakao 등)를 통한 OAuth2 인증을 시작합니다.")
    fun oauth2Authorization(@PathVariable provider: String, request: HttpServletRequest): ResponseEntity<Void> {
        // OAuth2 인증 시작은 Spring Security의 필터가 처리
        // 이 메서드는 API 명세서와의 일관성을 위해 존재
        return ResponseEntity.ok().build()
    }
    
    @GetMapping("/oauth2/callback/{provider}")
    @ResponseBody
    @Operation(summary = "OAuth2 콜백 처리", description = "OAuth2 인증 완료 후 리다이렉트되는 콜백을 처리합니다.")
    fun oauth2Callback(@PathVariable provider: String, request: HttpServletRequest): ResponseEntity<Void> {
        // OAuth2 콜백은 OAuth2SuccessHandler에서 처리
        // 이 메서드는 API 명세서와의 일관성을 위해 존재
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/pin/setup")
    @ResponseBody
    @Operation(summary = "PIN 설정", description = "사용자의 PIN 번호를 설정합니다.")
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
    @ResponseBody
    @Operation(summary = "PIN 확인", description = "사용자의 PIN 번호를 검증합니다.")
    fun verifyPin(@Valid @RequestBody request: PinVerifyRequest): ResponseEntity<LoginResponse> {
        val response = authFactorService.verifyPin(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/biometric/setup")
    @ResponseBody
    @Operation(summary = "생체인증 설정", description = "사용자의 생체인증을 설정합니다.")
    fun setupBiometric(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: BiometricSetupRequest
    ): ResponseEntity<BiometricSetupResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
        val response = authFactorService.setupBiometric(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/token-display")
    @Operation(summary = "토큰 표시", description = "OAuth2 인증 후 발급된 토큰을 표시하는 페이지와 API를 제공합니다.")
    fun displayToken(
        @RequestParam(required = false) token: String?,
        @RequestParam(required = false) refreshToken: String?,
        @RequestParam(required = false) error: String?,
        @RequestParam(required = false) code: String?,
        @RequestHeader(name = "Accept", required = false, defaultValue = MediaType.TEXT_HTML_VALUE) accept: String,
        model: Model
    ): Any {
        logger.info("토큰 표시 페이지/API 접근: token=${token != null}, error=$error, code=$code, accept=$accept")

        // API 응답 요청인 경우 (JSON)
        if (accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            if (code != null && token == null && error == null) {
                // 인증 코드만 있고 토큰이 없는 경우 (처리 중)
                return ResponseEntity.ok(mapOf(
                    "status" to "processing",
                    "code" to code
                ))
            }
            
            if (error != null) {
                // 에러 발생
                val errorMessage = StringUtils.formatErrorMessage(error)
                return ResponseEntity.badRequest().body(mapOf(
                    "status" to "error",
                    "error" to errorMessage
                ))
            }
            
            if (token.isNullOrEmpty()) {
                // 토큰 없음
                return ResponseEntity.badRequest().body(mapOf(
                    "status" to "error",
                    "error" to "인증 토큰이 발급되지 않았습니다"
                ))
            }
            
            // 토큰 발급 성공
            return ResponseEntity.ok(mapOf(
                "status" to "success",
                "accessToken" to token,
                "refreshToken" to refreshToken
            ))
        }
        
        // HTML 응답 요청인 경우 (페이지)
        // 인증 코드만 있고 토큰이 없는 경우는 인증 과정 중이므로 에러가 아님
        if (code != null && token == null && error == null) {
            model.addAttribute("code", code)
            return "auth/processing"
        }
        
        // 오류가 발생한 경우
        if (error != null) {
            // 특수문자 처리
            val errorMessage = StringUtils.formatErrorMessage(error)
            
            logger.warn("토큰 표시 오류: $errorMessage")
            model.addAttribute("errorMessage", errorMessage)
            return "auth/token-error"
        }
        
        // 토큰이 null인 경우
        if (token.isNullOrEmpty()) {
            logger.warn("토큰 표시 오류: 인증 토큰이 발급되지 않았습니다")
            model.addAttribute("errorMessage", "인증 토큰이 발급되지 않았습니다")
            return "auth/token-error"
        }

        // 토큰이 발급된 경우
        logger.info("토큰 발급 성공: ${token.take(10)}...")
        model.addAttribute("token", token)
        model.addAttribute("refreshToken", refreshToken)
        
        return "auth/token-display"
    }
} 