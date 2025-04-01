package com.pleasybank.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import java.util.*

object CookieUtils {
    private val logger = LoggerFactory.getLogger(CookieUtils::class.java)
    
    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        val cookies = request.cookies ?: return null
        
        for (cookie in cookies) {
            if (cookie.name == name) {
                logger.debug("쿠키 찾음: ${cookie.name}, 값 길이=${cookie.value.length}, 경로=${cookie.path}, maxAge=${cookie.maxAge}")
                return cookie
            }
        }
        
        logger.info("쿠키를 찾을 수 없음: $name")
        return null
    }
    
    fun addCookie(
        response: HttpServletResponse, 
        name: String, 
        value: String, 
        maxAge: Int,
        path: String = "/",
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true,
        sameSite: String = "Lax"
    ) {
        val cookie = Cookie(name, value)
        cookie.path = path
        cookie.isHttpOnly = httpOnly
        cookie.maxAge = maxAge
        
        if (domain != null) {
            cookie.domain = domain
        }
        
        cookie.secure = secure
        
        // SameSite 설정은 헤더에 직접 추가
        val cookieHeader = StringBuilder()
            .append(cookie.name).append("=").append(cookie.value)
            .append("; Max-Age=").append(cookie.maxAge)
            .append("; Path=").append(cookie.path)
            
        if (cookie.domain != null) {
            cookieHeader.append("; Domain=").append(cookie.domain)
        }
        
        if (cookie.secure) {
            cookieHeader.append("; Secure")
        }
        
        if (cookie.isHttpOnly) {
            cookieHeader.append("; HttpOnly")
        }
        
        cookieHeader.append("; SameSite=").append(sameSite)
        
        response.addHeader("Set-Cookie", cookieHeader.toString())
        
        logger.info("쿠키 추가: $name, 값 길이=${value.length}, maxAge=$maxAge, path=$path, sameSite=$sameSite")
    }
    
    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    cookie.value = ""
                    cookie.path = "/"
                    cookie.maxAge = 0
                    response.addCookie(cookie)
                    
                    // SameSite=None 설정을 위한 추가 헤더
                    response.addHeader("Set-Cookie", 
                        "${cookie.name}=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax")
                    
                    logger.info("쿠키 삭제: ${cookie.name}")
                    return
                }
            }
        }
        logger.warn("삭제할 쿠키를 찾을 수 없음: $name")
    }
} 