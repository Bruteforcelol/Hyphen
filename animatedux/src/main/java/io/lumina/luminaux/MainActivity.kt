package io.lumina.luminaux

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import io.lumina.luminaux.components.FlickeringStartButton
import io.lumina.luminaux.components.GlassmorphicCard
import io.lumina.luminaux.components.VideoBackground
import io.lumina.luminaux.ui.theme.HyphenUXTheme
import io.lumina.luminaux.ui.theme.HyphenCyan
import io.lumina.luminaux.ui.theme.HyphenIndigo
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            HyphenUXTheme {
                LauncherUI(hideSystemBars = { hideSystemBars() })
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @Composable
    fun LauncherUI(hideSystemBars: () -> Unit = {}) {
        var uiVisible by remember { mutableStateOf(false) }
        var interactionTime by remember { mutableStateOf(0L) }

        LaunchedEffect(interactionTime) { delay(3000); hideSystemBars() }
        LaunchedEffect(Unit) {
            delay(180)
            uiVisible = true
            interactionTime = System.currentTimeMillis()
            hideSystemBars()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { interactionTime = System.currentTimeMillis() }
                }
        ) {
            // ── Video background ──────────────────────────────────────────
            VideoBackground()

            // ── Deep overlay gradient ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xCC0C0D10),
                                Color(0x880C0D10),
                                Color(0xBB0C0D10)
                            )
                        )
                    )
            )

            // ── Cyan glow blob (top-left) ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .offset(x = (-60).dp, y = (-80).dp)
                    .blur(120.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x3300C8FF), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )

            // ── Indigo glow blob (bottom-right) ───────────────────────────
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 80.dp)
                    .blur(100.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x225E5CE6), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )

            // ── Right glass panel ─────────────────────────────────────────
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(tween(600, easing = FastOutSlowInEasing)) +
                        expandHorizontally(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 48.dp, horizontal = 58.dp)
            ) {
                GlassmorphicCard(
                    title = "Hyphen",
                    modifier = Modifier
                        .fillMaxHeight(0.90f)
                        .width(200.dp)
                        .animateEnterExit(
                            enter = fadeIn(tween(500, delayMillis = 100)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 5 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                        )
                )
            }

            // ── Left glass panel ──────────────────────────────────────────
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(tween(600, easing = FastOutSlowInEasing)) +
                        expandHorizontally(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 48.dp, horizontal = 58.dp)
            ) {
                GlassmorphicCard(
                    title = "Client",
                    modifier = Modifier
                        .fillMaxHeight(0.90f)
                        .width(200.dp)
                        .animateEnterExit(
                            enter = fadeIn(tween(500, delayMillis = 100)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 5 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                        )
                )
            }

            // ── Center hero text ──────────────────────────────────────────
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(tween(800, delayMillis = 200)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Wordmark
                    Text(
                        text = "HYPHEN",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 10.sp,
                        fontFamily = FontFamily.Default,
                        modifier = Modifier.alpha(0.95f)
                    )
                    Spacer(Modifier.height(4.dp))
                    // Cyan accent underline
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, HyphenCyan, Color.Transparent)
                                )
                            )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "CLIENT",
                        color = HyphenCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 8.sp
                    )
                }
            }

            // ── Bottom bar ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(tween(600, delayMillis = 300)) +
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "© Hyphen Client 2026",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                    FlickeringStartButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = {}
                    )
                    Text(
                        text = "Precision. Stealth. Power.",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}
