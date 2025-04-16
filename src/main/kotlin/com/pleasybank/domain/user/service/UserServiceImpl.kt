package com.pleasybank.domain.user.service

import com.pleasybank.core.exception.InvalidCredentialsException
import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.core.service.CacheService
import com.pleasybank.core.service.FileStorageService
import com.pleasybank.domain.user.dto.UserDto
import com.pleasybank.domain.user.entity.User
import com.pleasybank.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val fileStorageService: FileStorageService,
    private val cacheService: CacheService
) : UserService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val USER_CACHE_KEY = "user"
        private const val USER_CACHE_TTL = 3600L  // 1시간
        private const val USER_STATUS_ACTIVE = "ACTIVE"
        private const val USER_STATUS_INACTIVE = "INACTIVE"
        private const val PROFILE_IMAGE_PATH = "profiles"
    }

    @Transactional(readOnly = true)
    override fun getUserById(id: Long): UserDto.Response {
        // 캐시에서 조회
        val cacheKey = buildUserCacheKey(id)
        val cachedUser = cacheService.getValue(cacheKey) as? UserDto.Response
        
        if (cachedUser != null) {
            logger.debug("캐시에서 사용자 정보 조회 성공: ID={}", id)
            return cachedUser
        }
        
        // DB에서 조회
        val user = findUserById(id)
        val response = UserDto.Response.fromEntity(user)
        
        // 캐시에 저장
        cacheUserData(id, response)
        logger.debug("사용자 정보 조회 및 캐시 저장: ID={}", id)
        
        return response
    }

    @Transactional
    override fun updateUser(id: Long, request: UserDto.UpdateRequest): UserDto.Response {
        val user = findUserById(id)
        
        // 필드 업데이트
        request.name?.let { user.name = it }
        request.phoneNumber?.let { user.phoneNumber = it }
        request.profileImageUrl?.let { user.profileImageUrl = it }
        request.status?.let { user.status = it }
        user.updatedAt = LocalDateTime.now()
        
        logger.info("사용자 정보 업데이트: ID={}", id)
        
        // 저장 및 캐시 처리
        return saveUserAndInvalidateCache(user, id)
    }

    @Transactional
    override fun updatePassword(id: Long, currentPassword: String, newPassword: String): Boolean {
        val user = findUserById(id)
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            logger.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: ID={}", id)
            return false
        }
        
        // 비밀번호 업데이트
        user.password = passwordEncoder.encode(newPassword)
        user.updatedAt = LocalDateTime.now()
        
        userRepository.save(user)
        invalidateUserCache(id)
        
        logger.info("사용자 비밀번호 변경 성공: ID={}", id)
        return true
    }

    @Transactional
    override fun deleteUser(id: Long) {
        val user = findUserById(id)
        
        // 소프트 삭제 (상태만 변경)
        user.status = USER_STATUS_INACTIVE
        user.updatedAt = LocalDateTime.now()
        
        userRepository.save(user)
        invalidateUserCache(id)
        
        logger.info("사용자 비활성화(소프트 삭제) 처리 완료: ID={}", id)
    }

    @Transactional
    override fun updateProfileImage(id: Long, file: MultipartFile): UserDto.Response {
        if (file.isEmpty) {
            logger.warn("프로필 이미지 업데이트 실패 - 빈 파일: ID={}", id)
            throw IllegalArgumentException("업로드할 이미지가 없습니다")
        }
        
        val user = findUserById(id)
        
        // 기존 이미지가 있으면 삭제
        tryDeleteExistingProfileImage(user.profileImageUrl)
        
        // 새 이미지 업로드
        val imageFilename = fileStorageService.uploadFile(file, PROFILE_IMAGE_PATH)
        
        // 사용자 정보 업데이트
        user.profileImageUrl = imageFilename
        user.updatedAt = LocalDateTime.now()
        
        logger.info("사용자 프로필 이미지 업데이트 완료: ID={}, 이미지={}", id, imageFilename)
        
        // 저장 및 캐시 처리
        return saveUserAndInvalidateCache(user, id)
    }

    @Transactional(readOnly = true)
    override fun getUserByEmail(email: String): User? {
        val user = userRepository.findByEmail(email)
        logger.debug("이메일로 사용자 조회: email={}, 결과={}", email, user != null)
        return user
    }
    
    /**
     * ID로 사용자 조회
     */
    private fun findUserById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow { 
                logger.warn("사용자를 찾을 수 없음: ID={}", id)
                ResourceNotFoundException("ID가 ${id}인 사용자를 찾을 수 없습니다") 
            }
    }
    
    /**
     * 사용자 캐시 키 생성
     */
    private fun buildUserCacheKey(id: Long): String = "${USER_CACHE_KEY}:$id"
    
    /**
     * 사용자 데이터를 캐시에 저장
     */
    private fun cacheUserData(id: Long, userData: UserDto.Response) {
        cacheService.setValue(buildUserCacheKey(id), userData, USER_CACHE_TTL)
    }
    
    /**
     * 사용자 캐시 무효화
     */
    private fun invalidateUserCache(id: Long) {
        cacheService.delete(buildUserCacheKey(id))
        logger.debug("사용자 캐시 무효화: ID={}", id)
    }
    
    /**
     * 사용자 정보 저장 및 캐시 무효화 후 응답 생성
     */
    private fun saveUserAndInvalidateCache(user: User, id: Long): UserDto.Response {
        val savedUser = userRepository.save(user)
        invalidateUserCache(id)
        return UserDto.Response.fromEntity(savedUser)
    }
    
    /**
     * 기존 프로필 이미지 삭제 시도 (실패 시 예외를 던지지 않음)
     */
    private fun tryDeleteExistingProfileImage(profileImageUrl: String?) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return
        }
        
        try {
            fileStorageService.deleteFile(profileImageUrl, PROFILE_IMAGE_PATH)
            logger.debug("기존 프로필 이미지 삭제 성공: {}", profileImageUrl)
        } catch (e: Exception) {
            // 기존 이미지 삭제 실패는 무시하고 계속 진행
            logger.warn("기존 프로필 이미지 삭제 실패: {}, 원인: {}", profileImageUrl, e.message)
        }
    }
} 