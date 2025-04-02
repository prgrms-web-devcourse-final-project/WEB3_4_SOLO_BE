package com.pleasybank.openbanking.controller

import com.pleasybank.authentication.repository.OAuthProviderRepository
import com.pleasybank.authentication.repository.UserOAuthRepository
import com.pleasybank.openbanking.dto.*
import com.pleasybank.openbanking.service.OpenBankingService
import com.pleasybank.openbanking.service.OpenBankingTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        @RequestParam("provider", required = false, defaultValue = "KAKAO") provider: String
    ): RedirectView {
        logger.info("오픈뱅킹 사용자 인증 페이지 리다이렉트: provider=$provider, oauthUserId=$oauthUserId")
        
        // 사용자 ID와 제공자 정보를 상태 파라미터에 인코딩하여 콜백에서 사용
        val stateJson = """{"provider":"$provider","oauthUserId":"$oauthUserId"}"""
        val state = URLEncoder.encode(stateJson, StandardCharsets.UTF_8.toString())
        
        val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString())
        val authPageUrl = "$authUrl?response_type=code&client_id=$clientId&redirect_uri=$encodedRedirectUri&scope=login inquiry transfer&state=$state&auth_type=0"
        
        return RedirectView(authPageUrl)
    }
    
    /**
     * 오픈뱅킹 콜백 처리
     */
    @GetMapping("/callback")
    @Operation(
        summary = "오픈뱅킹 콜백", 
        description = "오픈뱅킹 인증 후 콜백을 처리합니다. 인증 코드를 받아 액세스 토큰으로 교환하고 사용자 계정과 연동합니다.",
        parameters = [
            Parameter(name = "code", description = "금융결제원에서 발급한 인증 코드", required = true),
            Parameter(name = "state", description = "요청 시 전송한 상태값", required = true)
        ],
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "토큰 발급 및 연동 성공",
                content = [Content(schema = Schema(implementation = Map::class))]
            )
        ]
    )
    @Transactional
    fun callback(
        @RequestParam code: String, 
        @RequestParam state: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("오픈뱅킹 콜백 수신: code=${code.substring(0, 10)}..., state=$state")
        
        try {
            // state 파라미터에서 제공자와 사용자 ID 추출
            val decodedState = URLEncoder.encode(state, StandardCharsets.UTF_8.toString())
            
            // 간단한 방식으로 JSON 파싱 (실제로는 JSON 라이브러리 사용 권장)
            val stateString = decodedState.replace("%7B", "{").replace("%7D", "}")
                .replace("%22", "\"").replace("%3A", ":").replace("%2C", ",")
            
            val providerName = stateString.substringAfter("\"provider\":\"").substringBefore("\"")
            val oauthUserId = stateString.substringAfter("\"oauthUserId\":\"").substringBefore("\"")
            
            logger.info("추출된 정보: provider=$providerName, oauthUserId=$oauthUserId")
            
            // 제공자 ID 조회
            val provider = oAuthProviderRepository.findByProviderName(providerName)
                .orElseThrow { IllegalStateException("인증 제공자를 찾을 수 없습니다: $providerName") }
            
            // 사용자 OAuth 정보 조회
            val userOAuth = userOAuthRepository.findByProviderIdAndOauthUserId(provider.id!!, oauthUserId)
                .orElseThrow { IllegalStateException("사용자 OAuth 정보를 찾을 수 없습니다: $oauthUserId") }
            
            // 인증 코드로 토큰 발급 및 저장
            val tokenResponse = openBankingTokenService.processAuthorizationCode(code, userOAuth)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "오픈뱅킹 연동이 완료되었습니다.",
                "userSeqNo" to tokenResponse.user_seq_no,
                "expiresIn" to tokenResponse.expires_in
            ))
        } catch (e: Exception) {
            logger.error("오픈뱅킹 콜백 처리 중 오류 발생", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to "오픈뱅킹 연동 중 오류가 발생했습니다: ${e.message}"
            ))
        }
    }
    
    /**
     * 사용자 계좌 목록 조회
     */
    @GetMapping("/accounts")
    @Operation(
        summary = "계좌 목록 조회", 
        description = "사용자의 계좌 목록을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "계좌 목록 조회 성공",
                content = [Content(schema = Schema(implementation = AccountListResponse::class))]
            )
        ]
    )
    fun getAccountList(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<AccountListResponse> {
        val userId = userDetails.username.toLong()
        logger.info("계좌 목록 조회 요청: userId=$userId")
        
        // 사용자의 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: return ResponseEntity.badRequest().build()
        
        // 사용자 일련번호 조회
        val userOAuthOpt = userOAuthRepository.findByUserIdWithOpenBankingToken(userId)
        if (userOAuthOpt.isEmpty) {
            return ResponseEntity.badRequest().build()
        }
        
        val userOAuth = userOAuthOpt.get()
        val userSeqNo = userOAuth.openBankingUserSeqNo
            ?: return ResponseEntity.badRequest().build()
        
        // 계좌 목록 조회
        val response = openBankingService.getAccountList(accessToken, userSeqNo)
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 계좌 잔액 조회
     */
    @GetMapping("/accounts/{fintechUseNum}/balance")
    @Operation(
        summary = "계좌 잔액 조회", 
        description = "특정 계좌의 잔액을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "fintechUseNum", 
                description = "핀테크 이용번호", 
                required = true, 
                `in` = ParameterIn.PATH
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "계좌 잔액 조회 성공",
                content = [Content(schema = Schema(implementation = AccountBalanceResponse::class))]
            )
        ]
    )
    fun getAccountBalance(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable fintechUseNum: String
    ): ResponseEntity<AccountBalanceResponse> {
        val userId = userDetails.username.toLong()
        logger.info("계좌 잔액 조회 요청: userId=$userId, fintechUseNum=$fintechUseNum")
        
        // 사용자의 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: return ResponseEntity.badRequest().build()
        
        // 계좌 잔액 조회
        val response = openBankingService.getAccountBalance(accessToken, fintechUseNum)
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 계좌 거래내역 조회
     */
    @GetMapping("/accounts/{fintechUseNum}/transactions")
    @Operation(
        summary = "거래내역 조회", 
        description = "특정 계좌의 거래내역을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "fintechUseNum", 
                description = "핀테크 이용번호", 
                required = true, 
                `in` = ParameterIn.PATH
            ),
            Parameter(
                name = "fromDate", 
                description = "조회 시작일(YYYYMMDD)", 
                required = true
            ),
            Parameter(
                name = "toDate", 
                description = "조회 종료일(YYYYMMDD)", 
                required = true
            ),
            Parameter(
                name = "inquiryType", 
                description = "조회 구분(A: 전체, I: 입금, O: 출금)", 
                required = false
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "거래내역 조회 성공",
                content = [Content(schema = Schema(implementation = TransactionListResponse::class))]
            )
        ]
    )
    fun getTransactionList(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable fintechUseNum: String,
        @RequestParam fromDate: String,
        @RequestParam toDate: String,
        @RequestParam(required = false, defaultValue = "A") inquiryType: String
    ): ResponseEntity<TransactionListResponse> {
        val userId = userDetails.username.toLong()
        logger.info("거래내역 조회 요청: userId=$userId, fintechUseNum=$fintechUseNum, fromDate=$fromDate, toDate=$toDate")
        
        // 사용자의 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: return ResponseEntity.badRequest().build()
        
        // 거래내역 조회
        val response = openBankingService.getTransactionList(
            accessToken, fintechUseNum, fromDate, toDate, inquiryType
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 계좌 이체
     */
    @PostMapping("/transfer")
    @Operation(
        summary = "계좌 이체", 
        description = "계좌에서 다른 계좌로 송금합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "이체 요청 정보",
            required = true,
            content = [
                Content(
                    schema = Schema(
                        implementation = Map::class,
                        example = """
                            {
                              "fintechUseNum": "계좌의 핀테크 이용번호",
                              "amount": "이체 금액",
                              "receiverName": "수취인 이름",
                              "receiverBankCode": "수취인 은행 코드",
                              "receiverAccountNum": "수취인 계좌번호"
                            }
                        """
                    )
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "이체 성공",
                content = [Content(schema = Schema(implementation = TransferResponse::class))]
            )
        ]
    )
    fun transferMoney(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<TransferResponse> {
        val userId = userDetails.username.toLong()
        val fintechUseNum = request["fintechUseNum"] ?: throw IllegalArgumentException("fintechUseNum은 필수입니다.")
        val amount = request["amount"] ?: throw IllegalArgumentException("amount는 필수입니다.")
        val receiverName = request["receiverName"] ?: throw IllegalArgumentException("receiverName은 필수입니다.")
        val receiverBankCode = request["receiverBankCode"] ?: throw IllegalArgumentException("receiverBankCode는 필수입니다.")
        val receiverAccountNum = request["receiverAccountNum"] ?: throw IllegalArgumentException("receiverAccountNum은 필수입니다.")
        
        logger.info("계좌 이체 요청: userId=$userId, fintechUseNum=$fintechUseNum, 금액=$amount, 수취인=$receiverName")
        
        // 사용자의 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: return ResponseEntity.badRequest().build()
        
        // 계좌 이체 실행
        val response = openBankingService.transferMoney(
            accessToken,
            fintechUseNum,
            amount,
            receiverName,
            receiverBankCode,
            receiverAccountNum
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 사용자의 오픈뱅킹 연동 상태 확인
     */
    @GetMapping("/status")
    @Operation(
        summary = "오픈뱅킹 연동 상태 확인", 
        description = "현재 로그인한 사용자의 오픈뱅킹 연동 상태를 확인합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200", 
                description = "상태 조회 성공",
                content = [Content(schema = Schema(implementation = Map::class))]
            )
        ]
    )
    fun getOpenBankingStatus(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        logger.info("오픈뱅킹 연동 상태 확인: userId=$userId")
        
        val userOAuthOpt = userOAuthRepository.findByUserIdWithOpenBankingToken(userId)
        
        return if (userOAuthOpt.isPresent) {
            val userOAuth = userOAuthOpt.get()
            val isValid = openBankingTokenService.isOpenBankingTokenValid(userOAuth)
            
            ResponseEntity.ok(mapOf(
                "isLinked" to userOAuth.isOpenBankingLinked,
                "isTokenValid" to isValid,
                "userSeqNo" to (userOAuth.openBankingUserSeqNo ?: "")
            ))
        } else {
            ResponseEntity.ok(mapOf(
                "isLinked" to false,
                "isTokenValid" to false,
                "userSeqNo" to ""
            ))
        }
    }
} 