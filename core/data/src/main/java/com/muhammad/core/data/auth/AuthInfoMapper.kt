package com.muhammad.core.data.auth

import com.muhammad.core.domain.AuthInfo

fun AuthInfo.toAuthInfoSerializable() : AuthInfoSerializable{
    return AuthInfoSerializable(
        accessToken = accessToken, refreshToken = refreshToken, userId = userId
    )
}
fun AuthInfoSerializable.toAuthInfo() : AuthInfo{
    return AuthInfo(refreshToken = refreshToken, accessToken = accessToken, userId = userId)
}