package com.pleasybank.integration.openbanking.controller

import com.pleasybank.domain.auth.repository.OAuthProviderRepository
import com.pleasybank.domain.auth.repository.UserOAuthRepository
import com.pleasybank.integration.openbanking.dto.TokenResponse
import com.pleasybank.integration.openbanking.service.OpenBankingService
import com.pleasybank.integration.openbanking.service.OpenBankingTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 오픈뱅킹 컨트롤러
 * 금융결제원 오픈뱅킹 API 연동을 위한 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/openbanking")
@Tag(name = "오픈뱅킹 API", description = "금융결제원 오픈뱅킹 API 연동 관련 엔드포인트")
class OpenBankingController(
    private val openBankingService: OpenBankingService,
    private val openBankingTokenService: OpenBankingTokenService,
    private val userOAuthRepository: UserOAuthRepository,
    private val oAuthProviderRepository: OAuthProviderRepository,
    
    @Value("\${openbanking.client-id}")
    private val clientId: String,
    
    @Value("\${openbanking.redirect-uri}")
    private val redirectUri: String,
    
    @Value("\${openbanking.auth-url}")
    private val authUrl: String
) {
    private val logger = LoggerFactory.getLogger(OpenBankingController::class.java)
    
    /**
     * 프론트엔드를 위한 오픈뱅킹 인증 URL 제공
     */
    @GetMapping("/auth-url")
    @Operation(
        summary = "오픈뱅킹 인증 URL 제공", 
        description = "인증된 사용자에게 오픈뱅킹 인증 페이지 URL을 제공합니다.",
        security = [SecurityRequirement(name = "bearer-key")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 URL 정보 반환",
                content = [Content(schema = Schema(implementation = Map::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(schema = Schema(implementation = Map::class))]
            )
        ]
    )
    fun getAuthUrl(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("오픈뱅킹 인증 URL 요청: user=${userDetails.username}")
        
        try {
            // 사용자 정보 (OAuth 제공자는 KAKAO로 고정)
            val provider = "KAKAO"
            val oauthUserId = userDetails.username
            
            // 세션에 사용자 정보 저장 (콜백에서 사용)
            request.session.setAttribute("OPENBANKING_AUTH_PROVIDER", provider)
            request.session.setAttribute("OPENBANKING_AUTH_OAUTH_USER_ID", oauthUserId)
            
            // 사용자 ID와 제공자 정보를 상태 파라미터에 인코딩하여 콜백에서 사용
            val stateJson = """{"provider":"$provider","oauthUserId":"$oauthUserId","sessionId":"${request.session.id}"}"""
            val state = URLEncoder.encode(stateJson, StandardCharsets.UTF_8.toString())
            
            val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString())
            val authPageUrl = "$authUrl?response_type=code&client_id=$clientId&redirect_uri=$encodedRedirectUri&scope=login inquiry transfer&state=$state&auth_type=0"
            
            logger.info("오픈뱅킹 인증 URL 생성: $authPageUrl")
            return ResponseEntity.ok(mapOf("authorizationUrl" to authPageUrl))
        } catch (e: Exception) {
            logger.error("오픈뱅킹 인증 URL 생성 중 오류 발생", e)
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "open_banking_auth_error", "message" to (e.message ?: "인증 URL 생성 중 오류가 발생했습니다")))
        }
    }
    
    /**
     * 오픈뱅킹 사용자 인증 페이지로 리다이렉트
     */
    @GetMapping("/auth")
    @Operation(
        summary = "오픈뱅킹 사용자 인증", 
        description = "사용자를 오픈뱅킹 사용자 인증 페이지로 리다이렉트합니다. 사용자는 이 페이지에서 계좌 접근 동의를 제공합니다.",
        parameters = [
            Parameter(name = "provider", description = "OAuth 제공자 (기본값: KAKAO)", required = false),
            Parameter(name = "oauthUserId", description = "OAuth 사용자 ID", required = true)
        ],
        responses = [
            ApiResponse(
                responseCode = "302",
                description = "금융결제원 인증 페이지로 리다이렉트",
                content = [Content(schema = Schema(implementation = Void::class))]
            )
        ]
    )
    fun authRedirect(
        @RequestParam("oauthUserId") oauthUserId: String,
        @RequestParam("provider", required = false, defaultValue = "KAKAO") provider: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): RedirectView {
        logger.info("오픈뱅킹 사용자 인증 페이지 리다이렉트: provider=$provider, oauthUserId=$oauthUserId, sessionId=${request.session.id}")
        
        try {
            // 세션에 사용자 정보 저장 (콜백에서 사용)
            request.session.setAttribute("OPENBANKING_AUTH_PROVIDER", provider)
            request.session.setAttribute("OPENBANKING_AUTH_OAUTH_USER_ID", oauthUserId)
            
            // 사용자 ID와 제공자 정보를 상태 파라미터에 인코딩하여 콜백에서 사용
            val stateJson = """{"provider":"$provider","oauthUserId":"$oauthUserId","sessionId":"${request.session.id}"}"""
            val state = URLEncoder.encode(stateJson, StandardCharsets.UTF_8.toString())
            
            val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString())
            val authPageUrl = "$authUrl?response_type=code&client_id=$clientId&redirect_uri=$encodedRedirectUri&scope=login inquiry transfer&state=$state&auth_type=0"
            
            logger.info("오픈뱅킹 인증 URL 생성: $authPageUrl")
            return RedirectView(authPageUrl)
        } catch (e: Exception) {
            logger.error("오픈뱅킹 인증 URL 생성 중 오류 발생", e)
            return RedirectView("/auth/token-display?error=open_banking_auth_error&message=${URLEncoder.encode(e.message ?: "인증 URL 생성 중 오류가 발생했습니다", StandardCharsets.UTF_8.toString())}")
        }
    }
    
    // 상태 확인용 엔드포인트 추가
    @GetMapping("/auth/status")
    fun getAuthStatus(request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        val sessionId = request.session.id
        val provider = request.session.getAttribute("OPENBANKING_AUTH_PROVIDER") as? String
        val oauthUserId = request.session.getAttribute("OPENBANKING_AUTH_OAUTH_USER_ID") as? String
        
        return ResponseEntity.ok(mapOf(
            "sessionId" to sessionId,
            "provider" to provider,
            "oauthUserId" to oauthUserId,
            "sessionAttributes" to request.session.attributeNames.toList().associateWith { request.session.getAttribute(it) }
        ))
    }
    
    // 나머지 코드는 그대로 유지
} 