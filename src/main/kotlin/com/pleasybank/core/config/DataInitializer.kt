package com.pleasybank.core.config

import com.pleasybank.domain.product.entity.FinancialProduct
import com.pleasybank.domain.product.repository.FinancialProductRepository
import com.pleasybank.domain.user.entity.User
import com.pleasybank.domain.user.entity.Role
import com.pleasybank.domain.user.repository.UserRepository
import com.pleasybank.domain.user.repository.RoleRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
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
    fun initializeData(
        financialProductRepository: FinancialProductRepository,
        userRepository: UserRepository,
        roleRepository: RoleRepository,
        passwordEncoder: PasswordEncoder
    ): CommandLineRunner {
        return CommandLineRunner {
            logger.info("데이터 초기화 시작...")
            initializeFinancialProducts(financialProductRepository)
            initializeAdminUser(userRepository, roleRepository, passwordEncoder)
            logger.info("데이터 초기화 완료!")
        }
    }
    
    /**
     * 관리자 계정 초기화
     */
    private fun initializeAdminUser(
        userRepository: UserRepository,
        roleRepository: RoleRepository,
        passwordEncoder: PasswordEncoder
    ) {
        // 관리자 계정 이메일 확인
        val adminEmail = "admin@pleasybank.com"
        if (userRepository.findByEmail(adminEmail) != null) {
            logger.info("관리자 계정이 이미 존재합니다. 초기화를 건너뜁니다.")
            return
        }
        
        // 역할 확인 및 생성
        var adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null)
        if (adminRole == null) {
            adminRole = Role(name = "ROLE_ADMIN", description = "관리자 권한")
            roleRepository.save(adminRole)
            logger.info("관리자 역할 생성 완료")
        }
        
        var userRole = roleRepository.findByName("ROLE_USER").orElse(null)
        if (userRole == null) {
            userRole = Role(name = "ROLE_USER", description = "일반 사용자 권한")
            roleRepository.save(userRole)
            logger.info("사용자 역할 생성 완료")
        }
        
        // 관리자 계정 생성
        val adminUser = User(
            email = adminEmail,
            password = passwordEncoder.encode("admin1234"), // 기본 비밀번호
            name = "관리자",
            phoneNumber = "010-1234-5678",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = "ACTIVE"
        )
        
        val savedUser = userRepository.save(adminUser)
        
        // 관리자 역할 부여
        savedUser.addRole(adminRole, "시스템")
        userRepository.save(savedUser)
        
        logger.info("관리자 계정 생성 완료 - 이메일: $adminEmail, 비밀번호: admin1234")
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
        
        // 요청된 4개 상품만 초기화
        val products = listOf(
            FinancialProduct(
                id = 1, // ID 명시
                name = "플레지 정기예금",
                category = "DEPOSIT",
                interestRate = BigDecimal("3.600"),
                term = 36,
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
                id = 2, // ID 명시
                name = "플레지 정기적금",
                category = "SAVINGS",
                interestRate = BigDecimal("4.200"),
                term = 36,
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
                id = 3, // ID 명시
                name = "플레지 신용대출",
                category = "LOAN",
                interestRate = BigDecimal("5.100"),
                term = 360,
                minAmount = BigDecimal("10000000.00"),
                maxAmount = BigDecimal("30000000.00"),
                description = "신용 기반의 저금리 대출 상품입니다.",
                features = listOf("저금리", "장기 상환", "신속한 심사"),
                isActive = true,
                status = "ACTIVE",
                imageUrl = "https://example.com/images/loan.jpg",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            FinancialProduct(
                id = 9, // ID 명시
                name = "플레지 자유입출금",
                category = "CHECKING",
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