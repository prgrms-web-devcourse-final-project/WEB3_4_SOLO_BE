package com.pleasybank.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretString: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long,
    @Value("\${jwt.refresh-expiration}") private val refreshExpirationMs: Long
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretString.toByteArray())
    }
    
    fun createToken(username: String, role: String): String {
        val claims: Claims = Jwts.claims().setSubject(username)
        claims["role"] = role
        
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }
    
    fun createRefreshToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshExpirationMs)
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }
    
    fun getAuthentication(token: String): Authentication {
        val claims = getClaims(token)
        val authorities = listOf(SimpleGrantedAuthority(claims["role"].toString()))
        
        val principal = User(claims.subject, "", authorities)
        
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }
    
    fun validateToken(token: String): Boolean {
        try {
            val claims = getClaims(token)
            return !claims.expiration.before(Date())
        } catch (e: Exception) {
            return false
        }
    }
    
    fun getUsername(token: String): String {
        return getClaims(token).subject
    }
    
    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
} 