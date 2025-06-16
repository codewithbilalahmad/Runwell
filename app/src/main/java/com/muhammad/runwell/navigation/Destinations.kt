package com.muhammad.runwell.navigation

import kotlinx.serialization.Serializable

sealed interface Destinations{
    @Serializable
    data object Intro : Destinations
    @Serializable
    data object Auth : Destinations
    @Serializable
    data object Login : Destinations
    @Serializable
    data object Register : Destinations
    @Serializable
    data object Run : Destinations
    @Serializable
    data object RunOverview : Destinations
    @Serializable
    data object ActiveRun : Destinations
}