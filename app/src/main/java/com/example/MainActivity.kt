package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.*
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
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val savedDrawings by viewModel.savedDrawings.collectAsState()

    // Dialog & UI toggle states
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showGalleryDrawer by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0: Màu sắc (Colors), 1: Nhãn dán (Stickers), 2: Tô màu (Templates)

    // Running particle animation loop
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateParticles()
            delay(16) // ~60fps smooth physics tick
        }
    }

    // List of standard kid-friendly vibrant colors
    val colorPalette = listOf(
        Color(0xFFFF5252), // Cherry Red
        Color(0xFFFF4081), // Pink Candy
        Color(0xFFFF9100), // Orange Sun
        Color(0xFFFFD700), // Lemon Yellow
        Color(0xFF69F0AE), // Frog Green
        Color(0xFF4CAF50), // Forest Green
        Color(0xFF40C4FF), // Sky Blue
        Color(0xFF2979FF), // Marine Blue
        Color(0xFFB2FF59), // Lime Green
        Color(0xFFE040FB), // Magic Purple
        Color(0xFF8D6E63), // Teddy Brown
        Color(0xFF37474F), // Midnight Black
    )

    // List of adorable emoji stickers
    val emojiStickers = listOf(
        "🧸", "🐱", "🐶", "🦄", "🌈", "🍭", "🎈", "🎂", "🚀", "🦖", "🦁", "🐼", "🍓", "🍦", "🍩", "🎨", "✈️", "🚗", "🚂"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF0)) // Soft cream background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- TOP BAR: Title & Actions ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Playful App Title
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bé Tập Vẽ ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4081)
                        )
                        Text(
                            text = "Tô Màu 🎨",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2979FF)
                        )
                    }
                    Text(
                        text = when (selectedTemplate) {
                            ColoringTemplate.BLANK -> "Trang vẽ tự do của bé"
                            else -> "Đang tô màu: ${selectedTemplate.title}"
                        },
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Header buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                            .testTag("undo_button")
                    ) {
                        Text("↩️", fontSize = 18.sp)
                    }

                    // Redo Button
                    IconButton(
                        onClick = { viewModel.redo() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                            .testTag("redo_button")
                    ) {
                        Text("↪️", fontSize = 18.sp)
                    }

                    // Clear Canvas Button
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .shadow(2.dp, CircleShape)
                            .testTag("clear_button")
                    ) {
                        Text("🧹", fontSize = 18.sp)
                    }

                    // Save Masterpiece Button
                    IconButton(
                        onClick = {
                            saveTitleInput = ""
                            showSaveDialog = true
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE8F5E9), CircleShape)
                            .shadow(2.dp, CircleShape)
                            .testTag("save_button")
                    ) {
                        Text("💾", fontSize = 18.sp)
                    }

                    // Open Gallery Drawer Button
                    IconButton(
                        onClick = { showGalleryDrawer = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE3F2FD), CircleShape)
                            .shadow(2.dp, CircleShape)
                            .testTag("gallery_button")
                    ) {
                        Text("🖼️", fontSize = 18.sp)
                    }
                }
            }

            // --- WORKSPACE: Drawing Sketchbook Canvas ---
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .border(4.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)) // Yellow playful frame border
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. Drawing Canvas Paint Area
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            viewModel.startNewStroke(offset.x, offset.y)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            viewModel.updateCurrentStroke(
                                                change.position.x,
                                                change.position.y
                                            )
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        // Tap empty canvas space to deselect sticker and spawn cute sparkles
                                        viewModel.selectSticker(null)
                                        viewModel.spawnParticles(offset.x, offset.y, Color(0xFFFFD54F), count = 6)
                                    }
                                }
                        ) {
                            // Draw Coloring Outlines Template
                            TemplateDrawer.drawTemplate(selectedTemplate, this)

                            // Draw All Strokes
                            strokes.forEach { stroke ->
                                if (stroke.points.size > 1) {
                                    val path = Path()
                                    path.moveTo(stroke.points[0].x, stroke.points[0].y)
                                    for (i in 1 until stroke.points.size) {
                                        path.lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }

                                    if (stroke.isEraser) {
                                        drawPath(
                                            path = path,
                                            color = Color.White,
                                            style = Stroke(
                                                width = stroke.width,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    } else if (stroke.isRainbow) {
                                        // Draw rainbow segmented strokes with transitioning hue
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
                                        drawPath(
                                            path = path,
                                            color = stroke.color,
                                            style = Stroke(
                                                width = stroke.width,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                } else if (stroke.points.size == 1) {
                                    val pt = stroke.points[0]
                                    val color = if (stroke.isEraser) {
                                        Color.White
                                    } else if (stroke.isRainbow) {
                                        Color.Red
                                    } else {
                                        stroke.color
                                    }
                                    drawCircle(color, stroke.width / 2f, Offset(pt.x, pt.y))
                                }
                            }
                        }

                        // 2. Stickers Overlaid on top of Drawing Canvas
                        stickers.forEach { sticker ->
                            val isSelected = sticker.id == selectedStickerId
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(sticker.x.toInt() - 50, sticker.y.toInt() - 50) }
                                    .graphicsLayer(
                                        scaleX = sticker.scale,
                                        scaleY = sticker.scale,
                                        rotationZ = sticker.rotation
                                    )
                                    .pointerInput(sticker.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                viewModel.selectSticker(sticker.id)
                                            },
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
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = sticker.emoji,
                                    fontSize = 42.sp,
                                    modifier = Modifier.clickable {
                                        viewModel.selectSticker(sticker.id)
                                    }
                                )

                                if (isSelected) {
                                    // Remove Sticker Button (Top-Left ❌)
                                    IconButton(
                                        onClick = { viewModel.deleteSticker(sticker.id) },
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .offset(x = (-24).dp, y = (-24).dp)
                                            .size(28.dp)
                                            .background(Color.White, CircleShape)
                                            .border(1.5.dp, Color(0xFFFF4081), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xóa sticker",
                                            tint = Color(0xFFFF4081),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Quick Touch Handles for Sizing & Rotating on Bottom-Right (Kid Friendly!)
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 24.dp, y = 24.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "➖",
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .clickable {
                                                    viewModel.updateStickerScaleAndRotation(
                                                        sticker.id,
                                                        0.8f,
                                                        0f
                                                    )
                                                }
                                                .padding(4.dp)
                                        )
                                        Text(
                                            text = "➕",
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .clickable {
                                                    viewModel.updateStickerScaleAndRotation(
                                                        sticker.id,
                                                        1.2f,
                                                        0f
                                                    )
                                                }
                                                .padding(4.dp)
                                        )
                                        Text(
                                            text = "🔄",
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .clickable {
                                                    viewModel.updateStickerScaleAndRotation(
                                                        sticker.id,
                                                        1.0f,
                                                        15f
                                                    )
                                                }
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Starry Sparkles Canvas Overlay (GPU-accelerated native drawings)
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .transparentReceiver()
                        ) {
                            particles.forEach { particle ->
                                drawCircle(
                                    color = particle.color.copy(alpha = particle.alpha),
                                    radius = particle.size * particle.alpha,
                                    center = Offset(particle.x, particle.y)
                                )
                            }
                        }
                    }
                }
            }

            // --- BOTTOM SECTION: Tabbed Kid Control Station ---
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Navigation tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabButton(
                            title = "Màu sắc 🎨",
                            isSelected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            selectedColor = Color(0xFFFFD54F),
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            title = "Nhãn dán 🧸",
                            isSelected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            selectedColor = Color(0xFFEA80FC),
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            title = "Tô màu 🌸",
                            isSelected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            selectedColor = Color(0xFF80D8FF),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Tab contents
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tab_content_animation"
                    ) { tab ->
                        when (tab) {
                            0 -> {
                                // --- TAB 0: Rich Crayons & Brush Width ---
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Row 1: Horizontal Crayons
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Standard Colors
                                        items(colorPalette) { color ->
                                            val isSelectedColor = brushColor == color && !isEraser && !isRainbow
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isSelectedColor) 52.dp else 44.dp)
                                                    .background(color, CircleShape)
                                                    .border(
                                                        width = if (isSelectedColor) 4.dp else 1.dp,
                                                        color = if (isSelectedColor) Color(0xFF37474F) else Color.LightGray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        viewModel.selectColor(color)
                                                        viewModel.spawnParticles(
                                                            x = 200f,
                                                            y = 200f,
                                                            color = color,
                                                            count = 4
                                                        )
                                                    }
                                                    .shadow(if (isSelectedColor) 4.dp else 1.dp, CircleShape)
                                            )
                                        }

                                        // Rainbow Magic Pen Button
                                        item {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(if (isRainbow) 52.dp else 44.dp)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                Color.Red,
                                                                Color.Yellow,
                                                                Color.Green,
                                                                Color.Blue,
                                                                Color.Magenta
                                                            )
                                                        ),
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = if (isRainbow) 4.dp else 1.dp,
                                                        color = if (isRainbow) Color(0xFF37474F) else Color.LightGray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.selectRainbowMode() }
                                            ) {
                                                Text("🌈", fontSize = 20.sp)
                                            }
                                        }

                                        // Eraser Button
                                        item {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(if (isEraser) 52.dp else 44.dp)
                                                    .background(Color.White, CircleShape)
                                                    .border(
                                                        width = if (isEraser) 4.dp else 1.dp,
                                                        color = if (isEraser) Color(0xFFFF4081) else Color.LightGray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.selectEraserMode() }
                                            ) {
                                                Text("🧽", fontSize = 20.sp)
                                            }
                                        }
                                    }

                                    // Row 2: Brush size selector
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "Cỡ cọ:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )

                                        // 4 Preset easy dot selections for kids
                                        val sizes = listOf(
                                            8f to "Sọc nhỏ",
                                            16f to "Vừa nè",
                                            26f to "To bự",
                                            42f to "Siêu to"
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceAround,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            sizes.forEach { (widthValue, label) ->
                                                val isSelectedSize = brushWidth == widthValue
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { viewModel.setBrushWidth(widthValue) }
                                                        .background(
                                                            if (isSelectedSize) Color(0xFFFFE082) else Color.Transparent
                                                        )
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size((widthValue / 1.5f).coerceIn(6f, 22f).dp)
                                                            .background(Color(0xFF37474F), CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = label,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // --- TAB 1: Lovely Stickers ---
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Bấm để thêm nhãn dán lấp lánh lên bức tranh:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        items(emojiStickers) { sticker ->
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(Color(0xFFF3E5F5), CircleShape)
                                                    .border(2.dp, Color(0xFFCE93D8), CircleShape)
                                                    .clickable {
                                                        // Spawn sticker at estimated canvas center coords
                                                        viewModel.addSticker(sticker)
                                                    }
                                                    .shadow(2.dp, CircleShape)
                                            ) {
                                                Text(sticker, fontSize = 28.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // --- TAB 2: Coloring Page Template Outlines ---
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Chọn trang vẽ để bé tô màu nha:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        items(ColoringTemplate.values()) { template ->
                                            val isSelected = selectedTemplate == template
                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFFE1F5FE) else Color(0xFFF9F9F9)
                                                ),
                                                modifier = Modifier
                                                    .width(110.dp)
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFF2979FF) else Color.LightGray,
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { viewModel.selectTemplate(template) }
                                                    .shadow(1.dp, RoundedCornerShape(16.dp))
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(template.icon, fontSize = 26.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = template.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center,
                                                        color = if (isSelected) Color(0xFF0D47A1) else Color.DarkGray
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
        }

        // --- DIALOG 1: Clean/Reset Canvas Dialog ---
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

        // --- DIALOG 2: Save Masterpiece Artwork ---
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
                            viewModel.saveToGallery(saveTitleInput)
                            showSaveDialog = false
                            Toast.makeText(context, "🎉 Đã lưu tranh vào Bộ sưu tập rồi!", Toast.LENGTH_LONG).show()
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

        // --- BOTTOM SHEET DIALOG / OVERLAY DRAWER: Saved Gallery ---
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
                                text = "Bộ Sưu Tập 🖼️",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                            IconButton(onClick = { showGalleryDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Đóng")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        if (savedDrawings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bé chưa có tranh lưu nào hết trơn. Vẽ xong bé bấm nút Lưu nha! 🌟",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(1),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(savedDrawings) { drawing ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.loadDrawing(drawing)
                                                showGalleryDrawer = false
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Đã tải bức tranh: ${drawing.title}",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = drawing.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = Color.Black
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Trang nền: ${drawing.template.icon}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteSavedDrawing(drawing.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Xóa tranh",
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(18.dp)
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
    }
}

// --- Auxiliary Compose Helpers ---

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(if (isSelected) 3.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) selectedColor else Color(0xFFF5F5F5))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF37474F) else Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF37474F) else Color.DarkGray
        )
    }
}

// Custom modifier helper to allow drawings clicks underneath transparent canvas overlay elements
fun Modifier.transparentReceiver(): Modifier = this.then(
    Modifier.pointerInput(Unit) {} // lightweight pass-through pointer capture
)
