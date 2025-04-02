package com.pleasybank.core.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 보안 관련 유틸리티 클래스
 */
object SecurityUtils {
    private const val HASH_ALGORITHM = "SHA-256"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256
    
    /**
     * 문자열 해시 생성
     */
    fun hashString(input: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(input.toByteArray())
        return bytesToHex(hashBytes)
    }
    
    /**
     * 비밀번호 해싱 (salt 자동 생성)
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashPasswordWithSalt(password, salt)
        
        // salt와 해시를 결합하여 저장 (salt:hash 형식)
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash)
    }
    
    /**
     * 비밀번호 검증
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        // 저장된 해시에서 salt와 해시 추출
        val parts = storedHash.split(":")
        if (parts.size != 2) {
            return false
        }
        
        val salt = Base64.getDecoder().decode(parts[0])
        val hash = Base64.getDecoder().decode(parts[1])
        
        // 입력된 비밀번호를 동일한 salt로 해싱
        val calculatedHash = hashPasswordWithSalt(password, salt)
        
        // 계산된 해시와 저장된 해시 비교
        return calculatedHash.contentEquals(hash)
    }
    
    /**
     * 랜덤 salt 생성
     */
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }
    
    /**
     * 소금을 사용한 비밀번호 해싱
     */
    private fun hashPasswordWithSalt(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
    
    /**
     * 문자열 암호화
     */
    fun encrypt(plainText: String, secretKey: String): String {
        val key = generateKey(secretKey)
        val iv = generateIv()
        
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        
        // IV와 암호화된 텍스트를 결합하여 반환
        val combined = ByteArray(iv.iv.size + encryptedBytes.size)
        System.arraycopy(iv.iv, 0, combined, 0, iv.iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.iv.size, encryptedBytes.size)
        
        return Base64.getEncoder().encodeToString(combined)
    }
    
    /**
     * 문자열 복호화
     */
    fun decrypt(encryptedText: String, secretKey: String): String {
        val combined = Base64.getDecoder().decode(encryptedText)
        
        // IV 추출
        val iv = ByteArray(16)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        
        // 암호화된 텍스트 추출
        val encryptedBytes = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)
        
        // 복호화
        val key = generateKey(secretKey)
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes)
    }
    
    /**
     * 비밀키 생성
     */
    private fun generateKey(secretKey: String): SecretKeySpec {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val keyBytes = digest.digest(secretKey.toByteArray())
        return SecretKeySpec(keyBytes, "AES")
    }
    
    /**
     * IV(Initialization Vector) 생성
     */
    private fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }
    
    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_ARRAY[v ushr 4]
            hexChars[i * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
    
    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
} 