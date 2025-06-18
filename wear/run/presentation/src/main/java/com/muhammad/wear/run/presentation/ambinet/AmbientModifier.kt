package com.muhammad.wear.run.presentation.ambinet

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import kotlin.random.Random

fun Modifier.ambientMode(isAmbientMode: Boolean, burnInProtectionRequired: Boolean) = composed {
    val transitionX by rememberBurnInTransition(
        isAmbientMode = isAmbientMode,
        burnInProtectionRequired = burnInProtectionRequired
    )
    val transitionY by rememberBurnInTransition(
        isAmbientMode = isAmbientMode,
        burnInProtectionRequired = burnInProtectionRequired
    )
    this.graphicsLayer {
        this.translationX = transitionX
        this.translationY = transitionY
    }.ambientGray(isAmbientMode)
}
internal fun Modifier.ambientGray(isAmbientMode: Boolean) : Modifier{
    return if(isAmbientMode){
        val grayScale = Paint().apply {
            colorFilter = ColorFilter.colorMatrix(
                colorMatrix = ColorMatrix().apply {
                    setToSaturation(0f)
                }
            )
        }
        drawWithContent {
            drawIntoCanvas {canvas ->
                canvas.withSaveLayer(size.toRect(), grayScale){
                    drawContent()
                }
            }
        }
    } else {
        this
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun rememberBurnInTransition(
    isAmbientMode: Boolean, burnInProtectionRequired: Boolean,
): State<Float> {
    val transition = remember { Animatable(0f) }
    LaunchedEffect(isAmbientMode, burnInProtectionRequired) {
        if (isAmbientMode && burnInProtectionRequired) {
            transition.animateTo(
                targetValue = Random.nextInt(-10, 10).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 60000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            transition.snapTo(0f)
        }
    }
    return transition.asState()
}
