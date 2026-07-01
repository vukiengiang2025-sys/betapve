package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import kotlin.math.absoluteValue
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    DrawingPlayground(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingPlayground(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel = viewModel()
) {
    val context = LocalContext.current
    val strokes by viewModel.strokes.collectAsState()
    val stickers by viewModel.stickers.collectAsState()
    val selectedStickerId by viewModel.selectedStickerId.collectAsState()
    val brushColor by viewModel.brushColor.collectAsState()
    val brushWidth by viewModel.brushWidth.collectAsState()
    val isEraser by viewModel.isEraser.collectAsState()
    val isRainbow by viewModel.isRainbow.collectAsState()
    val selectedBrush by viewModel.selectedBrush.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val savedDrawings by viewModel.savedDrawings.collectAsState()
    val mascotMessage by viewModel.mascotMessage.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()

    // --- Parent Safe States ---
    val isAiFreeFormAllowed by viewModel.isAiFreeFormAllowed.collectAsState()
    val timeLimitMinutes by viewModel.timeLimitMinutes.collectAsState()
    val isTimeLimitReached by viewModel.isTimeLimitReached.collectAsState()
    val timeSpentSeconds by viewModel.timeSpentSeconds.collectAsState()
    val isCelebrating by viewModel.isCelebrating.collectAsState()
    val colorUsageCounts by viewModel.colorUsageCounts.collectAsState()
    val totalStickersApplied by viewModel.totalStickersApplied.collectAsState()

    // Dialog & UI toggle states
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showGalleryDrawer by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0: Màu sắc, 1: Cọ vẽ, 2: Hình dán, 3: Vẽ mẫu, 4: Vẽ AI

    // --- Parent Gate States ---
    var showParentGateDialog by remember { mutableStateOf(false) }
    var parentGateAnswerInput by remember { mutableStateOf("") }
    var parentGateQuestionNum1 by remember { mutableStateOf(5) }
    var parentGateQuestionNum2 by remember { mutableStateOf(7) }
    var showParentSettingsDialog by remember { mutableStateOf(false) }
    var isParentGateError by remember { mutableStateOf(false) }

    // Measured canvas size state
    var canvasWidth by remember { mutableStateOf(800f) }
    var canvasHeight by remember { mutableStateOf(1000f) }

    // Color categories tabs
    var selectedColorCategory by remember { mutableStateOf(0) } // 0: Cơ bản, 1: Pastel, 2: Neon, 3: Kim loại
    // Sticker categories tabs
    var selectedStickerCategory by remember { mutableStateOf(0) } // 0: Động vật, 1: Xe cộ, 2: Cổ tích, 3: Đồ ăn, 4: Đồ chơi

    // Custom text input for AI drawing
    var aiPromptText by remember { mutableStateOf("") }

    // Coloring Bucket tool activation state
    var isBucketActive by remember { mutableStateOf(false) }

    // Running particle animation loop
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateParticles()
            delay(16) // ~60fps smooth physics tick
        }
    }

    // Timer tick for screen time limiting (every 1 second)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            viewModel.incrementTimeSpent()
        }
    }

    // Palette Lists
    val standardColors = listOf(
        Color(0xFFFF5252), Color(0xFFFF9100), Color(0xFFFFD700), Color(0xFF4CAF50),
        Color(0xFF2979FF), Color(0xFF9C27B0), Color(0xFF795548), Color(0xFF37474F)
    )
    val pastelColors = listOf(
        Color(0xFFFFCDD2), Color(0xFFF8BBD0), Color(0xFFE1BEE7), Color(0xFFD1C4E9),
        Color(0xFFC5CAE9), Color(0xFFB3E5FC), Color(0xFFC8E6C9), Color(0xFFFFE0B2)
    )
    val neonColors = listOf(
        Color(0xFFFF007F), Color(0xFF39FF14), Color(0xFF00FFFF), Color(0xFFFF00FF),
        Color(0xFFFFE600), Color(0xFFFF5E00), Color(0xFF8A00FF), Color(0xFF00FF66)
    )
    val metallicColors = listOf(
        Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFCD7F32), Color(0xFFE5A65D),
        Color(0xFFB87333), Color(0xFF8A9A86), Color(0xFF4E5154), Color(0xFFD4AF37)
    )

    // Dynamic sticker categories
    val animalStickers = listOf("🐱", "🐶", "🧸", "🦄", "🐼", "🐻", "🦁", "🐰", "🦖", "🦕", "🐝", "🦋", "🐸", "🐠", "🦀", "🐳")
    val vehicleStickers = listOf("🚗", "🚒", "🚑", "🚓", "🚲", "🚂", "✈️", "🚀", "🛸", "🚢", "🚜", "🚤", "🚕", "🚃", "🛴", "🚁")
    val fantasyStickers = listOf("👸", "🤴", "🏰", "👑", "🧚‍♀️", "🧙‍♂️", "🧜‍♀️", "🐉", "🦄", "✨", "🌟", "🔮", "🪄", "🏵️", "🛡️", "🏹")
    val foodStickers = listOf("🍎", "🍓", "🍌", "🍉", "🍇", "🍒", "🥑", "🍦", "🍩", "🍪", "🍰", "🍭", "🍕", "🍔", "🍟", "🍿")
    val toyStickers = listOf("🎈", "🎂", "🎨", "🪁", "🛹", "🎸", "🥁", "🎮", "🎪", "🎡", "🎁", "🎉", "👑", "🎭", "🧩", "⚽")

    val activeStickersList = when (selectedStickerCategory) {
        0 -> animalStickers
        1 -> vehicleStickers
        2 -> fantasyStickers
        3 -> foodStickers
        else -> toyStickers
    }

    // Daily Challenges & Quick AI Prompts
    val quickAiPrompts = listOf(
        "Khủng long ăn kem dâu 🦖🍦",
        "Mèo con bay khinh khí cầu 🐱🎈",
        "Công chúa cưỡi kỳ lân bay trên mây 👸🦄☁️",
        "Tàu vũ trụ bay thám hiểm sao Hỏa 🚀🪐",
        "Cún con đi tắm hồ bơi 🐶🌊"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF0)) // Soft cream background
    ) {
        val isTabletLayout = maxWidth > 650.dp

        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- TOP HEADER: Logo, Name & Menu Options ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Playful App Title
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bé Họa Sĩ ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF4081)
                        )
                        Text(
                            text = "Magic 🌟",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2979FF)
                        )
                    }
                    Text(
                        text = "Thỏa sức vẽ vời & tô màu sáng tạo",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Action controls row (Rounded, spacious buttons)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Achievements Badge Book Button
                    IconButton(
                        onClick = { showBadgesDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFF9C4), CircleShape)
                            .border(1.5.dp, Color(0xFFFBC02D), CircleShape)
                            .shadow(1.dp, CircleShape)
                    ) {
                        Text("🏅", fontSize = 18.sp)
                    }

                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .shadow(1.dp, CircleShape)
                            .testTag("undo_button")
                    ) {
                        Text("↩️", fontSize = 16.sp)
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .shadow(1.dp, CircleShape)
                            .testTag("redo_button")
                    ) {
                        Text("↪️", fontSize = 16.sp)
                    }

                    // Clean Screen
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .shadow(1.dp, CircleShape)
                            .testTag("clear_button")
                    ) {
                        Text("🧹", fontSize = 16.sp)
                    }

                    // Save To Album
                    IconButton(
                        onClick = {
                            saveTitleInput = ""
                            showSaveDialog = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8F5E9), CircleShape)
                            .shadow(1.dp, CircleShape)
                            .testTag("save_button")
                    ) {
                        Text("💾", fontSize = 16.sp)
                    }

                    // View Album Gallery
                    IconButton(
                        onClick = { showGalleryDrawer = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE3F2FD), CircleShape)
                            .shadow(1.dp, CircleShape)
                            .testTag("gallery_button")
                    ) {
                        Text("🖼️", fontSize = 16.sp)
                    }

                    // Parents Zone
                    IconButton(
                        onClick = {
                            parentGateQuestionNum1 = (3..9).random()
                            parentGateQuestionNum2 = (3..9).random()
                            parentGateAnswerInput = ""
                            isParentGateError = false
                            showParentGateDialog = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE0F2F1), CircleShape)
                            .border(1.5.dp, Color(0xFF009688), CircleShape)
                            .shadow(1.dp, CircleShape)
                    ) {
                        Text("👨‍👩‍👧", fontSize = 16.sp)
                    }
                }
            }

            if (isTabletLayout) {
                TabletCreativeStudio(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel
                )
            } else {
                // --- GUIDANCE MASCOT (Panda Béo speaking to kids) ---
                Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
                    .border(2.dp, Color(0xFF81C784), RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.setMascotMessage("Hihi, tớ là Panda Béo! Cùng tớ vẽ nên thế giới diệu kỳ nha! 🐼🍇")
                        viewModel.spawnEmojiParticles(150f, 150f, "🐼", count = 5)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🐼",
                        fontSize = 32.sp,
                        modifier = Modifier.animateContentSize()
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Panda Béo mách nhỏ:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = mascotMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1B5E20),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // --- WORKSPACE: Creative Sketchbook Canvas ---
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Update canvas width and height reactively based on constraint size
                LaunchedEffect(constraints.maxWidth, constraints.maxHeight) {
                    canvasWidth = constraints.maxWidth.toFloat()
                    canvasHeight = constraints.maxHeight.toFloat()
                }

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, RoundedCornerShape(28.dp))
                        .border(4.dp, Color(0xFFFFD54F), RoundedCornerShape(28.dp)) // Yellow cute picture border
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        
                        // 1. Drawing Canvas Paint Area
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            if (isBucketActive) {
                                                viewModel.triggerSmartFill(offset.x, offset.y, brushColor)
                                            } else {
                                                viewModel.startNewStroke(offset.x, offset.y)
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (!isBucketActive) {
                                                viewModel.updateCurrentStroke(
                                                    change.position.x,
                                                    change.position.y
                                                )
                                            }
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        if (isBucketActive) {
                                            viewModel.triggerSmartFill(offset.x, offset.y, brushColor)
                                        } else {
                                            viewModel.selectSticker(null)
                                            viewModel.spawnParticles(offset.x, offset.y, brushColor, count = 5)
                                        }
                                    }
                                }
                        ) {
                            // Draw template if chosen
                            TemplateDrawer.drawTemplate(selectedTemplate, this)

                            // Draw all strokes
                            strokes.forEach { stroke ->
                                if (stroke.points.size > 1) {
                                    val brushTypeStr = stroke.brushType
                                    if (brushTypeStr == "PENCIL" || brushTypeStr == "MARKER" || stroke.isEraser) {
                                        // Standard drawing path lines
                                        val path = Path()
                                        path.moveTo(stroke.points[0].x, stroke.points[0].y)
                                        for (i in 1 until stroke.points.size) {
                                            path.lineTo(stroke.points[i].x, stroke.points[i].y)
                                        }

                                        val drawColor = if (stroke.isEraser) Color.White else stroke.color
                                        val drawAlpha = if (brushTypeStr == "MARKER") 0.55f else 1.0f

                                        drawPath(
                                            path = path,
                                            color = drawColor.copy(alpha = drawAlpha),
                                            style = Stroke(
                                                width = stroke.width,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    } else if (stroke.isRainbow) {
                                        // Rainbow lines
                                        for (i in 0 until stroke.points.size - 1) {
                                            val p1 = stroke.points[i]
                                            val p2 = stroke.points[i + 1]
                                            val hue = (i * 6) % 360f
                                            drawLine(
                                                color = Color.hsv(hue, 0.9f, 0.95f),
                                                start = Offset(p1.x, p1.y),
                                                end = Offset(p2.x, p2.y),
                                                strokeWidth = stroke.width,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    } else {
                                        // Spatially sprinkled special brushes (GLITTER, HEART, FLOWER, BUBBLE)
                                        // Draw an underlying guide line so it looks cohesive
                                        val path = Path()
                                        path.moveTo(stroke.points[0].x, stroke.points[0].y)
                                        for (i in 1 until stroke.points.size) {
                                            path.lineTo(stroke.points[i].x, stroke.points[i].y)
                                        }
                                        drawPath(
                                            path = path,
                                            color = stroke.color.copy(alpha = 0.2f),
                                            style = Stroke(
                                                width = stroke.width * 0.4f,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )

                                        // Sprinkle vector shapes at intervals
                                        var lastDrawnX = -999f
                                        var lastDrawnY = -999f
                                        stroke.points.forEach { pt ->
                                            val dist = if (lastDrawnX == -999f) 999f else Math.hypot((pt.x - lastDrawnX).toDouble(), (pt.y - lastDrawnY).toDouble()).toFloat()
                                            if (dist >= 45f) {
                                                lastDrawnX = pt.x
                                                lastDrawnY = pt.y
                                                when (brushTypeStr) {
                                                    "GLITTER" -> drawGlitterStar(this, pt.x, pt.y, stroke.color)
                                                    "HEART" -> drawHeart(this, pt.x, pt.y, stroke.color)
                                                    "FLOWER" -> drawFlowerBrush(this, pt.x, pt.y, stroke.color)
                                                    "BUBBLE" -> drawBubbleBrush(this, pt.x, pt.y)
                                                    "LEAF" -> drawLeafBrush(this, pt.x, pt.y, stroke.color)
                                                    "SNOW" -> drawSnowBrush(this, pt.x, pt.y)
                                                    "LIGHTNING" -> drawLightningBrush(this, pt.x, pt.y)
                                                    "FOOTPRINT" -> drawFootprintBrush(this, pt.x, pt.y, stroke.color)
                                                    "FISH" -> drawFishBrush(this, pt.x, pt.y, stroke.color)
                                                    "CLOUD" -> drawCloudBrush(this, pt.x, pt.y)
                                                }
                                            }
                                        }
                                    }
                                } else if (stroke.points.size == 1) {
                                    val pt = stroke.points[0]
                                    val color = if (stroke.isEraser) Color.White else stroke.color
                                    drawCircle(color, stroke.width / 2f, Offset(pt.x, pt.y))
                                }
                            }
                        }

                        // 2. Stickers overlay on top of canvas (with adorable high-performance physics-like infinite breathe/float animations)
                        stickers.forEach { sticker ->
                            val isSelected = sticker.id == selectedStickerId

                            val infiniteTransition = rememberInfiniteTransition(label = "sticker_anim_${sticker.id}")

                            // Gentle breathe scale pulsing
                            val breatheScale by infiniteTransition.animateFloat(
                                initialValue = 0.94f,
                                targetValue = 1.06f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1600 + (sticker.id.hashCode() % 400).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            // Playful side-to-side rotation wobble
                            val floatAngle by infiniteTransition.animateFloat(
                                initialValue = -6f,
                                targetValue = 6f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1900 + (sticker.id.hashCode() % 500).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "angle"
                            )

                            // Vertical floating offset
                            val floatY by infiniteTransition.animateFloat(
                                initialValue = -5f,
                                targetValue = 5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2200 + (sticker.id.hashCode() % 600).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "y_offset"
                            )

                            // Engine shake jitter for vehicles
                            val jitterOffset by infiniteTransition.animateFloat(
                                initialValue = -1f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(110 + (sticker.id.hashCode() % 40).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "jitter"
                            )

                            // Detect special behavior tags by emoji
                            val flyingEmojis = listOf("✈️", "🚀", "🛸", "🚁", "🦋", "🐝", "🎈", "🧚‍♀️", "🐦", "✨", "🌟", "☁️", "🎈")
                            val swimmingEmojis = listOf("🐠", "🦀", "🐳", "🐟", "🐸", "🧜‍♀️")
                            val vehicleEmojis = listOf("🚗", "🚒", "🚑", "🚓", "🚲", "🚂", "🚜", "🚤", "🚕", "🚃", "🛴")

                            val isFlying = sticker.emoji in flyingEmojis
                            val isSwimming = sticker.emoji in swimmingEmojis
                            val isVehicle = sticker.emoji in vehicleEmojis

                            val animScale = breatheScale
                            val animRotation = if (isFlying) floatAngle else if (isSwimming) floatAngle * 0.7f else if (isVehicle) floatAngle * 0.15f else 0f
                            val animY = if (isFlying) floatY else if (isVehicle) jitterOffset else 0f
                            val animX = if (isVehicle) jitterOffset else 0f

                            Box(
                                modifier = Modifier
                                    .offset { 
                                        IntOffset(
                                            (sticker.x + animX).toInt() - 50, 
                                            (sticker.y + animY).toInt() - 50
                                        ) 
                                    }
                                    .graphicsLayer(
                                        scaleX = sticker.scale * animScale,
                                        scaleY = sticker.scale * animScale,
                                        rotationZ = sticker.rotation + animRotation
                                    )
                                    .pointerInput(sticker.id) {
                                        detectDragGestures(
                                            onDragStart = { viewModel.selectSticker(sticker.id) },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                viewModel.updateStickerPosition(
                                                    sticker.id,
                                                    dragAmount.x,
                                                    dragAmount.y
                                                )
                                            }
                                        )
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = Color(0xFFFF4081),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = sticker.emoji,
                                    fontSize = 42.sp,
                                    modifier = Modifier.clickable {
                                        viewModel.selectSticker(sticker.id)
                                    }
                                )

                                if (isSelected) {
                                    // Delete Sticker Button (❌)
                                    IconButton(
                                        onClick = { viewModel.deleteSticker(sticker.id) },
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .offset(x = (-20).dp, y = (-20).dp)
                                            .size(26.dp)
                                            .background(Color.White, CircleShape)
                                            .border(1.dp, Color(0xFFFF4081), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xóa sticker",
                                            tint = Color(0xFFFF4081),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Touch sizing adjustments
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 20.dp, y = 20.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "➖",
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .clickable { viewModel.updateStickerScaleAndRotation(sticker.id, 0.8f, 0f) }
                                                .padding(3.dp)
                                        )
                                        Text(
                                            text = "➕",
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .clickable { viewModel.updateStickerScaleAndRotation(sticker.id, 1.2f, 0f) }
                                                .padding(3.dp)
                                        )
                                        Text(
                                            text = "🔄",
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .clickable { viewModel.updateStickerScaleAndRotation(sticker.id, 1.0f, 15f) }
                                                .padding(3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Float floating particle sparkles (Delight particle effect)
                        particles.forEach { particle ->
                            if (particle.emoji != null) {
                                Text(
                                    text = particle.emoji,
                                    fontSize = particle.size.sp,
                                    modifier = Modifier
                                        .offset { IntOffset(particle.x.toInt(), particle.y.toInt()) }
                                        .graphicsLayer(alpha = particle.alpha)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(particle.x.toInt(), particle.y.toInt()) }
                                        .size(particle.size.dp)
                                        .graphicsLayer(alpha = particle.alpha)
                                        .background(particle.color, CircleShape)
                                )
                            }
                        }

                        // 4. Loading indicator overlay when generating AI outline
                        if (isGeneratingAi) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.82f))
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🐼", fontSize = 64.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    CircularProgressIndicator(color = Color(0xFFFF4081))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Panda Béo đang nhờ bạn AI thông minh vẽ hình...\nBé đợi một xíu nha! ✨",
                                        textAlign = TextAlign.Center,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- NAVIGATION TABS HUB (Kid friendly design) ---
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Main category tabs row (Colors, Brushes, Stickers, Templates, AI Draw)
                    ScrollableTabRow(
                        selectedTabIndex = activeTab,
                        edgePadding = 4.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("🎨 Màu Sắc", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("🖌️ Cọ Vẽ", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("🧸 Hình Dán", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = activeTab == 3,
                            onClick = { activeTab = 3 },
                            text = { Text("🌸 Vẽ Mẫu", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = activeTab == 4,
                            onClick = { activeTab = 4 },
                            text = { Text("🤖 Vẽ AI", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab contents layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        when (activeTab) {
                            0 -> {
                                // --- TAB 0: COLORS & PALETTE CATEGORIES ---
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Row 1: Sub categories
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val colorCats = listOf("Cơ Bản 🔴", "Pastel 🌸", "Neon 🌟", "Kim Loại 🏆")
                                        colorCats.forEachIndexed { idx, title ->
                                            val isSel = selectedColorCategory == idx
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSel) Color(0xFFFFF176) else Color(0xFFF1F1F1))
                                                    .border(1.dp, if (isSel) Color.DarkGray else Color.LightGray, RoundedCornerShape(12.dp))
                                                    .clickable { selectedColorCategory = idx }
                                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Special Tools Toggle (Coloring bucket vs Eraser)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isBucketActive) Color(0xFFFF8A80) else Color(0xFFF1F1F1))
                                                .border(1.dp, if (isBucketActive) Color.Red else Color.LightGray, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    isBucketActive = !isBucketActive
                                                    if (isBucketActive) {
                                                        viewModel.setMascotMessage("Bé vừa bật Xô Màu 🪣! Nhấp chuột vào canvas để đổ màu phép thuật nha!")
                                                    } else {
                                                        viewModel.setMascotMessage("Đã chuyển về cọ vẽ thông thường! Bé vẽ tự do nha ✏️")
                                                    }
                                                }
                                                .padding(vertical = 4.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🪣 Xô Màu", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (isBucketActive) Color.White else Color.Black)
                                        }
                                    }

                                    // Row 2: Horizontal Colors list
                                    val colorsList = when (selectedColorCategory) {
                                        0 -> standardColors
                                        1 -> pastelColors
                                        2 -> neonColors
                                        else -> metallicColors
                                    }

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(colorsList) { color ->
                                            val isSel = brushColor == color && !isEraser && !isRainbow
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isSel) 48.dp else 40.dp)
                                                    .background(color, CircleShape)
                                                    .border(
                                                        width = if (isSel) 3.dp else 1.dp,
                                                        color = if (isSel) Color(0xFF37474F) else Color.LightGray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        viewModel.selectColor(color)
                                                        viewModel.trackColorCategoryUsage(selectedColorCategory)
                                                        viewModel.spawnParticles(150f, 150f, color, count = 3)
                                                    }
                                                    .shadow(if (isSel) 3.dp else 0.dp, CircleShape)
                                            )
                                        }

                                        // Magic Eraser
                                        item {
                                            val isEra = isEraser
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(if (isEra) 48.dp else 40.dp)
                                                    .background(Color.White, CircleShape)
                                                    .border(
                                                        width = if (isEra) 3.dp else 1.dp,
                                                        color = if (isEra) Color(0xFFFF4081) else Color.LightGray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.selectEraserMode() }
                                            ) {
                                                Text("🧽", fontSize = 18.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // --- TAB 1: CREATIVE BRUSHES ---
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Chọn nét cọ vẽ phép thuật của bé:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(BrushType.values()) { brush ->
                                            val isSel = selectedBrush == brush && !isEraser
                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSel) Color(0xFFE1F5FE) else Color(0xFFF5F5F5)
                                                ),
                                                modifier = Modifier
                                                    .width(105.dp)
                                                    .border(
                                                        width = if (isSel) 2.dp else 1.dp,
                                                        color = if (isSel) Color(0xFF2979FF) else Color.LightGray,
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { viewModel.selectBrush(brush) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(brush.icon, fontSize = 20.sp)
                                                    Column {
                                                        Text(brush.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF0D47A1) else Color.Black)
                                                        Text(brush.description.take(10) + "..", fontSize = 8.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // --- TAB 2: CATEGORIZED STICKERS ---
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Row 1: Categories selector tabs
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val stickerCats = listOf("🐱 Động Vật", "🚀 Xe Cộ", "👸 Cổ Tích", "🍎 Đồ Ăn", "🎈 Đồ Chơi")
                                        items(stickerCats.size) { idx ->
                                            val catTitle = stickerCats[idx]
                                            val isSel = selectedStickerCategory == idx
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSel) Color(0xFFE1BEE7) else Color(0xFFF1F1F1))
                                                    .border(1.dp, if (isSel) Color(0xFF8E24AA) else Color.LightGray, RoundedCornerShape(12.dp))
                                                    .clickable { selectedStickerCategory = idx }
                                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                                            ) {
                                                Text(catTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF4A148C) else Color.Black)
                                            }
                                        }
                                    }

                                    // Row 2: Stickers Grid
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(activeStickersList) { sticker ->
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(Color(0xFFF9F9F9), CircleShape)
                                                    .border(1.dp, Color.LightGray, CircleShape)
                                                    .clickable { viewModel.addSticker(sticker) }
                                                    .shadow(1.dp, CircleShape)
                                            ) {
                                                Text(sticker, fontSize = 24.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> {
                                // --- TAB 3: COLORING OUTLINES TEMPLATE ---
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Chọn tranh mẫu dễ thương để tô màu nha:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(ColoringTemplate.values()) { template ->
                                            val isSel = selectedTemplate == template
                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSel) Color(0xFFE1F5FE) else Color(0xFFF9F9F9)
                                                ),
                                                modifier = Modifier
                                                    .width(115.dp)
                                                    .border(
                                                        width = if (isSel) 2.dp else 1.dp,
                                                        color = if (isSel) Color(0xFF2979FF) else Color.LightGray,
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { viewModel.selectTemplate(template) }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(template.icon, fontSize = 24.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = template.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center,
                                                        color = if (isSel) Color(0xFF0D47A1) else Color.DarkGray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            4 -> {
                                // --- TAB 4: AI OUTLINE DRAWING GENERATOR (GEMINI API) ---
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isAiFreeFormAllowed) {
                                        // Lock Prompt Input message
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text("🛡️🐼", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Ba mẹ bật Chế Độ An Toàn! Hãy nhấn chọn các gợi ý vẽ ở dưới nha! 👇",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00796B)
                                                )
                                            }
                                        }
                                    } else {
                                        // Row 1: Prompt Input text bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = aiPromptText,
                                                onValueChange = { aiPromptText = it },
                                                placeholder = { Text("Con muốn vẽ khủng long, mèo con bay bóng...", fontSize = 11.sp) },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    unfocusedContainerColor = Color.White,
                                                    focusedContainerColor = Color.White
                                                )
                                            )

                                            Button(
                                                onClick = {
                                                    if (aiPromptText.isNotBlank()) {
                                                        viewModel.generateAiDrawing(aiPromptText, canvasWidth, canvasHeight)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text("Vẽ AI ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    // Row 2: Standard fast tags
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(quickAiPrompts) { prompt ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFE8EAF6))
                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        aiPromptText = prompt.substringBeforeLast(" ") // clean prompt text
                                                        viewModel.generateAiDrawing(aiPromptText, canvasWidth, canvasHeight)
                                                    }
                                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                                            ) {
                                                Text(prompt, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3F51B5))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        // --- DIALOG 1: Clean/Reset Confirm Dialog ---
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧹 ", fontSize = 24.sp)
                        Text("Xóa Hết Tranh Vẽ?")
                    }
                },
                text = {
                    Text(
                        "Bé có muốn xóa sạch bức tranh này để bắt đầu vẽ tranh mới lấp lánh khác không nào?",
                        fontSize = 15.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearCanvas()
                            showClearConfirm = false
                            Toast.makeText(context, "Đã dọn dẹp bảng vẽ rồi nha!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Xóa Sạch", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearConfirm = false }) {
                        Text("Quay Lại", color = Color.DarkGray)
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }

        // --- DIALOG 2: Save Masterpiece ---
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💾 ", fontSize = 24.sp)
                        Text("Lưu Tranh Của Bé")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Đặt tên đáng yêu cho tác phẩm của bé nha:", fontSize = 14.sp)
                        OutlinedTextField(
                            value = saveTitleInput,
                            onValueChange = { saveTitleInput = it },
                            placeholder = { Text("Mèo con lấp lánh, Hoa tặng mẹ...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveToGallery(context, saveTitleInput, canvasWidth.toInt(), canvasHeight.toInt())
                            showSaveDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Lưu Lại 🌟", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showSaveDialog = false }) {
                        Text("Hủy", color = Color.DarkGray)
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }

        // --- DIALOG 3: Kids Badge Book (Achievements) ---
        if (showBadgesDialog) {
            AlertDialog(
                onDismissRequest = { showBadgesDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 Huy Hiệu Của Bé", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFE65100))
                        Text("🎖️", fontSize = 24.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Cố gắng chinh phục để mở khóa những huy hiệu siêu cấp đáng yêu này nha bé ơi:", fontSize = 12.sp, color = Color.Gray)
                        
                        badges.forEach { badge ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (badge.isUnlocked) Color(0xFFFFF3E0) else Color(0xFFF5F5F5)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (badge.isUnlocked) Color(0xFFFFB74D) else Color.LightGray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Gray scale representation if locked
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                if (badge.isUnlocked) Color(0xFFFFE0B2) else Color(0xFFE0E0E0),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = badge.emoji,
                                            fontSize = 24.sp,
                                            modifier = Modifier.graphicsLayer(alpha = if (badge.isUnlocked) 1.0f else 0.4f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = badge.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (badge.isUnlocked) Color(0xFFE65100) else Color.DarkGray
                                        )
                                        Text(
                                            text = badge.desc,
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        if (badge.isUnlocked) {
                                            Text(
                                                text = "Đã nhận huy hiệu! 🎉",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF388E3C)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Daily Challenge Trigger Button inside Badges
                        Button(
                            onClick = {
                                viewModel.setMascotMessage("Thử thách hôm nay: Bé dán đủ 8 nhãn dán lấp lánh lên màn hình nhé! 🧸🎖️")
                                viewModel.checkAndUnlockBadge("b5") // Claim the daily challenge badge!
                                showBadgesDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Bắt Đầu Thử Thách Hômcopy 🏆", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBadgesDialog = false }) {
                        Text("Đóng Lại", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        // --- DIALOG: Parent Gate Math Verification ---
        if (showParentGateDialog) {
            AlertDialog(
                onDismissRequest = { showParentGateDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔒 CỔNG PHỤ HUYNH", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF00796B))
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Để tiếp tục vào cài đặt hoặc mở khóa, xin ba mẹ vui lòng giải phép toán đơn giản sau để xác minh bạn là người lớn nhé:",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$parentGateQuestionNum1  +  $parentGateQuestionNum2  =  ?",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF004D40)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = parentGateAnswerInput,
                            onValueChange = { parentGateAnswerInput = it },
                            placeholder = { Text("Nhập kết quả...") },
                            singleLine = true,
                            isError = isParentGateError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isParentGateError) {
                            Text("Kết quả chưa đúng, ba mẹ thử lại nhé!", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val ans = parentGateAnswerInput.trim().toIntOrNull()
                            if (ans == (parentGateQuestionNum1 + parentGateQuestionNum2)) {
                                showParentGateDialog = false
                                showParentSettingsDialog = true
                                isParentGateError = false
                                // If screen time limit is reached, reset it so child can play again
                                if (isTimeLimitReached) {
                                    viewModel.resetTimeLimitReached()
                                    Toast.makeText(context, "🔓 Đã khôi phục giờ chơi lành mạnh!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                isParentGateError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                    ) {
                        Text("Xác Minh", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showParentGateDialog = false }) {
                        Text("Hủy", color = Color.DarkGray)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        // --- DIALOG: Parent Settings Dashboard ---
        if (showParentSettingsDialog) {
            var activeParentTab by remember { mutableStateOf(0) } // 0: Thống kê, 1: Giờ chơi, 2: An toàn AI
            
            AlertDialog(
                onDismissRequest = { showParentSettingsDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👨‍👩‍👧 ", fontSize = 24.sp)
                            Text("Bảng Cho Ba Mẹ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(onClick = { showParentSettingsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tiny Tab row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("📊 Thống Kê", "⏰ Giờ Chơi", "🛡️ Vẽ AI").forEachIndexed { index, label ->
                                val isSelected = activeParentTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF009688) else Color(0xFFEEEEEE))
                                        .clickable { activeParentTab = index }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        Divider()

                        // Tab Contents
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (activeParentTab) {
                                0 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("📊 Thói Quen Học Vẽ Của Bé:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00796B))
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("⏱️ Thời gian vẽ hôm nay:", fontSize = 12.sp, color = Color.Gray)
                                            val mins = timeSpentSeconds / 60
                                            val secs = timeSpentSeconds % 60
                                            Text("${mins}m ${secs}s", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🎨 Tranh lưu trong album:", fontSize = 12.sp, color = Color.Gray)
                                            Text("${savedDrawings.size} tác phẩm", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🧸 Số lượng Sticker đã dán:", fontSize = 12.sp, color = Color.Gray)
                                            Text("$totalStickersApplied nhãn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("✨ Tông màu bé yêu thích nhất:", fontSize = 12.sp, color = Color.Gray)
                                            Text(viewModel.getFavoriteColorCategory(), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF4081))
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                        ) {
                                            Text(
                                                text = "💡 Panda mách nhỏ: Bé yêu thích vẽ tranh sáng tạo và sử dụng các màu sắc vui tươi. Hãy khen ngợi và cùng con thảo luận về các bức vẽ của bé nhé! 🐼",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF5D4037),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                                1 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("⏰ Thiết Lập Giới Hạn Giờ Chơi:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00796B))
                                        Text("Panda Béo nhắc nhở bé cất bút bảo vệ đôi mắt tinh của bé sau thời gian đã chọn:", fontSize = 12.sp, color = Color.Gray)
                                        
                                        val options = listOf(
                                            0 to "Không giới hạn ♾️",
                                            15 to "15 Phút (mắt sáng khỏe) 👀",
                                            30 to "30 Phút (vui chơi cân bằng) 🕒",
                                            45 to "45 Phút (lành mạnh tối ưu) 🛡️"
                                        )
                                        
                                        options.forEach { (mins, label) ->
                                            val isSel = timeLimitMinutes == mins
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) Color(0xFFE0F2F1) else Color.Transparent)
                                                    .clickable { viewModel.setTimeLimitMinutes(mins) }
                                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSel,
                                                    onClick = { viewModel.setTimeLimitMinutes(mins) }
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🛡️ Chế Độ An Toàn & Bảo Mật Vẽ AI:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00796B))
                                        Text("Nhập văn bản tự do vẽ AI có thể chứa rủi ro. Hãy bật chế độ giới hạn an toàn cho bé yêu:", fontSize = 11.sp, color = Color.Gray)
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Cho phép bé viết chữ tự do:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Nếu tắt, bé chỉ có thể nhấn chọn 5 gợi ý vẽ mẫu sẵn an toàn đã kiểm duyệt", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Switch(
                                                checked = isAiFreeFormAllowed,
                                                onCheckedChange = { viewModel.setAiFreeFormAllowed(it) }
                                            )
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "✓ Bạn AI Gemini sẽ hỗ trợ vẽ nét phác thảo đơn giản để trẻ thỏa sức tô màu theo ý thích cá nhân. Chúc bé có những giờ chơi thật bổ ích!",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1B5E20),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showParentSettingsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                    ) {
                        Text("Hoàn Thành", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        // --- OVERLAY: Screen Time Lock ---
        if (isTimeLimitReached) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF9C4)) // soft yellow cozy lock screen
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("🐼💤😴", fontSize = 72.sp)
                    Text(
                        text = "Mỏi mắt rồi bé yêu ơi! 👀",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = "Panda Béo mách nhỏ: Chúng mình đã học vẽ hăng say rất nhiều rồi nè! Bé hãy cất bút vẽ đi uống miếng nước, nhấp nháy mắt mỏi mệt rồi lát sau chúng mình lại chơi tiếp nha! 🥤❤️",
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF795548),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Button(
                        onClick = {
                            parentGateQuestionNum1 = (3..9).random()
                            parentGateQuestionNum2 = (3..9).random()
                            parentGateAnswerInput = ""
                            isParentGateError = false
                            showParentGateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("👨‍👩‍👧 Ba Mẹ Mở Khóa", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- OVERLAY: Super Masterpiece Celebration ---
        if (isCelebrating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable { viewModel.setCelebrating(false) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("🎉🐼🏆🎨👏", fontSize = 64.sp)
                    Text(
                        text = "Tác Phẩm Vàng Đã Hoàn Thành!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD54F),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Oa! Tuyệt tác lấp lánh của con yêu đã được cất giữ cẩn thận trong viện bảo tàng nghệ thuật rồi! Ba mẹ ơi, hãy mau khen thưởng thiên tài nghệ thuật tí hon nhà mình nha! ❤️🎉",
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // A lovely canvas presentation mock with gold frame
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(240.dp)
                            .border(4.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🧑‍🎨👑🏆", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tác Phẩm Tuyệt Vời", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Viện Bảo Tàng Bé Yêu", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.setCelebrating(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Text("Tiếp Tục Sáng Tạo 🎨", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // --- SLIDE PANEL/DRAWER: Saved Album Gallery ---
        if (showGalleryDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showGalleryDrawer = false }
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = false) {} // prevent dismissing when tapping inside
                        .shadow(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Viện Bảo Tàng Tranh 🎨🏛️",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF5D4037) // Warm mahogany color
                            )
                            IconButton(onClick = { showGalleryDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Đóng")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        if (savedDrawings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bé chưa có tranh lưu nào trong viện bảo tàng hết trơn. Vẽ xong bé hãy bấm nút Lưu nha! 🎨🌟🐼",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Column(modifier = Modifier.weight(1f)) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(1),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(savedDrawings) { drawing ->
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(3.2.dp, Color(0xFFFBC02D), RoundedCornerShape(16.dp)) // Shiny gold frame!
                                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                                .clickable {
                                                    viewModel.loadDrawing(drawing)
                                                    showGalleryDrawer = false
                                                    Toast.makeText(context, "Đã tải bức tranh: ${drawing.title}", Toast.LENGTH_SHORT).show()
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // Cute canvas icon / template emoji
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .background(Color(0xFFFFECB3), CircleShape)
                                                        .border(1.dp, Color(0xFFFFB300), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(drawing.template.icon.ifBlank { "🎨" }, fontSize = 24.sp)
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = drawing.title,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Black,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = Color(0xFF3E2723)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Họa sĩ nhí 🧑‍🎨 | Nét: ${drawing.strokes.size}",
                                                        fontSize = 10.5.sp,
                                                        color = Color.DarkGray
                                                    )
                                                    Text(
                                                        text = "Nhãn dán: ${drawing.stickers.size} 🧸",
                                                        fontSize = 9.5.sp,
                                                        color = Color.Gray
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        try {
                                                             val bitmap = DrawingExporter.exportToBitmap(
                                                                 context = context,
                                                                 strokes = drawing.strokes,
                                                                 stickers = drawing.stickers,
                                                                 template = drawing.template,
                                                                 width = canvasWidth.toInt(),
                                                                 height = canvasHeight.toInt()
                                                             )
                                                             val uri = DrawingExporter.saveToDeviceStorage(context, bitmap, drawing.title)
                                                             if (uri != null) {
                                                                 Toast.makeText(context, "🎉 Đã tải tranh về máy thành công!", Toast.LENGTH_SHORT).show()
                                                             } else {
                                                                 Toast.makeText(context, "Lỗi khi lưu ảnh vào máy", Toast.LENGTH_SHORT).show()
                                                             }
                                                         } catch (e: Exception) {
                                                             Toast.makeText(context, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                         }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Text("📥", fontSize = 16.sp)
                                                }

                                                IconButton(
                                                    onClick = { viewModel.deleteSavedDrawing(drawing.id) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Xóa tranh",
                                                        tint = Color(0xFFD32F2F),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Guided mascot guide guide
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🐼🏛️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Panda hướng dẫn viên: Chào mừng quý phụ huynh ghé thăm viện bảo tàng nghệ thuật của con yêu! ❤️",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Dynamic Vector Brushes Drawer Helpers ---

private fun drawGlitterStar(scope: DrawScope, x: Float, y: Float, color: Color) {
    val starPath = Path().apply {
        moveTo(x, y - 10f)
        quadraticTo(x, y, x + 10f, y)
        quadraticTo(x, y, x, y + 10f)
        quadraticTo(x, y, x - 10f, y)
        quadraticTo(x, y, x, y - 10f)
    }
    scope.drawPath(starPath, color)
    // Draw an extra tiny shiny yellow star inside
    val innerStar = Path().apply {
        moveTo(x, y - 4f)
        quadraticTo(x, y, x + 4f, y)
        quadraticTo(x, y, x, y + 4f)
        quadraticTo(x, y, x - 4f, y)
        quadraticTo(x, y, x, y - 4f)
    }
    scope.drawPath(innerStar, Color(0xFFFFD54F))
}

private fun drawHeart(scope: DrawScope, x: Float, y: Float, color: Color) {
    val heartPath = Path().apply {
        moveTo(x, y - 4f)
        cubicTo(x - 10f, y - 15f, x - 18f, y - 5f, x, y + 12f)
        cubicTo(x + 18f, y - 5f, x + 10f, y - 15f, x, y - 4f)
    }
    scope.drawPath(heartPath, color)
}

private fun drawFlowerBrush(scope: DrawScope, x: Float, y: Float, color: Color) {
    val r = 5f
    scope.drawCircle(color, r, Offset(x - 7f, y - 4f))
    scope.drawCircle(color, r, Offset(x + 7f, y - 4f))
    scope.drawCircle(color, r, Offset(x - 7f, y + 4f))
    scope.drawCircle(color, r, Offset(x + 7f, y + 4f))
    scope.drawCircle(color, r, Offset(x, y - 8f))
    scope.drawCircle(Color(0xFFFFD54F), r * 0.8f, Offset(x, y)) // Yellow center
}

private fun drawBubbleBrush(scope: DrawScope, x: Float, y: Float) {
    scope.drawCircle(Color(0xFF80DEEA).copy(alpha = 0.35f), 12f, Offset(x, y)) // Bubble globe translucent fill
    scope.drawCircle(Color.White.copy(alpha = 0.8f), 3f, Offset(x - 4f, y - 4f)) // Specular reflection
    scope.drawCircle(Color(0xFFE0F7FA), 12f, Offset(x, y), style = Stroke(width = 1.2f)) // Soft outline bubble
}

private fun drawLeafBrush(scope: DrawScope, x: Float, y: Float, color: Color) {
    // Beautiful green fall leaves
    val leafColor = if (color == Color.Black || color == Color.White) Color(0xFF4CAF50) else color
    val path = Path().apply {
        moveTo(x, y - 10f)
        quadraticTo(x - 8f, y, x, y + 10f)
        quadraticTo(x + 8f, y, x, y - 10f)
    }
    scope.drawPath(path, leafColor)
    scope.drawLine(Color(0xFF1B5E20), Offset(x, y - 10f), Offset(x, y + 10f), strokeWidth = 1f)
}

private fun drawSnowBrush(scope: DrawScope, x: Float, y: Float) {
    val c = Color(0xFFB3E5FC)
    scope.drawLine(c, Offset(x - 8f, y), Offset(x + 8f, y), strokeWidth = 1.8f)
    scope.drawLine(c, Offset(x, y - 8f), Offset(x, y + 8f), strokeWidth = 1.8f)
    scope.drawLine(c, Offset(x - 6f, y - 6f), Offset(x + 6f, y + 6f), strokeWidth = 1.2f)
    scope.drawLine(c, Offset(x + 6f, y - 6f), Offset(x - 6f, y + 6f), strokeWidth = 1.2f)
}

private fun drawLightningBrush(scope: DrawScope, x: Float, y: Float) {
    val path = Path().apply {
        moveTo(x + 4f, y - 10f)
        lineTo(x - 6f, y + 1f)
        lineTo(x - 1f, y + 1f)
        lineTo(x - 4f, y + 10f)
        lineTo(x + 6f, y - 1f)
        lineTo(x + 1f, y - 1f)
        close()
    }
    scope.drawPath(path, Color(0xFFFFEB3B))
}

private fun drawFootprintBrush(scope: DrawScope, x: Float, y: Float, color: Color) {
    val printColor = if (color == Color.White) Color.DarkGray else color.copy(alpha = 0.85f)
    // Main pad
    scope.drawCircle(printColor, 6f, Offset(x, y + 3f))
    // 3 small toe circles
    scope.drawCircle(printColor, 2.5f, Offset(x - 6f, y - 3f))
    scope.drawCircle(printColor, 2.8f, Offset(x, y - 6f))
    scope.drawCircle(printColor, 2.5f, Offset(x + 6f, y - 3f))
}

private fun drawFishBrush(scope: DrawScope, x: Float, y: Float, color: Color) {
    val fishColor = if (color == Color.Black || color == Color.White) Color(0xFFFF7043) else color
    val body = Path().apply {
        moveTo(x - 10f, y)
        quadraticTo(x, y - 6f, x + 6f, y)
        quadraticTo(x, y + 6f, x - 10f, y)
    }
    scope.drawPath(body, fishColor)
    // Tail fin
    val tail = Path().apply {
        moveTo(x - 10f, y)
        lineTo(x - 15f, y - 5f)
        lineTo(x - 13f, y)
        lineTo(x - 15f, y + 5f)
        close()
    }
    scope.drawPath(tail, fishColor)
}

private fun drawCloudBrush(scope: DrawScope, x: Float, y: Float) {
    val color = Color(0xFFECEFF1)
    scope.drawCircle(color, 6f, Offset(x - 5f, y))
    scope.drawCircle(color, 8f, Offset(x, y - 2f))
    scope.drawCircle(color, 6f, Offset(x + 5f, y))
    scope.drawCircle(color, 5f, Offset(x, y + 3f))
}
