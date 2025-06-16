package com.muhammad.auth.domain

interface PatternValidator {
    fun matches(value : String) : Boolean
}