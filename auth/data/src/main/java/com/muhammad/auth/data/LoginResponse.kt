package com.muhammad.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken : String,
    val refreshToken : String,
    val accessTokenExpirationTimeStamp : Long,
    val userId : String
)
