package moe.rukamori.hikki.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.MorphShape
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val motion = MaterialTheme.motionScheme
    val currentOnFinished by rememberUpdatedState(onFinished)
    val shapeProgress = remember { Animatable(0f) }
    val textProgress = remember { Animatable(0f) }
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }

    LaunchedEffect(Unit) {
        launch { textProgress.animateTo(1f, motion.slowEffectsSpec()) }
        shapeProgress.animateTo(1f, motion.slowSpatialSpec())
        currentOnFinished()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val progress = shapeProgress.value
        Box(
            modifier =
                Modifier
                    .size(132.dp)
                    .graphicsLayer {
                        val scale = 0.7f + 0.3f * progress
                        scaleX = scale
                        scaleY = scale
                        rotationZ = -60f * (1f - progress)
                    }
                    .clip(MorphShape(morph, progress.coerceIn(0f, 1f)))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(132.dp),
            )
        }
        Spacer(Modifier.height(MdSpacing.md))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.graphicsLayer {
                    val value = textProgress.value
                    alpha = value
                    translationY = (1f - value) * 24.dp.toPx()
                },
        )
    }
}
