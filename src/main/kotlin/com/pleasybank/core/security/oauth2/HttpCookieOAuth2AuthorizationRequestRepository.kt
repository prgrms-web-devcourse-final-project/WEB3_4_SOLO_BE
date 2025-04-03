package com.pleasybank.core.security.oauth2

import com.pleasybank.core.security.oauth2.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.util.Base64

@Component
class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private val logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository::class.java)

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
        const val COOKIE_EXPIRE_SECONDS = 1800
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        try {
            val cookie = CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            logger.debug("Loading authorization request from cookie: {}", cookie?.value?.take(50))
            return cookie?.let {
                val value = Base64.getUrlDecoder().decode(it.value)
                SerializationUtils.deserialize(value) as OAuth2AuthorizationRequest
            }
        } catch (e: Exception) {
            logger.error("Error loading authorization request from cookie", e)
            return null
        }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            logger.debug("Authorization request is null, clearing cookies")
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            return
        }

        logger.debug("Saving authorization request to cookie: {}", authorizationRequest.authorizationUri)
        val serializedAuth = SerializationUtils.serialize(authorizationRequest)
        val encodedAuth = Base64.getUrlEncoder().encodeToString(serializedAuth)
        CookieUtils.addCookie(
            response, 
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, 
            encodedAuth, 
            COOKIE_EXPIRE_SECONDS
        )

        val redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)
        if (redirectUriAfterLogin != null && redirectUriAfterLogin.isNotBlank()) {
            logger.debug("Saving redirect URI to cookie: {}", redirectUriAfterLogin)
            CookieUtils.addCookie(
                response, 
                REDIRECT_URI_PARAM_COOKIE_NAME, 
                redirectUriAfterLogin, 
                COOKIE_EXPIRE_SECONDS
            )
        }
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest, response: HttpServletResponse): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request).also {
            removeAuthorizationRequestCookies(request, response)
        }
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        logger.debug("Removing authorization request cookies")
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
    }
} 