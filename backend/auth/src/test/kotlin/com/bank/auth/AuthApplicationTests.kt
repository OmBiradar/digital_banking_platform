package com.bank.auth

import com.bank.auth.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val TEST_USERNAME = "testuser"
        const val TEST_PASSWORD = "testpass123"
        const val TEST_EMAIL = "test@example.com"
        
        const val TEST_USERNAME_2 = "demouser"
        const val TEST_PASSWORD_2 = "demopass456"
        const val TEST_EMAIL_2 = "demo@example.com"
    }

    @BeforeEach
    fun setup() {
        // Clean up test users before each test
        userService.deleteUser(TEST_USERNAME)
        userService.deleteUser(TEST_USERNAME_2)
    }

    @AfterEach
    fun cleanup() {
        // Clean up test users after each test
        userService.deleteUser(TEST_USERNAME)
        userService.deleteUser(TEST_USERNAME_2)
    }

    @Test
    fun contextLoads() {
        // Verify Spring context loads successfully
        assertNotNull(mockMvc)
        assertNotNull(userService)
    }

    @Test
    fun `should register a new user successfully`() {
        val registerRequest = mapOf(
            "username" to TEST_USERNAME,
            "password" to TEST_PASSWORD,
            "email" to TEST_EMAIL
        )

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("User registered successfully"))
    }

    @Test
    fun `should return conflict when registering duplicate username`() {
        // First registration
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)

        // Attempt duplicate registration
        val registerRequest = mapOf(
            "username" to TEST_USERNAME,
            "password" to TEST_PASSWORD,
            "email" to TEST_EMAIL
        )

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Username already exists"))
    }

    @Test
    fun `should login successfully with valid credentials`() {
        // Register user first
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)

        val loginRequest = mapOf(
            "username" to TEST_USERNAME,
            "password" to TEST_PASSWORD
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `should fail login with invalid password`() {
        // Register user first
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)

        val loginRequest = mapOf(
            "username" to TEST_USERNAME,
            "password" to "wrongpassword"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid credentials"))
    }

    @Test
    fun `should fail login with non-existent username`() {
        val loginRequest = mapOf(
            "username" to "nonexistent",
            "password" to TEST_PASSWORD
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid credentials"))
    }

    @Test
    fun `should validate a valid token`() {
        // Register and login
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        val token = userService.login(TEST_USERNAME, TEST_PASSWORD)!!

        val validateRequest = mapOf("token" to token)

        mockMvc.perform(
            post("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.username").value(TEST_USERNAME))
    }

    @Test
    fun `should invalidate an invalid token`() {
        val validateRequest = mapOf("token" to "invalid.token.here")

        mockMvc.perform(
            post("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(false))
            .andExpect(jsonPath("$.username").isEmpty)
    }

    @Test
    fun `should verify token via Authorization header`() {
        // Register and login
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        val token = userService.login(TEST_USERNAME, TEST_PASSWORD)!!

        mockMvc.perform(
            get("/auth/verify")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.username").value(TEST_USERNAME))
    }

    @Test
    fun `should reject verify request without Authorization header`() {
        mockMvc.perform(get("/auth/verify"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Missing or invalid token"))
    }

    @Test
    fun `should get current user info with valid token`() {
        // Register and login
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        val token = userService.login(TEST_USERNAME, TEST_PASSWORD)!!

        mockMvc.perform(
            get("/auth/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(TEST_USERNAME))
            .andExpect(jsonPath("$.email").value(TEST_EMAIL))
    }

    @Test
    fun `should reject get current user without token`() {
        mockMvc.perform(get("/auth/me"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Missing or invalid token"))
    }

    @Test
    fun `should delete user successfully`() {
        // Register user first
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)

        mockMvc.perform(delete("/auth/user/$TEST_USERNAME"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("User deleted successfully"))

        // Verify user is deleted
        assertNull(userService.getUserByUsername(TEST_USERNAME))
    }

    @Test
    fun `should return 404 when deleting non-existent user`() {
        mockMvc.perform(delete("/auth/user/nonexistent"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("User not found"))
    }

    @Test
    fun `should support multiple users`() {
        // Register multiple users
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        userService.register(TEST_USERNAME_2, TEST_PASSWORD_2, TEST_EMAIL_2)

        // Login as first user
        val token1 = userService.login(TEST_USERNAME, TEST_PASSWORD)
        assertNotNull(token1)

        // Login as second user
        val token2 = userService.login(TEST_USERNAME_2, TEST_PASSWORD_2)
        assertNotNull(token2)

        // Tokens should be different
        assertNotEquals(token1, token2)

        // Both tokens should validate correctly
        assertEquals(TEST_USERNAME, userService.validate(token1!!))
        assertEquals(TEST_USERNAME_2, userService.validate(token2!!))
    }

    @Test
    fun `should handle complete user lifecycle`() {
        // 1. Register
        userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL)
        val user = userService.getUserByUsername(TEST_USERNAME)
        assertNotNull(user)
        assertEquals(TEST_USERNAME, user.username)

        // 2. Login
        val token = userService.login(TEST_USERNAME, TEST_PASSWORD)
        assertNotNull(token)

        // 3. Validate token
        assertEquals(TEST_USERNAME, userService.validate(token!!))
        assertTrue(userService.verifyToken(token))

        // 4. Delete user
        assertTrue(userService.deleteUser(TEST_USERNAME))

        // 5. Verify deletion
        assertNull(userService.getUserByUsername(TEST_USERNAME))
        assertFalse(userService.deleteUser(TEST_USERNAME)) // Already deleted
    }
}


