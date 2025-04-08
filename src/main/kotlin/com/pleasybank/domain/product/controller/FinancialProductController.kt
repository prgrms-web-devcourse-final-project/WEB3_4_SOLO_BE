package com.pleasybank.domain.product.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.product.dto.*
import com.pleasybank.domain.product.service.FinancialProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
@Tag(name = "금융 상품 API", description = "금융 상품 관련 API")
class FinancialProductController(
    private val financialProductService: FinancialProductService
) {

    @Operation(summary = "금융 상품 생성", description = "새로운 금융 상품을 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createFinancialProduct(
        @Valid @RequestBody request: CreateFinancialProductRequest
    ): ResponseEntity<FinancialProductResponse> {
        val product = financialProductService.createFinancialProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @Operation(summary = "금융 상품 상세 조회", description = "지정된 ID의 금융 상품 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    fun getFinancialProduct(
        @PathVariable id: Long
    ): ResponseEntity<FinancialProductResponse> {
        val product = financialProductService.getFinancialProductById(id)
        return ResponseEntity.ok(product)
    }

    @Operation(summary = "금융 상품 목록 조회", description = "모든 금융 상품 목록을 페이지네이션으로 조회합니다.")
    @GetMapping
    fun getAllFinancialProducts(
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<FinancialProductResponse>> {
        val products = financialProductService.getAllFinancialProducts(pageable)
        return ResponseEntity.ok(products)
    }

    @Operation(summary = "유형별 금융 상품 조회", description = "특정 유형의 금융 상품 목록을 조회합니다.")
    @GetMapping("/type/{category}")
    fun getFinancialProductsByCategory(
        @PathVariable category: String,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<FinancialProductResponse>> {
        val products = financialProductService.getFinancialProductsByCategory(category, pageable)
        return ResponseEntity.ok(products)
    }

    @Operation(summary = "금융 상품 수정", description = "기존 금융 상품 정보를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateFinancialProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateFinancialProductRequest
    ): ResponseEntity<FinancialProductResponse> {
        val product = financialProductService.updateFinancialProduct(id, request)
        return ResponseEntity.ok(product)
    }

    @Operation(summary = "금융 상품 삭제", description = "금융 상품을 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteFinancialProduct(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        financialProductService.deleteFinancialProduct(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "고금리 상품 조회", description = "이자율이 높은 순서로 금융 상품 목록을 조회합니다.")
    @GetMapping("/top-rate")
    fun getTopRateProducts(
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<List<FinancialProductResponse>> {
        val products = financialProductService.getTopRateProducts(limit)
        return ResponseEntity.ok(products)
    }

    @Operation(summary = "상품 구독 생성", description = "금융 상품에 가입합니다.")
    @PostMapping("/subscriptions")
    @PreAuthorize("isAuthenticated()")
    fun createProductSubscription(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: CreateProductSubscriptionRequest
    ): ResponseEntity<ProductSubscriptionResponse> {
        val subscription = financialProductService.createProductSubscription(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription)
    }

    @Operation(summary = "상품 구독 상세 조회", description = "특정 상품 구독 정보를 조회합니다.")
    @GetMapping("/subscriptions/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getProductSubscription(
        @PathVariable id: Long
    ): ResponseEntity<ProductSubscriptionResponse> {
        val subscription = financialProductService.getProductSubscriptionById(id)
        return ResponseEntity.ok(subscription)
    }

    @Operation(summary = "사용자 상품 구독 목록 조회", description = "현재 사용자의 모든 상품 구독 목록을 조회합니다.")
    @GetMapping("/subscriptions")
    @PreAuthorize("isAuthenticated()")
    fun getUserProductSubscriptions(
        @CurrentUser userId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<ProductSubscriptionResponse>> {
        val subscriptions = financialProductService.getUserProductSubscriptions(userId, pageable)
        return ResponseEntity.ok(subscriptions)
    }

    @Operation(summary = "상품 구독 정보 수정", description = "기존 상품 구독 정보를 수정합니다.")
    @PutMapping("/subscriptions/{id}")
    @PreAuthorize("isAuthenticated()")
    fun updateProductSubscription(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductSubscriptionRequest
    ): ResponseEntity<ProductSubscriptionResponse> {
        val subscription = financialProductService.updateProductSubscription(id, request)
        return ResponseEntity.ok(subscription)
    }

    @Operation(summary = "상품 구독 해지", description = "상품 구독을 해지합니다.")
    @PostMapping("/subscriptions/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    fun cancelProductSubscription(
        @PathVariable id: Long
    ): ResponseEntity<ProductSubscriptionResponse> {
        val subscription = financialProductService.cancelProductSubscription(id)
        return ResponseEntity.ok(subscription)
    }

    @Operation(summary = "총 투자 금액 조회", description = "사용자의 총 투자 금액을 조회합니다.")
    @GetMapping("/user/total-invested")
    @PreAuthorize("isAuthenticated()")
    fun getUserTotalInvestedAmount(
        @CurrentUser userId: Long
    ): ResponseEntity<Map<String, BigDecimal>> {
        val totalAmount = financialProductService.getUserTotalInvestedAmount(userId)
        return ResponseEntity.ok(mapOf("totalInvestedAmount" to totalAmount))
    }

    @Operation(summary = "카테고리별 금융 상품 조회", description = "특정 카테고리의 금융 상품 목록을 조회합니다.")
    @GetMapping("/category/{category}")
    fun getProductsByCategory(
        @PathVariable category: String,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<FinancialProductResponse>> {
        val products = financialProductService.getFinancialProductsByCategory(category, pageable)
        return ResponseEntity.ok(products)
    }
} 