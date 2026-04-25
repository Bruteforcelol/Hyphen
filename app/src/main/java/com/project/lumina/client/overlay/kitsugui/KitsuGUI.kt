package com.project.lumina.client.overlay.kitsugui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.R
import com.project.lumina.client.chat.ChatCategoryContent
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.overlay.manager.KitsuSettingsOverlay
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.component.ConfigCategoryContent
import com.project.lumina.client.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KitsuGUI : OverlayWindow() {

    companion object {
        const val FILE_PICKER_REQUEST_CODE = 1001
    }

    private val hyphenFont = FontFamily(Font(R.font.fredoka_light))

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            if (Build.VERSION.SDK_INT >= 31) blurBehindRadius = 24
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.72f
            windowAnimations = 0
            width  = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams get() = _layoutParams

    // null  = tab overview screen
    // non-null = inside that category
    private var selectedCategory by mutableStateOf<CheatCategory?>(null)
    private var selectedModule   by mutableStateOf<Element?>(null)

    // ─────────────────────────────────────────────────────────────────────────
    //  Root content
    // ─────────────────────────────────────────────────────────────────────────
    @Composable
    override fun Content() {
        var shouldAnimate by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) { delay(40); shouldAnimate = true }

        val translateY by animateFloatAsState(
            targetValue = if (shouldAnimate) 0f else 80f,
            animationSpec = tween(380, easing = FastOutSlowInEasing),
            label = "slide"
        )
        val alpha by animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = tween(320, easing = LinearOutSlowInEasing),
            label = "fade"
        )

        val dismissWithAnimation: () -> Unit = remember {
            {
                shouldAnimate = false
                scope.launch {
                    delay(200)
                    selectedCategory = null
                    selectedModule   = null
                    OverlayManager.dismissOverlayWindow(this@KitsuGUI)
                }
                Unit
            }
        }

        // Full-screen dim backdrop — tap outside to close (only when on tab overview)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x780C0D10), Color(0xA00C0D10))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (selectedModule == null && selectedCategory == null) {
                        dismissWithAnimation()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Main card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KitsuSurface),
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .heightIn(min = 460.dp, max = 520.dp)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .graphicsLayer { translationY = translateY; this.alpha = alpha }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks */ },
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(dismissWithAnimation)
                    HorizontalDivider(color = KitsuSurfaceVariant.copy(alpha = 0.5f))
                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            if (targetState != null) {
                                // Entering a category
                                (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 5 })
                                    .togetherWith(fadeOut(tween(160)) + slideOutHorizontally(tween(200)) { -it / 5 })
                            } else {
                                // Returning to tab overview
                                (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { -it / 5 })
                                    .togetherWith(fadeOut(tween(160)) + slideOutHorizontally(tween(200)) { it / 5 })
                            }
                        },
                        label = "screenTransition"
                    ) { category ->
                        if (category == null) {
                            TabOverview()
                        } else {
                            CategoryContent(category)
                        }
                    }
                }
            }

            // Module settings sheet
            selectedModule?.let { module ->
                KitsuSettingsOverlay(
                    element = module,
                    onDismiss = { selectedModule = null }
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Top bar — logo + action buttons (+ optional back arrow)
    // ─────────────────────────────────────────────────────────────────────────
    @Composable
    private fun TopBar(dismissWithAnimation: () -> Unit) {
        val context = LocalContext.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Back arrow (only visible when inside a category)
                AnimatedVisibility(
                    visible = selectedCategory != null,
                    enter = fadeIn(tween(180)) + expandHorizontally(tween(200)),
                    exit  = fadeOut(tween(150)) + shrinkHorizontally(tween(180))
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(KitsuSurfaceVariant)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedCategory = null; selectedModule = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back_black_24dp),
                            contentDescription = "Back",
                            tint = KitsuOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                }

                // Hyphen logo box
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(KitsuPrimary, KitsuSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // "—" glyph as the hyphen mark
                    Text(
                        text = "—",
                        color = Color(0xFF0C0D10),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Title — changes based on state
                val titleText = selectedCategory?.let { getCategoryTitle(it) } ?: "HYPHEN"
                Text(
                    text = titleText,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontFamily = hyphenFont,
                        fontWeight = FontWeight.Bold,
                        color = KitsuOnSurface,
                        letterSpacing = if (selectedCategory == null) 3.sp else 0.sp
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(iconRes = R.drawable.discord_24) {
                    openUrl("https://discord.gg/hyphen", context)
                }
                ActionButton(iconRes = R.drawable.browser_24) {
                    openUrl("https://hyphen-client.netlify.app/", context)
                }
                ActionButton(iconRes = R.drawable.cross_circle_24, isClose = true) {
                    dismissWithAnimation()
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tab overview — 2-column card grid
    // ─────────────────────────────────────────────────────────────────────────
    @Composable
    private fun TabOverview() {
        val categories = CheatCategory.entries

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(categories) { category ->
                CategoryTabCard(
                    category = category,
                    onClick  = { selectedCategory = category }
                )
            }
        }
    }

    @Composable
    private fun CategoryTabCard(category: CheatCategory, onClick: () -> Unit) {
        var pressed by remember { mutableStateOf(false) }

        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.96f else 1f,
            animationSpec = tween(90),
            label = "cardScale"
        )

        LaunchedEffect(pressed) { if (pressed) { delay(120); pressed = false } }

        // Per-category gradient colors
        val (grad1, grad2) = categoryGradient(category)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = KitsuSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { pressed = true; onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Image / preview area (top ~55%) ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.linearGradient(
                                listOf(grad1.copy(alpha = 0.25f), grad2.copy(alpha = 0.12f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Large icon centered
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(grad1.copy(alpha = 0.35f), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = category.iconResId),
                            contentDescription = null,
                            tint = grad1,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    // Subtle corner glow
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(60.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(grad2.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                    )
                }

                // ── Bottom label row ──────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KitsuSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = getCategoryTitle(category),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = hyphenFont,
                            fontWeight = FontWeight.Bold,
                            color = KitsuOnSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Arrow indicator
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward_black_24dp),
                        contentDescription = null,
                        tint = grad1.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Category content (after tapping a tab)
    // ─────────────────────────────────────────────────────────────────────────
    @Composable
    private fun CategoryContent(category: CheatCategory) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KitsuSurface)
        ) {
            when (category) {
                CheatCategory.Config -> ConfigCategoryContent()
                CheatCategory.Home   -> HomeCategoryUi()
                CheatCategory.Chat   -> ChatCategoryContent()
                else -> ModuleContent(
                    selectedCheatCategory = category,
                    onOpenSettings = { module -> selectedModule = module }
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared action button component
    // ─────────────────────────────────────────────────────────────────────────
    @Composable
    private fun ActionButton(
        iconRes: Int,
        isClose: Boolean = false,
        onClick: () -> Unit
    ) {
        var pressed by remember { mutableStateOf(false) }
        val bg by animateColorAsState(
            targetValue = when {
                isClose && pressed -> Color(0xFFE81123)
                isClose            -> Color(0xFFE81123).copy(alpha = 0.85f)
                pressed            -> KitsuSurfaceVariant
                else               -> KitsuSurfaceVariant.copy(alpha = 0.55f)
            },
            animationSpec = tween(130),
            label = "btnBg"
        )
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.93f else 1f,
            animationSpec = tween(90),
            label = "btnScale"
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(bg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { pressed = true; onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (isClose) Color.White else KitsuOnSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
        LaunchedEffect(pressed) { if (pressed) { delay(100); pressed = false } }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun openUrl(url: String, context: android.content.Context) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun getCategoryTitle(category: CheatCategory): String = when (category) {
        CheatCategory.Combat -> "Combat"
        CheatCategory.Motion -> "Movement"
        CheatCategory.World  -> "World"
        CheatCategory.Visual -> "Render"
        CheatCategory.Misc   -> "Misc"
        CheatCategory.Config -> "Config"
        CheatCategory.Home   -> "Home"
        CheatCategory.Chat   -> "Chat"
        else                 -> "Modules"
    }

    /** Gradient pair for each category tab card */
    private fun categoryGradient(category: CheatCategory): Pair<Color, Color> = when (category) {
        CheatCategory.Combat -> Color(0xFFFF4466) to Color(0xFFFF8800)
        CheatCategory.Motion -> Color(0xFF00C8FF) to Color(0xFF0080FF)
        CheatCategory.World  -> Color(0xFF00E888) to Color(0xFF00C8FF)
        CheatCategory.Visual -> Color(0xFFB060FF) to Color(0xFF5E5CE6)
        CheatCategory.Misc   -> Color(0xFFFFCC00) to Color(0xFFFF8800)
        CheatCategory.Config -> Color(0xFF8B8FA8) to Color(0xFF555A70)
        CheatCategory.Home   -> Color(0xFF00C8FF) to Color(0xFF5E5CE6)
        CheatCategory.Chat   -> Color(0xFF00E888) to Color(0xFF00C8FF)
        else                 -> Color(0xFF00C8FF) to Color(0xFF5E5CE6)
    }
}
