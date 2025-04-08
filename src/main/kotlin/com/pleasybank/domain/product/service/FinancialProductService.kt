package com.pleasybank.domain.product.service

import com.pleasybank.domain.product.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

interface FinancialProductService {
    
    // 금융 상품 관련 서비스
    fun createFinancialProduct(request: CreateFinancialProductRequest): FinancialProductResponse
    
    fun getFinancialProductById(id: Long): FinancialProductResponse
    
    fun getAllFinancialProducts(pageable: Pageable): Page<FinancialProductResponse>
    
    fun getFinancialProductsByCategory(category: String, pageable: Pageable): Page<FinancialProductResponse>
    
    fun getFinancialProductsByType(type: String, pageable: Pageable): Page<FinancialProductResponse>
    
    fun updateFinancialProduct(id: Long, request: UpdateFinancialProductRequest): FinancialProductResponse
    
    fun deleteFinancialProduct(id: Long)
    
    fun getTopRateProducts(limit: Int): List<FinancialProductResponse>
    
    // 상품 구독 관련 서비스
    fun createProductSubscription(userId: Long, request: CreateProductSubscriptionRequest): ProductSubscriptionResponse
    
    fun getProductSubscriptionById(id: Long): ProductSubscriptionResponse
    
    fun getUserProductSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse>
    
    fun updateProductSubscription(id: Long, request: UpdateProductSubscriptionRequest): ProductSubscriptionResponse
    
    fun cancelProductSubscription(id: Long): ProductSubscriptionResponse
    
    fun getUserTotalInvestedAmount(userId: Long): BigDecimal
    
    // 추가 기능
    fun getProductsByCategory(category: String, pageable: Pageable): Page<FinancialProductResponse>
    
    fun getPopularProducts(limit: Int): List<FinancialProductResponse>
    
    fun getUserSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse>
    
    fun getUserActiveSubscriptions(userId: Long, pageable: Pageable): Page<ProductSubscriptionResponse>
} 