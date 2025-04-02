package com.pleasybank.core.security.jwt

import io.jsonwebtoken.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 */
@Component
class JwtTokenProvider(
    private val userDetailsService: UserDetailsService
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    
    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${app.jwt.expirationMs}")
    private var jwtExpirationMs: Long = 0
    
    @Value("\${app.jwt.refreshExpirationMs}")
    private var jwtRefreshExpirationMs: Long = 0
    
    /**
     * 사용자 정보로 액세스 토큰 생성
     */
    fun createToken(username: String, role: String): String {
        val claims = Jwts.claims().setSubject(username)
        claims["role"] = role
        
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        
        val signingKey = SecretKeySpec(
            DatatypeConverter.parseBase64Binary(jwtSecret),
            SignatureAlgorithm.HS512.jcaName
        )
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, signingKey)
            .compact()
    }
    
    /**
     * 리프레시 토큰 생성
     */
    fun createRefreshToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtRefreshExpirationMs)
        
        val signingKey = SecretKeySpec(
            DatatypeConverter.parseBase64Binary(jwtSecret),
            SignatureAlgorithm.HS512.jcaName
        )
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, signingKey)
            .compact()
    }
    
    /**
     * 토큰에서 사용자명 추출
     */
    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parser()
            .setSigningKey(DatatypeConverter.parseBase64Binary(jwtSecret))
            .parseClaimsJws(token)
            .body
        
        return claims.subject
    }
    
    /**
     * 토큰으로부터 인증 정보 생성
     */
    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUsernameFromToken(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }
    
    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        try {
            val claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(jwtSecret))
                .parseClaimsJws(token)
            
            return !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            when (e) {
                is SignatureException -> logger.error("Invalid JWT signature: ${e.message}")
                is MalformedJwtException -> logger.error("Invalid JWT token: ${e.message}")
                is ExpiredJwtException -> logger.error("JWT token is expired: ${e.message}")
                is UnsupportedJwtException -> logger.error("JWT token is unsupported: ${e.message}")
                is IllegalArgumentException -> logger.error("JWT claims string is empty: ${e.message}")
                else -> logger.error("JWT validation error: ${e.message}")
            }
            return false
        }
    }
} 