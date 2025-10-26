package com.bank.auth.service

import com.bank.auth.model.User
import com.bank.auth.repository.UserRepository
import com.bank.auth.util.JwtUtil
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    fun register(username: String, password: String, email: String?): User {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent) {
            throw IllegalArgumentException("Username already exists")
        }
        
        val hashed = passwordEncoder.encode(password)
        val user = User(username = username, passwordHash = hashed, email = email)
        return userRepository.save(user)
    }

    fun authenticate(username: String, password: String): Boolean {
        val user = userRepository.findByUsername(username)
        return if (user.isPresent) {
            passwordEncoder.matches(password, user.get().passwordHash)
        } else {
            false
        }
    }

    fun login(username: String, password: String): String? {
        return if (authenticate(username, password)) {
            JwtUtil.generateToken(username)
        } else {
            null
        }
    }

    fun validate(token: String): String? {
        return try {
            JwtUtil.validateToken(token)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username).orElse(null)
    }

    fun verifyToken(token: String): Boolean {
        val username = JwtUtil.validateToken(token)
        return username != null && !JwtUtil.isTokenExpired(token)
    }

    fun deleteUser(username: String): Boolean {
        val user = userRepository.findByUsername(username)
        return if (user.isPresent) {
            userRepository.delete(user.get())
            true
        } else {
            false
        }
    }

    fun deleteAllUsers() {
        userRepository.deleteAll()
    }
}
