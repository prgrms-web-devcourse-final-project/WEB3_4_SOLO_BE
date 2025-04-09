package com.pleasybank.domain.product.repository

import com.pleasybank.domain.product.entity.ProductSubscription
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface ProductSubscriptionRepository : JpaRepository<ProductSubscription, Long> {
    
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<ProductSubscription>
    
    fun findByUserId(userId: Long, pageable: Pageable): Page<ProductSubscription>
    
    fun findAllByUserIdAndStatus(userId: Long, status: String): List<ProductSubscription>
    
    @Query("SELECT ps FROM ProductSubscription ps WHERE ps.user.id = :userId AND ps.status = :status")
    fun findByUserIdAndStatusWithPaging(
        @Param("userId") userId: Long, 
        @Param("status") status: String, 
        pageable: Pageable
    ): Page<ProductSubscription>
    
    @Query("SELECT ps FROM ProductSubscription ps WHERE ps.maturityDate <= :date AND ps.status = 'ACTIVE'")
    fun findAllByMaturityDateBefore(@Param("date") date: LocalDate): List<ProductSubscription>
    
    @Query("SELECT ps FROM ProductSubscription ps WHERE ps.maturityDate <= :date AND ps.status = 'ACTIVE'")
    fun findMaturityDueSubscriptions(@Param("date") date: LocalDate): List<ProductSubscription>
    
    @Query("SELECT ps FROM ProductSubscription ps WHERE ps.user.id = :userId AND ps.product.id = :productId")
    fun findByUserIdAndProductId(
        @Param("userId") userId: Long, 
        @Param("productId") productId: Long
    ): List<ProductSubscription>
    
    @Query("SELECT SUM(ps.amount) FROM ProductSubscription ps WHERE ps.user.id = :userId AND ps.status = :status")
    fun sumAmountByUserIdAndStatus(@Param("userId") userId: Long, @Param("status") status: String): BigDecimal?
    
    @Query("SELECT SUM(ps.amount) FROM ProductSubscription ps WHERE ps.user.id = :userId AND ps.status = 'ACTIVE'")
    fun getTotalInvestedAmountByUserId(@Param("userId") userId: Long): BigDecimal?
    
    @Query("SELECT COUNT(ps) FROM ProductSubscription ps WHERE ps.product.id = :productId")
    fun countByProductId(@Param("productId") productId: Long): Long
} 