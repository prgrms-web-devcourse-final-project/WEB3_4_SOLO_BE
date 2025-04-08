package com.pleasybank.domain.product.scheduler

import com.pleasybank.domain.account.service.AccountService
import com.pleasybank.domain.product.entity.ProductSubscription
import com.pleasybank.domain.product.repository.ProductSubscriptionRepository
import com.pleasybank.domain.product.service.FinancialProductService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 금융 상품 만기 관리 스케줄러
 * 만기가 도래한 금융 상품을 처리하고 알림을 생성합니다.
 */
@Component
class ProductSubscriptionScheduler(
    private val productSubscriptionRepository: ProductSubscriptionRepository,
    private val accountService: AccountService,
    private val financialProductService: FinancialProductService
) {
    private val logger = LoggerFactory.getLogger(ProductSubscriptionScheduler::class.java)
    
    /**
     * 매일 자정에 만기 도래 상품 처리
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    fun processMaturityDueProducts() {
        val today = LocalDate.now()
        logger.info("만기 도래 상품 처리 스케줄러 실행: {}", today)
        
        val maturityDueSubscriptions = productSubscriptionRepository.findMaturityDueSubscriptions(today)
        logger.info("만기 도래 상품 수: {}", maturityDueSubscriptions.size)
        
        for (subscription in maturityDueSubscriptions) {
            try {
                processSubscription(subscription)
            } catch (e: Exception) {
                logger.error("상품 만기 처리 중 오류 발생. 상품 ID: {}, 오류: {}", subscription.id, e.message)
            }
        }
        
        logger.info("만기 도래 상품 처리 완료")
    }
    
    /**
     * 만기 도래 3일 전에 알림 생성
     */
    @Scheduled(cron = "0 0 10 * * *") // 매일 오전 10시
    @Transactional
    fun notifyUpcomingMaturities() {
        val threeDaysLater = LocalDate.now().plusDays(3)
        logger.info("만기 예정 상품 알림 스케줄러 실행. 대상 날짜: {}", threeDaysLater)
        
        val upcomingMaturities = productSubscriptionRepository.findMaturityDueSubscriptions(threeDaysLater)
        logger.info("만기 예정 상품 수: {}", upcomingMaturities.size)
        
        for (subscription in upcomingMaturities) {
            try {
                // 알림 생성 로직 (실제 알림 서비스 연동 필요)
                logger.info("만기 예정 알림 생성: 사용자 ID: {}, 상품 ID: {}, 만기일: {}", 
                    subscription.user.id, subscription.product.id, subscription.maturityDate)
                
                // TODO: 알림 서비스 연동
            } catch (e: Exception) {
                logger.error("만기 예정 알림 생성 중 오류 발생. 상품 ID: {}, 오류: {}", subscription.id, e.message)
            }
        }
        
        logger.info("만기 예정 상품 알림 생성 완료")
    }
    
    /**
     * 개별 상품 구독 만기 처리
     */
    private fun processSubscription(subscription: ProductSubscription) {
        logger.info("상품 구독 만기 처리 시작. ID: {}, 사용자: {}, 상품: {}", 
            subscription.id, subscription.user.id, subscription.product.id)
        
        // 자동 연장 여부 확인
        if (subscription.autoRenew) {
            logger.info("자동 연장 처리. 구독 ID: {}", subscription.id)
            // 새 만기일 계산 (기존 만기일로부터 상품의 기본 기간만큼 연장)
            val newMaturityDate = calculateNewMaturityDate(subscription)
            
            // 구독 상태 업데이트 (만기일 연장)
            val request = com.pleasybank.domain.product.dto.UpdateProductSubscriptionRequest(
                maturityDate = newMaturityDate
            )
            
            financialProductService.updateProductSubscription(subscription.id!!, request)
            logger.info("자동 연장 완료. 구독 ID: {}, 새 만기일: {}", subscription.id, newMaturityDate)
        } else {
            logger.info("만기 처리 (원금+이자 지급). 구독 ID: {}", subscription.id)
            
            // 원금과 이자 합산 금액
            val totalAmount = subscription.amount.add(subscription.expectedReturn ?: BigDecimal.ZERO)
            
            // 계좌에 입금
            accountService.deposit(
                subscription.account.id!!, 
                totalAmount, 
                "금융 상품 만기 지급 (상품: ${subscription.product.name})"
            )
            
            // 구독 상태 업데이트
            val request = com.pleasybank.domain.product.dto.UpdateProductSubscriptionRequest(
                status = "MATURED"
            )
            
            financialProductService.updateProductSubscription(subscription.id!!, request)
            logger.info("만기 처리 완료. 구독 ID: {}, 지급 금액: {}", subscription.id, totalAmount)
        }
    }
    
    /**
     * 새 만기일 계산
     */
    private fun calculateNewMaturityDate(subscription: ProductSubscription): LocalDate {
        // 상품의 기본 기간 (없으면 1년으로 가정)
        val termMonths = subscription.product.term ?: 12
        
        // 현재 만기일로부터 기간만큼 연장
        return subscription.maturityDate.plusMonths(termMonths.toLong())
    }
} 