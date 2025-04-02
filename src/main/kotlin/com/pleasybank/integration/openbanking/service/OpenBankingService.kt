package com.pleasybank.integration.openbanking.service

import com.pleasybank.core.util.BankUtils
import com.pleasybank.core.util.DateUtils
import com.pleasybank.integration.openbanking.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

/**
 * 오픈뱅킹 서비스
 * 금융결제원 오픈뱅킹 API 연동 기능을 제공합니다.
 */
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
            .header("bank_tran_id", BankUtils.generateBankTranId())
            .header("tran_dtime", DateUtils.getCurrentDateTime())
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
        
        return webClient.get()
            .uri("$baseUrl/v2.0/account/balance/fin_num")
            .header("Authorization", "Bearer $accessToken")
            .header("bank_tran_id", BankUtils.generateBankTranId())
            .header("fintech_use_num", fintechUseNum)
            .header("tran_dtime", DateUtils.getCurrentDateTime())
            .retrieve()
            .bodyToMono(AccountBalanceResponse::class.java)
            .doOnSuccess { response ->
                logger.info("계좌 잔액 조회 성공: 응답 코드={}, 잔액={}", response.rsp_code, response.available_amt)
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
        logger.info("거래내역 조회 시작: fintechUseNum={}, 기간={} ~ {}", fintechUseNum, fromDate, toDate)
        
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("$baseUrl/v2.0/account/transaction_list/fin_num")
                    .queryParam("bank_tran_id", BankUtils.generateBankTranId())
                    .queryParam("fintech_use_num", fintechUseNum)
                    .queryParam("inquiry_type", inquiryType)
                    .queryParam("inquiry_base", "D") // 일자 기준
                    .queryParam("from_date", fromDate)
                    .queryParam("to_date", toDate)
                    .queryParam("sort_order", "D") // 내림차순
                    .queryParam("tran_dtime", DateUtils.getCurrentDateTime())
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
     * 계좌 이체 (출금 이체)
     */
    fun transferMoney(
        accessToken: String,
        fintechUseNum: String,
        amount: String,
        receiverName: String,
        receiverBankCode: String,
        receiverAccountNum: String
    ): TransferResponse {
        logger.info("계좌 이체 요청 시작: 출금계좌={}, 입금계좌={}, 금액={}", fintechUseNum, receiverAccountNum, amount)
        
        val request = TransferRequest(
            bank_tran_id = BankUtils.generateBankTranId(),
            cntr_account_type = "N", // 일반 계좌
            cntr_account_num = receiverAccountNum,
            dps_print_content = "PleasyBank 이체",
            fintech_use_num = fintechUseNum,
            tran_amt = amount,
            tran_dtime = DateUtils.getCurrentDateTime(),
            req_client_name = "사용자",
            req_client_fintech_use_num = fintechUseNum,
            req_client_num = "PLEASYBANK",
            transfer_purpose = "TR", // TR: 이체
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
} 