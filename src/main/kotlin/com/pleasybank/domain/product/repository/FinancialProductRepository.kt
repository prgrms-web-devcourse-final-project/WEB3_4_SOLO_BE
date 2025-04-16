package com.pleasybank.domain.product.repository

import com.pleasybank.domain.product.entity.FinancialProduct
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface FinancialProductRepository : JpaRepository<FinancialProduct, Long> {

    fun findAllByCategory(category: String, pageable: Pageable): Page<FinancialProduct>
    
    fun findByCategory(category: String, pageable: Pageable): Page<FinancialProduct>
    
    @Query("SELECT p FROM FinancialProduct p WHERE p.isActive = true ORDER BY p.interestRate DESC")
    fun findAllByOrderByInterestRateDesc(): List<FinancialProduct>
    
    @Query("SELECT p FROM FinancialProduct p WHERE p.isActive = true ORDER BY p.interestRate DESC")
    fun findTopRateProducts(): List<FinancialProduct>
    
    fun findAllByStatus(status: String, pageable: Pageable): Page<FinancialProduct>
    
    fun findByStatus(status: String, pageable: Pageable): Page<FinancialProduct>
    
    fun findAllByNameContaining(name: String, pageable: Pageable): Page<FinancialProduct>
    
    @Query("SELECT p FROM FinancialProduct p WHERE p.minAmount <= :amount AND (p.maxAmount IS NULL OR p.maxAmount >= :amount)")
    fun findAllByAmountRange(@Param("amount") amount: BigDecimal, pageable: Pageable): Page<FinancialProduct>
    
    fun findByIsActive(isActive: Boolean, pageable: Pageable): Page<FinancialProduct>
} 