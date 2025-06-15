package com.muhammad.core.connectivity.data.di

import com.muhammad.core.connectivity.data.WearNodeDiscovery
import com.muhammad.core.connectivity.data.messaging.WearMessagingClient
import com.muhammad.core.connectivity.domain.NodeDiscovery
import com.muhammad.core.connectivity.domain.messaging.MessagingClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.*

val connectivityModule = module {
    singleOf(::WearMessagingClient).bind<MessagingClient>()
    singleOf(::WearNodeDiscovery).bind<NodeDiscovery>()
}