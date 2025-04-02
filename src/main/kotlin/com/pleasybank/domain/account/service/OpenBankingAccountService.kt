package com.pleasybank.domain.account.service

import com.pleasybank.domain.account.dto.*
import com.pleasybank.core.exception.BadRequestException
import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.integration.openbanking.dto.AccountDto
import com.pleasybank.integration.openbanking.service.OpenBankingService
import com.pleasybank.integration.openbanking.service.OpenBankingTokenService
import com.pleasybank.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 계좌 관련 서비스를 오픈뱅킹 API에 의존하여 구현한 클래스
 * 계좌 정보는 오픈뱅킹 API를 통해 실시간으로 조회하며, 자체 DB에는 최소한의 정보만 저장
 */
@Service
class OpenBankingAccountService(
    private val openBankingService: OpenBankingService,
    private val openBankingTokenService: OpenBankingTokenService,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(OpenBankingAccountService::class.java)
    
    /**
     * 사용자의 계좌 목록 조회
     */
    @Transactional(readOnly = true)
    fun getAccountsByUserId(userId: Long, page: Int, size: Int): AccountListResponse {
        // 사용자 정보 확인
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다: $userId") }
        
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 사용자 오픈뱅킹 일련번호 조회
        val userOAuthOpt = openBankingTokenService.getUserOAuth(userId)
        if (userOAuthOpt.isEmpty) {
            throw BadRequestException("오픈뱅킹 연동 정보가 없습니다")
        }
        
        val userSeqNo = userOAuthOpt.get().openBankingUserSeqNo
            ?: throw BadRequestException("오픈뱅킹 사용자 번호가 없습니다")
        
        // 오픈뱅킹 API로 계좌 목록 조회
        val openBankingAccountList = openBankingService.getAccountList(accessToken, userSeqNo)
        
        // 오픈뱅킹 응답을 PleasyBank 응답 형식으로 변환
        val accounts = openBankingAccountList.res_list.map { convertToAccountDetail(it) }
        
        return AccountListResponse(
            accounts = accounts,
            page = page,
            size = accounts.size,
            totalElements = accounts.size.toLong(),
            totalPages = 1
        )
    }
    
    /**
     * 계좌 상세 정보 조회
     */
    @Transactional(readOnly = true)
    fun getAccountByFintechNum(userId: Long, fintechUseNum: String): AccountDetailResponse {
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 오픈뱅킹 API로 계좌 정보 조회
        val accountListResponse = openBankingService.getAccountList(
            accessToken,
            openBankingTokenService.getUserOAuth(userId).get().openBankingUserSeqNo!!
        )
        
        // 핀테크 이용번호로 계좌 찾기
        val account = accountListResponse.res_list.find { acc -> acc.fintech_use_num == fintechUseNum }
            ?: throw ResourceNotFoundException("계좌를 찾을 수 없습니다: $fintechUseNum")
        
        return convertToAccountDetail(account)
    }
    
    /**
     * 계좌 잔액 조회
     */
    @Transactional(readOnly = true)
    fun getAccountBalance(userId: Long, fintechUseNum: String): AccountBalanceResponse {
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 오픈뱅킹 API로 잔액 조회
        val balanceResponse = openBankingService.getAccountBalance(accessToken, fintechUseNum)
        
        return AccountBalanceResponse(
            accountId = 0L, // 실제 ID는 사용하지 않음
            accountNumber = "마스킹된 계좌번호", // 보안상 실제 계좌번호는 사용하지 않음
            availableBalance = BigDecimal(balanceResponse.available_amt),
            currency = "KRW", // 오픈뱅킹은 기본적으로 KRW 사용
            timestamp = LocalDateTime.now()
        )
    }
    
    /**
     * 계좌 거래내역 조회
     */
    @Transactional(readOnly = true)
    fun getAccountTransactions(
        userId: Long,
        fintechUseNum: String,
        fromDate: String,
        toDate: String,
        inquiryType: String = "A"
    ): AccountTransactionListResponse {
        // 오픈뱅킹 토큰 조회
        val accessToken = openBankingTokenService.getValidOpenBankingToken(userId)
            ?: throw BadRequestException("오픈뱅킹 연동이 필요합니다")
        
        // 오픈뱅킹 API로 거래내역 조회
        val transactionList = openBankingService.getTransactionList(
            accessToken, fintechUseNum, fromDate, toDate, inquiryType
        )
        
        // 거래내역을 PleasyBank 형식으로 변환
        val transactions = transactionList.res_list.map { trans ->
            val isIncome = trans.inout_type == "I"
            val amount = BigDecimal(trans.tran_amt)
            val balanceAfter = BigDecimal(trans.after_balance_amt)
            
            val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")
            val transactionDateTime = LocalDateTime.parse(
                trans.tran_date, dateFormatter
            ).withHour(
                trans.tran_time.substring(0, 2).toInt()
            ).withMinute(
                trans.tran_time.substring(2, 4).toInt()
            ).withSecond(
                trans.tran_time.substring(4, 6).toInt()
            )
            
            AccountTransactionDto(
                id = 0L, // 실제 ID는 사용하지 않음
                transactionType = if (isIncome) "DEPOSIT" else "WITHDRAWAL",
                amount = amount,
                description = trans.print_content,
                balanceBefore = balanceAfter.minus(if (isIncome) amount else amount.negate()),
                balanceAfter = balanceAfter,
                currency = "KRW",
                transactionDateTime = transactionDateTime,
                status = "COMPLETED", // 오픈뱅킹은 완료된 거래만 조회
                counterpartyName = trans.print_content, // 거래 내용에서 추출 (실제론 더 복잡할 수 있음)
                counterpartyAccountNumber = "", // 오픈뱅킹 API에서 제공하지 않음
                category = categorizeTransaction(trans.print_content) // 거래 내용 기반 카테고리 추론
            )
        }
        
        return AccountTransactionListResponse(
            accountId = 0L, // 실제 ID는 사용하지 않음
            accountNumber = "",
            transactions = transactions,
            page = 0,
            size = transactions.size,
            totalElements = transactions.size.toLong(),
            totalPages = 1
        )
    }
    
    /**
     * 오픈뱅킹 계좌 정보를 PleasyBank 계좌 정보로 변환
     */
    private fun convertToAccountDetail(account: AccountDto): AccountDetailResponse {
        val accountType = when (account.account_type) {
            "1" -> "SAVINGS"
            "2" -> "CHECKING"
            else -> "OTHER"
        }
        
        return AccountDetailResponse(
            id = 0L, // 실제 ID 대신 핀테크 이용번호를 사용
            accountNumber = account.account_num_masked,
            accountName = account.account_alias,
            accountType = accountType,
            balance = BigDecimal.ZERO, // 잔액은 별도 API 호출 필요
            currency = "KRW",
            status = if (account.account_state == "01") "ACTIVE" else "INACTIVE",
            interestRate = BigDecimal.ZERO, // 이자율은 오픈뱅킹에서 제공하지 않음
            lastActivityAt = null,
            createdAt = LocalDateTime.now(),
            fintechUseNum = account.fintech_use_num, // 핀테크 이용번호 추가
            bankName = account.bank_name, // 은행명 추가
            bankCode = account.bank_code_std // 은행 코드 추가
        )
    }
    
    /**
     * 거래내용을 기반으로 카테고리 추론
     */
    private fun categorizeTransaction(description: String): String {
        return when {
            description.contains("카페") || description.contains("커피") -> "CAFE"
            description.contains("마트") || description.contains("슈퍼") -> "GROCERY"
            description.contains("식당") || description.contains("배달") -> "FOOD"
            description.contains("택시") || description.contains("버스") || description.contains("지하철") -> "TRANSPORTATION"
            description.contains("급여") || description.contains("월급") -> "INCOME"
            description.contains("이체") || description.contains("송금") -> "TRANSFER"
            description.contains("ATM") || description.contains("출금") -> "WITHDRAWAL"
            description.contains("입금") -> "DEPOSIT"
            else -> "OTHER"
        }
    }
} 