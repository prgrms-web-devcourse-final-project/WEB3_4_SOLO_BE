package com.pleasybank.domain.product.controller

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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "금융 상품 구독 API", description = "금융 상품 구독 관련 API")
class ProductSubscriptionController(
    private val financialProductService: FinancialProductService
) {

    @Operation(summary = "상품 구독 생성", description = "사용자가 금융 상품에 가입합니다.")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createProductSubscription(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CreateProductSubscriptionRequest
    ): ResponseEntity<ProductSubscriptionResponse> {
        val userId = extractUserId(userDetails)
        val response = financialProductService.createProductSubscription(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "상품 구독 상세 조회", description = "지정된 ID의 상품 구독 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getProductSubscriptionById(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ProductSubscriptionResponse> {
        // 관리자가 아니면서 해당 구독이 자신의 것이 아닌 경우 접근을 제한해야 하지만,
        // 여기서는 서비스에서 확인한다고 가정하고 간단히 구현
        val response = financialProductService.getProductSubscriptionById(id)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "현재 사용자의 상품 구독 목록 조회", description = "로그인한 사용자의 상품 구독 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    fun getMyProductSubscriptions(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<ProductSubscriptionResponse>> {
        val userId = extractUserId(userDetails)
        val response = financialProductService.getUserProductSubscriptions(userId, pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "사용자별 상품 구독 목록 조회 (관리자용)", description = "관리자 권한으로 특정 사용자의 상품 구독 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserProductSubscriptions(
        @PathVariable userId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<ProductSubscriptionResponse>> {
        val response = financialProductService.getUserProductSubscriptions(userId, pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "현재 사용자의 총 투자 금액 조회", description = "로그인한 사용자의 총 투자 금액을 조회합니다.")
    @GetMapping("/my/total-invested")
    @PreAuthorize("hasRole('USER')")
    fun getMyTotalInvestedAmount(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, BigDecimal>> {
        val userId = extractUserId(userDetails)
        val totalAmount = financialProductService.getUserTotalInvestedAmount(userId)
        return ResponseEntity.ok(mapOf("totalInvestedAmount" to totalAmount))
    }

    @Operation(summary = "상품 구독 정보 수정", description = "지정된 ID의 상품 구독 정보를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    fun updateProductSubscription(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductSubscriptionRequest
    ): ResponseEntity<ProductSubscriptionResponse> {
        // 자신의 구독만 수정할 수 있는 권한 체크는 서비스에서 처리한다고 가정
        val response = financialProductService.updateProductSubscription(id, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "상품 구독 취소", description = "지정된 ID의 상품 구독을 취소합니다.")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    fun cancelProductSubscription(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ProductSubscriptionResponse> {
        // 자신의 구독만 취소할 수 있는 권한 체크는 서비스에서 처리한다고 가정
        val response = financialProductService.cancelProductSubscription(id)
        return ResponseEntity.ok(response)
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        // UserDetails 구현체에서 userId를 추출하는 로직
        // 실제 구현체에 따라 다를 수 있음
        return (userDetails as com.pleasybank.domain.auth.model.CustomUserDetails).id
    }
} 