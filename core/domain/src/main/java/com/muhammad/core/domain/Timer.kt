package com.muhammad.core.domain

import kotlin.time.Duration.Companion.milliseconds

object Timer{
    fun timeAndEmit() : Flow<Duration>{
        return flow{
            var lastEmitTime = System.currentTimeMillis()
            while(true){
                delay(200L)
                val currentTime= System.currentTimeMillis()
                val elapsedTime = currentTime - lastEmitTime
                emit(elapsedTime.milliseconds)
                lastEmitTime = currentTime
            }
        }
    }
}