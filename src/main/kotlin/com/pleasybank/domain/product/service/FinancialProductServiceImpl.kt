package com.pleasybank.domain.product.service

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.core.service.CacheService
import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.repository.AccountRepository
import com.pleasybank.domain.product.dto.*
import com.pleasybank.domain.product.entity.FinancialProduct
import com.pleasybank.domain.product.entity.ProductSubscription
import com.pleasybank.domain.product.repository.FinancialProductRepository
import com.pleasybank.domain.product.repository.ProductSubscriptionRepository
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

@Service
class FinancialProductServiceImpl(
    private val financialProductRepository: FinancialProductRepository,
    private val productSubscriptionRepository: ProductSubscriptionRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val cacheService: CacheService
) : FinancialProductService {

    companion object {
        private const val PRODUCT_CACHE_KEY = "product"
        private const val PRODUCT_LIST_CACHE_KEY = "product_list"
        private const val TOP_RATE_PRODUCTS_CACHE_KEY = "top_rate_products"
        private const val USER_SUBSCRIPTIONS_CACHE_KEY = "user_subscriptions"
        private const val PRODUCT_CACHE_TTL = 3600L  // 1시간
        private const val PRODUCT_LIST_CACHE_TTL = 1800L  // 30분
    }

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
        
        // 캐시 무효화
        cacheService.delete(PRODUCT_LIST_CACHE_KEY)
        cacheService.delete(TOP_RATE_PRODUCTS_CACHE_KEY)
        
        return FinancialProductResponse.fromEntity(savedProduct)
    }

    @Transactional(readOnly = true)
    override fun getFinancialProductById(id: Long): FinancialProductResponse {
        // 캐시에서 상품 정보 조회
        val cacheKey = "${PRODUCT_CACHE_KEY}:$id"
        val cachedProduct = cacheService.getValue(cacheKey) as? FinancialProductResponse
        
        if (cachedProduct != null) {
            return cachedProduct
        }
        
        // 캐시에 없으면 DB에서 조회
        val product = financialProductRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("금융 상품을 찾을 수 없습니다. ID: $id") }
        
        val response = FinancialProductResponse.fromEntity(product)
        
        // 캐시에 저장
        cacheService.setValue(cacheKey, response, PRODUCT_CACHE_TTL)
        
        return response
    }

    @Transactional(readOnly = true)
    override fun getAllFinancialProducts(pageable: Pageable): Page<FinancialProductResponse> {
        // 페이지네이션이 있어 캐싱에 제한이 있지만, 첫 페이지는 자주 접근할 수 있으므로 캐싱
        val isFirstPage = pageable.pageNumber == 0 && pageable.pageSize <= 10
        val cacheKey = "$PRODUCT_LIST_CACHE_KEY:${pageable.pageNumber}:${pageable.pageSize}"
        
        if (isFirstPage) {
            val cachedValue = cacheService.getValue(cacheKey)
            if (cachedValue is Page<*>) {
                // 타입 안전성 체크 및 매핑
                @Suppress("UNCHECKED_CAST")
                val cachedProducts = cachedValue as Page<FinancialProductResponse>
                return cachedProducts
            }
        }
        
        val products = financialProductRepository.findAll(pageable)
            .map { FinancialProductResponse.fromEntity(it) }
        
        if (isFirstPage) {
            cacheService.setValue(cacheKey, products, PRODUCT_LIST_CACHE_TTL)
        }
        
        return products
    }

    @Transactional(readOnly = true)
    override fun getFinancialProductsByCategory(category: String, pageable: Pageable): Page<FinancialProductResponse> {
        return financialProductRepository.findAllByCategory(category, pageable)
            .map { FinancialProductResponse.fromEntity(it) }
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
        
        // 캐시 무효화
        cacheService.delete("${PRODUCT_CACHE_KEY}:$id")
        cacheService.delete(PRODUCT_LIST_CACHE_KEY)
        cacheService.delete(TOP_RATE_PRODUCTS_CACHE_KEY)
        
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
        
        // 캐시 무효화
        cacheService.delete("${PRODUCT_CACHE_KEY}:$id")
        cacheService.delete(PRODUCT_LIST_CACHE_KEY)
        cacheService.delete(TOP_RATE_PRODUCTS_CACHE_KEY)
    }

    @Transactional(readOnly = true)
    override fun getTopRateProducts(limit: Int): List<FinancialProductResponse> {
        val cacheKey = "$TOP_RATE_PRODUCTS_CACHE_KEY:$limit"
        
        // 캐시에서 조회
        val cachedValue = cacheService.getValue(cacheKey)
        if (cachedValue is List<*> && (cachedValue.isEmpty() || cachedValue[0] is FinancialProductResponse)) {
            // 타입 안전성 체크 및 매핑
            @Suppress("UNCHECKED_CAST")
            val cachedProducts = cachedValue as List<FinancialProductResponse>
            return cachedProducts
        }
        
        // 캐시에 없으면 DB에서 조회
        val products = financialProductRepository.findTopRateProducts()
            .take(limit)
            .map { FinancialProductResponse.fromEntity(it) }
        
        // 캐시에 저장
        cacheService.setValue(cacheKey, products, PRODUCT_LIST_CACHE_TTL)
        
        return products
    }

    // 상품 구독 관련 서비스 구현

    @Transactional
    override fun createProductSubscription(userId: Long, request: CreateProductSubscriptionRequest): ProductSubscriptionResponse {
        // 사용자 확인
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: $userId") }
        
        // 상품 확인
        val product = financialProductRepository.findById(request.productId)
            .orElseThrow { ResourceNotFoundException("금융 상품을 찾을 수 없습니다. ID: ${request.productId}") }
        
        // 계좌 확인
        val account = accountRepository.findById(request.accountId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다. ID: ${request.accountId}") }
        
        // 금액 검증
        if (product.minAmount != null && request.amount < product.minAmount) {
            throw IllegalArgumentException("최소 가입 금액(${product.minAmount})보다 적은 금액입니다.")
        }
        
        if (product.maxAmount != null && request.amount > product.maxAmount) {
            throw IllegalArgumentException("최대 가입 금액(${product.maxAmount})을 초과하는 금액입니다.")
        }
        
        // 계좌 잔액 확인
        if (account.balance < request.amount) {
            throw IllegalArgumentException("계좌 잔액이 부족합니다.")
        }
        
        // 계좌에서 금액 차감
        val updatedAccount = account.copy(
            balance = account.balance.subtract(request.amount),
            updatedAt = LocalDateTime.now()
        )
        accountRepository.save(updatedAccount)
        
        // 상품 구독 생성
        val subscription = ProductSubscription(
            user = user,
            product = product,
            account = account,
            amount = request.amount,
            subscriptionDate = LocalDate.now(),
            maturityDate = request.maturityDate,
            interestRate = product.interestRate,
            expectedReturn = calculateExpectedReturn(request.amount, product.interestRate, ChronoUnit.MONTHS.between(LocalDate.now(), request.maturityDate).toInt()),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedSubscription = productSubscriptionRepository.save(subscription)
        
        // 캐시 무효화
        cacheService.delete("$USER_SUBSCRIPTIONS_CACHE_KEY:$userId")
        
        return ProductSubscriptionResponse.fromEntity(savedSubscription)
    }

    @Transactional(readOnly = true)
    override fun getProductSubscriptionById(id: Long): ProductSubscriptionResponse {
        val subscription = productSubscriptionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("상품 구독 정보를 찾을 수 없습니다. ID: $id") }
        return ProductSubscriptionResponse.fromEntity(subscription)
    }

    @Transactional(readOnly = true)
    override fun getUserProductSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse> {
        val isFirstPage = pageable.pageNumber == 0 && pageable.pageSize <= 10
        val cacheKey = "$USER_SUBSCRIPTIONS_CACHE_KEY:$userId:${pageable.pageNumber}:${pageable.pageSize}"
        
        if (isFirstPage) {
            val cachedValue = cacheService.getValue(cacheKey)
            if (cachedValue is Page<*>) {
                // 타입 안전성 체크 및 매핑
                @Suppress("UNCHECKED_CAST")
                val cachedSubscriptions = cachedValue as Page<ProductSubscriptionResponse>
                return cachedSubscriptions
            }
        }
        
        val subscriptions = productSubscriptionRepository.findAllByUserId(userId, pageable)
            .map { ProductSubscriptionResponse.fromEntity(it) }
        
        if (isFirstPage) {
            cacheService.setValue(cacheKey, subscriptions, PRODUCT_LIST_CACHE_TTL)
        }
        
        return subscriptions
    }

    @Transactional
    override fun updateProductSubscription(id: Long, request: UpdateProductSubscriptionRequest): ProductSubscriptionResponse {
        val subscription = productSubscriptionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("상품 구독 정보를 찾을 수 없습니다. ID: $id") }
        
        // 변경 가능한 필드만 업데이트
        val updatedSubscription = subscription.copy(
            amount = request.amount ?: subscription.amount,
            maturityDate = request.maturityDate ?: subscription.maturityDate,
            status = request.status ?: subscription.status,
            updatedAt = LocalDateTime.now()
        )
        
        // 상품 구독 업데이트
        val savedSubscription = productSubscriptionRepository.save(updatedSubscription)
        
        // 캐시 무효화
        cacheService.delete("$USER_SUBSCRIPTIONS_CACHE_KEY:${subscription.user.id}")
        
        // 해지 처리의 경우 계좌에 금액 반환
        if (request.status == "TERMINATED") {
            // 계좌에 금액 반환
            val account = subscription.account
            val updatedAccount = account.copy(
                balance = account.balance.add(subscription.amount),
                updatedAt = LocalDateTime.now()
            )
            accountRepository.save(updatedAccount)
        }
        
        return ProductSubscriptionResponse.fromEntity(savedSubscription)
    }

    @Transactional
    override fun cancelProductSubscription(id: Long): ProductSubscriptionResponse {
        val subscription = productSubscriptionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("상품 구독 정보를 찾을 수 없습니다. ID: $id") }
        
        // 이미 취소된 구독인지 확인
        if (subscription.status != "ACTIVE") {
            throw IllegalArgumentException("이미 취소되었거나 만기된 구독입니다.")
        }
        
        // 계좌 잔액 반환 로직
        val account = subscription.account
        
        // 중도 해지 시 일부 이자 차감 등의 로직이 필요할 수 있음
        // 여기서는 원금만 반환하는 단순 로직으로 구현
        val updatedAccount = account.copy(
            balance = account.balance.add(subscription.amount),
            updatedAt = LocalDateTime.now()
        )
        accountRepository.save(updatedAccount)
        
        // 구독 상태 변경
        val canceledSubscription = subscription.copy(
            status = "CANCELLED", // 상태를 취소로 변경
            updatedAt = LocalDateTime.now()
        )
        
        val savedSubscription = productSubscriptionRepository.save(canceledSubscription)
        
        // 캐시 무효화
        cacheService.delete("$USER_SUBSCRIPTIONS_CACHE_KEY:${subscription.user.id}")
        
        return ProductSubscriptionResponse.fromEntity(savedSubscription)
    }

    @Transactional(readOnly = true)
    override fun getUserTotalInvestedAmount(userId: Long): BigDecimal {
        return productSubscriptionRepository.sumAmountByUserIdAndStatus(userId, "ACTIVE") ?: BigDecimal.ZERO
    }

    /**
     * 예상 수익 계산 함수 (단순화된 계산)
     */
    private fun calculateExpectedReturn(
        principal: BigDecimal, 
        interestRate: BigDecimal, 
        termMonths: Int
    ): BigDecimal {
        val annualRate = interestRate.divide(BigDecimal(100))
        val monthlyRate = annualRate.divide(BigDecimal(12), 10, RoundingMode.HALF_UP)
        val months = BigDecimal(termMonths)
        
        // 단리 계산: 원금 * 이자율 * 기간
        return principal.multiply(monthlyRate).multiply(months)
    }

    @Transactional(readOnly = true)
    override fun getProductsByCategory(category: String, pageable: Pageable): Page<FinancialProductResponse> {
        return financialProductRepository.findByCategory(category, pageable)
            .map { FinancialProductResponse.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getPopularProducts(limit: Int): List<FinancialProductResponse> {
        val pageRequest = org.springframework.data.domain.PageRequest.of(0, limit, 
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "subscriptionCount"))
        val products = financialProductRepository.findByStatus("ACTIVE", pageRequest)
        return products.map { FinancialProductResponse.fromEntity(it) }.content
    }
    
    // ... 다른 메서드들 ...
    
    @Transactional(readOnly = true)
    override fun getUserSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse> {
        val subscriptions = productSubscriptionRepository.findByUserId(userId, pageable)
        return subscriptions.map { ProductSubscriptionResponse.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getUserActiveSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse> {
        val subscriptions = productSubscriptionRepository.findByUserIdAndStatusWithPaging(userId, "ACTIVE", pageable)
        return subscriptions.map { ProductSubscriptionResponse.fromEntity(it) }
    }
    
    // ... 다른 메서드들 ...
} 