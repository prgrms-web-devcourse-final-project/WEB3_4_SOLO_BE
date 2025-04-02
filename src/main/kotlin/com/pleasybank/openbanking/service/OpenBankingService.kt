package com.pleasybank.openbanking.service

import com.pleasybank.openbanking.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class OpenBankingService(
    private val webClient: WebClient,
    
    @Value("\${openbanking.base-url}")
    private val baseUrl: String,
    
    @Value("\${openbanking.client-id}")
    private val clientId: String,
    
    @Value("\${openbanking.client-secret}")
    private val clientSecret: String
) {
    private val logger = LoggerFactory.getLogger(OpenBankingService::class.java)
    
    /**
     * 사용자 계좌 목록 조회
     */
    fun getAccountList(accessToken: String, userSeqNo: String): AccountListResponse {
        logger.info("계좌 목록 조회 시작: userSeqNo={}", userSeqNo)
        
        val request = AccountListRequest(user_seq_no = userSeqNo)
        
        return webClient.get()
            .uri("$baseUrl/v2.0/account/list?user_seq_no=${request.user_seq_no}&include_cancel_yn=${request.include_cancel_yn}&sort_order=${request.sort_order}")
            .header("Authorization", "Bearer $accessToken")
            .header("bank_tran_id", generateBankTranId())
            .header("tran_dtime", getCurrentDateTime())
            .retrieve()
            .bodyToMono(AccountListResponse::class.java)
            .doOnSuccess { response ->
                logger.info("계좌 목록 조회 성공: 응답 코드={}, 계좌 수={}", response.rsp_code, response.res_cnt)
            }
            .doOnError { error ->
                logger.error("계좌 목록 조회 실패: {}", error.message, error)
            }
            .block() ?: throw RuntimeException("계좌 목록 조회 중 오류가 발생했습니다.")
    }
    
    /**
     * 계좌 잔액 조회
     */
    fun getAccountBalance(accessToken: String, fintechUseNum: String): AccountBalanceResponse {
        logger.info("계좌 잔액 조회 시작: fintechUseNum={}", fintechUseNum)
        
        val bankTranId = generateBankTranId()
        val tranDtime = getCurrentDateTime()
        
        return webClient.get()
            .uri("$baseUrl/v2.0/account/balance/fin_num")
            .header("Authorization", "Bearer $accessToken")
            .header("bank_tran_id", bankTranId)
            .header("fintech_use_num", fintechUseNum)
            .header("tran_dtime", tranDtime)
            .retrieve()
            .bodyToMono(AccountBalanceResponse::class.java)
            .doOnSuccess { response ->
                logger.info("계좌 잔액 조회 성공: 응답 코드={}, 계좌번호={}", response.rsp_code, response.fintech_use_num)
            }
            .doOnError { error ->
                logger.error("계좌 잔액 조회 실패: {}", error.message, error)
            }
            .block() ?: throw RuntimeException("계좌 잔액 조회 중 오류가 발생했습니다.")
    }
    
    /**
     * 계좌 거래내역 조회
     */
    fun getTransactionList(
        accessToken: String,
        fintechUseNum: String,
        fromDate: String,
        toDate: String,
        inquiryType: String = "A"
    ): TransactionListResponse {
        logger.info("거래내역 조회 시작: fintechUseNum={}, fromDate={}, toDate={}", fintechUseNum, fromDate, toDate)
        
        val bankTranId = generateBankTranId()
        val tranDtime = getCurrentDateTime()
        
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("$baseUrl/v2.0/account/transaction_list/fin_num")
                    .queryParam("bank_tran_id", bankTranId)
                    .queryParam("fintech_use_num", fintechUseNum)
                    .queryParam("inquiry_type", inquiryType)
                    .queryParam("inquiry_base", "D")
                    .queryParam("from_date", fromDate)
                    .queryParam("to_date", toDate)
                    .queryParam("sort_order", "D")
                    .queryParam("tran_dtime", tranDtime)
                    .build()
            }
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(TransactionListResponse::class.java)
            .doOnSuccess { response ->
                logger.info("거래내역 조회 성공: 응답 코드={}, 거래 수={}", response.rsp_code, response.res_cnt)
            }
            .doOnError { error ->
                logger.error("거래내역 조회 실패: {}", error.message, error)
            }
            .block() ?: throw RuntimeException("거래내역 조회 중 오류가 발생했습니다.")
    }
    
    /**
     * 계좌 이체
     */
    fun transferMoney(
        accessToken: String,
        fintechUseNum: String,
        amount: String,
        receiverName: String,
        receiverBankCode: String,
        receiverAccountNum: String
    ): TransferResponse {
        logger.info("계좌 이체 시작: fintechUseNum={}, 금액={}, 수취인={}", fintechUseNum, amount, receiverName)
        
        val bankTranId = generateBankTranId()
        val tranDtime = getCurrentDateTime()
        
        val request = TransferRequest(
            bank_tran_id = bankTranId,
            cntr_account_type = "N",
            cntr_account_num = receiverAccountNum,
            dps_print_content = "플리지뱅크송금",
            fintech_use_num = fintechUseNum,
            tran_amt = amount,
            tran_dtime = tranDtime,
            req_client_name = "사용자",
            req_client_fintech_use_num = fintechUseNum,
            req_client_num = "PLEASYBANK",
            transfer_purpose = "TR",
            recv_client_name = receiverName,
            recv_client_bank_code = receiverBankCode,
            recv_client_account_num = receiverAccountNum
        )
        
        return webClient.post()
            .uri("$baseUrl/v2.0/transfer/withdraw/fin_num")
            .header("Authorization", "Bearer $accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TransferResponse::class.java)
            .doOnSuccess { response ->
                logger.info("계좌 이체 성공: 응답 코드={}, 거래 금액={}", response.rsp_code, response.tran_amt)
            }
            .doOnError { error ->
                logger.error("계좌 이체 실패: {}", error.message, error)
            }
            .block() ?: throw RuntimeException("계좌 이체 중 오류가 발생했습니다.")
    }
    
    /**
     * 토큰 발급
     */
    fun getToken(code: String, redirectUri: String): TokenResponse {
        logger.info("토큰 발급 시작: code={}", code)
        
        return webClient.post()
            .uri("$baseUrl/oauth/2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                mapOf(
                    "code" to code,
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "redirect_uri" to redirectUri,
                    "grant_type" to "authorization_code"
                )
            )
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .doOnSuccess { response ->
                logger.info("토큰 발급 성공: user_seq_no={}", response.user_seq_no)
            }
            .doOnError { error ->
                logger.error("토큰 발급 실패: {}", error.message, error)
            }
            .block() ?: throw RuntimeException("토큰 발급 중 오류가 발생했습니다.")
    }
    
    /**
     * 토큰 갱신
     */
    fun refreshToken(refreshToken: String): TokenResponse {
        logger.info("토큰 갱신 시작")
        
        return webClient.post()
            .uri("$baseUrl/oauth/2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                mapOf(
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "refresh_token" to refreshToken,
                    "grant_type" to "refresh_token"
                )
            )
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .doOnSuccess { response ->
                logger.info("토큰 갱신 성공: user_seq_no={}", response.user_seq_no)
            }
            .doOnError { error ->
                logger.error("토큰 갱신 실패: {}", error.message, error)
            }
            .block() ?: throw RuntimeException("토큰 갱신 중 오류가 발생했습니다.")
    }
    
    /**
     * 은행거래고유번호 생성
     */
    private fun generateBankTranId(): String {
        val prefix = "U" + clientId.substring(0, 9)
        val randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 9)
        return prefix + randomString
    }
    
    /**
     * 현재 시간 포맷팅
     */
    private fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    }
} 