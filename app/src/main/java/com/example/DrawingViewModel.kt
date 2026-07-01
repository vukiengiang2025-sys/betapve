package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.squareup.moshi.JsonClass

// --- Retrofit & Moshi Request/Response Definitions for Gemini API ---
@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiResponseFormatText(val mimeType: String)

@JsonClass(generateAdapter = true)
data class GeminiResponseFormat(val text: GeminiResponseFormatText)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseFormat: GeminiResponseFormat? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class DrawingViewModel : ViewModel() {

    // Current strokes drawn on the canvas
    private val _strokes = MutableStateFlow<List<DrawingStroke>>(emptyList())
    val strokes: StateFlow<List<DrawingStroke>> = _strokes.asStateFlow()

    // Undo/Redo support for strokes
    private val _redoStrokes = MutableStateFlow<List<DrawingStroke>>(emptyList())

    // Placed stickers
    private val _stickers = MutableStateFlow<List<PlacedSticker>>(emptyList())
    val stickers: StateFlow<List<PlacedSticker>> = _stickers.asStateFlow()

    // Currently selected sticker for resizing/deleting/rotating
    private val _selectedStickerId = MutableStateFlow<String?>(null)
    val selectedStickerId: StateFlow<String?> = _selectedStickerId.asStateFlow()

    // Brush configurations
    private val _brushColor = MutableStateFlow(Color(0xFFFF5252)) // Cherry Red
    val brushColor: StateFlow<Color> = _brushColor.asStateFlow()

    private val _brushWidth = MutableStateFlow(16f) // Thick brush for children
    val brushWidth: StateFlow<Float> = _brushWidth.asStateFlow()

    private val _isEraser = MutableStateFlow(false)
    val isEraser: StateFlow<Boolean> = _isEraser.asStateFlow()

    private val _isRainbow = MutableStateFlow(false)
    val isRainbow: StateFlow<Boolean> = _isRainbow.asStateFlow()

    private val _selectedBrush = MutableStateFlow(BrushType.PENCIL)
    val selectedBrush: StateFlow<BrushType> = _selectedBrush.asStateFlow()

    // Selected template background
    private val _selectedTemplate = MutableStateFlow(ColoringTemplate.BLANK)
    val selectedTemplate: StateFlow<ColoringTemplate> = _selectedTemplate.asStateFlow()

    // Sparkle particles for kids' delight
    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    // Local in-memory Saved Drawings Gallery
    private val _savedDrawings = MutableStateFlow<List<SavedDrawing>>(emptyList())
    val savedDrawings: StateFlow<List<SavedDrawing>> = _savedDrawings.asStateFlow()

    // Mascot Speech / Tutorial Helper for Kids
    private val _mascotMessage = MutableStateFlow("Chào bé yêu! Cùng tớ vẽ những bức tranh phép thuật thật rực rỡ nhé! 🐼✨")
    val mascotMessage: StateFlow<String> = _mascotMessage.asStateFlow()

    // Kids badges / achievements book (encouraging, non-competitive)
    private val _badges = MutableStateFlow<List<KidsBadge>>(emptyList())
    val badges: StateFlow<List<KidsBadge>> = _badges.asStateFlow()

    // AI Generation loading state
    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    // --- Parent's Safe Mode & Kid Statistics ---
    private val _isAiFreeFormAllowed = MutableStateFlow(false) // Safe mode: predefined templates & suggestions only by default
    val isAiFreeFormAllowed: StateFlow<Boolean> = _isAiFreeFormAllowed.asStateFlow()

    private val _timeLimitMinutes = MutableStateFlow(0) // 0: Unlimited screen time
    val timeLimitMinutes: StateFlow<Int> = _timeLimitMinutes.asStateFlow()

    private val _isTimeLimitReached = MutableStateFlow(false)
    val isTimeLimitReached: StateFlow<Boolean> = _isTimeLimitReached.asStateFlow()

    private val _timeSpentSeconds = MutableStateFlow(0)
    val timeSpentSeconds: StateFlow<Int> = _timeSpentSeconds.asStateFlow()

    private val _isCelebrating = MutableStateFlow(false)
    val isCelebrating: StateFlow<Boolean> = _isCelebrating.asStateFlow()

    private val _colorUsageCounts = MutableStateFlow<Map<String, Int>>(
        mapOf("Cơ Bản" to 0, "Pastel" to 0, "Neon" to 0, "Kim Loại" to 0)
    )
    val colorUsageCounts: StateFlow<Map<String, Int>> = _colorUsageCounts.asStateFlow()

    private val _totalStickersApplied = MutableStateFlow(0)
    val totalStickersApplied: StateFlow<Int> = _totalStickersApplied.asStateFlow()

    private val _childName = MutableStateFlow("Bé Bún")
    val childName: StateFlow<String> = _childName.asStateFlow()

    private val _childAge = MutableStateFlow("4")
    val childAge: StateFlow<String> = _childAge.asStateFlow()

    private val _pandaName = MutableStateFlow("Panda Béo")
    val pandaName: StateFlow<String> = _pandaName.asStateFlow()

    fun setChildName(name: String) {
        _childName.value = name
    }

    fun setChildAge(age: String) {
        _childAge.value = age
    }

    fun setPandaName(name: String) {
        _pandaName.value = name
    }

    fun setAiFreeFormAllowed(allowed: Boolean) {
        _isAiFreeFormAllowed.value = allowed
    }

    fun setTimeLimitMinutes(minutes: Int) {
        _timeLimitMinutes.value = minutes
        if (minutes == 0) {
            _isTimeLimitReached.value = false
        } else {
            _isTimeLimitReached.value = _timeSpentSeconds.value >= minutes * 60
        }
    }

    fun resetTimeLimitReached() {
        _isTimeLimitReached.value = false
        _timeSpentSeconds.value = 0 // Restart stopwatch
    }

    fun incrementTimeSpent() {
        _timeSpentSeconds.value += 1
        val limitMins = _timeLimitMinutes.value
        if (limitMins > 0) {
            val limitSecs = limitMins * 60
            if (_timeSpentSeconds.value >= limitSecs) {
                _isTimeLimitReached.value = true
            }
        }
    }

    fun setCelebrating(celebrate: Boolean) {
        _isCelebrating.value = celebrate
        if (celebrate) {
            viewModelScope.launch {
                spawnEmojiParticles(400f, 300f, "🎉", count = 12)
                spawnEmojiParticles(200f, 400f, "🎈", count = 10)
                spawnEmojiParticles(600f, 400f, "✨", count = 14)
                spawnEmojiParticles(400f, 500f, "🌟", count = 12)
            }
        }
    }

    fun trackColorCategoryUsage(categoryIndex: Int) {
        val catName = when (categoryIndex) {
            0 -> "Cơ Bản"
            1 -> "Pastel"
            2 -> "Neon"
            else -> "Kim Loại"
        }
        val currentCounts = _colorUsageCounts.value.toMutableMap()
        currentCounts[catName] = (currentCounts[catName] ?: 0) + 1
        _colorUsageCounts.value = currentCounts
    }

    fun getFavoriteColorCategory(): String {
        val maxUsage = _colorUsageCounts.value.maxByOrNull { it.value }
        return if (maxUsage != null && maxUsage.value > 0) {
            "${maxUsage.key} 🎨 (${maxUsage.value} nét vẽ)"
        } else {
            "Bé vẽ rất đều tay các tông màu! 🌈"
        }
    }

    private var particleIdCounter = 0L

    // Network clients for Gemini API
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geminiService = retrofit.create(GeminiApiService::class.java)

    init {
        // Load default mock/preset saved drawings to showcase the gallery
        loadPresetSavedDrawings()
        // Initialize achievement badge book
        initBadges()
    }

    private fun initBadges() {
        _badges.value = listOf(
            KidsBadge("b1", "Danh Họa Tí Hon", "Lưu thành công 3 bức tranh của bé vào album", "🎨", false),
            KidsBadge("b2", "Phù Thủy Sticker", "Dán 8 hình dán đáng yêu lên bức vẽ", "🧸", false),
            KidsBadge("b3", "Cầu Vồng Phép Thuật", "Sử dụng cọ vẽ bảy sắc cầu vồng lấp lánh", "🌈", false),
            KidsBadge("b4", "Bạn Thân Của AI", "Nhờ bạn AI thông minh vẽ tranh hộ bé một lần", "🤖", false),
            KidsBadge("b5", "Người Thách Thức", "Nhận phần thưởng từ Thử Thách Mỗi Ngày", "🏆", false)
        )
    }

    fun checkAndUnlockBadge(id: String) {
        _badges.value = _badges.value.map { badge ->
            if (badge.id == id && !badge.isUnlocked) {
                // Spawn a big circle of celebratory emojis on the canvas center coords
                spawnEmojiParticles(400f, 350f, badge.emoji, count = 12)
                setMascotMessage("Oa! Bé vừa đạt Huy Hiệu mới cực xinh xắn: \"${badge.title}\"! Giỏi quá đi! 🎉")
                badge.copy(isUnlocked = true)
            } else {
                badge
            }
        }
    }

    fun setMascotMessage(message: String) {
        _mascotMessage.value = message
    }

    // --- Drawing Operations ---
    fun startNewStroke(x: Float, y: Float) {
        val currentStrokes = _strokes.value.toMutableList()
        val newStroke = DrawingStroke(
            points = listOf(StrokePoint(x, y)),
            color = if (_isEraser.value) Color.White else _brushColor.value,
            width = _brushWidth.value,
            isEraser = _isEraser.value,
            isRainbow = _isRainbow.value,
            brushType = if (_isEraser.value) "ERASER" else if (_isRainbow.value) "RAINBOW" else _selectedBrush.value.name
        )
        currentStrokes.add(newStroke)
        _strokes.value = currentStrokes
        _redoStrokes.value = emptyList() // Clear redo stack on new stroke

        // Spawn drawing sparkles
        spawnParticles(x, y, if (_isEraser.value) Color(0xFF81D4FA) else _brushColor.value, count = 3)

        // Check for Rainbow badge trigger!
        if (_isRainbow.value) {
            checkAndUnlockBadge("b3")
        }
    }

    fun updateCurrentStroke(x: Float, y: Float) {
        val currentStrokes = _strokes.value.toMutableList()
        if (currentStrokes.isNotEmpty()) {
            val lastStroke = currentStrokes.last()
            val updatedPoints = lastStroke.points.toMutableList().apply { add(StrokePoint(x, y)) }
            currentStrokes[currentStrokes.size - 1] = lastStroke.copy(points = updatedPoints)
            _strokes.value = currentStrokes

            // Occasional sparkles while dragging
            if (updatedPoints.size % 4 == 0) {
                val color = if (lastStroke.isRainbow) {
                    val hue = (updatedPoints.size * 5) % 360f
                    Color.hsv(hue, 0.9f, 0.95f)
                } else {
                    lastStroke.color
                }
                spawnParticles(x, y, color, count = 1)
            }
        }
    }

    fun selectColor(color: Color) {
        _isEraser.value = false
        _isRainbow.value = false
        _brushColor.value = color
    }

    fun selectRainbowMode() {
        _isEraser.value = false
        _isRainbow.value = true
        selectBrush(BrushType.RAINBOW)
    }

    fun selectEraserMode() {
        _isEraser.value = true
        _isRainbow.value = false
    }

    fun setBrushWidth(width: Float) {
        _brushWidth.value = width
    }

    fun selectBrush(brush: BrushType) {
        _selectedBrush.value = brush
        _isEraser.value = false
        if (brush == BrushType.RAINBOW) {
            _isRainbow.value = true
        } else {
            _isRainbow.value = false
        }

        // Adjust default child-friendly widths for special brushes
        _brushWidth.value = when (brush) {
            BrushType.PENCIL -> 10f
            BrushType.MARKER -> 26f
            else -> 22f // balanced size for glitter, flowers, hearts, bubbles
        }

        setMascotMessage("Bé vừa chọn cọ vẽ: \"${brush.title}\"! Cùng vẽ những nét vẽ diệu kỳ nhé! ✨")
    }

    fun selectTemplate(template: ColoringTemplate) {
        _selectedTemplate.value = template
        clearCanvas(keepTemplate = true) // Clear custom drawings when switching coloring pages
        setMascotMessage("Bé chuẩn bị tô màu hình mẫu \"${template.title}\" thật xinh xắn nha! 🌸")
    }

    // --- Smart Fill Tap (Coloring Bucket) ---
    fun triggerSmartFill(x: Float, y: Float, color: Color) {
        // Spawn concentric circle waves / magic waves
        spawnParticles(x, y, color, count = 16)
        
        // Let's draw an actual giant soft color splash/circle stroke onto the background of coordinates
        val currentStrokes = _strokes.value.toMutableList()
        val fillStroke = DrawingStroke(
            points = (0..12).map { i ->
                val angle = (i * (2 * Math.PI / 12))
                val r = 100f
                StrokePoint(x + (Math.cos(angle) * r).toFloat(), y + (Math.sin(angle) * r).toFloat())
            },
            color = color.copy(alpha = 0.45f), // soft beautiful translucent splash
            width = 120f,
            isEraser = false,
            isRainbow = false,
            brushType = "MARKER"
        )
        currentStrokes.add(0, fillStroke) // insert at bottom of lines so it doesn't overlap outlines!
        _strokes.value = currentStrokes

        setMascotMessage("Oa! Đổ màu rực rỡ lấp lánh lên bức tranh rồi nè bé ơi! 🪣💖")
    }

    // --- AI Outline Generation via Gemini API ---
    fun generateAiDrawing(prompt: String, canvasWidth: Float, canvasHeight: Float) {
        if (prompt.isBlank()) return
        _isGeneratingAi.value = true
        setMascotMessage("Đang nhờ bạn AI thông minh vẽ tranh \"$prompt\" cho bé yêu nhé... Chờ một xíu nha! 🤖✨")

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    setMascotMessage("Mách nhỏ: Hãy thiết lập Khóa Bảo Mật (API Key) trong bảng điều khiển AI Studio để dùng tính năng vẽ AI nhé! 🔑")
                    _isGeneratingAi.value = false
                    return@launch
                }

                val systemPrompt = """
                    You are an adorable, kid-friendly vector drawing assistant.
                    Your task is to generate a simple, clean, easily-recognizable vector black-and-white outline drawing for kids to color based on the child's prompt.
                    The drawing must be returned as a JSON object matching this schema:
                    {
                      "title": "Title of the drawing",
                      "strokes": [
                        {
                          "points": [
                            {"x": 120, "y": 250},
                            {"x": 130, "y": 260}
                          ]
                        }
                      ]
                    }
                    
                    CRITICAL REQUIREMENTS:
                    1. Coordinates: All points must be scaled within a 150 to 850 bounding box (meaning minimum x/y is 150, maximum is 850, inside a 1000x1000 coordinate system).
                    2. Simplicity: The outline must be extremely simple and clear, suitable for toddlers (e.g., maximum of 10-14 strokes, maximum of 12-15 points per stroke). Continuous cartoon-style shapes are preferred.
                    3. Do not output solid black filled shapes. ONLY output outline paths.
                    4. Response: You MUST respond with ONLY the pure JSON object. Do not wrap it in markdown code fences like ```json, do not write any introductory or trailing text. Just plain JSON text.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = "Prompt: $prompt")))
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                    generationConfig = GeminiGenerationConfig(
                        responseFormat = GeminiResponseFormat(text = GeminiResponseFormatText(mimeType = "application/json")),
                        temperature = 0.5f
                    )
                )

                val response = geminiService.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (rawText != null) {
                    var cleanJson = rawText.trim()
                    if (cleanJson.startsWith("```json")) {
                        cleanJson = cleanJson.substringAfter("```json").substringBeforeLast("```").trim()
                    } else if (cleanJson.startsWith("```")) {
                        cleanJson = cleanJson.substringAfter("```").substringBeforeLast("```").trim()
                    }

                    val adapter = moshi.adapter(DrawingJson::class.java)
                    val drawingObj = adapter.fromJson(cleanJson)

                    if (drawingObj != null && drawingObj.strokes.isNotEmpty()) {
                        loadAiDrawingToCanvas(drawingObj, canvasWidth, canvasHeight)
                        setMascotMessage("Tadaaa! Bạn AI thông minh đã vẽ xong bức tranh \"${drawingObj.title}\" rồi nè! Bé tô màu đi nha! 😍🎨")
                        checkAndUnlockBadge("b4") // Unlock "Bạn Thân Của AI" badge!
                    } else {
                        setMascotMessage("Bạn AI bận tí xíu rồi. Bé thử lại bằng câu khác xem sao nha! 🥺")
                    }
                } else {
                    setMascotMessage("Bạn AI bận tí xíu rồi. Bé thử lại bằng câu khác xem sao nha! 🥺")
                }
            } catch (e: Exception) {
                setMascotMessage("Ôi! Bạn AI đang ngủ quên rồi. Bé thử lại nha! 🧸")
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    private fun loadAiDrawingToCanvas(drawing: DrawingJson, width: Float, height: Float) {
        // Clear canvas first
        clearCanvas(keepTemplate = false)

        val targetW = if (width > 0) width else 800f
        val targetH = if (height > 0) height else 1000f

        val wScale = targetW / 1000f
        val hScale = targetH / 1000f

        val newStrokes = drawing.strokes.map { stroke ->
            DrawingStroke(
                points = stroke.points.map { pt ->
                    val px = pt.x * wScale
                    val py = pt.y * hScale
                    StrokePoint(px, py)
                },
                color = Color(0xFF37474F), // Dark outline
                width = 8f,
                isEraser = false,
                isRainbow = false,
                brushType = "PENCIL"
            )
        }
        _strokes.value = newStrokes
        spawnParticles(targetW / 2f, targetH / 2f, Color(0xFFFFD54F), count = 15)
    }

    // --- Undo / Redo / Clear ---
    fun undo() {
        val currentStrokes = _strokes.value.toMutableList()
        if (currentStrokes.isNotEmpty()) {
            val lastStroke = currentStrokes.removeAt(currentStrokes.size - 1)
            _strokes.value = currentStrokes

            val redoList = _redoStrokes.value.toMutableList()
            redoList.add(lastStroke)
            _redoStrokes.value = redoList
            setMascotMessage("Đã quay lại nét vẽ trước rồi bé ơi! ↩️")
        }
    }

    fun redo() {
        val redoList = _redoStrokes.value.toMutableList()
        if (redoList.isNotEmpty()) {
            val restoredStroke = redoList.removeAt(redoList.size - 1)
            _redoStrokes.value = redoList

            val currentStrokes = _strokes.value.toMutableList()
            currentStrokes.add(restoredStroke)
            _strokes.value = currentStrokes
            setMascotMessage("Khôi phục lại nét vẽ lấp lánh rồi nha! ↪️")
        }
    }

    fun clearCanvas(keepTemplate: Boolean = false) {
        _strokes.value = emptyList()
        _redoStrokes.value = emptyList()
        _stickers.value = emptyList()
        _selectedStickerId.value = null
        if (!keepTemplate) {
            _selectedTemplate.value = ColoringTemplate.BLANK
            setMascotMessage("Đã dọn dẹp sạch sẽ bảng vẽ rồi nha! Bé vẽ gì tiếp nào? 🧼🎨")
        }
    }

    // --- Stickers Operations ---
    fun addSticker(emoji: String) {
        _totalStickersApplied.value += 1
        val newSticker = PlacedSticker(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            x = 350f,
            y = 450f,
            scale = 1.3f,
            rotation = 0f
        )
        _stickers.value = _stickers.value + newSticker
        _selectedStickerId.value = newSticker.id

        // Pop celebratory emoji particles!
        spawnEmojiParticles(newSticker.x, newSticker.y, emoji, count = 8)
        setMascotMessage("Bé dán nhãn dán \"$emoji\" thật là dễ thương! 🥰")

        // Check for sticker achievement
        if (_stickers.value.size >= 8) {
            checkAndUnlockBadge("b2")
        }
    }

    fun selectSticker(id: String?) {
        _selectedStickerId.value = id
    }

    fun updateStickerPosition(id: String, dx: Float, dy: Float) {
        _stickers.value = _stickers.value.map { sticker ->
            if (sticker.id == id) {
                sticker.copy(x = sticker.x + dx, y = sticker.y + dy)
            } else {
                sticker
            }
        }
    }

    fun updateStickerScaleAndRotation(id: String, deltaScale: Float, deltaRotation: Float) {
        _stickers.value = _stickers.value.map { sticker ->
            if (sticker.id == id) {
                val newScale = (sticker.scale * deltaScale).coerceIn(0.5f, 4.0f)
                val newRotation = (sticker.rotation + deltaRotation) % 360f
                sticker.copy(scale = newScale, rotation = newRotation)
            } else {
                sticker
            }
        }
    }

    fun deleteSticker(id: String) {
        _stickers.value = _stickers.value.filterNot { it.id == id }
        if (_selectedStickerId.value == id) {
            _selectedStickerId.value = null
        }
    }

    // --- Gallery Save & Load ---
    fun saveToGallery(context: Context, title: String, width: Int, height: Int) {
        val finalTitle = if (title.isBlank()) "Tác phẩm của bé #${_savedDrawings.value.size + 1}" else title
        val currentDrawing = SavedDrawing(
            id = UUID.randomUUID().toString(),
            title = finalTitle,
            strokes = _strokes.value,
            stickers = _stickers.value,
            template = _selectedTemplate.value,
            timestamp = System.currentTimeMillis()
        )
        _savedDrawings.value = listOf(currentDrawing) + _savedDrawings.value
        
        try {
            val bitmap = DrawingExporter.exportToBitmap(
                context = context,
                strokes = _strokes.value,
                stickers = _stickers.value,
                template = _selectedTemplate.value,
                width = width,
                height = height
            )
            val uri = DrawingExporter.saveToDeviceStorage(context, bitmap, finalTitle)
            if (uri != null) {
                setMascotMessage("Bức tranh \"$finalTitle\" đã được cất vào Album và lưu thành công vào bộ sưu tập ảnh của máy rồi nha! Siêu cấp tuyệt vời! 🌟🖼️")
            } else {
                setMascotMessage("Bức tranh \"$finalTitle\" đã được cất vào Album của bé rồi! Đẹp tuyệt vời! 🌟🖼️")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setMascotMessage("Bức tranh \"$finalTitle\" đã được cất vào Album của bé rồi! Đẹp tuyệt vời! 🌟🖼️")
        }
        
        setCelebrating(true)

        // Check save badge
        if (_savedDrawings.value.size >= 3) {
            checkAndUnlockBadge("b1")
        }
    }

    fun loadDrawing(saved: SavedDrawing) {
        _strokes.value = saved.strokes
        _stickers.value = saved.stickers
        _selectedTemplate.value = saved.template
        _selectedStickerId.value = null
        _redoStrokes.value = emptyList()
        setMascotMessage("Đã mở bức tranh tuyệt tác \"${saved.title}\" ra rồi nha! Bé chơi tiếp thôi! 🌻")
    }

    fun deleteSavedDrawing(id: String) {
        _savedDrawings.value = _savedDrawings.value.filterNot { it.id == id }
    }

    // --- Visual Sparkles (Particle Physics Engine) ---
    fun spawnParticles(x: Float, y: Float, color: Color, count: Int = 4) {
        val newParticles = (0 until count).map {
            val angle = Math.random() * 2 * Math.PI
            val speed = (Math.random() * 8 + 4).toFloat()
            Particle(
                id = ++particleIdCounter,
                x = x,
                y = y,
                color = color,
                size = (Math.random() * 12 + 8).toFloat(),
                vx = (Math.cos(angle) * speed).toFloat(),
                vy = (Math.sin(angle) * speed - 5).toFloat() // drift upwards
            )
        }
        _particles.value = _particles.value + newParticles
    }

    fun spawnEmojiParticles(x: Float, y: Float, emoji: String, count: Int = 6) {
        val newParticles = (0 until count).map {
            val angle = Math.random() * 2 * Math.PI
            val speed = (Math.random() * 10 + 6).toFloat()
            Particle(
                id = ++particleIdCounter,
                x = x,
                y = y,
                color = Color.Transparent,
                size = (Math.random() * 18 + 20).toFloat(),
                vx = (Math.cos(angle) * speed).toFloat(),
                vy = (Math.sin(angle) * speed - 4).toFloat(),
                emoji = emoji
            )
        }
        _particles.value = _particles.value + newParticles
    }

    fun updateParticles() {
        val current = _particles.value
        if (current.isEmpty()) return

        val updated = current.mapNotNull { particle ->
            val nextAlpha = particle.alpha - 0.03f
            if (nextAlpha <= 0f) {
                null // remove dead particles
            } else {
                particle.copy(
                    x = particle.x + particle.vx,
                    y = particle.y + particle.vy,
                    vy = particle.vy + 0.2f, // mild gravity pull
                    alpha = nextAlpha
                )
            }
        }
        _particles.value = updated
    }

    private fun loadPresetSavedDrawings() {
        val preset1 = SavedDrawing(
            id = "preset-1",
            title = "Khu Vườn Hoa Lấp Lánh",
            strokes = listOf(
                DrawingStroke(
                    points = listOf(StrokePoint(100f, 600f), StrokePoint(300f, 550f), StrokePoint(500f, 600f)),
                    color = Color(0xFF4CAF50), // Green grass line
                    width = 20f
                )
            ),
            stickers = listOf(
                PlacedSticker("s1", "🌸", 150f, 400f, scale = 1.5f),
                PlacedSticker("s2", "🦋", 300f, 250f, scale = 1.3f, rotation = -15f),
                PlacedSticker("s3", "☀️", 500f, 150f, scale = 1.8f)
            ),
            template = ColoringTemplate.FLOWER,
            timestamp = System.currentTimeMillis() - 100000
        )

        val preset2 = SavedDrawing(
            id = "preset-2",
            title = "Mèo Con Đuổi Bướm",
            strokes = emptyList(),
            stickers = listOf(
                PlacedSticker("s4", "🐱", 200f, 450f, scale = 2.0f),
                PlacedSticker("s5", "🎈", 450f, 300f, scale = 1.4f, rotation = 10f),
                PlacedSticker("s6", "🦋", 420f, 120f, scale = 1.2f)
            ),
            template = ColoringTemplate.KITTY,
            timestamp = System.currentTimeMillis() - 50000
        )

        _savedDrawings.value = listOf(preset1, preset2)
    }
}
