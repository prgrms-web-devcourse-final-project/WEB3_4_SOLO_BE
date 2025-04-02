package com.pleasybank.transaction.service

import com.pleasybank.exception.BadRequestException
import com.pleasybank.exception.ResourceNotFoundException
import com.pleasybank.openbanking.service.OpenBankingService
import com.pleasybank.openbanking.service.OpenBankingTokenService
import com.pleasybank.transaction.dto.TransactionCreateRequest
import com.pleasybank.transaction.dto.TransactionDetailResponse
import com.pleasybank.transaction.dto.TransactionListResponse
import com.pleasybank.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 거래 관련 서비스를 오픈뱅킹 API에 의존하여 구현한 클래스
 * 거래 처리는 오픈뱅킹 API를 통해 실시간으로 처리하며, 자체 DB에는 최소한의 정보만 저장
 */
@Service
class OpenBankingTransactionService(
    private val openBankingService: OpenBankingService,
    private val openBankingTokenService: OpenBankingTokenService,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(OpenBankingTransactionService::class.java)
    
    /**
     * 계좌 이체 처리
     */
    @Transactional
    fun transferMoney(userId: Long, request: TransactionCreateRequest): TransactionDetailResponse {
        // 사용자 정보 확인
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다: $userId") }
        
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 필수 파라미터 검증
        val fintechUseNum = request.fintechUseNum
            ?: throw BadRequestException("핀테크 이용번호는 필수입니다")
        
        val receiverName = request.receiverName
            ?: throw BadRequestException("수취인 이름은 필수입니다")
        
        val receiverBankCode = request.receiverBankCode
            ?: throw BadRequestException("수취인 은행 코드는 필수입니다")
        
        val receiverAccountNum = request.receiverAccountNum
            ?: throw BadRequestException("수취인 계좌번호는 필수입니다")
        
        val amount = request.amount?.toString()
            ?: throw BadRequestException("이체 금액은 필수입니다")
        
        // 오픈뱅킹 API로 이체 요청
        val transferResponse = openBankingService.transferMoney(
            accessToken = accessToken,
            fintechUseNum = fintechUseNum,
            amount = amount,
            receiverName = receiverName,
            receiverBankCode = receiverBankCode,
            receiverAccountNum = receiverAccountNum
        )
        
        // 응답을 PleasyBank 형식으로 변환
        return TransactionDetailResponse(
            id = 0L, // 실제 ID는 사용하지 않음
            transactionType = "TRANSFER",
            sourceAccountId = 0L,
            sourceAccountNumber = transferResponse.account_num_masked,
            sourceBank = transferResponse.bank_name,
            destinationAccountId = null,
            destinationAccountNumber = receiverAccountNum,
            destinationBank = transferResponse.dps_bank_name,
            amount = BigDecimal(transferResponse.tran_amt),
            currency = "KRW",
            description = transferResponse.dps_print_content,
            status = "COMPLETED",
            feeAmount = BigDecimal.ZERO,
            transactionDateTime = LocalDateTime.now(),
            apiTransactionId = transferResponse.api_tran_id
        )
    }
    
    /**
     * 일별 거래 내역 요약 (예: 일별 지출 금액, 카테고리별 분석 등)
     * 오픈뱅킹 API를 통해 거래 내역을 가져와 분석
     */
    @Transactional(readOnly = true)
    fun getDailyTransactionSummary(userId: Long, fintechUseNum: String, date: String): Map<String, Any> {
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 해당 날짜의 거래내역 조회
        val transactionList = openBankingService.getTransactionList(
            accessToken = accessToken,
            fintechUseNum = fintechUseNum,
            fromDate = date,
            toDate = date,
            inquiryType = "A"
        )
        
        // 수입/지출 분리
        val incomeTrans = transactionList.res_list.filter { it.inout_type == "I" }
        val outgoingTrans = transactionList.res_list.filter { it.inout_type == "O" }
        
        // 총 금액 계산
        val totalIncome = incomeTrans.sumOf { BigDecimal(it.tran_amt) }
        val totalOutgoing = outgoingTrans.sumOf { BigDecimal(it.tran_amt) }
        
        // 카테고리별 그룹화 (간단한 구현)
        val categoryMap = outgoingTrans.groupBy {
            when {
                it.print_content.contains("카페") || it.print_content.contains("커피") -> "CAFE"
                it.print_content.contains("마트") || it.print_content.contains("슈퍼") -> "GROCERY"
                it.print_content.contains("식당") || it.print_content.contains("배달") -> "FOOD"
                it.print_content.contains("교통") || it.print_content.contains("택시") -> "TRANSPORTATION"
                else -> "OTHER"
            }
        }
        
        // 카테고리별 금액 계산
        val categoryAmounts = categoryMap.mapValues { (_, transactions) ->
            transactions.sumOf { BigDecimal(it.tran_amt) }
        }
        
        return mapOf(
            "date" to date,
            "totalIncome" to totalIncome,
            "totalOutgoing" to totalOutgoing,
            "netChange" to totalIncome.minus(totalOutgoing),
            "transactionCount" to transactionList.res_list.size,
            "categoryBreakdown" to categoryAmounts
        )
    }
    
    /**
     * 월별 거래 분석 (예: 지난달 대비 지출 증감, 카테고리별 월간 추이 등)
     * 오픈뱅킹 API를 통해 거래 내역을 가져와 분석
     */
    @Transactional(readOnly = true)
    fun getMonthlyTransactionAnalysis(userId: Long, fintechUseNum: String, year: Int, month: Int): Map<String, Any> {
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 날짜 형식 지정 (YYYYMMDD)
        val monthStr = month.toString().padStart(2, '0')
        val startDate = "$year${monthStr}01"
        
        // 월의 마지막 날 계산
        val lastDay = when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        
        val endDate = "$year$monthStr${lastDay.toString().padStart(2, '0')}"
        
        // 해당 월의 거래내역 조회
        val transactionList = openBankingService.getTransactionList(
            accessToken = accessToken,
            fintechUseNum = fintechUseNum,
            fromDate = startDate,
            toDate = endDate,
            inquiryType = "A"
        )
        
        // 일별 그룹화
        val dailyTransactions = transactionList.res_list.groupBy { it.tran_date }
        
        // 일별 수입/지출 계산
        val dailySummary = dailyTransactions.mapValues { (date, transactions) ->
            val incomes = transactions.filter { it.inout_type == "I" }
            val outgoings = transactions.filter { it.inout_type == "O" }
            
            mapOf(
                "income" to incomes.sumOf { BigDecimal(it.tran_amt) },
                "outgoing" to outgoings.sumOf { BigDecimal(it.tran_amt) }
            )
        }
        
        // 카테고리별 지출 합계
        val categoryOutgoings = transactionList.res_list
            .filter { it.inout_type == "O" }
            .groupBy {
                when {
                    it.print_content.contains("카페") || it.print_content.contains("커피") -> "CAFE"
                    it.print_content.contains("마트") || it.print_content.contains("슈퍼") -> "GROCERY"
                    it.print_content.contains("식당") || it.print_content.contains("배달") -> "FOOD"
                    it.print_content.contains("교통") || it.print_content.contains("택시") -> "TRANSPORTATION"
                    else -> "OTHER"
                }
            }
            .mapValues { (_, transactions) ->
                transactions.sumOf { BigDecimal(it.tran_amt) }
            }
        
        // 총계 계산
        val totalIncome = transactionList.res_list
            .filter { it.inout_type == "I" }
            .sumOf { BigDecimal(it.tran_amt) }
            
        val totalOutgoing = transactionList.res_list
            .filter { it.inout_type == "O" }
            .sumOf { BigDecimal(it.tran_amt) }
        
        return mapOf(
            "year" to year,
            "month" to month,
            "totalIncome" to totalIncome,
            "totalOutgoing" to totalOutgoing,
            "netChange" to totalIncome.minus(totalOutgoing),
            "dailySummary" to dailySummary,
            "categoryBreakdown" to categoryOutgoings,
            "transactionCount" to transactionList.res_list.size
        )
    }
} 