package com.pleasybank.account.repository

import com.pleasybank.account.entity.Account
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AccountRepository : JpaRepository<Account, Long> {

    // 사용자 ID로 계좌 목록 조회
    fun findByUserId(userId: Long, pageable: Pageable): Page<Account>
    
    // 계좌 ID와 사용자 ID로 계좌 조회
    fun findByIdAndUserId(id: Long, userId: Long): Optional<Account>
    
    // 계좌 번호로 계좌 조회
    fun findByAccountNumber(accountNumber: String): Optional<Account>
    
    // 계좌 번호 중복 확인
    fun existsByAccountNumber(accountNumber: String): Boolean
    
    // 사용자 ID별 계좌 수 조회
    fun countByUserId(userId: Long): Long
    
    // 계좌 유형별 사용자 계좌 조회
    fun findByUserIdAndAccountType(userId: Long, accountType: String, pageable: Pageable): Page<Account>
    
    // 계좌 상태별 사용자 계좌 조회
    fun findByUserIdAndStatus(userId: Long, status: String, pageable: Pageable): Page<Account>
} 