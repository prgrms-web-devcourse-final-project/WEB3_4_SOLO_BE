package com.pleasybank.integration.kakao

import com.pleasybank.integration.kakao.dto.KakaoTokenResponse
import com.pleasybank.integration.kakao.dto.KakaoUserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

/**
 * 카카오 API 클라이언트
 * 카카오 인증 및 사용자 정보 조회 API 호출을 담당합니다.
 */
@Component
class KakaoApiClient(
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(KakaoApiClient::class.java)
    
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}")
    private lateinit var clientId: String
    
    @Value("\${spring.security.oauth2.client.registration.kakao.client-secret}")
    private lateinit var clientSecret: String
    
    /**
     * 인증 코드로 카카오 액세스 토큰 요청
     */
    fun getAccessToken(code: String, redirectUri: String): KakaoTokenResponse {
        logger.info("카카오 토큰 요청 시작 - 인증코드: ${code.take(10)}..., 리다이렉트 URI: $redirectUri")
        
        try {
            // 카카오 API 요청을 위한 폼 파라미터 구성
            val formParams = LinkedMultiValueMap<String, String>()
            formParams.add("grant_type", "authorization_code")
            formParams.add("client_id", clientId)
            formParams.add("client_secret", clientSecret)
            formParams.add("redirect_uri", redirectUri)
            formParams.add("code", code)
            
            // HTTP 헤더 설정
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            headers.set("User-Agent", "PleasyBank/1.0")
            
            val request = HttpEntity(formParams, headers)
            
            // 요청 실행
            val response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", 
                request, 
                String::class.java
            )
            
            // 응답 처리
            if (response.statusCode.is2xxSuccessful) {
                val mapper = com.fasterxml.jackson.databind.ObjectMapper()
                val tokenResponse = mapper.readValue(response.body, KakaoTokenResponse::class.java)
                logger.info("카카오 토큰 요청 성공: access_token=${tokenResponse.access_token.take(10)}...")
                return tokenResponse
            } else {
                logger.error("카카오 토큰 요청 실패: ${response.statusCode}, 응답: ${response.body}")
                throw KakaoApiException("카카오 토큰 요청 실패: ${response.statusCode}")
            }
        } catch (ex: HttpClientErrorException) {
            val responseBody = ex.responseBodyAsString
            logger.error("카카오 토큰 요청 HTTP 오류: ${ex.statusCode}, 응답: $responseBody", ex)
            throw KakaoApiException("카카오 토큰 요청 오류: ${ex.statusCode}", ex)
        } catch (ex: Exception) {
            logger.error("카카오 토큰 요청 중 예외 발생", ex)
            throw KakaoApiException("카카오 토큰 요청 중 오류 발생: ${ex.message}", ex)
        }
    }
    
    /**
     * 액세스 토큰으로 카카오 사용자 정보 요청
     */
    fun getUserInfo(accessToken: String): KakaoUserInfoResponse {
        logger.info("카카오 사용자 정보 요청 시작")
        
        try {
            val headers = HttpHeaders()
            headers.set("Authorization", "Bearer $accessToken")
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            
            val request = HttpEntity<String>(headers)
            
            val response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v2/user/me", 
                request, 
                KakaoUserInfoResponse::class.java
            )
            
            if (response.statusCode.is2xxSuccessful && response.body != null) {
                logger.info("카카오 사용자 정보 요청 성공: id=${response.body?.id}")
                return response.body!!
            } else {
                logger.error("카카오 사용자 정보 요청 실패: ${response.statusCode}")
                throw KakaoApiException("카카오 사용자 정보 요청 실패: ${response.statusCode}")
            }
        } catch (ex: HttpClientErrorException) {
            logger.error("카카오 사용자 정보 요청 HTTP 오류: ${ex.statusCode}", ex)
            throw KakaoApiException("카카오 사용자 정보 요청 오류: ${ex.statusCode}", ex)
        } catch (ex: Exception) {
            logger.error("카카오 사용자 정보 요청 중 예외 발생", ex)
            throw KakaoApiException("카카오 사용자 정보 요청 중 오류 발생: ${ex.message}", ex)
        }
    }
}

/**
 * 카카오 API 예외
 */
class KakaoApiException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
} 