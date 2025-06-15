package com.muhammad.core.data.networking

import com.muhammad.core.domain.AuthInfo
import com.muhammad.core.domain.SessionStorage
import com.muhammad.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(private val sessionStorage : SessionStorage){
    fun build() : HttpClient{
        return HttpClient(CIO){
            install(ContentNegotiation){
                json(Json{
                    ignoreUnknownKeys = true
                })
            }
            install(Logging){
                logger = object : Logger{
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                header("api_key","" )
            }
            install(Auth){
                bearer {
                    loadTokens {
                        val info = sessionStorage.get()
                        BearerTokens(accessToken = info?.accessToken ?:"", refreshToken = info?.refreshToken ?: "")
                    }
                    refreshTokens {
                        val info = sessionStorage.get()
                        val response=  client.post<AccessTokenRequest, AccessTokenResponse>(
                            route = "/accessToken", body = AccessTokenRequest(
                                refreshToken = info?.refreshToken ?: "",
                                userId = info?.userId ?: ""
                            )
                        )
                        if(response is Result.Success){
                            val newAuthInfo = AuthInfo(
                                accessToken = response.data.accessToken,
                                refreshToken = info?.refreshToken ?: "",
                                userId = info?.userId ?: ""
                            )
                            sessionStorage.set(newAuthInfo)
                            BearerTokens(accessToken = newAuthInfo.accessToken, refreshToken = newAuthInfo.refreshToken)
                        } else{
                            BearerTokens(accessToken = "", refreshToken = "")
                        }
                    }
                }
            }
        }
    }
}