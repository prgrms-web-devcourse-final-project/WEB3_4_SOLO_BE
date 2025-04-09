package com.pleasybank.core.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret-key:pleasyBankSecretKey12345678901234567890}")
    private val secretKey: String,
    
    @Value("\${jwt.access-token-expiration-ms:86400000}") // 기본값 1일
    private val tokenValidityInMilliseconds: Long
) {

    private val key: Key
    
    init {
        // 안전한 키 생성 - HS512 알고리즘에 적합한 크기의 키 생성
        key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
        
        // 또는 기존 secretKey를 기반으로 하되 충분한 길이를 보장하려면:
        // val extendedKey = secretKey.padEnd(64, '0') // 최소 64바이트 (512비트) 이상 확보
        // key = Keys.hmacShaKeyFor(extendedKey.toByteArray())
    }

    /**
     * 사용자 인증 정보를 바탕으로 JWT 토큰 생성
     */
    fun createToken(authentication: Authentication): String {
        val authorities = authentication.authorities.joinToString(",") { it.authority }
        val now = Date()
        val validity = Date(now.time + tokenValidityInMilliseconds)

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim("auth", authorities)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * 사용자 ID와 권한을 바탕으로 JWT 토큰 생성
     */
    fun createToken(userId: Long, roles: List<String>): String {
        val authorities = roles.joinToString(",")
        val now = Date()
        val validity = Date(now.time + tokenValidityInMilliseconds)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("auth", authorities)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * JWT 토큰에서 인증 정보 추출
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        val authorities = getAuthorities(claims)
        val userId = claims.subject.toLong()
        
        // 사용자 ID를 기반으로 CustomUserDetails 객체 생성
        val customUserDetails = com.pleasybank.domain.auth.model.CustomUserDetails(
            com.pleasybank.domain.user.entity.User(
                id = userId,
                email = "user-$userId@pleasybank.com", // 임시 이메일
                name = "사용자 $userId",                // 임시 이름
                password = "",                          // 비밀번호는 필요 없음
                provider = "TOKEN",
                status = "ACTIVE"
            )
        )

        return UsernamePasswordAuthenticationToken(customUserDetails, token, authorities)
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject.toLong()
    }

    /**
     * JWT 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            // 토큰 파싱 실패 시 로그 출력
            // 여기서는 간단히 false 반환
            return false
        }
    }

    /**
     * JWT Claims에서 권한 정보 추출
     */
    private fun getAuthorities(claims: Claims): Collection<GrantedAuthority> {
        val authString = claims["auth"] as? String ?: return emptyList()
        return authString.split(",")
            .filter { it.isNotEmpty() }
            .map { SimpleGrantedAuthority(it) }
    }
} 