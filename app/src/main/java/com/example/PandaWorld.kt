package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.geometry.Offset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PandaWorldHome(
    activeRoom: String,
    onRoomChange: (String) -> Unit,
    viewModel: DrawingViewModel,
    canvasWidth: Float,
    canvasHeight: Float,
    context: Context,
    savedDrawings: List<SavedDrawing>,
    badges: List<KidsBadge>,
    aiPromptText: String,
    onAiPromptChange: (String) -> Unit,
    quickAiPrompts: List<String>,
    isGeneratingAi: Boolean,
    mascotMessage: String,
    onSaveTitleInput: (String) -> Unit,
    onShowSaveDialog: (Boolean) -> Unit
) {
    when (activeRoom) {
        "home" -> {
            PandaHomeLobby(
                onRoomChange = onRoomChange,
                viewModel = viewModel,
                savedDrawingsCount = savedDrawings.size
            )
        }
        "drawing_class" -> {
            PandaDrawingClassRoom(
                onBack = { onRoomChange("home") },
                onGoToStudio = { onRoomChange("studio") },
                viewModel = viewModel
            )
        }
        "ai_room" -> {
            PandaAiRoom(
                onBack = { onRoomChange("home") },
                onGoToStudio = { onRoomChange("studio") },
                viewModel = viewModel,
                aiPromptText = aiPromptText,
                onAiPromptChange = onAiPromptChange,
                quickAiPrompts = quickAiPrompts,
                isGeneratingAi = isGeneratingAi,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )
        }
        "museum" -> {
            PandaMuseum(
                onBack = { onRoomChange("home") },
                savedDrawings = savedDrawings,
                viewModel = viewModel,
                onLoad = { onRoomChange("studio") },
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                context = context
            )
        }
        "badge_room" -> {
            PandaBadgeRoom(
                onBack = { onRoomChange("home") },
                badges = badges
            )
        }
        "gift_room" -> {
            PandaGiftRoom(
                onBack = { onRoomChange("home") },
                viewModel = viewModel
            )
        }
        "garden" -> {
            PandaGarden(
                onBack = { onRoomChange("home") },
                savedDrawingsCount = savedDrawings.size,
                viewModel = viewModel
            )
        }
    }
}

data class VisualSound(
    val id: Long,
    val text: String,
    val emoji: String,
    val x: Float,
    val y: Float,
    val color: Color
)

@Composable
fun PandaHomeLobby(
    onRoomChange: (String) -> Unit,
    viewModel: DrawingViewModel,
    savedDrawingsCount: Int
) {
    val context = LocalContext.current
    val childName by viewModel.childName.collectAsState()
    val childAge by viewModel.childAge.collectAsState()
    val pandaName by viewModel.pandaName.collectAsState()

    // --- Dynamic Time of Day Cycle (Chu kỳ ngày & đêm thực tế) ---
    val currentHour = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val isNight = currentHour >= 18 || currentHour < 6
    val isSunset = currentHour == 17

    // --- Panda Schedule Engine (Lịch trình của chú Panda theo giờ thực) ---
    val (scheduleEmoji, scheduleSpeech) = remember(currentHour, childName, pandaName) {
        when {
            currentHour in 6..10 -> {
                "🐼💦" to "Chào buổi sáng bé $childName yêu! $pandaName đang tưới tre non xanh mướt ở Vườn Trúc nè, cùng đi vẽ tranh đón ngày mới nha! 🎋💦"
            }
            currentHour in 11..13 -> {
                "🐼🥣" to "Nhoàm nhoàm... Đến giờ ăn trưa rồi, $pandaName đang mút tre non ngon ngọt lịm! Bé $childName nhớ ăn ngoan và ngủ trưa nha! 🥣🎋"
            }
            currentHour in 14..17 -> {
                "🐼🎨" to "Họa sĩ $pandaName xin chào bé $childName! Chiều nay mát mẻ quá, chúng mình cùng vào xưởng vẽ tranh rực rỡ sắc màu nhé! 🎨🖌"
            }
            currentHour in 18..20 -> {
                "🐼📚" to "Tối ấm áp nhé bé $childName! $pandaName đang ngồi đọc truyện tranh cổ tích thần tiên. Chúng mình cùng khám phá vương quốc nào! 📚✨"
            }
            else -> {
                "🐼💤" to "Suỵt... khò khò... Đến giờ đi ngủ ấm áp rồi bé yêu ơi! Chúc bé $childName ngủ ngon và có giấc mơ bay bổng lấp lánh nhé! 🛌💤"
            }
        }
    }

    // --- Panda Memory & Progression Dialog Box (Nhớ sở thích màu & thành tựu vẽ của bé) ---
    val favoriteColor = viewModel.getFavoriteColorCategory()
    val isFirstTime = savedDrawingsCount == 0
    val ageTierText = viewModel.getAgeTier().levelName

    val initialPandaGreeting = remember(childName, savedDrawingsCount, favoriteColor, scheduleSpeech) {
        val colorReminder = if (favoriteColor.contains("nét vẽ")) {
            " Tớ nhớ hôm trước bé thích tô tông màu $favoriteColor lắm nè! Hôm nay vẽ tiếp xem có mở khóa thêm hoa mới không nha!"
        } else " Hôm nay bé muốn thử sức với cọ vẽ Bảy Sắc Cầu Vồng 🌈 lấp lánh không nè?"
        
        when {
            isFirstTime -> {
                "Chào bé $childName yêu! Vương quốc của chúng mình còn mới toanh nè. Mau vào Xưởng Vẽ 🎨 tô tranh đầu tiên để trồng thêm cây xanh nha! 🌳✨"
            }
            savedDrawingsCount in 1..2 -> {
                "Oa! Bé $childName đã vẽ được $savedDrawingsCount bức tranh rồi! Làng của tụi mình có thêm cây rừng 🌳 và hoa hồng nở rực rỡ 🌷 rồi đó! Bé giỏi quá!$colorReminder"
            }
            savedDrawingsCount in 3..4 -> {
                "Tuyệt vời ông mặt trời! Bé $childName vẽ siêu thế, làng của chúng mình đã mọc thêm cây cầu gỗ 🌉 bắc qua sông lấp lánh rồi!$colorReminder"
            }
            savedDrawingsCount in 5..6 -> {
                "Quá là đỉnh luôn! Bé $childName đã vẽ được $savedDrawingsCount tranh rồi! Làng của chúng mình giờ có thêm Đài Phun Nước Phép Thuật ⛲ lộng lẫy phun bong bóng nữa nè!$colorReminder"
            }
            else -> {
                "Kính coong! Đại họa sĩ $childName ($ageTierText) đã vẽ tận $savedDrawingsCount tranh! Vương quốc giờ có thêm Vòng Quay Ngôi Sao 🎡 khổng lồ lấp lánh rồi!$colorReminder"
            }
        }
    }

    val pandaSayings = remember(childName, childAge, pandaName) {
        listOf(
            "Hê lô bé $childName ($childAge tuổi) yêu! Cùng $pandaName vẽ một bức tranh siêu lấp lánh hôm nay nha! 🐼🎨",
            "Bé $childName có biết $pandaName thích nhất là ăn bánh quy dâu không? Chẹp chẹp... ngon lắm! 🍓🍪",
            "Cậu vẽ đẹp thế này chắc chắn sau này bé $childName sẽ là đại họa sĩ đấy nhé! 🧑‍🎨🌟",
            "Tớ có chuẩn bị một hộp quà siêu to khổng lồ ở Phòng Quà Tặng đó nha! 🎁🤩",
            "Này bé $childName ơi, tớ đang thèm tre non lắm, hãy vào Khu Vườn chơi với tớ đi! 🎋🐼",
            "Bạn AI thông minh của tớ vừa học thêm nét vẽ mới đấy, thử xem sao nhé! 🤖✨"
        )
    }

    var speechText by remember { mutableStateOf(initialPandaGreeting) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(childName, pandaName, savedDrawingsCount) {
        speechText = initialPandaGreeting
    }

    // --- Interactive Weather System (Chạm mây làm mưa bong bóng và hiện cầu vồng) ---
    var isRaining by remember { mutableStateOf(false) }
    var showRainbow by remember { mutableStateOf(false) }

    // --- Living Village & Animation Task States ---
    var pandaTaskState by remember { mutableStateOf("IDLE") } // "IDLE", "PLANT_DIGGING", "PLANT_WATERING", "PLANT_GROWING", "FOUNTAIN_ACTIVATE", "FOUNTAIN_SHOWING", "FERRIS_RIDING", "FERRIS_WAVING"
    var showFountainGuests by remember { mutableStateOf(false) }
    var isPandaOnFerrisWheel by remember { mutableStateOf(false) }
    var showDuckBabies by remember { mutableStateOf(false) }
    var isEnteringApp by remember { mutableStateOf(true) }

    // Click counters for Hidden Discoveries
    var treeTapCount by remember { mutableStateOf(0) }
    var cloudTapCount by remember { mutableStateOf(0) }
    var isCandyRainActive by remember { mutableStateOf(false) }
    var showUnicorn by remember { mutableStateOf(false) }
    var showSquirrel by remember { mutableStateOf(false) }

    // Bread Toss Animation states
    var isBreadFlying by remember { mutableStateOf(false) }
    var breadStartX by remember { mutableStateOf(0f) }
    var breadStartY by remember { mutableStateOf(0f) }
    var breadEndX by remember { mutableStateOf(0f) }
    var breadEndY by remember { mutableStateOf(0f) }
    var duckTapCount by remember { mutableStateOf(0) }

    val breadProgress by animateFloatAsState(
        targetValue = if (isBreadFlying) 1.0f else 0.0f,
        animationSpec = tween(1200, easing = LinearEasing),
        label = "BreadProgress",
        finishedListener = {
            if (it >= 0.99f) {
                isBreadFlying = false
                // Also trigger particles at duck destination!
                viewModel.spawnEmojiParticles(breadEndX * 2.5f, breadEndY * 2.5f, "❤️", count = 6)
            }
        }
    )

    // --- Parent Settings Dialog States ---
    var showSettingsDialog by remember { mutableStateOf(false) }

    // --- Interactive Visual Sound Effects (Toca Boca/Sago Mini-style visual sounds) ---
    var visualSounds by remember { mutableStateOf<List<VisualSound>>(emptyList()) }
    var soundIdCounter by remember { mutableStateOf(0L) }
    
    val triggerVisualSound = { text: String, emoji: String, x: Float, y: Float, color: Color ->
        val newId = soundIdCounter++
        visualSounds = visualSounds + VisualSound(newId, text, emoji, x, y, color)
    }

    // --- Village Map Configurations & Destinations ---
    // Proportional Coordinates for locations on our cute village map
    val destinations = remember {
        listOf(
            VillageDestination(
                id = "home_action",
                title = "Nhà Cozy",
                description = "Nhà tranh ấm cúng của Panda & Bé",
                emoji = "🏡",
                relativeX = 0.15f,
                relativeY = 0.22f,
                bgColor = Color(0xFFFFF9C4),
                borderColor = Color(0xFFFBC02D),
                isHomeAction = true
            ),
            VillageDestination(
                id = "studio",
                title = "Xưởng Vẽ",
                description = "Cọ màu phép thuật & hình dán",
                emoji = "🎨",
                relativeX = 0.48f,
                relativeY = 0.16f,
                bgColor = Color(0xFFFFEBEE),
                borderColor = Color(0xFFE57373)
            ),
            VillageDestination(
                id = "drawing_class",
                title = "Lớp Học Vẽ",
                description = "Tập vẽ từng bước ngộ nghĩnh",
                emoji = "🏫",
                relativeX = 0.80f,
                relativeY = 0.24f,
                bgColor = Color(0xFFE8EAF6),
                borderColor = Color(0xFF7986CB)
            ),
            VillageDestination(
                id = "ai_room",
                title = "Robot AI",
                description = "Gợi ý vẽ phác thảo từ ý tưởng",
                emoji = "🤖",
                relativeX = 0.18f,
                relativeY = 0.54f,
                bgColor = Color(0xFFE0F7FA),
                borderColor = Color(0xFF4DD0E1)
            ),
            VillageDestination(
                id = "garden",
                title = "Vườn Trúc",
                description = "Khu vườn trúc xanh ngát",
                emoji = "🎋",
                relativeX = 0.48f,
                relativeY = 0.48f,
                bgColor = Color(0xFFE8F5E9),
                borderColor = Color(0xFF81C784)
            ),
            VillageDestination(
                id = "badge_room",
                title = "Vinh Danh",
                description = "Khoe huy hiệu bé đạt được",
                emoji = "🏆",
                relativeX = 0.82f,
                relativeY = 0.58f,
                bgColor = Color(0xFFF3E5F5),
                borderColor = Color(0xFFBA68C8)
            ),
            VillageDestination(
                id = "museum",
                title = "Bảo Tàng",
                description = "Nơi trưng bày kiệt tác của bé",
                emoji = "🏛️",
                relativeX = 0.32f,
                relativeY = 0.82f,
                bgColor = Color(0xFFFFF8E1),
                borderColor = Color(0xFFFFD54F)
            ),
            VillageDestination(
                id = "gift_room",
                title = "Phòng Quà",
                description = "Mở hộp quà phép thuật",
                emoji = "🎁",
                relativeX = 0.66f,
                relativeY = 0.80f,
                bgColor = Color(0xFFEFEBE9),
                borderColor = Color(0xFFA1887F)
            )
        )
    }

    // --- Interactive Traveling Panda State ---
    var targetDestination by remember { mutableStateOf<VillageDestination?>(null) }
    var isPandaMoving by remember { mutableStateOf(false) }
    var currentRelX by remember { mutableStateOf(0.15f) }
    var currentRelY by remember { mutableStateOf(0.22f) }

    val animatedRelX by animateFloatAsState(
        targetValue = targetDestination?.relativeX ?: currentRelX,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        finishedListener = { finalX ->
            currentRelX = finalX
            targetDestination?.let { dest ->
                isPandaMoving = false
                if (dest.isHomeAction) {
                    showSettingsDialog = true
                } else {
                    viewModel.spawnEmojiParticles(400f, 300f, "✨", count = 15)
                    viewModel.spawnEmojiParticles(400f, 300f, "🌟", count = 10)
                    onRoomChange(dest.id)
                }
                targetDestination = null
            }
        },
        label = "PandaX"
    )

    val animatedRelY by animateFloatAsState(
        targetValue = targetDestination?.relativeY ?: currentRelY,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "PandaY"
    )

    // --- Global Map Infinite Animations ---
    val infiniteTransition = rememberInfiniteTransition(label = "VillageMapAnims")
    
    // 1. Drifting Clouds
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DriftingCloud1"
    )
    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 800f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DriftingCloud2"
    )

    // 2. Butterfly Fluttering (Sinuous wave path)
    val timeSeconds by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FlutterTime"
    )
    val butterflyX = 220f + Math.sin(timeSeconds.toDouble()).toFloat() * 140f
    val butterflyY = 180f + Math.cos(2 * timeSeconds.toDouble()).toFloat() * 60f

    // 3. Floating leaves drifting down gently
    val leafProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FallingLeaf"
    )
    val leafX = (leafProgress * 700f) % 600f
    val leafY = (leafProgress * 800f) % 900f

    // 4. Raindrops animation (when isRaining is active)
    val rainProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainDrop"
    )

    // 5. Panda Wobble & Jump scale when running
    val currentWobbleRotation = if (isPandaMoving) {
        Math.sin(timeSeconds.toDouble() * 12).toFloat() * 14f
    } else {
        0f
    }
    val currentWobbleScale = if (isPandaMoving) {
        1.0f + Math.abs(Math.sin(timeSeconds.toDouble() * 12)).toFloat() * 0.18f
    } else {
        1.0f
    }

    // --- Camera scrolling viewport factor with Signature Opening Entrance Zoom ---
    val density = LocalDensity.current
    val cameraScaleAnimate by animateFloatAsState(
        targetValue = if (isEnteringApp) 1.55f else 1.15f,
        animationSpec = tween(2200, easing = FastOutSlowInEasing),
        label = "CameraScale"
    )
    val cameraScale = cameraScaleAnimate
    val cameraOffsetX = -(animatedRelX - 0.5f) * 110f // Dynamic shift based on Panda X
    val cameraOffsetY = -(animatedRelY - 0.5f) * 110f // Dynamic shift based on Panda Y

    // --- Signature Opening Sequence on First Launch ---
    LaunchedEffect(Unit) {
        // Panda starts inside Cozy Cottage at (0.15f, 0.22f)
        delay(600)
        // Panda runs out to the cozy courtyard!
        isPandaMoving = true
        currentRelX = 0.24f
        currentRelY = 0.28f
        delay(1400)
        isPandaMoving = false
        // Zoom out the camera
        isEnteringApp = false
        speechText = "Hôm nay mình vẽ gì và chơi gì nào bé ơi! 🎨✨"
    }

    // --- Dynamic Background Gradient based on Time of Day (Chu kỳ sáng, chiều tà, tối thẫm) ---
    val mapSkyBackground = remember(isNight, isSunset) {
        when {
            isNight -> Brush.verticalGradient(
                listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
            )
            isSunset -> Brush.verticalGradient(
                listOf(Color(0xFFF97316), Color(0xFFF43F5E), Color(0xFFFEF08A))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFFF0FDF4))
            )
        }
    }

    // --- Main Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mapSkyBackground)
    ) {
        // --- Ambient Star Field for Night Mode (Mưa đom đóm lấp lánh ban đêm) ---
        if (isNight) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val numFireflies = 10
                for (i in 0 until numFireflies) {
                    val angleOffset = i * (2 * Math.PI / numFireflies)
                    val glowX = (size.width * 0.1f) + ((Math.sin(timeSeconds.toDouble() + angleOffset).toFloat() + 1f) / 2f) * (size.width * 0.8f)
                    val glowY = (size.height * 0.2f) + ((Math.cos(timeSeconds.toDouble() * 0.7 + angleOffset).toFloat() + 1f) / 2f) * (size.height * 0.6f)
                    val glowAlpha = 0.4f + Math.abs(Math.sin(timeSeconds.toDouble() * 2 + i)).toFloat() * 0.6f
                    drawCircle(
                        color = Color(0xFFA3E635),
                        radius = 6.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(glowX, glowY),
                        alpha = glowAlpha * 0.7f
                    )
                }
            }
        }

        // Camera-scrolled inner world container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = cameraScale
                    scaleY = cameraScale
                    translationX = with(density) { cameraOffsetX.dp.toPx() }
                    translationY = with(density) { cameraOffsetY.dp.toPx() }
                }
        ) {
            // 0. Dynamic Winding River background canvas (Flowing diagonally/vertically)
            Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { offset ->
                    triggerVisualSound("róc rách", "🌊", offset.x / 2.5f, offset.y / 2.5f, Color(0xFF29B6F6))
                }
            }) {
                val riverPath = Path()
                riverPath.moveTo(size.width * 0.38f, 0f)
                riverPath.quadraticTo(size.width * 0.44f, size.height * 0.30f, size.width * 0.35f, size.height * 0.60f)
                riverPath.quadraticTo(size.width * 0.41f, size.height * 0.85f, size.width * 0.37f, size.height * 1.10f)

                drawPath(
                    path = riverPath,
                    color = Color(0xFF90CAF9), // Soft sparkling river blue
                    style = Stroke(
                        width = 24.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                drawPath(
                    path = riverPath,
                    color = Color(0xFFE3F2FD), // white sparkles
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Dynamic map background canvas: Beautiful pathways connecting our buildings
            Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { offset ->
                    triggerVisualSound("sột soạt", "🌿", offset.x / 2.5f, offset.y / 2.5f, Color(0xFF81C784))
                }
            }) {
                val path = Path()
                // Start at Cozy Cottage
                path.moveTo(size.width * 0.15f, size.height * 0.22f)
                
                // Bezier curve to Art Studio
                path.quadraticTo(
                    size.width * 0.30f, size.height * 0.18f,
                    size.width * 0.48f, size.height * 0.16f
                )
                // Bezier curve to Art School
                path.quadraticTo(
                    size.width * 0.65f, size.height * 0.18f,
                    size.width * 0.80f, size.height * 0.24f
                )
                // Bezier curve to Badge Room
                path.quadraticTo(
                    size.width * 0.85f, size.height * 0.40f,
                    size.width * 0.82f, size.height * 0.58f
                )
                // Bezier curve to Gift Shop
                path.quadraticTo(
                    size.width * 0.75f, size.height * 0.70f,
                    size.width * 0.66f, size.height * 0.80f
                )
                // Bezier curve to Gallery
                path.quadraticTo(
                    size.width * 0.48f, size.height * 0.85f,
                    size.width * 0.32f, size.height * 0.82f
                )
                // Bezier curve to AI Creator Lab
                path.quadraticTo(
                    size.width * 0.15f, size.height * 0.70f,
                    size.width * 0.18f, size.height * 0.54f
                )
                // Bezier curve to Garden
                path.quadraticTo(
                    size.width * 0.30f, size.height * 0.50f,
                    size.width * 0.48f, size.height * 0.48f
                )

                // Draw the path outline shadow (sand color)
                drawPath(
                    path = path,
                    color = if (isNight) Color(0xFF334155) else Color(0xFFD7CCC8),
                    style = Stroke(
                        width = 18.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                // Draw the pathway itself
                drawPath(
                    path = path,
                    color = if (isNight) Color(0xFF475569) else Color(0xFFF5F5F5),
                    style = Stroke(
                        width = 12.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                // Center dashed line of the road
                drawPath(
                    path = path,
                    color = if (isNight) Color(0xFF94A3B8) else Color(0xFFFFB74D),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f),
                        cap = StrokeCap.Round
                    )
                )
            }

            // --- Celestial Bodies depending on Time of Day (Mặt trời mỉm cười hoặc Mặt trăng khuyết lấp lánh) ---
            if (isNight) {
                // Moon 🌙 with ambient glow animation
                val moonPulse by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "MoonGlow"
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 50.dp)
                        .scale(moonPulse)
                        .clickable {
                            triggerVisualSound("lấp lánh", "✨🌙", 280f, 100f, Color(0xFFFBC02D))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌙", fontSize = 48.sp)
                    Text("✨", fontSize = 16.sp, modifier = Modifier.offset(x = (-25).dp, y = (-15).dp))
                    Text("⭐", fontSize = 14.sp, modifier = Modifier.offset(x = 20.dp, y = 25.dp))
                }
            } else {
                // Sun ☀️ with dynamic smiling rays
                val sunWobble by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(25000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "SunRotation"
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 50.dp)
                        .rotate(sunWobble)
                        .clickable {
                            triggerVisualSound("ấm áp", "☀️💛", 280f, 100f, Color(0xFFFFB74D))
                        }
                ) {
                    Text("☀️", fontSize = 54.sp)
                }
            }

            // --- Drifting Animated Elements on the Background layer ---
            // Clouds - Interactive: Tap to trigger magical rain!
            Box(
                modifier = Modifier
                    .offset(x = cloudOffset1.dp, y = 30.dp)
                    .clickable {
                        triggerVisualSound("vút bay", "☁️✨", cloudOffset1 + 30f, 40f, Color(0xFF64B5F6))
                        cloudTapCount++
                        if (cloudTapCount >= 5) {
                            cloudTapCount = 0
                            if (!isCandyRainActive && !isRaining && !showRainbow) {
                                scope.launch {
                                    isCandyRainActive = true
                                    speechText = "Ôi sướng thế! Cơn mưa kẹo ngọt cầu vồng 🍬🍭 lấp lánh rơi đầy đầu tụi mình kìa bé yêu ơi! 🌈🥰"
                                    viewModel.spawnEmojiParticles(400f, 200f, "🍬", count = 15)
                                    viewModel.spawnEmojiParticles(400f, 200f, "🍭", count = 15)
                                    delay(7000)
                                    isCandyRainActive = false
                                    showRainbow = true
                                    speechText = "Kìa bé ơi! Cầu vồng bồng bềnh bảy sắc lấp lánh xuất hiện sau cơn mưa kìa! Tuyệt vời quá! 🌈✨"
                                    viewModel.spawnEmojiParticles(400f, 200f, "✨", count = 30)
                                    delay(8000)
                                    showRainbow = false
                                    speechText = initialPandaGreeting
                                }
                            }
                        } else {
                            if (!isRaining && !isCandyRainActive && !showRainbow) {
                                scope.launch {
                                    isRaining = true
                                    speechText = "Oa! Cơn mưa mát lành rơi tí tách rồi bé ơi! Tớ che ô xinh xắn che chở cho bé nhé! 🌧️☂️"
                                    viewModel.spawnEmojiParticles(400f, 200f, "💧", count = 25)
                                    delay(6000)
                                    isRaining = false
                                    showRainbow = true
                                    speechText = "Kìa bé ơi! Cầu vồng bồng bềnh bảy sắc lấp lánh xuất hiện sau cơn mưa kìa! Tuyệt vời quá! 🌈✨"
                                    viewModel.spawnEmojiParticles(400f, 200f, "✨", count = 30)
                                    delay(8000)
                                    showRainbow = false
                                    speechText = initialPandaGreeting
                                }
                            }
                        }
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("☁️", fontSize = 58.sp, modifier = Modifier.alpha(if (isNight) 0.4f else 0.85f))
                    Text("Chạm tớ! 👇", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isNight) Color.White.copy(alpha = 0.5f) else Color(0xFF388E3C))
                }
            }
            Text("☁️", fontSize = 42.sp, modifier = Modifier.offset(x = cloudOffset2.dp, y = 110.dp).alpha(if (isNight) 0.3f else 0.7f).clickable {
                triggerVisualSound("bồng bềnh", "☁️🎈", cloudOffset2 + 20f, 120f, Color(0xFF90CAF9))
            })

            // Rainbow 🌈 - Beautiful, sweeping arch on screen when showRainbow is active
            if (showRainbow) {
                val rainbowAlpha by animateFloatAsState(targetValue = 1.0f, animationSpec = tween(1000), label = "RainbowFade")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp)
                        .alpha(rainbowAlpha)
                        .clickable {
                            triggerVisualSound("diệu kỳ", "🌈✨", 180f, 150f, Color(0xFFFF4081))
                            if (!showUnicorn) {
                                scope.launch {
                                    showUnicorn = true
                                    speechText = "Aaa! Kỳ lân phép thuật bảy màu 🦄 lấp lánh vừa bay nhảy qua bầu trời kìa bé ơi! Đẹp lung linh chưa! ✨🌈"
                                    viewModel.spawnEmojiParticles(400f, 200f, "⭐", count = 12)
                                    delay(4000)
                                    speechText = initialPandaGreeting
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌈", fontSize = 120.sp, modifier = Modifier.scale(1.8f))
                    Text("✨ CẦU VỒNG PHÉP THUẬT ✨", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF6D00), modifier = Modifier.offset(y = 80.dp))
                }
            }

            // Raindrop overlays when isRaining is active
            if (isRaining) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val colCount = 8
                    val stepW = size.width / colCount
                    for (c in 0 until colCount) {
                        val dropY = ((rainProgress * size.height) + (c * 150f)) % size.height
                        val dropX = (c * stepW) + (rainProgress * 50f)
                        drawCircle(
                            color = Color(0xFF60A5FA),
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(dropX, dropY),
                            alpha = 0.6f
                        )
                    }
                }
            }

            // Candy Rain overlay when isCandyRainActive is active
            if (isCandyRainActive) {
                // Spawn beautiful falling candies instead of rain
                var candyRainProgress by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    ) { valVal, _ ->
                        candyRainProgress = valVal
                    }
                }
                
                // Render 8 columns of falling candies
                val candies = listOf("🍬", "🍭", "🍩", "🍪", "🍫", "🍬", "🍭", "🍩")
                candies.forEachIndexed { col, emoji ->
                    val candyY = ((candyRainProgress * 800f) + (col * 150f)) % 800f
                    val candyX = (col * (600f / 8)) + (candyY * 0.15f)
                    Text(
                        emoji,
                        fontSize = 22.sp,
                        modifier = Modifier.offset(x = candyX.dp, y = candyY.dp)
                    )
                }
            }

            // Unicorn leap when showUnicorn is active
            if (showUnicorn) {
                var unicornProgress by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = tween(3500, easing = LinearEasing)
                    ) { valVal, _ ->
                        unicornProgress = valVal
                    }
                }
                Text(
                    "🦄",
                    fontSize = 48.sp,
                    modifier = Modifier
                        .offset(
                            x = (-100).dp + (500.dp) * unicornProgress,
                            y = 120.dp + (Math.sin(unicornProgress * Math.PI).toFloat() * -80f).dp
                        )
                        .scale(if (unicornProgress > 0.5f) -1.2f else 1.2f, 1.2f)
                )
            }

            // Squirrel discovery when showSquirrel is active
            if (showSquirrel) {
                var squirrelOffset by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = tween(4000, easing = LinearOutSlowInEasing)
                    ) { valVal, _ ->
                        squirrelOffset = valVal
                    }
                }
                Text(
                    "🐿️",
                    fontSize = 34.sp,
                    modifier = Modifier
                        .offset(
                            x = 20.dp + (260.dp * squirrelOffset),
                            y = 350.dp + (Math.abs(Math.sin(squirrelOffset * 3 * Math.PI)).toFloat() * -40f).dp
                        )
                )
            }

            // Fluttering Butterfly
            Text("🦋", fontSize = 28.sp, modifier = Modifier.offset(x = butterflyX.dp, y = butterflyY.dp).clickable {
                triggerVisualSound("vút bay", "🦋✨", butterflyX, butterflyY, Color(0xFFF06292))
                viewModel.spawnEmojiParticles(butterflyX * 2.5f, butterflyY * 2.5f, "✨", count = 3)
            })

            // Falling leaves
            Text("🍃", fontSize = 18.sp, modifier = Modifier.offset(x = leafX.dp, y = leafY.dp).alpha(if (isNight) 0.4f else 0.7f).clickable {
                triggerVisualSound("rì rào", "🍃", leafX, leafY, Color(0xFF4CAF50))
            })
            Text("🌸", fontSize = 16.sp, modifier = Modifier.offset(x = (leafX + 250f).dp, y = (leafY + 150f).dp).alpha(if (isNight) 0.3f else 0.6f).clickable {
                triggerVisualSound("rơi nhẹ", "🌸", leafX + 250f, leafY + 150f, Color(0xFFFF8A80))
            })

            // Scenic Background Deco Trees & Flowers (Static details to enhance depth)
            Text("🌲", fontSize = 36.sp, modifier = Modifier.align(Alignment.TopStart).offset(x = 10.dp, y = 90.dp).alpha(if (isNight) 0.5f else 0.8f).clickable {
                triggerVisualSound("rì rào", "🌲🍃", 10f, 90f, Color(0xFF2E7D32))
            })
            Text("🌲", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-30).dp, y = 60.dp).alpha(if (isNight) 0.4f else 0.8f).clickable {
                triggerVisualSound("rì rào", "🌲🍃", 320f, 60f, Color(0xFF2E7D32))
            })
            Text("🎋", fontSize = 36.sp, modifier = Modifier.align(Alignment.CenterStart).offset(x = 30.dp, y = 0.dp).alpha(if (isNight) 0.5f else 0.8f).clickable {
                triggerVisualSound("rào rào", "🎋✨", 30f, 350f, Color(0xFF4CAF50))
            })
            Text("🌻", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 24.dp, y = (-20).dp).alpha(if (isNight) 0.5f else 1.0f).clickable {
                triggerVisualSound("chíp chíp", "🐦🌻", 24f, 680f, Color(0xFFFFB74D))
            })
            Text("🌻", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-24).dp, y = (-40).dp).alpha(if (isNight) 0.5f else 1.0f).clickable {
                triggerVisualSound("chíp chíp", "🐦🌻", 320f, 660f, Color(0xFFFFB74D))
            })

            // --- Interactive Map UI elements ---
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val mapW = maxWidth
                val mapH = maxHeight

                // --- PROGRESSION SYSTEM DECORATIONS (Vương Quốc phát triển theo số lượng tranh bé vẽ) ---
                
                // --- Living Village Interactive Task Definitions ---
                val plantTreeTask = {
                    if (!isPandaMoving && pandaTaskState == "IDLE" && savedDrawingsCount >= 1) {
                        scope.launch {
                            val tx = 0.05f
                            val ty = 0.35f
                            isPandaMoving = true
                            currentRelX = tx
                            currentRelY = ty
                            delay(1400)
                            isPandaMoving = false
                            
                            pandaTaskState = "PLANT_DIGGING"
                            speechText = "Đợi $pandaName một xíu nhé... Tớ đang dùng xẻng đào hố đất để trồng cây xanh nè! ⛏️🌱"
                            triggerVisualSound("bộp bộp", "⛏️🌱", tx * mapW.value, ty * mapH.value, Color(0xFF8D6E63))
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "🪨", count = 6)
                            delay(2200)
                            
                            pandaTaskState = "PLANT_WATERING"
                            speechText = "Giờ tưới nước mát lành cho mầm cây xanh lớn lên vù vù nào bé ơi! 🚿💦"
                            triggerVisualSound("xoẹt xoẹt", "🚿💦", tx * mapW.value, ty * mapH.value, Color(0xFF29B6F6))
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "💧", count = 12)
                            delay(2200)
                            
                            pandaTaskState = "PLANT_GROWING"
                            speechText = "Yee! Cây xanh cao to lấp lánh đã mọc lên rồi nè! Bé vẽ giỏi nên làng xanh lắm! 🌳✨"
                            triggerVisualSound("tèn ten", "🌳✨", tx * mapW.value, ty * mapH.value, Color(0xFF4CAF50))
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "🍃", count = 14)
                            delay(2500)
                            
                            pandaTaskState = "IDLE"
                            speechText = initialPandaGreeting
                        }
                    }
                }

                val activateFountainTask = {
                    if (!isPandaMoving && pandaTaskState == "IDLE" && savedDrawingsCount >= 5) {
                        scope.launch {
                            val tx = 0.50f
                            val ty = 0.70f
                            isPandaMoving = true
                            currentRelX = tx
                            currentRelY = ty
                            delay(1400)
                            isPandaMoving = false
                            
                            pandaTaskState = "FOUNTAIN_ACTIVATE"
                            speechText = "$pandaName đang vặn van khóa phép thuật để kích hoạt đài phun nước nha! 🔧⛲"
                            triggerVisualSound("két két", "🔧⛲", tx * mapW.value, ty * mapH.value, Color.Gray)
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "⚙️", count = 6)
                            delay(1800)
                            
                            pandaTaskState = "FOUNTAIN_SHOWING"
                            showFountainGuests = true
                            speechText = "Oa! Cậu nhìn xem kìa! Chim non 🐦 kéo đến uống nước, bướm hồng 🦋 đậu lại, và có cả cầu vồng tí hon nữa! ⛲🌈"
                            triggerVisualSound("ào ào", "⛲✨", tx * mapW.value, ty * mapH.value, Color(0xFF03A9F4))
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "💧", count = 18)
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "🫧", count = 10)
                            delay(5000)
                            
                            showFountainGuests = false
                            pandaTaskState = "IDLE"
                            speechText = initialPandaGreeting
                        }
                    }
                }

                val rideFerrisWheelTask = {
                    if (!isPandaMoving && pandaTaskState == "IDLE" && savedDrawingsCount >= 7) {
                        scope.launch {
                            val tx = 0.88f
                            val ty = 0.42f
                            isPandaMoving = true
                            currentRelX = tx
                            currentRelY = ty
                            delay(1400)
                            isPandaMoving = false
                            
                            pandaTaskState = "FERRIS_RIDING"
                            isPandaOnFerrisWheel = true
                            speechText = "Tớ leo lên cabin đu quay khổng lồ đây! Vù vù... quay vòng thích quá bé ơi! 🎡🐼👋"
                            triggerVisualSound("vù vù", "🎡✨", tx * mapW.value, ty * mapH.value, Color(0xFFFFB74D))
                            viewModel.spawnEmojiParticles(tx * mapW.value * 2.5f, ty * mapH.value * 2.5f, "⭐", count = 8)
                            delay(4000)
                            
                            pandaTaskState = "FERRIS_WAVING"
                            speechText = "Hú hu! $pandaName đang ở trên cao nhất nè! Thấy cả xưởng vẽ luôn! 👋🐼🎡"
                            delay(2500)
                            
                            isPandaOnFerrisWheel = false
                            pandaTaskState = "IDLE"
                            speechText = initialPandaGreeting
                        }
                    }
                }

                // 1. Extra Lush Forest Trees 🌳 (Enabled if savedDrawingsCount >= 1)
                if (savedDrawingsCount >= 1) {
                    Text(
                        "🌳",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .offset(x = mapW * 0.05f, y = mapH * 0.35f)
                            .clickable {
                                triggerVisualSound("rì rào", "🌳🍃", mapW.value * 0.05f, mapH.value * 0.35f, Color(0xFF4CAF50))
                                viewModel.spawnEmojiParticles(mapW.value * 0.05f * 2.5f, mapH.value * 0.35f * 2.5f, "🍃", count = 5)
                                treeTapCount++
                                if (treeTapCount >= 10) {
                                    treeTapCount = 0
                                    scope.launch {
                                        showSquirrel = true
                                        speechText = "Chít chít! Một chú sóc nhỏ đáng yêu 🐿️🥜 vừa nhảy ra từ trong tán lá cây kìa bé ơi!"
                                        viewModel.spawnEmojiParticles(mapW.value * 0.05f * 2.5f, mapH.value * 0.35f * 2.5f, "🥜", count = 8)
                                        delay(4500)
                                        speechText = initialPandaGreeting
                                    }
                                } else {
                                    plantTreeTask()
                                }
                            }
                    )
                    Text(
                        "🌳",
                        fontSize = 34.sp,
                        modifier = Modifier
                            .offset(x = mapW * 0.08f, y = mapH * 0.65f)
                            .clickable {
                                triggerVisualSound("rì rào", "🌳🍃", mapW.value * 0.08f, mapH.value * 0.65f, Color(0xFF4CAF50))
                                viewModel.spawnEmojiParticles(mapW.value * 0.08f * 2.5f, mapH.value * 0.65f * 2.5f, "🍃", count = 5)
                                treeTapCount++
                                if (treeTapCount >= 10) {
                                    treeTapCount = 0
                                    scope.launch {
                                        showSquirrel = true
                                        speechText = "Chít chít! Một chú sóc nhỏ đáng yêu 🐿️🥜 vừa nhảy ra từ trong tán lá cây kìa bé ơi!"
                                        viewModel.spawnEmojiParticles(mapW.value * 0.08f * 2.5f, mapH.value * 0.65f * 2.5f, "🥜", count = 8)
                                        delay(4500)
                                        speechText = initialPandaGreeting
                                    }
                                } else {
                                    plantTreeTask()
                                }
                            }
                    )
                }

                // 2. Blooming Flowers 🌷🌹 (Enabled if savedDrawingsCount >= 2)
                if (savedDrawingsCount >= 2) {
                    Text(
                        "🌷",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .offset(x = mapW * 0.26f, y = mapH * 0.28f)
                            .clickable {
                                triggerVisualSound("thơm ngát", "🌷🌸", mapW.value * 0.26f, mapH.value * 0.28f, Color(0xFFEC407A))
                                viewModel.spawnEmojiParticles(mapW.value * 0.26f * 2.5f, mapH.value * 0.28f * 2.5f, "✨", count = 4)
                            }
                    )
                    Text(
                        "🌹",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .offset(x = mapW * 0.58f, y = mapH * 0.12f)
                            .clickable {
                                triggerVisualSound("bừng nở", "🌹💖", mapW.value * 0.58f, mapH.value * 0.12f, Color(0xFFE91E63))
                                viewModel.spawnEmojiParticles(mapW.value * 0.58f * 2.5f, mapH.value * 0.12f * 2.5f, "💖", count = 4)
                            }
                    )
                }

                // 3. Stepping Stones 🪨 OR Wooden Bridge 🌉 (Stepping stones if <3, Wooden Bridge if >= 3)
                if (savedDrawingsCount < 3) {
                    // Simple stepping stones drawn on the river intersection
                    Row(
                        modifier = Modifier
                            .offset(x = mapW * 0.32f, y = mapH * 0.18f)
                            .clickable {
                                triggerVisualSound("bạch bạch", "🪨🐾", mapW.value * 0.34f, mapH.value * 0.19f, Color.Gray)
                            }
                    ) {
                        Text("🪨", fontSize = 16.sp)
                        Text("🪨", fontSize = 16.sp)
                    }
                } else {
                    // Beautiful wooden bridge constructed across the sparkling river!
                    Text(
                        "🌉",
                        fontSize = 42.sp,
                        modifier = Modifier
                            .offset(x = mapW * 0.32f, y = mapH * 0.17f)
                            .clickable {
                                triggerVisualSound("cộp cộp", "🌉🐾", mapW.value * 0.35f, mapH.value * 0.18f, Color(0xFF8D6E63))
                                viewModel.spawnEmojiParticles(mapW.value * 0.35f * 2.5f, mapH.value * 0.18f * 2.5f, "✨", count = 6)
                            }
                    )
                }

                // 4. Central Wishing Fountain ⛲ (Enabled if savedDrawingsCount >= 5)
                if (savedDrawingsCount >= 5) {
                    val fountainScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "FountainBounce"
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = mapW * 0.50f, y = mapH * 0.72f)
                            .scale(fountainScale)
                            .clickable {
                                triggerVisualSound("tủm tủm", "⛲💦", mapW.value * 0.54f, mapH.value * 0.74f, Color(0xFF29B6F6))
                                viewModel.spawnEmojiParticles(mapW.value * 0.54f * 2.5f, mapH.value * 0.74f * 2.5f, "💧", count = 8)
                                activateFountainTask()
                            }
                    ) {
                        Text("⛲", fontSize = 46.sp)
                        // Show fountain guests (bird, butterfly, tiny rainbow) if active!
                        if (showFountainGuests) {
                            Text("🐦", fontSize = 18.sp, modifier = Modifier.offset(x = (-16).dp, y = 20.dp))
                            Text("🦋", fontSize = 14.sp, modifier = Modifier.offset(x = 24.dp, y = (-12).dp))
                            Text("🌈", fontSize = 22.sp, modifier = Modifier.offset(x = 4.dp, y = (-26).dp))
                        }
                    }
                }

                // 5. Spinning Star Ferris Wheel 🎡 (Enabled if savedDrawingsCount >= 7)
                if (savedDrawingsCount >= 7) {
                    val ferrisWheelRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "FerrisSpin"
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = mapW * 0.88f, y = mapH * 0.42f)
                            .clickable {
                                triggerVisualSound("vù vù", "🎡✨", mapW.value * 0.92f, mapH.value * 0.45f, Color(0xFFFFB74D))
                                viewModel.spawnEmojiParticles(mapW.value * 0.92f * 2.5f, mapH.value * 0.45f * 2.5f, "🌟", count = 6)
                                rideFerrisWheelTask()
                            }
                    ) {
                        Text(
                            "🎡",
                            fontSize = 54.sp,
                            modifier = Modifier.rotate(ferrisWheelRotation)
                        )
                        // If riding, draw tiny waving panda face on the wheel!
                        if (isPandaOnFerrisWheel) {
                            val angleRad = (ferrisWheelRotation * Math.PI / 180).toFloat()
                            val riderX = 18.dp + (Math.cos(angleRad.toDouble()).toFloat() * 18).dp
                            val riderY = 18.dp + (Math.sin(angleRad.toDouble()).toFloat() * 18).dp
                            Text(
                                "🐼👋",
                                fontSize = 16.sp,
                                modifier = Modifier.offset(x = riderX, y = riderY)
                            )
                        }
                    }
                }

                // --- Swimming Duck in the Winding River ---
                val duckProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(18000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "SwimmingDuck"
                )
                val duckY = duckProgress
                val duckX = if (duckY < 0.6f) {
                    0.38f + (duckY / 0.6f) * 0.05f
                } else {
                    0.43f - ((duckY - 0.6f) / 0.4f) * 0.08f
                }
                
                // Render Mama Duck 🦆
                Box(
                    modifier = Modifier
                        .offset(x = mapW * duckX, y = mapH * duckY)
                        .clickable {
                            triggerVisualSound("cạp cạp", "🦆💧", mapW.value * duckX, mapH.value * duckY, Color(0xFFFFA726))
                            viewModel.spawnEmojiParticles(mapW.value * duckX * 2.5f, mapH.value * duckY * 2.5f, "💧", count = 5)
                            
                            // Tiny Story & Toss Bread!
                            duckTapCount++
                            if (duckTapCount == 1) {
                                scope.launch {
                                    showDuckBabies = true
                                    speechText = "Quạc quạc! 🦆 Ôi vui quá, đàn con bé bỏng đã tìm thấy mẹ rồi! Cảm ơn bé $childName nhiều nha! ❤️🐾"
                                    viewModel.spawnEmojiParticles(mapW.value * duckX * 2.5f, mapH.value * duckY * 2.5f, "💖", count = 8)
                                    delay(4000)
                                    speechText = initialPandaGreeting
                                }
                            } else {
                                // Bread Toss animation!
                                breadStartX = mapW.value * animatedRelX
                                breadStartY = mapH.value * animatedRelY
                                breadEndX = mapW.value * duckX
                                breadEndY = mapH.value * duckY
                                isBreadFlying = true
                            }
                        }
                ) {
                    Text("🦆", fontSize = 24.sp)
                }

                // Render Ducklings swimming behind Mama Duck
                if (showDuckBabies) {
                    for (i in 1..3) {
                        val lagProgress = (duckProgress - (i * 0.04f) + 1.0f) % 1.0f
                        val babyY = lagProgress
                        val babyX = if (babyY < 0.6f) {
                            0.38f + (babyY / 0.6f) * 0.05f
                        } else {
                            0.43f - ((babyY - 0.6f) / 0.4f) * 0.08f
                        }
                        Text(
                            "🦆",
                            fontSize = 13.sp, // tiny size for baby ducklings
                            modifier = Modifier
                                .offset(x = mapW * babyX, y = mapH * babyY)
                                .clickable {
                                    triggerVisualSound("bíp bíp", "🦆✨", mapW.value * babyX, mapH.value * babyY, Color(0xFFFFF176))
                                    viewModel.spawnEmojiParticles(mapW.value * babyX * 2.5f, mapH.value * babyY * 2.5f, "✨", count = 3)
                                }
                        )
                    }
                }

                // Floating flying bread element
                if (isBreadFlying) {
                    val currentBreadX = breadStartX + (breadEndX - breadStartX) * breadProgress
                    val currentBreadY = breadStartY + (breadEndY - breadStartY) * breadProgress - (Math.sin(breadProgress.toDouble() * Math.PI).toFloat() * 60f)
                    Text(
                        "🍞",
                        fontSize = 20.sp,
                        modifier = Modifier.offset(x = currentBreadX.dp, y = currentBreadY.dp)
                    )
                }

                // Draw all building nodes
                destinations.forEach { dest ->
                    val nodeX = mapW * dest.relativeX - 38.dp
                    val nodeY = mapH * dest.relativeY - 48.dp

                    // Night light overlay box around nodes to show glowing windows
                    val isGlowing = isNight && (dest.id == "home_action" || dest.id == "museum" || dest.id == "ai_room")

                    Box(modifier = Modifier.offset(x = nodeX, y = nodeY)) {
                        // Golden glowing ring for evening/night mode cozy feel
                        if (isGlowing) {
                            val glowPulse by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 0.9f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "GlowRing"
                            )
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .background(Color(0xFFFEF08A).copy(alpha = glowPulse * 0.4f), CircleShape)
                            )
                        }

                        // Map Node
                        MapBuildingNode(
                            destination = dest,
                            onClick = {
                                if (!isPandaMoving) {
                                    targetDestination = dest
                                    isPandaMoving = true
                                    
                                    // Say something cute when traveling
                                    speechText = if (dest.isHomeAction) {
                                        "Mình cùng đi xem bảng cài đặt vương quốc nhé! 🏡🐾"
                                    } else {
                                        "Đang chạy vội vàng tới ${dest.title}... 🐼💨"
                                    }
                                    
                                    // Trigger dynamic background particles on tap
                                    viewModel.spawnEmojiParticles(
                                        x = (dest.relativeX * mapW.value * 2),
                                        y = (dest.relativeY * mapH.value * 2),
                                        emoji = "✨",
                                        count = 5
                                    )
                                }
                            }
                        )

                        // --- Micro Interactions / Mini Assets on Nodes ---
                        if (dest.id == "ai_room") {
                            // Rotating Gear over Robot AI Lab
                            val gearRotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(4000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "GearTurn"
                            )
                            Text(
                                text = "⚙️",
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 5.dp, y = (-8).dp)
                                    .rotate(gearRotation)
                            )
                            // Blinking lights red/green
                            val isLedGreen = timeSeconds % 1.5f > 0.75f
                            Text(
                                text = if (isLedGreen) "🟢" else "🔴",
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (-4).dp, y = (-2).dp)
                            )
                        }

                        if (dest.id == "home_action" && isNight) {
                            // Warm fireplace smoke emoji puff
                            val smokePulse by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "ChimneySmoke"
                            )
                            Text(
                                text = "💨",
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(x = 10.dp, y = (-22).dp)
                                    .scale(smokePulse)
                                    .alpha(1.5f - smokePulse)
                            )
                        }
                    }
                }

                // --- The Adorable Traveling Panda Character ---
                val pandaX = mapW * animatedRelX - 35.dp
                val pandaY = mapH * animatedRelY - 65.dp

                // Determine appropriate emoji of Panda
                val currentPandaEmoji = when {
                    isPandaOnFerrisWheel -> "🎡"
                    pandaTaskState == "PLANT_DIGGING" -> "⛏️"
                    pandaTaskState == "PLANT_WATERING" -> "🚿"
                    pandaTaskState == "PLANT_GROWING" -> "🌱"
                    isPandaMoving -> "🐼💨" // Panda running fast
                    isRaining -> "🐼☂️" // Panda with pink/blue umbrella
                    else -> scheduleEmoji // Custom emoji depending on hour of day
                }

                // Fade out Panda if riding Ferris Wheel
                val pandaAlpha by animateFloatAsState(
                    targetValue = if (isPandaOnFerrisWheel) 0f else 1f,
                    animationSpec = tween(400),
                    label = "PandaAlpha"
                )

                if (pandaAlpha > 0.05f) {
                    Column(
                        modifier = Modifier
                            .offset(x = pandaX, y = pandaY)
                            .width(130.dp)
                            .alpha(pandaAlpha),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Speech bubble right above Panda's head
                        Box(
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(14.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPandaMoving) "Chờ tớ nhé! 🐾" else speechText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Cute Pointer Indicator
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(currentWobbleScale)
                                .rotate(currentWobbleRotation)
                                .background(Color.White, CircleShape)
                                .border(3.dp, Color(0xFF2E7D32), CircleShape)
                                .clickable {
                                    scope.launch {
                                        speechText = pandaSayings.random()
                                        viewModel.spawnEmojiParticles(400f, 300f, "⭐", count = 6)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentPandaEmoji, fontSize = 28.sp)
                        }
                    }
                }

                // --- On-screen Floating Visual Sound design overlays (Toca Boca-style) ---
                visualSounds.forEach { snd ->
                    key(snd.id) {
                        var yOffset by remember { mutableStateOf(0f) }
                        var alpha by remember { mutableStateOf(1f) }
                        LaunchedEffect(Unit) {
                            animate(
                                initialValue = 0f,
                                targetValue = -65f,
                                animationSpec = tween(1200, easing = LinearOutSlowInEasing)
                            ) { value, _ ->
                                yOffset = value
                                alpha = 1f - (value / -65f)
                            }
                            visualSounds = visualSounds.filter { it.id != snd.id }
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = snd.x.dp, y = (snd.y + yOffset).dp)
                                .alpha(alpha)
                                .background(snd.color, RoundedCornerShape(12.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(snd.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(snd.text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- Settings / Customize profile gear (Floating button on top) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wood-styled Toy Title Sign
            Box(
                modifier = Modifier
                    .background(Color(0xFF81C784), RoundedCornerShape(16.dp))
                    .border(3.dp, Color(0xFF2E7D32), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏡 ", fontSize = 18.sp)
                    Text(
                        text = "VƯƠNG QUỐC $pandaName".uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(" 🐼", fontSize = 18.sp)
                }
            }

            // Settings button
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFFFB74D), CircleShape)
                    .border(2.5.dp, Color(0xFFE65100), CircleShape)
                    .shadow(3.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt thông tin",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // --- Custom Settings Dialog (ToyPopup) ---
        if (showSettingsDialog) {
            var tempChildName by remember { mutableStateOf(childName) }
            var tempChildAge by remember { mutableStateOf(childAge) }
            var tempPandaName by remember { mutableStateOf(pandaName) }
            val customApiKeyFlow by viewModel.customGeminiApiKey.collectAsState()
            var tempCustomApiKey by remember { mutableStateOf(customApiKeyFlow) }
            var isKeyVisible by remember { mutableStateOf(false) }

            ToyPopup(
                onDismissRequest = { showSettingsDialog = false },
                title = "Cài Đặt Vương Quốc",
                emoji = "⚙️",
                borderColor = Color(0xFFFBC02D),
                confirmButton = {
                    BubbleButton(
                        onClick = {
                            viewModel.setChildName(tempChildName)
                            viewModel.setChildAge(tempChildAge)
                            viewModel.setPandaName(tempPandaName)
                            viewModel.setCustomGeminiApiKey(tempCustomApiKey)
                            showSettingsDialog = false
                        },
                        backgroundColor = Color(0xFF4CAF50),
                        borderColor = Color(0xFF2E7D32)
                    ) {
                        Text("Lưu Lại", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Hủy bỏ", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Text(
                    text = "Hãy nhập thông tin để cá nhân hóa vương quốc diệu kỳ của bé yêu nhé!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = tempChildName,
                    onValueChange = { tempChildName = it },
                    label = { Text("Biệt danh/Tên của bé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempChildAge,
                    onValueChange = { tempChildAge = it },
                    label = { Text("Tuổi của bé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempPandaName,
                    onValueChange = { tempPandaName = it },
                    label = { Text("Tên chú Gấu Trúc") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔑 Khóa bảo mật Gemini API (API Key) cá nhân:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00796B)
                    )
                    Text(
                        text = "Nhập mã API Key của ba mẹ để mở khóa tính năng vẽ phác thảo AI cho bé tự do tô màu. Khóa được lưu trữ an toàn trong thiết bị này.",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        lineHeight = 11.sp
                    )

                    OutlinedTextField(
                        value = tempCustomApiKey,
                        onValueChange = { tempCustomApiKey = it },
                        label = { Text("Mã API Key (Gemini AI)") },
                        placeholder = { Text("Nhập API Key để mở khóa Vẽ AI tự do...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Text(if (isKeyVisible) "👁️" else "🙈", fontSize = 14.sp)
                            }
                        }
                    )

                    val isKeyValid = tempCustomApiKey.trim().startsWith("AIzaSy") && tempCustomApiKey.trim().length > 10
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (tempCustomApiKey.isBlank()) "ℹ️ Ba mẹ chưa thiết lập API Key riêng."
                                   else if (isKeyValid) "✓ Mã API Key có định dạng hợp lệ."
                                   else "⚠️ Định dạng API Key có thể chưa chính xác (thường bắt đầu bằng AIzaSy).",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (tempCustomApiKey.isBlank()) Color.Gray
                                    else if (isKeyValid) Color(0xFF2E7D32)
                                    else Color(0xFFC62828)
                        )
                    }

                    Text(
                        text = "👉 Ba mẹ lấy mã API Key miễn phí tại: aistudio.google.com",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                }
            }
        }
    }
}

// --- Supporting data classes and components ---

data class VillageDestination(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val relativeX: Float,
    val relativeY: Float,
    val bgColor: Color,
    val borderColor: Color,
    val isHomeAction: Boolean = false
)

@Composable
fun MapBuildingNode(
    destination: VillageDestination,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1.0f, label = "NodePress")

    Column(
        modifier = modifier
            .scale(pressScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Background/Shadow pulse
        Box(
            modifier = Modifier
                .size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Node
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(destination.bgColor, CircleShape)
                    .border(3.dp, destination.borderColor, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(destination.emoji, fontSize = 32.sp)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Cute Wood/Paper signboard
        Box(
            modifier = Modifier
                .background(Color(0xFFFFFDF0), RoundedCornerShape(10.dp))
                .border(2.dp, destination.borderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .shadow(1.dp, RoundedCornerShape(10.dp))
        ) {
            Text(
                text = destination.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF5D4037)
            )
        }
    }
}

@Composable
fun RoomCard(
    title: String,
    description: String,
    emoji: String,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1.0f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(3.dp, borderColor, RoundedCornerShape(24.dp))
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF37474F),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = Color(0xFF546E7A),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun PandaAiRoom(
    onBack: () -> Unit,
    onGoToStudio: () -> Unit,
    viewModel: DrawingViewModel,
    aiPromptText: String,
    onAiPromptChange: (String) -> Unit,
    quickAiPrompts: List<String>,
    isGeneratingAi: Boolean,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val context = LocalContext.current
    var showGeminiKeyModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA)) // Soft futuristic cyan
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Text("🏡", fontSize = 18.sp)
                }

                Text(
                    text = "🤖 Phòng Thí Nghiệm AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006064)
                )

                IconButton(
                    onClick = { showGeminiKeyModal = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Text("⚙️", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Robot card speaking to kid
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF00ACC1), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🤖", fontSize = 48.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Robot Tranh Phép Thuật:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00838F)
                        )
                        Text(
                            text = "Bé hãy nói tớ vẽ con gì, tớ sẽ pha chế ngay một phác thảo cực đáng yêu cho bé tô màu nha! 🪄",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF006064)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text Input Field for AI Prompt
            OutlinedTextField(
                value = aiPromptText,
                onValueChange = onAiPromptChange,
                placeholder = {
                    Text(
                        "Ví dụ: Mèo con ăn cá lấp lánh, Khủng long bay...",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00ACC1),
                    unfocusedBorderColor = Color(0xFFB2EBF2)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Prompts row
            Text(
                text = "💡 Gợi ý nhanh cho bé:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00838F),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickAiPrompts.size) { index ->
                    val prompt = quickAiPrompts[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                        modifier = Modifier
                            .clickable {
                                onAiPromptChange(prompt.substringBeforeLast(" "))
                            }
                            .border(1.dp, Color(0xFF80CBC4), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = prompt,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00796B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action: Magic generate button or active loader
            if (isGeneratingAi) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00ACC1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🔮 Đang dùng phép thuật AI vẽ tranh...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF006064)
                        )
                        Text(
                            text = "Bé đợi một tẹo nha, Robot đang mài màu...",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (aiPromptText.isBlank()) {
                            Toast.makeText(context, "Bé ơi hãy điền gì đó vào ô nhập nha!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generateAiDrawing(aiPromptText, canvasWidth, canvasHeight)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Text("Phép Thuật AI ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // If template loaded, go to canvas button!
                Button(
                    onClick = onGoToStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Text("Vào Xưởng Tô Màu 🎨", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            if (showGeminiKeyModal) {
                SecureGeminiKeyModal(
                    onDismissRequest = { showGeminiKeyModal = false },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun SecureGeminiKeyModal(
    onDismissRequest: () -> Unit,
    viewModel: DrawingViewModel
) {
    val customApiKeyFlow by viewModel.customGeminiApiKey.collectAsState()
    var tempKey by remember { mutableStateOf(customApiKeyFlow) }
    var isKeyVisible by remember { mutableStateOf(false) }
    
    // Simple parent gate challenge state (e.g., "7 + 5 = ?") so child cannot access it
    var showParentGate by remember { mutableStateOf(true) }
    var gateInput by remember { mutableStateOf("") }
    var gateError by remember { mutableStateOf(false) }
    
    // Random math question
    val num1 by remember { mutableStateOf((3..9).random()) }
    val num2 by remember { mutableStateOf((3..9).random()) }
    val correctAnswer = num1 + num2

    if (showParentGate) {
        ToyPopup(
            onDismissRequest = onDismissRequest,
            title = "Khu Vực Cho Ba Mẹ 👨‍👩‍👧",
            emoji = "🔒",
            borderColor = Color(0xFF009688),
            confirmButton = {
                BubbleButton(
                    onClick = {
                        val ans = gateInput.toIntOrNull()
                        if (ans == correctAnswer) {
                            showParentGate = false
                        } else {
                            gateError = true
                        }
                    },
                    backgroundColor = Color(0xFF009688),
                    borderColor = Color(0xFF004D40)
                ) {
                    Text("Xác nhận", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Hủy bỏ", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Text(
                text = "Để tiếp tục, ba mẹ hãy trả lời phép tính này để xác minh bảo mật nhé:",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Text(
                text = "$num1 + $num2 = ?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00796B),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = gateInput,
                onValueChange = { gateInput = it },
                placeholder = { Text("Nhập kết quả...") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (gateError) {
                Text(
                    text = "⚠️ Phép tính chưa chính xác, ba mẹ hãy thử lại nhé!",
                    fontSize = 11.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        ToyPopup(
            onDismissRequest = onDismissRequest,
            title = "Cài Đặt Gemini API Key",
            emoji = "🔑",
            borderColor = Color(0xFF00ACC1),
            confirmButton = {
                BubbleButton(
                    onClick = {
                        viewModel.setCustomGeminiApiKey(tempKey)
                        onDismissRequest()
                    },
                    backgroundColor = Color(0xFF00ACC1),
                    borderColor = Color(0xFF006064)
                ) {
                    Text("Lưu Trữ An Toàn", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Đóng", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bảo Mật API Key của Bạn:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006064)
                )

                Text(
                    text = "Mã API Key được lưu trữ an toàn ngay trên thiết bị này và được sử dụng cục bộ để gửi yêu cầu vẽ tranh tới mô hình Google Gemini. Chúng tôi KHÔNG truyền mã này về bất kỳ máy chủ bên thứ ba nào.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )

                OutlinedTextField(
                    value = tempKey,
                    onValueChange = { tempKey = it },
                    label = { Text("Mã API Key (Gemini API)") },
                    placeholder = { Text("Dán mã API Key của bạn vào đây...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Text(if (isKeyVisible) "👁️" else "🙈", fontSize = 14.sp)
                        }
                    }
                )

                val isKeyValid = tempKey.trim().startsWith("AIzaSy") && tempKey.trim().length > 10
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (tempKey.isBlank()) "ℹ️ Ba mẹ chưa thiết lập API Key."
                               else if (isKeyValid) "✓ Mã API Key có định dạng hợp lệ."
                               else "⚠️ Định dạng API Key chưa chính xác (thường bắt đầu bằng AIzaSy).",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (tempKey.isBlank()) Color.Gray
                                else if (isKeyValid) Color(0xFF2E7D32)
                                else Color(0xFFC62828)
                    )
                }

                Divider()

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💡 Cách lấy mã API Key miễn phí:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006064)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "1. Truy cập trang web Google AI Studio: aistudio.google.com\n2. Đăng nhập bằng tài khoản Google của ba mẹ.\n3. Nhấp vào nút \"Get API Key\" và tạo một mã khóa mới.\n4. Sao chép và dán vào đây để mở khóa tính năng vẽ AI diệu kỳ cho bé!",
                            fontSize = 10.sp,
                            color = Color(0xFF006064),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PandaMuseum(
    onBack: () -> Unit,
    savedDrawings: List<SavedDrawing>,
    viewModel: DrawingViewModel,
    onLoad: () -> Unit,
    canvasWidth: Float,
    canvasHeight: Float,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3E2723), Color(0xFF1A0C00)) // Wooden wall colors
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Text("🏡", fontSize = 18.sp)
                }

                Text(
                    text = "🏛️ Viện Bảo Tàng Tranh",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD54F)
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Những bức tranh quý giá do chính tay bé vẽ được đóng khung lộng lẫy!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (savedDrawings.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Bảo tàng chưa có tranh nào rùi!\nBé hãy vẽ rồi cất vào album nha!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(savedDrawings) { drawing ->
                        MuseumFrame(
                            drawing = drawing,
                            viewModel = viewModel,
                            onLoad = onLoad,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MuseumFrame(
    drawing: SavedDrawing,
    viewModel: DrawingViewModel,
    onLoad: () -> Unit,
    canvasWidth: Float,
    canvasHeight: Float,
    context: Context
) {
    val dateStr = remember(drawing.timestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(drawing.timestamp))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5D4037)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated Gallery Spotlight above frame
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 12.dp)
                    .background(Color(0xFFFFD54F), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFD54F).copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )

            // High Quality Golden Picture Frame
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 240.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(8.dp, Color(0xFFD4AF37), RoundedCornerShape(8.dp)) // Gold border
                    .border(10.dp, Color(0xFFB8860B), RoundedCornerShape(8.dp)) // Outer dark gold border
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Generate a real preview from exporter!
                val bitmapPreview = remember(drawing) {
                    try {
                        DrawingExporter.exportToBitmap(
                            context = context,
                            strokes = drawing.strokes,
                            stickers = drawing.stickers,
                            template = drawing.template,
                            width = 300,
                            height = 300
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                if (bitmapPreview != null) {
                    Image(
                        bitmap = bitmapPreview.asImageBitmap(),
                        contentDescription = "Bức tranh của bé",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎨 Tranh Vẽ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wooden/Brass Plate for name info
            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD4AF37)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color(0xFF8B7500), RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = drawing.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A0C00),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Vẽ ngày: $dateStr",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Re-color button
                Button(
                    onClick = {
                        viewModel.loadDrawing(drawing)
                        onLoad()
                        Toast.makeText(context, "Đã khôi phục tranh lên bàn vẽ!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("Tô Màu 🎨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Download image
                Button(
                    onClick = {
                        try {
                            val finalWidth = if (canvasWidth > 0) canvasWidth.toInt() else 800
                            val finalHeight = if (canvasHeight > 0) canvasHeight.toInt() else 1000
                            val bitmap = DrawingExporter.exportToBitmap(
                                context = context,
                                strokes = drawing.strokes,
                                stickers = drawing.stickers,
                                template = drawing.template,
                                width = finalWidth,
                                height = finalHeight
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("Tải Về 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Delete
                Button(
                    onClick = {
                        viewModel.deleteSavedDrawing(drawing.id)
                        Toast.makeText(context, "Đã gỡ tranh khỏi bảo tàng", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("Gỡ Bỏ 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PandaBadgeRoom(
    onBack: () -> Unit,
    badges: List<KidsBadge>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF311B92), Color(0xFF1A237E)) // Starry deep purple galaxy wall
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Text("🏡", fontSize = 18.sp)
                }

                Text(
                    text = "🏆 Phòng Trưng Bày Huy Chương",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD54F)
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bộ sưu tập những thành tích cực xuất sắc của bé họa sĩ nhà ta!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB39DDB),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(badges) { badge ->
                    BadgeCard(badge = badge)
                }
            }
        }
    }
}

@Composable
fun BadgeCard(badge: KidsBadge) {
    val isUnlocked = badge.isUnlocked
    val borderBrush = if (isUnlocked) {
        Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF9100)))
    } else {
        Brush.linearGradient(listOf(Color.LightGray, Color.Gray))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF4527A0) else Color(0xFF212121).copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, borderBrush, RoundedCornerShape(20.dp))
            .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shiny circle badge emoji
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isUnlocked) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color.DarkGray,
                        CircleShape
                    )
                    .border(2.dp, if (isUnlocked) Color(0xFFFFD54F) else Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(badge.emoji, fontSize = 38.sp)
                } else {
                    Text("🔒", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUnlocked) Color(0xFFFFD54F) else Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badge.desc,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFFD1C4E9) else Color.Gray,
                    lineHeight = 14.sp
                )
            }

            if (isUnlocked) {
                Text("⭐", fontSize = 24.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun PandaGiftRoom(
    onBack: () -> Unit,
    viewModel: DrawingViewModel
) {
    val context = LocalContext.current
    var isOpened by remember { mutableStateOf(false) }
    var giftScale by remember { mutableStateOf(1.0f) }
    var rewardText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Shaking offset effect
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rewards = remember {
        listOf(
            "✨ Bé vừa trúng: Cọ Bút Cá Chép lấp lánh độc quyền! 🐟",
            "🎨 Bé vừa trúng: Màu nhũ vàng hoàng gia cực đỉnh! 🌟",
            "🧸 Bé vừa trúng: 10 hình dán Cổ tích công chúa phép thuật! 👸",
            "⚡ Bé vừa trúng: Hiệu ứng cầu vồng lấp lánh khi vẽ! 🌈",
            "🐾 Bé vừa trúng: Bút dạ vẽ dấu chân mèo đáng yêu cực kỳ! 🐾"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0)) // Warm cozy orange
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Text("🏡", fontSize = 18.sp)
                }

                Text(
                    text = "🎁 Hộp Quà May Mắn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE65100)
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Gõ vào hộp quà đang lắc lư để nhận quà siêu bất ngờ từ Panda Béo nha! 🎉",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD84315),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Shaking interactive gift box
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .rotate(if (isOpened) 0f else rotation)
                    .scale(giftScale)
                    .clickable {
                        if (!isOpened) {
                            scope.launch {
                                // Shaking explosion effect
                                giftScale = 1.3f
                                delay(120)
                                giftScale = 0.8f
                                delay(100)
                                giftScale = 1.0f
                                isOpened = true
                                rewardText = rewards.random()
                                
                                // Unlock daily badge / triggers celebration
                                viewModel.checkAndUnlockBadge("b5")
                                
                                // Spawn stars and fun emojis!
                                viewModel.spawnEmojiParticles(400f, 400f, "🎉", count = 8)
                                viewModel.spawnEmojiParticles(400f, 400f, "🎁", count = 8)
                                viewModel.spawnEmojiParticles(400f, 400f, "✨", count = 10)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isOpened) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔓🎁🎉", fontSize = 100.sp)
                    }
                } else {
                    Text("🎁", fontSize = 120.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (isOpened) {
                ToyCard(
                    borderColor = Color(0xFFFF9100),
                    shadowColor = Color(0xFFFFE0B2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CONG-RA-TỦ-LỆT! 🎉", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF6D00))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rewardText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BubbleButton(
                            onClick = {
                                isOpened = false
                                rewardText = ""
                            },
                            backgroundColor = Color(0xFFE65100),
                            borderColor = Color(0xFFB53D00)
                        ) {
                            Text("Chơi Lại Nữa 🎁", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "👇 Chạm Vào Hộp Quà 👇",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

@Composable
fun PandaGarden(
    onBack: () -> Unit,
    savedDrawingsCount: Int,
    viewModel: DrawingViewModel
) {
    val scope = rememberCoroutineScope()
    var pandaScale by remember { mutableStateOf(1.0f) }

    // Growth state logic based on drawing count
    val growthLevel = when {
        savedDrawingsCount <= 0 -> "Sơ Sinh 🍼"
        savedDrawingsCount in 1..2 -> "Mẫu Giáo 🎒"
        else -> "Họa Sĩ Đại Tài 🧑‍🎨"
    }

    val pandaEmoji = when {
        savedDrawingsCount <= 0 -> "🐼🍼"
        savedDrawingsCount in 1..2 -> "🐼🎒"
        else -> "🧑‍🎨🐼🎨"
    }

    val pandaSizeScale = when {
        savedDrawingsCount <= 0 -> 0.75f
        savedDrawingsCount in 1..2 -> 1.0f
        else -> 1.35f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF81C784), Color(0xFFC8E6C9)) // Beautiful outdoor green lawn
                )
            )
    ) {
        // Decorative background elements
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("☁️", fontSize = 52.sp, modifier = Modifier.padding(start = 30.dp).alpha(0.5f))
            Text("☀️", fontSize = 48.sp, modifier = Modifier.padding(end = 30.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Text("🏡", fontSize = 18.sp)
                }

                Text(
                    text = "🌳 Khu Vườn Panda Béo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B5E20)
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Panda Béo sẽ lớn lên khi bé vẽ nhiều tranh mới nha! Hãy chăm sóc chú ấy nhé! 🥰🎋",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Giant Panda Avatar growing based on count
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pandaScale * pandaSizeScale)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    .border(3.dp, Color(0xFF4CAF50), CircleShape)
                    .clickable {
                        scope.launch {
                            // Cute bounce effect
                            pandaScale = 1.2f
                            delay(120)
                            pandaScale = 0.9f
                            delay(100)
                            pandaScale = 1.0f
                            
                            viewModel.spawnEmojiParticles(400f, 400f, "🎋", count = 6)
                            viewModel.spawnEmojiParticles(400f, 400f, "🍃", count = 6)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(pandaEmoji, fontSize = 90.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status plate
            ToyCard(
                borderColor = Color(0xFF4CAF50),
                shadowColor = Color(0xFFC8E6C9),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cấp độ Panda: $growthLevel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Số tranh bé vẽ trong album: $savedDrawingsCount 🖼️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF558B2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action: Feed the Panda!
            BubbleButton(
                onClick = {
                    scope.launch {
                        pandaScale = 1.25f
                        delay(120)
                        pandaScale = 0.9f
                        delay(100)
                        pandaScale = 1.0f
                        
                        viewModel.spawnEmojiParticles(400f, 400f, "🎋", count = 8)
                        viewModel.spawnEmojiParticles(400f, 400f, "🍃", count = 8)
                        viewModel.setMascotMessage("Nhoàm nhoàm... Trúc vườn ngọt lịm ngọt lịm! Cảm ơn cậu đã chăm sóc tớ nhé! 🥰🎋🐼")
                    }
                },
                backgroundColor = Color(0xFF4CAF50),
                borderColor = Color(0xFF2E7D32),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
            ) {
                Text("Cho Panda Ăn Trúc 🎋", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// =====================================================================
// CLASSROOM ROOM: PERSONALIZED AGE-ADAPTIVE LEARNING (NHÓM 3)
// =====================================================================

@Composable
fun PandaDrawingClassRoom(
    onBack: () -> Unit,
    onGoToStudio: () -> Unit,
    viewModel: DrawingViewModel
) {
    val childName by viewModel.childName.collectAsState()
    val childAge by viewModel.childAge.collectAsState()
    val pandaName by viewModel.pandaName.collectAsState()
    val ageTier = viewModel.getAgeTier()

    val lessonStep by viewModel.lessonStep.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, Color(0xFF4CAF50), CircleShape)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF2E7D32)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🏫 LỚP HỌC VẼ CỦA ${pandaName.uppercase()}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
            }

            // Current Class badge
            Text(
                text = ageTier.levelName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp, horizontal = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Kid Greeting Card with appropriate advice
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ageTier.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(6.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Chào bé $childName ($childAge tuổi) nhé! 👋",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = ageTier.description,
                        fontSize = 10.5.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Class Workspace divided by Age Tier
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (ageTier) {
                DrawingViewModel.KidAgeTier.TODDLER -> {
                    ToddlerDrawingClass(
                        childName = childName,
                        pandaName = pandaName,
                        onGoToStudio = onGoToStudio,
                        viewModel = viewModel
                    )
                }
                DrawingViewModel.KidAgeTier.PRESCHOOL -> {
                    PreschoolDrawingClass(
                        childName = childName,
                        pandaName = pandaName,
                        onGoToStudio = onGoToStudio,
                        viewModel = viewModel
                    )
                }
                DrawingViewModel.KidAgeTier.ELEMENTARY -> {
                    ElementaryDrawingClass(
                        childName = childName,
                        pandaName = pandaName,
                        lessonStep = lessonStep,
                        selectedLesson = selectedLesson,
                        onGoToStudio = onGoToStudio,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ToddlerDrawingClass(
    childName: String,
    pandaName: String,
    onGoToStudio: () -> Unit,
    viewModel: DrawingViewModel
) {
    val scope = rememberCoroutineScope()
    var selectedToyIndex by remember { mutableStateOf(-1) }
    val colors = listOf(Color(0xFFFF5252), Color(0xFFFFD700), Color(0xFF2979FF), Color(0xFF4CAF50))
    val colorNames = listOf("ĐỎ 🔴", "VÀNG 🟡", "XANH DƯƠNG 🔵", "XANH LÁ 🟢")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "🎈 TRÒ CHƠI BẤM BÓNG PHÁT HIỆN MÀU SẮC 🎈",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE91E63)
        )
        Text(
            text = "Bé hãy bấm vào bóng bay để bùng nổ phép màu sắc nha!",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        // Fun color bubbles popping
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(color, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable {
                            scope.launch {
                                viewModel.spawnEmojiParticles(
                                    300f + (index * 100),
                                    350f,
                                    "✨",
                                    count = 10
                                )
                                viewModel.selectColor(color)
                                viewModel.setMascotMessage("Bùm! Bé vừa bấm bóng màu ${colorNames[index]} lấp lánh cực đẹp! 🌟🎈")
                                selectedToyIndex = index
                            }
                        }
                        .shadow(2.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (selectedToyIndex == index) "💥" else "🎈", fontSize = 24.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
            modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFFFFF176), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🍎 THỬ THÁCH VẼ ĐƠN GIẢN CHO BÉ 🍎",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
                )
                Text(
                    text = "Chúng mình cùng tô màu Bông Hoa khổng lồ với nét cọ vẽ to dễ vẽ nhé!",
                    fontSize = 10.5.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        viewModel.selectColor(Color(0xFFFF5252)) // Red default
                        viewModel.selectTemplate(ColoringTemplate.FLOWER)
                        viewModel.selectBrush(BrushType.PENCIL)
                        onGoToStudio()
                        viewModel.setMascotMessage("Yee! Bé $childName ơi, tớ đã chuẩn bị hình Bông Hoa siêu to và cọ vẽ mầm non siêu dễ thương cho bé rồi nè! 🌸🎨")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Tô Màu Bông Hoa Ngay 🌸", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PreschoolDrawingClass(
    childName: String,
    pandaName: String,
    onGoToStudio: () -> Unit,
    viewModel: DrawingViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "📐 TẬP VẼ HÌNH KHỐI THEO NÉT ĐỨT (TRACING) 📐",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF00897B)
        )
        Text(
            text = "Học vẽ nét cơ bản giúp tay bé khéo léo hơn mỗi ngày!",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Kitty Card
            Card(
                modifier = Modifier.weight(1f).clickable {
                    viewModel.selectTemplate(ColoringTemplate.KITTY)
                    onGoToStudio()
                    viewModel.setMascotMessage("Chào bé $childName! Mau theo các đường nét đứt để vẽ khuôn mặt mèo Kitty dễ thương nhé! 🐱🖋️")
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🐱", fontSize = 28.sp)
                    Text("Mèo Kitty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                    Text("Nét vẽ đứt quãng", fontSize = 8.5.sp, color = Color.Gray)
                }
            }

            // Car Card
            Card(
                modifier = Modifier.weight(1f).clickable {
                    viewModel.selectTemplate(ColoringTemplate.CAR)
                    onGoToStudio()
                    viewModel.setMascotMessage("Chào bé! Cùng vẽ ô tô chạy bon bon trên phố nào! Bé hãy tô và vẽ theo đường viền nha! 🚗⚡")
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🚗", fontSize = 28.sp)
                    Text("Xe Ô Tô", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                    Text("Hình khối chuyển động", fontSize = 8.5.sp, color = Color.Gray)
                }
            }

            // Star Card
            Card(
                modifier = Modifier.weight(1f).clickable {
                    viewModel.selectTemplate(ColoringTemplate.STAR)
                    onGoToStudio()
                    viewModel.setMascotMessage("Gấu trúc $pandaName đố bé vẽ được ngôi sao lấp lánh đó! Bé vẽ theo nét viền mờ nhé! ⭐✨")
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⭐", fontSize = 28.sp)
                    Text("Ngôi Sao", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    Text("Nét vẽ góc cạnh", fontSize = 8.5.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Encouragement banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0F7FA), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF4DD0E1), RoundedCornerShape(14.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🐼", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$pandaName gợi ý: Hãy dán thêm các sticker động vật ngộ nghĩnh lên góc tranh sau khi vẽ xong nha!",
                    fontSize = 10.sp,
                    color = Color(0xFF006064),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun ElementaryDrawingClass(
    childName: String,
    pandaName: String,
    lessonStep: Int,
    selectedLesson: String,
    onGoToStudio: () -> Unit,
    viewModel: DrawingViewModel
) {
    val lessons = listOf(
        Pair("panda", "Gấu Trúc Mập Mạp 🐼"),
        Pair("fish", "Cá Vàng Bơi Lội 🐟"),
        Pair("star", "Ngôi Sao Kỳ Diệu ⭐")
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🎨 LỚP HỌC VẼ TỪNG NÉT CHUYÊN NGHIỆP 🎨",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3F51B5)
        )

        // Lesson selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            lessons.forEach { (id, title) ->
                val isSelected = selectedLesson == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF3F51B5) else Color.White)
                        .border(1.dp, if (isSelected) Color.Transparent else Color.LightGray, RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectLesson(id) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.split(" ")[0] + " " + title.split(" ")[1],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Step Display Layout
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFF3F51B5).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "BÀI VẼ: ${lessons.firstOrNull { it.first == selectedLesson }?.second?.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF303F9F)
                )

                // Step dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (step in 1..4) {
                        val isCurrent = lessonStep == step
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 14.dp else 10.dp)
                                .background(if (isCurrent) Color(0xFF3F51B5) else Color.LightGray, CircleShape)
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                Text(text = "$step", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Interactive Tutorial Diagram (Dynamic Canvas Drawing!)
                Box(
                    modifier = Modifier
                        .size(160.dp, 120.dp)
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        
                        when (selectedLesson) {
                            "panda" -> {
                                // Step 1: Draw head
                                if (lessonStep >= 1) {
                                    drawCircle(
                                        color = Color.LightGray,
                                        radius = 35f,
                                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                    )
                                }
                                // Step 2: Add ears
                                if (lessonStep >= 2) {
                                    drawCircle(Color.Black, 10f, androidx.compose.ui.geometry.Offset(cx - 28f, cy - 28f))
                                    drawCircle(Color.Black, 10f, androidx.compose.ui.geometry.Offset(cx + 28f, cy - 28f))
                                }
                                // Step 3: Add eyes/nose
                                if (lessonStep >= 3) {
                                    drawCircle(Color.Black, 6f, androidx.compose.ui.geometry.Offset(cx - 10f, cy - 4f))
                                    drawCircle(Color.Black, 6f, androidx.compose.ui.geometry.Offset(cx + 10f, cy - 4f))
                                    drawCircle(Color.Black, 4f, androidx.compose.ui.geometry.Offset(cx, cy + 8f))
                                }
                                // Step 4: Cheeks / color
                                if (lessonStep >= 4) {
                                    drawCircle(Color(0xFFFF8A80), 6f, androidx.compose.ui.geometry.Offset(cx - 20f, cy + 10f))
                                    drawCircle(Color(0xFFFF8A80), 6f, androidx.compose.ui.geometry.Offset(cx + 20f, cy + 10f))
                                }
                            }
                            "fish" -> {
                                // Step 1: Body oval
                                if (lessonStep >= 1) {
                                    drawOval(
                                        color = Color.LightGray,
                                        topLeft = androidx.compose.ui.geometry.Offset(cx - 45f, cy - 25f),
                                        size = androidx.compose.ui.geometry.Size(90f, 50f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                    )
                                }
                                // Step 2: Tail
                                if (lessonStep >= 2) {
                                    val tailPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(cx + 40f, cy)
                                        lineTo(cx + 65f, cy - 25f)
                                        lineTo(cx + 65f, cy + 25f)
                                        close()
                                    }
                                    drawPath(tailPath, Color(0xFFFFB74D))
                                }
                                // Step 3: Fins & eyes
                                if (lessonStep >= 3) {
                                    drawCircle(Color.Black, 4f, androidx.compose.ui.geometry.Offset(cx - 25f, cy - 6f))
                                    drawCircle(Color(0xFF29B6F6), 6f, androidx.compose.ui.geometry.Offset(cx - 50f, cy - 20f))
                                }
                                // Step 4: Rainbow scale sparkles
                                if (lessonStep >= 4) {
                                    drawCircle(Color(0xFFE040FB), 4f, androidx.compose.ui.geometry.Offset(cx, cy))
                                    drawCircle(Color(0xFF00E676), 4f, androidx.compose.ui.geometry.Offset(cx + 15f, cy + 5f))
                                }
                            }
                            else -> { // Star
                                // Step 1: Pentagram skeleton
                                if (lessonStep >= 1) {
                                    drawCircle(
                                        color = Color.LightGray,
                                        radius = 35f,
                                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                    )
                                }
                                // Step 2: Star vertices
                                if (lessonStep >= 2) {
                                    val starPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(cx, cy - 40f)
                                        lineTo(cx + 12f, cy - 10f)
                                        lineTo(cx + 40f, cy - 10f)
                                        lineTo(cx + 16f, cy + 8f)
                                        lineTo(cx + 26f, cy + 38f)
                                        lineTo(cx, cy + 18f)
                                        lineTo(cx - 26f, cy + 38f)
                                        lineTo(cx - 16f, cy + 8f)
                                        lineTo(cx - 40f, cy - 10f)
                                        lineTo(cx - 12f, cy - 10f)
                                        close()
                                    }
                                    drawPath(starPath, Color(0xFFFFF176), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                                }
                                // Step 3: Glow / fill
                                if (lessonStep >= 3) {
                                    val starPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(cx, cy - 40f)
                                        lineTo(cx + 12f, cy - 10f)
                                        lineTo(cx + 40f, cy - 10f)
                                        lineTo(cx + 16f, cy + 8f)
                                        lineTo(cx + 26f, cy + 38f)
                                        lineTo(cx, cy + 18f)
                                        lineTo(cx - 26f, cy + 38f)
                                        lineTo(cx - 16f, cy + 8f)
                                        lineTo(cx - 40f, cy - 10f)
                                        lineTo(cx - 12f, cy - 10f)
                                        close()
                                    }
                                    drawPath(starPath, Color(0xFFFFF59D))
                                }
                                // Step 4: Sparkles
                                if (lessonStep >= 4) {
                                    drawCircle(Color(0xFFFFD54F), 6f, androidx.compose.ui.geometry.Offset(cx - 50f, cy - 30f))
                                    drawCircle(Color(0xFFFFD54F), 6f, androidx.compose.ui.geometry.Offset(cx + 50f, cy + 30f))
                                }
                            }
                        }
                    }
                }

                // Step instructions
                val stepText = when (selectedLesson) {
                    "panda" -> when (lessonStep) {
                        1 -> "Bước 1: Vẽ một hình tròn to ở giữa làm đầu gấu trúc béo nhé! ⚪"
                        2 -> "Bước 2: Vẽ thêm 2 tai tròn đen láy ở trên đầu nha! 🖤"
                        3 -> "Bước 3: Vẽ đôi mắt hột mít múp míp đen láy & chiếc mũi xinh xắn nhé! 👀"
                        else -> "Bước 4: Hãy dán má hồng lấp lánh bằng cọ Vẽ Kim Tuyến đón xuân thôi! ✨"
                    }
                    "fish" -> when (lessonStep) {
                        1 -> "Bước 1: Vẽ một hình bầu dục nằm ngang làm cơ thể chú cá vàng nha! 🐟"
                        2 -> "Bước 2: Vẽ một chiếc đuôi cá xinh xắn hình tam giác phía sau nhé! 🎏"
                        3 -> "Bước 3: Thêm mắt to bự tròn đen và chiếc vây cá vẫy vẫy! 👀"
                        else -> "Bước 4: Hãy dùng cọ vẽ CẦU VỒNG 🌈 vẽ làn vảy cá óng ánh phép thuật nha!"
                    }
                    else -> when (lessonStep) {
                        1 -> "Bước 1: Vẽ khung tròn mờ định vị để ngôi sao cân đối nhất nha! ⚪"
                        2 -> "Bước 2: Vẽ 5 cánh nhọn của ngôi sao may mắn theo hướng dẫn mờ nhé! ⭐"
                        3 -> "Bước 3: Tô màu vàng óng đầy đặn ngập tràn cho ngôi sao bừng sáng!"
                        else -> "Bước 4: Hãy dùng cọ KIM TUYẾN ✨ vẽ thêm các vệt sáng lấp lánh xung quanh nha!"
                    }
                }

                Text(
                    text = stepText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                // Navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.prevLessonStep() },
                        enabled = lessonStep > 1
                    ) {
                        Text("↩️ Quay Lại", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (lessonStep < 4) {
                        Button(
                            onClick = { viewModel.nextLessonStep(4) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                        ) {
                            Text("Tiếp Tục ↪️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.selectTemplate(ColoringTemplate.BLANK)
                                if (selectedLesson == "panda") {
                                    viewModel.selectBrush(BrushType.GLITTER)
                                } else if (selectedLesson == "fish") {
                                    viewModel.selectBrush(BrushType.RAINBOW)
                                } else {
                                    viewModel.selectBrush(BrushType.GLITTER)
                                }
                                onGoToStudio()
                                viewModel.setMascotMessage("Bé $childName ơi, hãy vẽ hình ${lessons.firstOrNull { it.first == selectedLesson }?.second} chúng mình vừa học lên tấm bảng trắng phép thuật này nhé! 🎨🌟")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Vào Xưởng Thực Hành 🎨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
