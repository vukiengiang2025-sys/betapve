package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun TabletCreativeStudio(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel
) {
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
    val mascotMessage by viewModel.mascotMessage.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()
    val isAiFreeFormAllowed by viewModel.isAiFreeFormAllowed.collectAsState()

    // Local workspace states
    var selectedColorCategory by remember { mutableStateOf(0) } // 0: Cơ bản, 1: Pastel, 2: Neon, 3: Kim loại
    var selectedStickerCategory by remember { mutableStateOf(0) } // 0: Động vật, 1: Xe cộ, 2: Cổ tích, 3: Đồ ăn, 4: Đồ chơi
    var aiPromptText by remember { mutableStateOf("") }
    var isBucketActive by remember { mutableStateOf(false) }

    // Canvas sizes measured dynamically inside layout
    var canvasWidth by remember { mutableStateOf(900f) }
    var canvasHeight by remember { mutableStateOf(800f) }

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

    val quickAiPrompts = listOf(
        "Khủng long ăn kem dâu 🦖🍦",
        "Mèo con bay khinh khí cầu 🐱🎈",
        "Công chúa cưỡi kỳ lân bay trên mây 👸🦄☁️",
        "Tàu vũ trụ bay thám hiểm sao Hỏa 🚀🪐",
        "Cún con đi tắm hồ bơi 🐶🌊"
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =====================================================================
        // 🐼 LEFT SIDEBAR: Mascot, stickers and templates
        // =====================================================================
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mascot
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.setMascotMessage("Hihi, tớ là Panda Béo! Thiết kế tablet siêu to vẽ đã tay luôn nha! 🐼🎈")
                        viewModel.spawnEmojiParticles(150f, 150f, "🐼", count = 5)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🐼", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Mách nhỏ:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text(
                            text = mascotMessage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1B5E20),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Magical Sticker dock
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🧸 Hình Dán Diệu Kỳ", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6A1B9A))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val stickerCats = listOf("🐱 Động Vật", "🚀 Xe Cộ", "👸 Cổ Tích", "🍎 Đồ Ăn", "🎈 Đồ Chơi")
                        stickerCats.forEachIndexed { idx, catTitle ->
                            val isSel = selectedStickerCategory == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFFE1BEE7) else Color(0xFFF1F1F1))
                                    .border(1.dp, if (isSel) Color(0xFF8E24AA) else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { selectedStickerCategory = idx }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Text(catTitle, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF4A148C) else Color.Black)
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(activeStickersList) { sticker ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(Color(0xFFF9F9F9), CircleShape)
                                    .border(1.dp, Color.LightGray, CircleShape)
                                    .clickable { viewModel.addSticker(sticker) }
                                    .shadow(1.dp, CircleShape)
                            ) {
                                Text(sticker, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            // AI Outline or pre-defined outlines
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🤖 Tranh Vẽ AI & Vẽ Mẫu", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

                    if (!isAiFreeFormAllowed) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🛡️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hãy chọn các gợi ý vẽ ở dưới nha! 👇", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                            }
                        }
                    } else {
                        // AI input bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = aiPromptText,
                                onValueChange = { aiPromptText = it },
                                placeholder = { Text("Con muốn vẽ gì...", fontSize = 9.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
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
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Vẽ AI", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    // Prompt suggestion horizontal row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickAiPrompts) { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8EAF6))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                    .clickable {
                                        aiPromptText = prompt.substringBeforeLast(" ")
                                        viewModel.generateAiDrawing(aiPromptText, canvasWidth, canvasHeight)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(prompt, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3F51B5))
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 2.dp))

                    // Templates List
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ColoringTemplate.values()) { template ->
                            val isSel = selectedTemplate == template
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Color(0xFFE1F5FE) else Color(0xFFF9F9F9))
                                    .border(1.dp, if (isSel) Color(0xFF2979FF) else Color.LightGray, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectTemplate(template) }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(template.icon, fontSize = 14.sp)
                                    Text(template.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF0D47A1) else Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 🖼️ CENTER COMPONENT: Massive creative sketchbook
        // =====================================================================
        Column(
            modifier = Modifier
                .weight(0.51f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
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
                        .border(5.dp, Color(0xFFFFD54F), RoundedCornerShape(28.dp)) // Bold beautiful yellow border
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Drawing paint canvas
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
                            // Predefined Coloring template outline path
                            TemplateDrawer.drawTemplate(selectedTemplate, this)

                            // Render drawn strokes
                            strokes.forEach { stroke ->
                                if (stroke.points.size > 1) {
                                    val brushTypeStr = stroke.brushType
                                    if (brushTypeStr == "PENCIL" || brushTypeStr == "MARKER" || stroke.isEraser) {
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
                                        // Custom vector sprinkle brushes
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

                                        var lastDrawnX = -999f
                                        var lastDrawnY = -999f
                                        stroke.points.forEach { pt ->
                                            val dist = if (lastDrawnX == -999f) 999f else Math.hypot((pt.x - lastDrawnX).toDouble(), (pt.y - lastDrawnY).toDouble()).toFloat()
                                            if (dist >= 45f) {
                                                lastDrawnX = pt.x
                                                lastDrawnY = pt.y
                                                when (brushTypeStr) {
                                                    "GLITTER" -> drawGlitterBrushLocal(this, pt.x, pt.y, stroke.color)
                                                    "HEART" -> drawHeartLocal(this, pt.x, pt.y, stroke.color)
                                                    "FLOWER" -> drawFlowerBrushLocal(this, pt.x, pt.y, stroke.color)
                                                    "BUBBLE" -> drawBubbleBrushLocal(this, pt.x, pt.y)
                                                    "LEAF" -> drawLeafBrushLocal(this, pt.x, pt.y, stroke.color)
                                                    "SNOW" -> drawSnowBrushLocal(this, pt.x, pt.y)
                                                    "LIGHTNING" -> drawLightningBrushLocal(this, pt.x, pt.y)
                                                    "FOOTPRINT" -> drawFootprintBrushLocal(this, pt.x, pt.y, stroke.color)
                                                    "FISH" -> drawFishBrushLocal(this, pt.x, pt.y, stroke.color)
                                                    "CLOUD" -> drawCloudBrushLocal(this, pt.x, pt.y)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Stickers
                        stickers.forEach { sticker ->
                            val isSelected = sticker.id == selectedStickerId
                            val infiniteTransition = rememberInfiniteTransition(label = "tablet_sticker_anim_${sticker.id}")
                            val breatheScale by infiniteTransition.animateFloat(
                                initialValue = 0.94f,
                                targetValue = 1.06f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1600 + (sticker.id.hashCode() % 400).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            val floatAngle by infiniteTransition.animateFloat(
                                initialValue = -6f,
                                targetValue = 6f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1900 + (sticker.id.hashCode() % 500).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "angle"
                            )
                            val floatY by infiniteTransition.animateFloat(
                                initialValue = -5f,
                                targetValue = 5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2200 + (sticker.id.hashCode() % 600).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "y_offset"
                            )
                            val jitterOffset by infiniteTransition.animateFloat(
                                initialValue = -1f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(110 + (sticker.id.hashCode() % 40).absoluteValue, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "jitter"
                            )

                            val flyingEmojis = listOf("✈️", "🚀", "🛸", "🚁", "🦋", "🐝", "🎈", "🧚‍♀️", "🐦", "✨", "🌟", "☁️")
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
                                    .offset { IntOffset((sticker.x + animX).toInt() - 50, (sticker.y + animY).toInt() - 50) }
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
                                                viewModel.updateStickerPosition(sticker.id, dragAmount.x, dragAmount.y)
                                            }
                                        )
                                    }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, Color(0xFFFF4081), RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                            ) {
                                Text(sticker.emoji, fontSize = 56.sp)

                                if (isSelected) {
                                    // Adorable scale and action modifiers
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .offset(y = (-32).dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color(0xFFE3F2FD), CircleShape)
                                                .border(1.dp, Color(0xFF2196F3), CircleShape)
                                                .clickable { viewModel.updateStickerScaleAndRotation(sticker.id, 1.2f, 0f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("➕", fontSize = 9.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color(0xFFE3F2FD), CircleShape)
                                                .border(1.dp, Color(0xFF2196F3), CircleShape)
                                                .clickable { viewModel.updateStickerScaleAndRotation(sticker.id, 0.8f, 0f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("➖", fontSize = 9.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color(0xFFEDE7F6), CircleShape)
                                                .border(1.dp, Color(0xFF673AB7), CircleShape)
                                                .clickable { viewModel.updateStickerScaleAndRotation(sticker.id, 1.0f, 25f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🔄", fontSize = 9.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color(0xFFFFEBEE), CircleShape)
                                                .border(1.dp, Color(0xFFF44336), CircleShape)
                                                .clickable { viewModel.deleteSticker(sticker.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("❌", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Mascot particles floating
                        particles.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(p.x.toInt() - 8, p.y.toInt() - 8) }
                                    .size(16.dp)
                            ) {
                                Text(
                                    text = p.emoji ?: "",
                                    fontSize = (10 + (p.alpha * 10)).sp,
                                    modifier = Modifier.alpha(p.alpha)
                                )
                            }
                        }

                        // Translucent loader overlay when AI generator is cooking
                        if (isGeneratingAi) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🐼", fontSize = 72.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    CircularProgressIndicator(color = Color(0xFFFF4081))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Panda Béo đang dán tranh AI xịn xò...\nBé đợi tớ một xíu nha! ✨", textAlign = TextAlign.Center, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 🎨 RIGHT SIDEBAR: Creative palettes & brushes always visible
        // =====================================================================
        Column(
            modifier = Modifier
                .weight(0.24f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Colors list card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎨 Bảng Màu", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00796B))
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBucketActive) Color(0xFFFF8A80) else Color(0xFFE0F2F1))
                                .clickable {
                                    isBucketActive = !isBucketActive
                                    viewModel.setMascotMessage(
                                        if (isBucketActive) "Bé bật Xô Màu 🪣! Click vào vùng vẽ để đổ màu nhanh nha!"
                                        else "Trở về cọ vẽ thông thường thôi bé ơi! ✏?"
                                    )
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            Text("🪣 Xô Màu", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (isBucketActive) Color.White else Color(0xFF004D40))
                        }
                    }

                    // Color Categories row selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val colorCats = listOf("Cơ Bản", "Pastel", "Neon", "Kim Loại")
                        colorCats.forEachIndexed { idx, title ->
                            val isSel = selectedColorCategory == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFFFFF176) else Color(0xFFF1F1F1))
                                    .clickable { selectedColorCategory = idx }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(title, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val colorsList = when (selectedColorCategory) {
                        0 -> standardColors
                        1 -> pastelColors
                        2 -> neonColors
                        else -> metallicColors
                    }

                    // Grid view of colors
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(colorsList) { color ->
                            val isSel = brushColor == color && !isEraser && !isRainbow
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
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
                            )
                        }

                        // Dynamic Eraser item in the same color block grid
                        item {
                            val isSel = isEraser
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(Color.White, CircleShape)
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) Color(0xFFFF4081) else Color.LightGray,
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

            // Brushes list card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .weight(0.75f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🖌️ Nét Vẽ Diệu Kỳ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(BrushType.values()) { brush ->
                            val isSel = selectedBrush == brush && !isEraser
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) Color(0xFFE1F5FE) else Color(0xFFF5F5F5)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectBrush(brush) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(brush.icon, fontSize = 16.sp)
                                    Column {
                                        Text(brush.title, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                        Text(brush.description.take(15) + "..", fontSize = 7.5.sp, color = Color.Gray)
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

// =====================================================================
// VECTOR SHAPES SPRINKLER BRUSHES FOR TABLET MODE
// =====================================================================

private fun drawGlitterBrushLocal(scope: DrawScope, x: Float, y: Float, color: Color) {
    scope.drawCircle(color.copy(alpha = 0.8f), 4f, Offset(x, y))
    val starColor = Color(0xFFFFEB3B)
    val starPath = Path().apply {
        moveTo(x, y - 8f)
        lineTo(x + 2f, y - 2f)
        lineTo(x + 8f, y)
        lineTo(x + 2f, y + 2f)
        moveTo(x, y + 8f)
        lineTo(x - 2f, y + 2f)
        lineTo(x - 8f, y)
        lineTo(x - 2f, y - 2f)
        close()
    }
    scope.drawPath(starPath, starColor)
}

private fun drawHeartLocal(scope: DrawScope, x: Float, y: Float, color: Color) {
    val heartPath = Path().apply {
        moveTo(x, y - 4f)
        cubicTo(x - 10f, y - 15f, x - 18f, y - 5f, x, y + 12f)
        cubicTo(x + 18f, y - 5f, x + 10f, y - 15f, x, y - 4f)
    }
    scope.drawPath(heartPath, color)
}

private fun drawFlowerBrushLocal(scope: DrawScope, x: Float, y: Float, color: Color) {
    val r = 5f
    scope.drawCircle(color, r, Offset(x - 7f, y - 4f))
    scope.drawCircle(color, r, Offset(x + 7f, y - 4f))
    scope.drawCircle(color, r, Offset(x - 7f, y + 4f))
    scope.drawCircle(color, r, Offset(x + 7f, y + 4f))
    scope.drawCircle(color, r, Offset(x, y - 8f))
    scope.drawCircle(Color(0xFFFFD54F), r * 0.8f, Offset(x, y)) // Yellow center
}

private fun drawBubbleBrushLocal(scope: DrawScope, x: Float, y: Float) {
    scope.drawCircle(Color(0xFF80DEEA).copy(alpha = 0.35f), 12f, Offset(x, y)) // Bubble globe translucent fill
    scope.drawCircle(Color.White.copy(alpha = 0.8f), 3f, Offset(x - 4f, y - 4f)) // Specular reflection
    scope.drawCircle(Color(0xFFE0F7FA), 12f, Offset(x, y), style = Stroke(width = 1.2f)) // Soft outline bubble
}

private fun drawLeafBrushLocal(scope: DrawScope, x: Float, y: Float, color: Color) {
    val leafColor = if (color == Color.Black || color == Color.White) Color(0xFF4CAF50) else color
    val path = Path().apply {
        moveTo(x, y - 10f)
        quadraticTo(x - 8f, y, x, y + 10f)
        quadraticTo(x + 8f, y, x, y - 10f)
    }
    scope.drawPath(path, leafColor)
    scope.drawLine(Color(0xFF1B5E20), Offset(x, y - 10f), Offset(x, y + 10f), strokeWidth = 1f)
}

private fun drawSnowBrushLocal(scope: DrawScope, x: Float, y: Float) {
    val c = Color(0xFFB3E5FC)
    scope.drawLine(c, Offset(x - 8f, y), Offset(x + 8f, y), strokeWidth = 1.8f)
    scope.drawLine(c, Offset(x, y - 8f), Offset(x, y + 8f), strokeWidth = 1.8f)
    scope.drawLine(c, Offset(x - 6f, y - 6f), Offset(x + 6f, y + 6f), strokeWidth = 1.2f)
    scope.drawLine(c, Offset(x + 6f, y - 6f), Offset(x - 6f, y + 6f), strokeWidth = 1.2f)
}

private fun drawLightningBrushLocal(scope: DrawScope, x: Float, y: Float) {
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

private fun drawFootprintBrushLocal(scope: DrawScope, x: Float, y: Float, color: Color) {
    val printColor = if (color == Color.White) Color.DarkGray else color.copy(alpha = 0.85f)
    scope.drawCircle(printColor, 6f, Offset(x, y + 3f))
    scope.drawCircle(printColor, 2.5f, Offset(x - 6f, y - 3f))
    scope.drawCircle(printColor, 2.8f, Offset(x, y - 6f))
    scope.drawCircle(printColor, 2.5f, Offset(x + 6f, y - 3f))
}

private fun drawFishBrushLocal(scope: DrawScope, x: Float, y: Float, color: Color) {
    val fishColor = if (color == Color.Black || color == Color.White) Color(0xFFFF7043) else color
    val body = Path().apply {
        moveTo(x - 10f, y)
        quadraticTo(x, y - 6f, x + 6f, y)
        quadraticTo(x, y + 6f, x - 10f, y)
    }
    scope.drawPath(body, fishColor)
    val tail = Path().apply {
        moveTo(x - 10f, y)
        lineTo(x - 15f, y - 5f)
        lineTo(x - 13f, y)
        lineTo(x - 15f, y + 5f)
        close()
    }
    scope.drawPath(tail, fishColor)
}

private fun drawCloudBrushLocal(scope: DrawScope, x: Float, y: Float) {
    val color = Color(0xFFECEFF1)
    scope.drawCircle(color, 6f, Offset(x - 5f, y))
    scope.drawCircle(color, 8f, Offset(x, y - 2f))
    scope.drawCircle(color, 6f, Offset(x + 5f, y))
    scope.drawCircle(color, 5f, Offset(x, y + 3f))
}
