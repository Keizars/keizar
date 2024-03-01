package org.keizar.android.client

import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.account.UsernameValidityResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The service for user-related operations.
 */
interface UserService {
    /**
     * Attempts to register a new user.
     *
     * The function does not throw, unless there is a network failure.
     */
    @POST("users/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    /**
     * Attempts to register a new user.
     *
     * The function does not throw, unless there is a network failure.
     */
    @POST("users/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    /**
     * Check if a username is available or already taken.
     *
     * The function does not throw, unless there is a network failure.
     */
    @POST("users/available")
    suspend fun isAvailable(@Query("username") username: String): UsernameValidityResponse

    /**
     * Get the self user's information.
     *
     * The function may throw if the user is not authenticated.
     */
    @GET("users/me")
    suspend fun self(): User

    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): User
}