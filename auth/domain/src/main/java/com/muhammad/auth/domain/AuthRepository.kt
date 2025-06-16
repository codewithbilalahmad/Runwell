package com.muhammad.auth.domain

import com.muhammad.core.domain.util.DataError
import com.muhammad.core.domain.util.EmptyResult

interface AuthRepository {
    suspend fun login(email : String,password : String) : EmptyResult<DataError.Network>
    suspend fun register(email : String,password : String) : EmptyResult<DataError.Network>
}