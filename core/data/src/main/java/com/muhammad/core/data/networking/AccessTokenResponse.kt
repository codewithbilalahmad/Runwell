package com.muhammad.core.data.networking

import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(
    val accessToken : String,
    val expirationTimeStamp : Long
)