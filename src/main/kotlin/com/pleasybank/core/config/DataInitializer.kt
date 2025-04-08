package com.pleasybank.core.config

import com.pleasybank.domain.product.entity.FinancialProduct
import com.pleasybank.domain.product.repository.FinancialProductRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 애플리케이션 시작 시 데이터를 초기화하는 설정 클래스
 */
@Configuration
class DataInitializer {
    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)
    
    /**
     * 개발 환경에서만 실행되는 데이터 초기화 빈
     */
    @Bean
    @Profile("!prod") // 운영 환경이 아닌 경우에만 실행
    fun initializeData(financialProductRepository: FinancialProductRepository): CommandLineRunner {
        return CommandLineRunner {
            logger.info("데이터 초기화 시작...")
            initializeFinancialProducts(financialProductRepository)
            logger.info("데이터 초기화 완료!")
        }
    }
    
    /**
     * 금융상품 데이터 초기화
     */
    private fun initializeFinancialProducts(repository: FinancialProductRepository) {
        // 기존 데이터가 있는지 확인
        if (repository.count() > 0) {
            logger.info("금융상품 데이터가 이미 존재합니다. 초기화를 건너뜁니다.")
            return
        }
        
        // V3__create_financial_products_tables.sql에 있는 금융상품 데이터 생성
        val products = listOf(
            FinancialProduct(
                name = "플리지뱅크 정기예금",
                category = "DEPOSIT",
                interestRate = BigDecimal("3.600"),
                term = 36, // maxTermMonths 값 사용
                minAmount = BigDecimal("1000000.00"),
                maxAmount = BigDecimal("50000000.00"),
                description = "안정적인 수익을 제공하는 정기예금 상품입니다.",
                features = listOf("안정적인 수익", "만기 시 이자 지급", "예금자 보호"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/deposit.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "플리지뱅크 정기적금",
                category = "SAVINGS",
                interestRate = BigDecimal("4.200"),
                term = 36, // maxTermMonths 값 사용
                minAmount = BigDecimal("100000.00"),
                maxAmount = BigDecimal("3000000.00"),
                description = "매월 일정액을 저축하여 목돈을 모으는 정기적금 상품입니다.",
                features = listOf("높은 이자율", "정기적 저축", "목돈 마련"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/savings.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "플리지뱅크 주택담보대출",
                category = "LOAN",
                interestRate = BigDecimal("5.100"),
                term = 360, // maxTermMonths 값 사용
                minAmount = BigDecimal("10000000.00"),
                maxAmount = BigDecimal("500000000.00"),
                description = "주택 구입을 위한 저금리 대출 상품입니다.",
                features = listOf("저금리", "장기 상환", "주택 담보"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/mortgage.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "청년 우대 적금",
                category = "SAVINGS",
                interestRate = BigDecimal("4.800"),
                term = 24, // maxTermMonths 값 사용
                minAmount = BigDecimal("50000.00"),
                maxAmount = BigDecimal("1000000.00"),
                description = "39세 이하 청년을 위한 우대금리 적금 상품입니다.",
                features = listOf("청년 우대", "높은 금리", "소액 시작"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/youth.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "플리지 펀드 A",
                category = "FUND",
                interestRate = BigDecimal("7.500"),
                term = 12, // minTermMonths 값 사용
                minAmount = BigDecimal("500000.00"),
                maxAmount = null,
                description = "국내 주식형 펀드로 높은 수익을 추구합니다.",
                features = listOf("높은 수익 가능성", "주식 투자", "적극적 운용"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/fund_a.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "플리지 펀드 B",
                category = "FUND",
                interestRate = BigDecimal("5.200"),
                term = 12, // minTermMonths 값 사용
                minAmount = BigDecimal("500000.00"),
                maxAmount = null,
                description = "글로벌 채권형 펀드로 안정적인 수익을 추구합니다.",
                features = listOf("안정적 수익", "글로벌 투자", "분산 투자"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/fund_b.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "개인사업자 대출",
                category = "LOAN",
                interestRate = BigDecimal("6.200"),
                term = 60, // maxTermMonths 값 사용
                minAmount = BigDecimal("5000000.00"),
                maxAmount = BigDecimal("100000000.00"),
                description = "개인사업자를 위한 운영자금 대출 상품입니다.",
                features = listOf("사업자 전용", "유연한 상환", "신속한 심사"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/business_loan.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "은퇴자 우대 예금",
                category = "DEPOSIT",
                interestRate = BigDecimal("3.900"),
                term = 24, // maxTermMonths 값 사용
                minAmount = BigDecimal("1000000.00"),
                maxAmount = BigDecimal("100000000.00"),
                description = "60세 이상 은퇴자를 위한 우대금리 예금 상품입니다.",
                features = listOf("시니어 우대", "추가 금리", "안정적인 운용"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/senior.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                name = "플리지 자유입출금",
                category = "DEPOSIT",
                interestRate = BigDecimal("1.800"),
                term = null,
                minAmount = BigDecimal("0"),
                maxAmount = null,
                description = "언제든지 자유롭게 입금하고 출금할 수 있는 예금 상품",
                features = listOf("입출금 자유", "연 1.8% 금리", "수수료 면제", "모바일뱅킹 연동"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/free_deposit.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        // 데이터베이스에 저장
        repository.saveAll(products)
        logger.info("금융상품 샘플 데이터 ${products.size}개 추가 완료")
    }
} 