package com.muhammad.auth.data

import com.muhammad.auth.domain.AuthRepository
import com.muhammad.core.data.networking.post
import com.muhammad.core.domain.AuthInfo
import com.muhammad.core.domain.SessionStorage
import com.muhammad.core.domain.util.DataError
import com.muhammad.core.domain.util.EmptyResult
import com.muhammad.core.domain.util.Result
import com.muhammad.core.domain.util.asEmptyDataResult
import io.ktor.client.HttpClient

class AuthRespositoryImp(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): EmptyResult<DataError.Network> {
        val result = httpClient.post<LoginRequest, LoginResponse>(
            route = "/login", body = LoginRequest(
                email = email,
                password = password
            )
        )
        if (result is Result.Success) {
            sessionStorage.set(
                AuthInfo(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                    userId = result.data.userId
                )
            )
        }
        return result.asEmptyDataResult()
    }

    override suspend fun register(
        email: String,
        password: String,
    ): EmptyResult<DataError.Network> {
        return httpClient.post<RegisterRequest, Unit>(
            route = "/register", body = RegisterRequest(
                email = email, password = password
            )
        )
    }
}