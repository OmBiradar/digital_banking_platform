package com.bank.auth.controller

import com.bank.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RegisterRequest(val username: String, val password: String, val email: String?)
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val token: String)
data class ValidateRequest(val token: String)
data class ValidateResponse(val valid: Boolean, val username: String?)

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: UserService) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> {
        return try {
            authService.register(req.username, req.password, req.email)
            ResponseEntity.ok(mapOf("message" to "User registered successfully"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Registration failed"))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
        val token = authService.login(req.username, req.password)
        return if (token != null) {
            ResponseEntity.ok(mapOf("token" to token))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid credentials"))
        }
    }

    @PostMapping("/validate")
    fun validate(@RequestBody req: ValidateRequest): ResponseEntity<ValidateResponse> {
        val username = authService.validate(req.token)
        val isValid = authService.verifyToken(req.token)
        return ResponseEntity.ok(ValidateResponse(isValid, username))
    }

    @GetMapping("/verify")
    fun verify(@RequestHeader("Authorization") authHeader: String?): ResponseEntity<Any> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Missing or invalid token"))
        }

        val token = authHeader.substring(7)
        val username = authService.validate(token)
        
        return if (username != null && authService.verifyToken(token)) {
            ResponseEntity.ok(mapOf(
                "valid" to true,
                "username" to username
            ))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid or expired token"))
        }
    }

    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") authHeader: String?): ResponseEntity<Any> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Missing or invalid token"))
        }

        val token = authHeader.substring(7)
        val username = authService.validate(token)
        
        return if (username != null) {
            val user = authService.getUserByUsername(username)
            if (user != null) {
                ResponseEntity.ok(mapOf(
                    "username" to user.username,
                    "email" to user.email
                ))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "User not found"))
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid token"))
        }
    }

    @DeleteMapping("/user/{username}")
    fun deleteUser(@PathVariable username: String): ResponseEntity<Any> {
        return if (authService.deleteUser(username)) {
            ResponseEntity.ok(mapOf("message" to "User deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "User not found"))
        }
    }
}


