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

    var speechText by remember { mutableStateOf("Chào mừng bé $childName đã tới với Thế Giới Hoạt Hình của $pandaName! 🏡🐼🌟") }
    var pandaScale by remember { mutableStateOf(1.0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(childName, pandaName) {
        speechText = "Chào mừng bé $childName đã tới với Thế Giới Hoạt Hình của $pandaName! 🏡🐼🌟"
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF9C4), Color(0xFFE8F5E9))
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
            Text("☁️", fontSize = 48.sp, modifier = Modifier.padding(start = 24.dp).alpha(0.6f))
            Text("🎈", fontSize = 36.sp, modifier = Modifier.padding(end = 40.dp).alpha(0.6f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Application logo, Title & Settings Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏡 ", fontSize = 28.sp)
                    Text(
                        text = "VƯƠNG QUỐC $pandaName".uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32),
                        letterSpacing = 1.sp
                    )
                    Text(" 🐼", fontSize = 28.sp)
                }

                var showSettingsDialog by remember { mutableStateOf(false) }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, Color(0xFF81C784), CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt thông tin",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (showSettingsDialog) {
                    var tempChildName by remember { mutableStateOf(childName) }
                    var tempChildAge by remember { mutableStateOf(childAge) }
                    var tempPandaName by remember { mutableStateOf(pandaName) }

                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚙️ ", fontSize = 24.sp)
                                Text("Cài Đặt Thông Tin Bé", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Nhập thông tin để cá nhân hóa thế giới vẽ tranh của bé nhé!",
                                    fontSize = 12.sp,
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
                                    label = { Text("Tên gọi chú Gấu Trúc") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.setChildName(tempChildName)
                                    viewModel.setChildAge(tempChildAge)
                                    viewModel.setPandaName(tempPandaName)
                                    showSettingsDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Lưu Lại", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("Hủy", color = Color.Gray)
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Text(
                text = "Thế giới hoạt hình diệu kỳ dành cho bé yêu và gia đình",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF558B2F),
                modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 12.dp)
            )

            if (isLandscape) {
                // Adaptive side-by-side design for widescreen / landscape / tablet
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Interactive speaking Panda box
                    Box(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                                .border(3.dp, Color(0xFF81C784), RoundedCornerShape(24.dp))
                                .padding(12.dp)
                                .clickable {
                                    scope.launch {
                                        pandaScale = 1.2f
                                        delay(120)
                                        pandaScale = 1.0f
                                        speechText = pandaSayings.random()
                                        viewModel.spawnEmojiParticles(400f, 300f, "🌟", count = 6)
                                        viewModel.spawnEmojiParticles(400f, 300f, "✨", count = 6)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .scale(pandaScale)
                                    .background(Color(0xFFE8F5E9), CircleShape)
                                    .border(2.dp, Color(0xFF4CAF50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🐼", fontSize = 42.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "$pandaName trò chuyện 💬",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF388E3C)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = speechText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 16.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Right Column: Room grid selection cards
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight()
                    ) {
                        item {
                            RoomCard(
                                title = "Xưởng Vẽ Mỹ Thuật",
                                description = "Cọ màu phép thuật & hình dán",
                                emoji = "🎨",
                                backgroundColor = Color(0xFFFFEBEE),
                                borderColor = Color(0xFFE57373),
                                onClick = { onRoomChange("studio") }
                            )
                        }
                        item {
                            RoomCard(
                                title = "Phòng Robot AI",
                                description = "Gợi ý nét vẽ phác thảo kỳ diệu",
                                emoji = "🤖",
                                backgroundColor = Color(0xFFE0F7FA),
                                borderColor = Color(0xFF4DD0E1),
                                onClick = { onRoomChange("ai_room") }
                            )
                        }
                        item {
                            RoomCard(
                                title = "Viện Bảo Tàng Tranh",
                                description = "Trưng bày tranh vẽ lộng lẫy",
                                emoji = "🏛️",
                                backgroundColor = Color(0xFFFFF8E1),
                                borderColor = Color(0xFFFFD54F),
                                onClick = { onRoomChange("museum") }
                            )
                        }
                        item {
                            RoomCard(
                                title = "Bức Tường Vinh Danh",
                                description = "Treo 5 huy hiệu lấp lánh bé đạt",
                                emoji = "🏆",
                                backgroundColor = Color(0xFFF3E5F5),
                                borderColor = Color(0xFFBA68C8),
                                onClick = { onRoomChange("badge_room") }
                            )
                        }
                        item {
                            RoomCard(
                                title = "Hộp Quà May Mắn",
                                description = "Mở hộp quà nhận cọ vẽ siêu hiếm",
                                emoji = "🎁",
                                backgroundColor = Color(0xFFEFEBE9),
                                borderColor = Color(0xFFA1887F),
                                onClick = { onRoomChange("gift_room") }
                            )
                        }
                        item {
                            RoomCard(
                                title = "Khu Vườn Panda",
                                description = "Cho Panda ăn trúc để lớn khôn",
                                emoji = "🌳",
                                backgroundColor = Color(0xFFE8F5E9),
                                borderColor = Color(0xFF81C784),
                                onClick = { onRoomChange("garden") }
                            )
                        }
                    }
                }
            } else {
                // Portrait Layout (Original vertical arrangement)
                // Large interactive Panda Béo speaking box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                        .border(3.dp, Color(0xFF81C784), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                        .clickable {
                            scope.launch {
                                // Wobble animation
                                pandaScale = 1.2f
                                delay(120)
                                pandaScale = 0.9f
                                delay(100)
                                pandaScale = 1.0f

                                // Random message
                                speechText = pandaSayings.random()

                                // Spawn beautiful sparkles
                                viewModel.spawnEmojiParticles(400f, 300f, "🌟", count = 6)
                                viewModel.spawnEmojiParticles(400f, 300f, "✨", count = 6)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(pandaScale)
                            .background(Color(0xFFE8F5E9), CircleShape)
                            .border(2.dp, Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐼", fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$pandaName trò chuyện 💬",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF388E3C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = speechText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6 Beautiful Room Selection Cards
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        RoomCard(
                            title = "Xưởng Vẽ Mỹ Thuật",
                            description = "Cọ màu phép thuật & hình dán ngộ nghĩnh",
                            emoji = "🎨",
                            backgroundColor = Color(0xFFFFEBEE),
                            borderColor = Color(0xFFE57373),
                            onClick = { onRoomChange("studio") }
                        )
                    }
                    item {
                        RoomCard(
                            title = "Phòng Robot AI",
                            description = "Nhờ Robot AI vẽ phác thảo kỳ diệu",
                            emoji = "🤖",
                            backgroundColor = Color(0xFFE0F7FA),
                            borderColor = Color(0xFF4DD0E1),
                            onClick = { onRoomChange("ai_room") }
                        )
                    }
                    item {
                        RoomCard(
                            title = "Viện Bảo Tàng Tranh",
                            description = "Trưng bày tranh vẽ lộng lẫy của bé",
                            emoji = "🏛️",
                            backgroundColor = Color(0xFFFFF8E1),
                            borderColor = Color(0xFFFFD54F),
                            onClick = { onRoomChange("museum") }
                        )
                    }
                    item {
                        RoomCard(
                            title = "Bức Tường Vinh Danh",
                            description = "Nơi treo 5 huy hiệu lấp lánh bé đạt được",
                            emoji = "🏆",
                            backgroundColor = Color(0xFFF3E5F5),
                            borderColor = Color(0xFFBA68C8),
                            onClick = { onRoomChange("badge_room") }
                        )
                    }
                    item {
                        RoomCard(
                            title = "Hộp Quà May Mắn",
                            description = "Mở hộp quà mỗi ngày nhận cọ vẽ siêu hiếm",
                            emoji = "🎁",
                            backgroundColor = Color(0xFFEFEBE9),
                            borderColor = Color(0xFFA1887F),
                            onClick = { onRoomChange("gift_room") }
                        )
                    }
                    item {
                        RoomCard(
                            title = "Khu Vườn Panda",
                            description = "Cho Panda ăn trúc để lớn khôn từng ngày",
                            emoji = "🌳",
                            backgroundColor = Color(0xFFE8F5E9),
                            borderColor = Color(0xFF81C784),
                            onClick = { onRoomChange("garden") }
                        )
                    }
                }
            }
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

                Spacer(modifier = Modifier.width(44.dp))
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
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, Color(0xFFFF9100), RoundedCornerShape(24.dp))
                        .shadow(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
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
                        Button(
                            onClick = {
                                isOpened = false
                                rewardText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
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
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
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
            Button(
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp))
            ) {
                Text("Cho Panda Ăn Trúc 🎋", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
