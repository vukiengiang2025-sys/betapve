package com.example

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

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

    // Selected template background
    private val _selectedTemplate = MutableStateFlow(ColoringTemplate.BLANK)
    val selectedTemplate: StateFlow<ColoringTemplate> = _selectedTemplate.asStateFlow()

    // Sparkle particles for kids' delight
    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    // Local in-memory Saved Drawings Gallery
    private val _savedDrawings = MutableStateFlow<List<SavedDrawing>>(emptyList())
    val savedDrawings: StateFlow<List<SavedDrawing>> = _savedDrawings.asStateFlow()

    private var particleIdCounter = 0L

    init {
        // Load default mock/preset saved drawings to showcase the gallery
        loadPresetSavedDrawings()
    }

    // --- Drawing Operations ---
    fun startNewStroke(x: Float, y: Float) {
        val currentStrokes = _strokes.value.toMutableList()
        val newStroke = DrawingStroke(
            points = listOf(StrokePoint(x, y)),
            color = if (_isEraser.value) Color.White else _brushColor.value,
            width = _brushWidth.value,
            isEraser = _isEraser.value,
            isRainbow = _isRainbow.value
        )
        currentStrokes.add(newStroke)
        _strokes.value = currentStrokes
        _redoStrokes.value = emptyList() // Clear redo stack on new stroke

        // Spawn drawing sparkles
        spawnParticles(x, y, if (_isEraser.value) Color(0xFF81D4FA) else _brushColor.value, count = 3)
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
    }

    fun selectEraserMode() {
        _isEraser.value = true
        _isRainbow.value = false
    }

    fun setBrushWidth(width: Float) {
        _brushWidth.value = width
    }

    fun selectTemplate(template: ColoringTemplate) {
        _selectedTemplate.value = template
        clearCanvas(keepTemplate = true) // Clear custom drawings when switching coloring pages
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
        }
    }

    fun clearCanvas(keepTemplate: Boolean = false) {
        _strokes.value = emptyList()
        _redoStrokes.value = emptyList()
        _stickers.value = emptyList()
        _selectedStickerId.value = null
        if (!keepTemplate) {
            _selectedTemplate.value = ColoringTemplate.BLANK
        }
    }

    // --- Stickers Operations ---
    fun addSticker(emoji: String) {
        val newSticker = PlacedSticker(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            x = 350f,
            y = 450f,
            scale = 1.2f,
            rotation = 0f
        )
        _stickers.value = _stickers.value + newSticker
        _selectedStickerId.value = newSticker.id

        // Pop celebratory emoji particles!
        spawnEmojiParticles(newSticker.x, newSticker.y, emoji, count = 8)
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
    fun saveToGallery(title: String) {
        val currentDrawing = SavedDrawing(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) "Tranh vẽ của bé #${_savedDrawings.value.size + 1}" else title,
            strokes = _strokes.value,
            stickers = _stickers.value,
            template = _selectedTemplate.value,
            timestamp = System.currentTimeMillis()
        )
        _savedDrawings.value = listOf(currentDrawing) + _savedDrawings.value
    }

    fun loadDrawing(saved: SavedDrawing) {
        _strokes.value = saved.strokes
        _stickers.value = saved.stickers
        _selectedTemplate.value = saved.template
        _selectedStickerId.value = null
        _redoStrokes.value = emptyList()
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
        // Create 2 fun preset drawings for the kids to load and play with right away
        val preset1 = SavedDrawing(
            id = "preset-1",
            title = "Khu Vườn Hoa Lấp Lánh",
            strokes = listOf(
                // Simple drawing lines for decoration
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
