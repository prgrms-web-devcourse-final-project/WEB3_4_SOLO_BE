package com.pleasybank.security.oauth2

import com.pleasybank.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

@Component
class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository::class.java)

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
        const val OAUTH2_AUTHORIZATION_REQUEST_SESSION_KEY = "SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST"
        const val REDIRECT_URI_PARAM_SESSION_KEY = "redirect_uri_session"
        private const val cookieExpireSeconds = 1800
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        logger.info("인증 요청 로드 시도 (세션 ID: ${request.session.id})")
        
        try {
            // 세션에서 먼저 확인
            val sessionRequest = request.session.getAttribute(OAUTH2_AUTHORIZATION_REQUEST_SESSION_KEY) as? OAuth2AuthorizationRequest
            if (sessionRequest != null) {
                logger.info("세션에서 인증 요청을 찾았습니다")
                return sessionRequest
            }
            
            // 쿠키에서 확인
            val cookie = CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            return cookie?.let {
                val value = it.value
                logger.info("쿠키에서 인증 요청을 찾았습니다 (길이: ${value.length})")
                try {
                    deserialize(Base64.getUrlDecoder().decode(value))
                } catch (e: Exception) {
                    logger.error("쿠키 역직렬화 실패: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("인증 요청 로드 중 예외 발생: ${e.message}", e)
            return null
        }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            logger.info("인증 요청이 null이므로 쿠키 삭제")
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            return
        }

        try {
            // 세션에 저장
            request.session.setAttribute(OAUTH2_AUTHORIZATION_REQUEST_SESSION_KEY, authorizationRequest)
            logger.info("세션에 인증 요청 저장 완료 (세션ID: ${request.session.id})")
            
            // 쿠키에 저장
            val serializedAuth = Base64.getUrlEncoder().encodeToString(serialize(authorizationRequest))
            logger.info("인증 요청을 쿠키에 저장 (길이: ${serializedAuth.length})")
            
            CookieUtils.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serializedAuth,
                cookieExpireSeconds,
                "/",
                null,
                false,
                true,
                "Lax"
            )

            val redirectUriAfterLogin = request.getParameter("redirect_uri")
            if (redirectUriAfterLogin != null && redirectUriAfterLogin.isNotBlank()) {
                logger.info("리다이렉트 URI 쿠키에 저장: $redirectUriAfterLogin")
                // 세션에도 저장
                request.session.setAttribute(REDIRECT_URI_PARAM_SESSION_KEY, redirectUriAfterLogin)
                
                CookieUtils.addCookie(
                    response,
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin,
                    cookieExpireSeconds,
                    "/",
                    null,
                    false,
                    true,
                    "Lax"
                )
            }
        } catch (e: Exception) {
            logger.error("인증 요청 저장 중 예외 발생: ${e.message}", e)
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        logger.info("인증 요청 제거 시도 (세션 ID: ${request.session.id})")
        val authRequest = loadAuthorizationRequest(request)
        
        try {
            // 세션에서 제거
            request.session.removeAttribute(OAUTH2_AUTHORIZATION_REQUEST_SESSION_KEY)
            request.session.removeAttribute(REDIRECT_URI_PARAM_SESSION_KEY)
            
            // 쿠키에서 제거
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            
            logger.info("인증 요청 제거 완료")
        } catch (e: Exception) {
            logger.error("인증 요청 제거 중 예외 발생: ${e.message}", e)
        }
        
        return authRequest
    }

    private fun serialize(obj: Any): ByteArray {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(obj)
            objectOutputStream.flush()
            return byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            logger.error("객체 직렬화 실패: ${e.message}", e)
            throw e
        }
    }

    private fun deserialize(bytes: ByteArray): OAuth2AuthorizationRequest? {
        return try {
            val byteArrayInputStream = ByteArrayInputStream(bytes)
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            objectInputStream.readObject() as OAuth2AuthorizationRequest
        } catch (e: Exception) {
            logger.error("인증 요청 역직렬화 실패: ${e.message}", e)
            null
        }
    }
} 