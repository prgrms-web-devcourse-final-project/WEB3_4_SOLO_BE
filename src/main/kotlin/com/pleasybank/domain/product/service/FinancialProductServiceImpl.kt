package com.pleasybank.domain.product.service

import com.pleasybank.core.exception.BadRequestException
import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.entity.AccountType
import com.pleasybank.domain.account.repository.AccountRepository
import com.pleasybank.domain.account.service.AccountService
import com.pleasybank.domain.account.dto.CreateAccountRequest
import com.pleasybank.domain.product.dto.*
import com.pleasybank.domain.product.entity.FinancialProduct
import com.pleasybank.domain.product.repository.FinancialProductRepository
import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Random
import java.util.Optional
import java.util.function.Supplier

@Service
class FinancialProductServiceImpl(
    private val financialProductRepository: FinancialProductRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val accountService: AccountService
) : FinancialProductService {

    // 금융 상품 관련 서비스 구현

    @Transactional
    override fun createFinancialProduct(request: CreateFinancialProductRequest): FinancialProductResponse {
        val product = FinancialProduct(
            name = request.name,
            description = request.description,
            category = request.category,
            interestRate = request.interestRate,
            minAmount = request.minAmount,
            maxAmount = request.maxAmount,
            term = request.term,
            status = "ACTIVE",
            features = request.features,
            isActive = request.isActive,
            imageUrl = request.imageUrl,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedProduct = financialProductRepository.save(product)
        
        return FinancialProductResponse.fromEntity(savedProduct)
    }

    @Transactional(readOnly = true)
    override fun getFinancialProductById(id: Long): FinancialProductResponse {
        // DB에서 조회
        val product = financialProductRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("금융 상품을 찾을 수 없습니다. ID: $id") }
        
        return FinancialProductResponse.fromEntity(product)
    }

    @Transactional(readOnly = true)
    override fun getAllFinancialProducts(pageable: Pageable): Page<FinancialProductResponse> {
        return financialProductRepository.findAll(pageable)
            .map { product -> FinancialProductResponse.fromEntity(product) }
    }

    @Transactional(readOnly = true)
    override fun getFinancialProductsByCategory(category: String, pageable: Pageable): Page<FinancialProductResponse> {
        return financialProductRepository.findAllByCategory(category, pageable)
            .map { product -> FinancialProductResponse.fromEntity(product) }
    }

    @Transactional(readOnly = true)
    override fun getFinancialProductsByType(type: String, pageable: Pageable): Page<FinancialProductResponse> {
        // type을 category로 간주하고 동일한 메서드를 호출 (이름 호환성 유지)
        return getFinancialProductsByCategory(type, pageable)
    }

    @Transactional
    override fun updateFinancialProduct(id: Long, request: UpdateFinancialProductRequest): FinancialProductResponse {
        val existingProduct = financialProductRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("금융 상품을 찾을 수 없습니다. ID: $id") }
        
        val updatedProduct = existingProduct.copy(
            name = request.name ?: existingProduct.name,
            category = request.category ?: existingProduct.category,
            description = request.description ?: existingProduct.description,
            interestRate = request.interestRate ?: existingProduct.interestRate,
            term = request.term ?: existingProduct.term,
            minAmount = request.minAmount ?: existingProduct.minAmount,
            maxAmount = request.maxAmount ?: existingProduct.maxAmount,
            features = request.features ?: existingProduct.features,
            status = request.status ?: existingProduct.status,
            isActive = request.isActive ?: existingProduct.isActive,
            imageUrl = request.imageUrl ?: existingProduct.imageUrl,
            updatedAt = LocalDateTime.now()
        )
        
        val savedProduct = financialProductRepository.save(updatedProduct)
        
        return FinancialProductResponse.fromEntity(savedProduct)
    }

    @Transactional
    override fun deleteFinancialProduct(id: Long) {
        val product = financialProductRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("금융 상품을 찾을 수 없습니다. ID: $id") }
        
        // 소프트 삭제 (상태 변경)
        val deletedProduct = product.copy(
            status = "INACTIVE",
            updatedAt = LocalDateTime.now()
        )
        
        financialProductRepository.save(deletedProduct)
    }

    @Transactional(readOnly = true)
    override fun getTopRateProducts(limit: Int): List<FinancialProductResponse> {
        // DB에서 조회
        return financialProductRepository.findTopRateProducts()
            .take(limit)
            .map { product -> FinancialProductResponse.fromEntity(product) }
    }

    // 상품 구독 관련 서비스 구현

    @Transactional
    override fun createProductSubscription(request: CreateProductSubscriptionRequest): ProductSubscriptionResponse {
        println("서비스 계층 - 상품 구독 시작: userId=${request.userId}, productId=${request.productId}")
        
        // 사용자 조회
        val user = userRepository.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: ${request.userId}") }
        
        println("사용자 정보 조회 성공: ${user.email}")
        
        // 상품 조회
        val product = financialProductRepository.findById(request.productId)
            .orElseThrow { ResourceNotFoundException("금융 상품을 찾을 수 없습니다. ID: ${request.productId}") }
        
        println("상품 정보 조회 성공: ${product.name}, 카테고리: ${product.category}")
        
        // 금액 유효성 검사 - 최소/최대 금액 확인
        if (product.minAmount != null && request.amount < product.minAmount) {
            throw BadRequestException("최소 금액(${product.minAmount})보다 작은 금액은 처리할 수 없습니다: ${request.amount}")
        }
        
        if (product.maxAmount != null && request.amount > product.maxAmount) {
            throw BadRequestException("최대 금액(${product.maxAmount})보다 큰 금액은 처리할 수 없습니다: ${request.amount}")
        }
        
        // term 필드 로깅
        val term = request.term
        println("상품 구독 기간(term): ${term}, 상품 기본 기간: ${product.term}")
        
        // 자유입출금 계좌(CHECKING)인 경우 특별 처리
        if (product.category == "CHECKING") {
            println("자유입출금 계좌 개설 처리 시작")
            try {
                // 계좌번호 생성
                val accountNumber = generateAccountNumber()
                println("새 계좌번호 생성: $accountNumber")
                
                // 계좌 생성 요청 객체 생성
                val accountRequest = CreateAccountRequest(
                    bank = "플레이지은행",
                    accountName = product.name,
                    accountType = "CHECKING",
                    accountNumber = accountNumber,
                    initialBalance = BigDecimal.ZERO,
                    fintechUseNum = null,
                    productId = product.id
                )
                
                // 계좌 서비스를 통해 계좌 생성
                println("자유입출금 계좌 생성 시작: ${accountRequest.accountNumber}")
                val createdAccount = accountService.createAccount(request.userId, accountRequest)
                println("자유입출금 계좌 생성 성공: ID=${createdAccount.id}")
                
                // 구독자 수 증가 처리
                val updatedProduct = updateProductSubscriptionCount(product)
                println("상품 구독자 수 증가 처리 완료")
                
                // 계좌 응답 객체로 변환하여 바로 반환
                return ProductSubscriptionResponse(
                    id = createdAccount.id ?: 0, // id가 nullable이므로 null 대신 0 사용
                    userId = request.userId,
                    userName = user.name,
                    productId = product.id ?: 0, // id가 nullable이므로 null 대신 0 사용
                    productName = product.name,
                    productCategory = product.category,
                    accountId = createdAccount.id,
                    accountNumber = accountRequest.accountNumber,
                    amount = request.amount,
                    subscriptionDate = LocalDate.now(),
                    maturityDate = LocalDate.now().plusYears(10),
                    interestRate = product.interestRate,
                    expectedReturn = null,
                    status = "ACTIVE",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            } catch (e: Exception) {
                println("자유입출금 계좌 생성 중 오류 발생: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        
        // 대출 상품 처리
        if (product.category == "LOAN") {
            try {
                println("대출 상품 처리 시작 - 금액: ${request.amount}, 기간: ${request.term ?: product.term}")
                
                // 계좌 조회 (대출 상품은 accountId 필수 아님)
                val account = if (request.accountId != null) {
                    accountRepository.findById(request.accountId).orElse(null)
                } else null
                
                println("대출금 입금 계좌 정보: ${account?.accountNumber ?: "계좌 정보 없음"}")
                
                // 만기일 및 종료일 계산
                val loanTerm = request.term ?: product.term ?: 12
                println("최종 대출 기간: $loanTerm")
                
                val maturityDate = calculateMaturityDate(product, loanTerm)
                val endDate = maturityDate ?: LocalDate.now().plusMonths(loanTerm.toLong())
                
                println("대출 만기일: $maturityDate, 종료일: $endDate")
                
                // 예상 수익 계산 (이자)
                val expectedReturn = calculateExpectedReturn(
                    request.amount.toDouble(),
                    product.interestRate?.toDouble() ?: 0.0,
                    loanTerm
                )?.let { BigDecimal.valueOf(it) }
                
                println("예상 이자: $expectedReturn")
                
                // 대출 계좌 생성
                val accountNumber = generateAccountNumber()
                val accountRequest = CreateAccountRequest(
                    bank = "플레이지은행",
                    accountName = "대출 - ${product.name}",
                    accountType = "LOAN",
                    accountNumber = accountNumber,
                    initialBalance = request.amount,
                    fintechUseNum = null,
                    productId = product.id
                )
                
                println("대출 계좌 생성 시작: ${accountRequest.accountNumber}")
                val createdAccount = accountService.createAccount(request.userId, accountRequest)
                println("대출 계좌 생성 성공: ID=${createdAccount.id}")
                
                // 구독자 수 증가
                updateProductSubscriptionCount(product)
                
                // 대출금 입금 처리 (선택된 계좌가 있는 경우만 시도)
                if (account != null && account.id != null) {
                    try {
                        println("대출금 입금 처리 시작 - 계좌 ID: ${account.id}, 금액: ${request.amount}")
                        accountService.deposit(account.id, request.amount, "${product.name} 대출금 입금")
                        println("대출금 입금 처리 성공 - 계좌: ${account.id}, 금액: ${request.amount}")
                    } catch (e: Exception) {
                        println("대출금 입금 처리 실패: ${e.message}")
                        println("입금 처리 실패에도 불구하고 대출 신청은 계속 진행합니다.")
                    }
                } else {
                    println("대출 상품이지만 계좌 정보가 없거나 유효하지 않아 입금 처리 없이 대출 생성만 진행")
                }
                
                // 응답 생성
                return ProductSubscriptionResponse(
                    id = createdAccount.id ?: 0,
                    userId = request.userId,
                    userName = user.name,
                    productId = product.id ?: 0,
                    productName = product.name,
                    productCategory = product.category,
                    accountId = createdAccount.id,
                    accountNumber = accountNumber,
                    amount = request.amount,
                    subscriptionDate = LocalDate.now(),
                    maturityDate = maturityDate,
                    interestRate = product.interestRate,
                    expectedReturn = expectedReturn,
                    status = "ACTIVE",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    errorMessage = null
                )
            } catch (e: Exception) {
                println("대출 상품 가입 처리 중 예외 발생: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        
        // 일반 상품 처리
        println("일반 상품 처리 시작 - 카테고리: ${product.category}, 금액: ${request.amount}")
        
        // 계좌 정보 조회
        val account = if (request.accountId != null) {
            println("계좌 ID: ${request.accountId} 조회 중")
            accountRepository.findById(request.accountId).orElse(null)
        } else null
        
        println("선택한 계좌 정보: ${account?.accountNumber ?: "계좌 선택 안함"}")
        
        if (account == null) {
            throw BadRequestException("연결할 계좌 정보가 없습니다. 계좌를 먼저 생성해주세요.")
        }
        
        // 만기일 및 종료일 계산
        val finalTerm = request.term ?: product.term ?: 12
        println("최종 기간: $finalTerm")
        
        val maturityDate = calculateMaturityDate(product, finalTerm)
        val endDate = maturityDate ?: LocalDate.now().plusMonths(finalTerm.toLong())
        
        println("상품 만기일: $maturityDate, 종료일: $endDate")
        
        // 예상 수익 계산
        val expectedReturn = calculateExpectedReturn(
            request.amount.toDouble(),
            product.interestRate?.toDouble() ?: 0.0,
            finalTerm
        )?.let { BigDecimal.valueOf(it) }
        
        println("예상 수익: $expectedReturn")
        
        // 기존 계좌 정보 업데이트 (productId 추가)
        val updatedAccount = account.copy(
            product = product,
            updatedAt = LocalDateTime.now()
        )
        val savedAccount = accountRepository.save(updatedAccount)
        println("계좌 업데이트 성공: ID=${savedAccount.id}")
        
        // 구독자 수 증가
        updateProductSubscriptionCount(product)
        
        return ProductSubscriptionResponse(
            id = savedAccount.id ?: 0,
            userId = request.userId,
            userName = user.name,
            productId = product.id ?: 0,
            productName = product.name,
            productCategory = product.category,
            accountId = savedAccount.id,
            accountNumber = savedAccount.accountNumber,
            amount = request.amount,
            subscriptionDate = LocalDate.now(),
            maturityDate = maturityDate,
            interestRate = product.interestRate,
            expectedReturn = expectedReturn,
            status = "ACTIVE",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Transactional(readOnly = true)
    override fun getProductSubscriptionById(id: Long): ProductSubscriptionResponse {
        // id가 계좌 ID로 변경되었으므로 계좌 정보 조회
        val account = accountRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("계좌 정보를 찾을 수 없습니다. ID: $id") }
        
        val product = account.product
        
        return ProductSubscriptionResponse(
            id = account.id ?: 0,
            userId = account.user.id ?: 0,
            userName = account.user.name,
            productId = product?.id ?: 0,
            productName = product?.name ?: account.accountName,
            productCategory = product?.category ?: account.accountType.toString(),
            accountId = account.id,
            accountNumber = account.accountNumber,
            amount = account.balance,
            subscriptionDate = LocalDate.from(account.createdAt),
            maturityDate = null,
            interestRate = product?.interestRate,
            expectedReturn = null,
            status = account.status,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }

    @Transactional(readOnly = true)
    override fun getUserProductSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse> {
        println("사용자 금융상품 목록 조회: userId=$userId")
        println("페이지 정보: size=${pageable.pageSize}, page=${pageable.pageNumber}, sort=${pageable.sort}")
        
        try {
            // 사용자의 계좌 정보를 가져옵니다
            val userAccounts = accountRepository.findByUserId(userId, pageable)
            println("사용자 계좌 조회 결과: ${userAccounts.totalElements}개")
            
            if (userAccounts.totalElements == 0L) {
                println("사용자($userId)의 계좌가 없습니다.")
                return Page.empty(pageable)
            }
            
            // 각 계좌의 상세 정보 로깅
            userAccounts.forEach { account ->
                println("계좌 ID: ${account.id}, 계좌번호: ${account.accountNumber}, 상태: ${account.status}")
                println("  - 연결된 상품: ${account.product?.id ?: "없음"}, 상품명: ${account.product?.name ?: "없음"}")
            }
            
            // 계좌 정보를 ProductSubscriptionResponse로 변환
            val responseList = userAccounts.map { account ->
                val product = account.product
                
                // 계좌 정보를 ProductSubscriptionResponse 객체로 변환
                ProductSubscriptionResponse(
                    id = account.id ?: 0,
                    userId = userId,
                    userName = account.user.name,
                    productId = product?.id ?: 0,
                    productName = product?.name ?: account.accountName,
                    productCategory = product?.category ?: account.accountType.toString(),
                    accountId = account.id,
                    accountNumber = account.accountNumber,
                    amount = account.balance,
                    subscriptionDate = LocalDate.from(account.createdAt),
                    maturityDate = null, // 계좌에는 만기일 개념이 없음
                    interestRate = product?.interestRate,
                    expectedReturn = null, // 계좌에는 예상 수익 개념이 없음
                    status = account.status,
                    createdAt = account.createdAt,
                    updatedAt = account.updatedAt
                )
            }
            
            println("변환된 응답 목록: ${responseList.totalElements}개")
            return responseList
            
        } catch (e: Exception) {
            println("사용자 금융상품 조회 중 오류 발생: ${e.message}")
            e.printStackTrace()
            return Page.empty(pageable)
        }
    }

    @Transactional
    override fun updateProductSubscription(id: Long, request: UpdateProductSubscriptionRequest): ProductSubscriptionResponse {
        // 계좌 정보 조회
        val account = accountRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("계좌 정보를 찾을 수 없습니다. ID: $id") }
        
        // 변경 가능한 필드만 업데이트
        val updatedAccount = account.copy(
            // status만 변경 가능 (다른 필드는 계좌 서비스를 통해 변경)
            status = request.status ?: account.status,
            updatedAt = LocalDateTime.now()
        )
        
        // 계좌 업데이트
        val savedAccount = accountRepository.save(updatedAccount)
        
        // 계좌 정보를 ProductSubscriptionResponse로 변환
        val product = savedAccount.product
        
        return ProductSubscriptionResponse(
            id = savedAccount.id ?: 0,
            userId = savedAccount.user.id ?: 0,
            userName = savedAccount.user.name,
            productId = product?.id ?: 0,
            productName = product?.name ?: savedAccount.accountName,
            productCategory = product?.category ?: savedAccount.accountType.toString(),
            accountId = savedAccount.id,
            accountNumber = savedAccount.accountNumber,
            amount = savedAccount.balance,
            subscriptionDate = LocalDate.from(savedAccount.createdAt),
            maturityDate = null,
            interestRate = product?.interestRate,
            expectedReturn = null,
            status = savedAccount.status,
            createdAt = savedAccount.createdAt,
            updatedAt = savedAccount.updatedAt
        )
    }

    @Transactional
    override fun cancelProductSubscription(id: Long): ProductSubscriptionResponse {
        // 계좌 정보 조회
        val account = accountRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("계좌 정보를 찾을 수 없습니다. ID: $id") }
        
        // 이미 취소된 계좌인지 확인
        if (account.status != "ACTIVE") {
            throw IllegalArgumentException("이미 해지되었거나 만기된 계좌입니다.")
        }
        
        // 계좌 상태 변경
        val canceledAccount = account.copy(
            status = "CANCELLED", // 상태를 취소로 변경
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(canceledAccount)
        
        // 계좌 정보를 ProductSubscriptionResponse로 변환
        val product = savedAccount.product
        
        return ProductSubscriptionResponse(
            id = savedAccount.id ?: 0,
            userId = savedAccount.user.id ?: 0,
            userName = savedAccount.user.name,
            productId = product?.id ?: 0,
            productName = product?.name ?: savedAccount.accountName,
            productCategory = product?.category ?: savedAccount.accountType.toString(),
            accountId = savedAccount.id,
            accountNumber = savedAccount.accountNumber,
            amount = savedAccount.balance,
            subscriptionDate = LocalDate.from(savedAccount.createdAt),
            maturityDate = null,
            interestRate = product?.interestRate,
            expectedReturn = null,
            status = savedAccount.status,
            createdAt = savedAccount.createdAt,
            updatedAt = savedAccount.updatedAt
        )
    }

    @Transactional(readOnly = true)
    override fun getUserTotalInvestedAmount(userId: Long): BigDecimal {
        println("사용자 총 투자금액 조회: userId=$userId")
        
        // 사용자의 계좌 잔액 합계를 계산합니다
        val totalBalance = accountRepository.sumBalanceByUserId(userId) ?: BigDecimal.ZERO
        println("사용자 총 계좌 잔액: $totalBalance")
        
        return totalBalance
    }

    // getUserSubscriptions 메서드 수정
    @Transactional(readOnly = true)
    override fun getUserSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse> {
        println("사용자 상품 구독 목록 조회: userId=$userId")
        
        // 사용자의 계좌 정보를 가져옵니다
        val userAccounts = accountRepository.findByUserId(userId, pageable)
        println("사용자 계좌 조회 결과: ${userAccounts.totalElements}개")
        
        // 계좌 정보를 ProductSubscriptionResponse로 변환
        return userAccounts.map { account ->
            val product = account.product
            
            // 계좌 정보를 ProductSubscriptionResponse 객체로 변환
            ProductSubscriptionResponse(
                id = account.id ?: 0,
                userId = userId,
                userName = account.user.name,
                productId = product?.id ?: 0,
                productName = product?.name ?: account.accountName,
                productCategory = product?.category ?: account.accountType.toString(),
                accountId = account.id,
                accountNumber = account.accountNumber,
                amount = account.balance,
                subscriptionDate = LocalDate.from(account.createdAt),
                maturityDate = null, // 계좌에는 만기일 개념이 없음
                interestRate = product?.interestRate,
                expectedReturn = null, // 계좌에는 예상 수익 개념이 없음
                status = account.status,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt
            )
        }
    }

    // getUserActiveSubscriptions 메서드 수정
    @Transactional(readOnly = true)
    override fun getUserActiveSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse> {
        println("사용자 활성 상품 구독 목록 조회: userId=$userId")
        
        // 먼저 계좌 리스트를 가져옵니다
        val allAccounts = accountRepository.findByUserId(userId)
        val activeAccounts = allAccounts.filter { it.status == "ACTIVE" }
        
        println("사용자 활성 계좌 조회 결과: ${activeAccounts.size}개")
        
        // pageable 객체로부터 start와 end 인덱스 계산
        val pageSize = pageable.pageSize
        val pageNumber = pageable.pageNumber
        val start = pageNumber * pageSize
        val end = minOf(start + pageSize, activeAccounts.size)
        val pageContent = if (activeAccounts.isEmpty() || start >= activeAccounts.size) {
            emptyList()
        } else {
            activeAccounts.subList(start, end)
        }
        
        // Page 객체로 변환
        val accountsPage = PageImpl(
            pageContent,
            pageable,
            activeAccounts.size.toLong()
        )
        
        // 계좌 정보를 ProductSubscriptionResponse로 변환
        return accountsPage.map { account ->
            val product = account.product
            
            // 계좌 정보를 ProductSubscriptionResponse 객체로 변환
            ProductSubscriptionResponse(
                id = account.id ?: 0,
                userId = userId,
                userName = account.user.name,
                productId = product?.id ?: 0,
                productName = product?.name ?: account.accountName,
                productCategory = product?.category ?: account.accountType.toString(),
                accountId = account.id,
                accountNumber = account.accountNumber,
                amount = account.balance,
                subscriptionDate = LocalDate.from(account.createdAt),
                maturityDate = null, // 계좌에는 만기일 개념이 없음
                interestRate = product?.interestRate,
                expectedReturn = null, // 계좌에는 예상 수익 개념이 없음
                status = account.status,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt
            )
        }
    }
    
    // 계좌번호 생성 함수
    private fun generateAccountNumber(): String {
        val random = Random()
        val prefix = "110" // 자유입출금 계좌 접두사
        val part2 = String.format("%03d", random.nextInt(1000))
        val part3 = String.format("%06d", random.nextInt(1000000))
        
        return "$prefix-$part2-$part3"
    }
    
    // 상품 구독자 수 증가 메서드
    private fun updateProductSubscriptionCount(product: FinancialProduct): FinancialProduct {
        try {
            val currentCount = product.javaClass.getDeclaredField("subscriptionCount")
                .apply { isAccessible = true }
                .get(product) as? Int ?: 0
                
            println("현재 구독자 수: $currentCount")
            
            val copy = product.copy()
            copy.javaClass.getDeclaredField("subscriptionCount")
                .apply { isAccessible = true }
                .set(copy, currentCount + 1)
            
            return financialProductRepository.save(copy)
        } catch (e: Exception) {
            println("구독자 수 증가 처리 실패, 무시합니다: ${e.message}")
            return product
        }
    }
    
    // 출금 계좌에서 금액 차감
    @Transactional
    override fun processWithdrawalFromAccount(accountId: Long, amount: BigDecimal, description: String): Boolean {
        try {
            // AccountService를 통해 출금 처리
            accountService.withdraw(accountId, amount, description)
            return true
        } catch (e: Exception) {
            throw BadRequestException("출금 처리 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    // 상품 유형에 따른 출금 계좌 필요 여부 확인
    override fun requiresSourceAccount(category: String): Boolean {
        // CHECKING(자유입출금), LOAN(대출)은 출금 계좌가 필요하지 않음
        // FUND(펀드), SAVINGS(적금), DEPOSIT(정기예금)은 출금 계좌가 필요함
        return category == "FUND" || category == "SAVINGS" || category == "DEPOSIT"
    }
    
    // 금액 유효성 검사
    private fun validateAmount(product: FinancialProduct, amount: BigDecimal) {
        // 최소 금액 검증
        if (product.minAmount != null && amount < product.minAmount) {
            throw BadRequestException("최소 가입 금액(${product.minAmount})보다 작은 금액입니다.")
        }
        
        // 최대 금액 검증
        if (product.maxAmount != null && amount > product.maxAmount) {
            throw BadRequestException("최대 가입 금액(${product.maxAmount})보다 큰 금액입니다.")
        }
    }
    
    // 만기일 계산
    private fun calculateMaturityDate(product: FinancialProduct, term: Int?): LocalDate? {
        println("만기일 계산 시작 - 상품: ${product.name}, 기간(term): $term")
        
        if (term == null) {
            println("기간(term)이 null이므로 만기일은 null로 설정됩니다.")
            return null
        }
        
        if (term <= 0) {
            println("기간(term)이 0보다 작거나 같으므로 ($term) 만기일은 null로 설정됩니다.")
            return null
        }
        
        // 현재 날짜에서 기간(개월)만큼 더함
        val result = LocalDate.now().plusMonths(term.toLong())
        println("계산된 만기일: $result (현재 날짜에서 $term 개월 추가)")
        
        return result
    }
    
    /**
     * 예상 수익을 계산합니다.
     * @param amount 금액
     * @param interestRate 이자율 (%)
     * @param term 기간 (개월 수)
     * @return 계산된 예상 수익
     */
    private fun calculateExpectedReturn(amount: Double, interestRate: Double, term: Int): Double {
        try {
            // 입력값 검증
            if (amount <= 0 || interestRate < 0 || term <= 0) {
                println("예상 수익 계산 오류: 유효하지 않은 입력값 (amount=$amount, interestRate=$interestRate, term=$term)")
                return 0.0
            }
            
            println("예상 수익 계산 시작(Double): amount=$amount, interestRate=$interestRate, term=$term")
            
            // BigDecimal로 변환하여 계산
            val principalBD = BigDecimal.valueOf(amount)
            val interestRateBD = BigDecimal.valueOf(interestRate)
            
            // 예상 수익 계산 함수 (단순화된 계산)
            val annualRate = interestRateBD.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
            if (annualRate == BigDecimal.ZERO) {
                println("예상 수익 계산: 이자율이 0입니다.")
                return 0.0
            }
            
            val monthlyRate = annualRate.divide(BigDecimal("12"), 10, RoundingMode.HALF_UP)
            println("월 이자율 계산 결과: $monthlyRate")
            
            // 원금에 이자율을 적용하여 수익 계산
            val termBD = BigDecimal(term)
            val interest = principalBD.multiply(monthlyRate).multiply(termBD)
            
            // 결과 반올림 및 반환
            val result = interest.setScale(2, RoundingMode.HALF_UP)
            println("최종 예상 수익 계산 결과: $result")
            
            return result.toDouble()
        } catch (e: Exception) {
            println("Double 예상 수익 계산 중 예외 발생: ${e.message}")
            e.printStackTrace()
            return 0.0
        }
    }
    
    @Transactional(readOnly = true)
    override fun getProductsByCategory(category: String, pageable: Pageable): Page<FinancialProductResponse> {
        return financialProductRepository.findByCategory(category, pageable)
            .map { product -> FinancialProductResponse.fromEntity(product) }
    }

    @Transactional(readOnly = true)
    override fun getPopularProducts(limit: Int): List<FinancialProductResponse> {
        val pageRequest = org.springframework.data.domain.PageRequest.of(0, limit, 
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "subscriptionCount"))
        val products = financialProductRepository.findByStatus("ACTIVE", pageRequest)
        return products.map { product -> FinancialProductResponse.fromEntity(product) }.content
    }
} 