package com.example

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

data class StrokePoint(val x: Float, val y: Float)

enum class BrushType(val title: String, val icon: String, val description: String) {
    PENCIL("Bút Chì", "✏️", "Nét vẽ thanh mảnh"),
    MARKER("Bút Dạ", "🖊️", "Nét vẽ đậm màu sắc"),
    GLITTER("Kim Tuyến", "✨", "Nét vẽ lấp lánh ngôi sao"),
    RAINBOW("Cầu Vồng", "🌈", "Nét vẽ bảy sắc cầu vồng"),
    HEART("Trái Tim", "❤️", "Nét vẽ rải tim ngập tràn"),
    FLOWER("Bút Hoa", "🌸", "Nét vẽ rải những bông hoa nhỏ"),
    BUBBLE("Bong Bóng", "🫧", "Nét vẽ bong bóng bay bay"),
    LEAF("Lá Rơi", "🍂", "Vẽ rải những thảm lá phong phong"),
    SNOW("Mây Tuyết", "❄️", "Bông tuyết mùa đông lấp lánh"),
    LIGHTNING("Sét Con", "⚡", "Tia chớp tinh nghịch vàng óng"),
    FOOTPRINT("Dấu Chân", "🐾", "Vết chân mèo con đáng yêu"),
    FISH("Cá Con", "🐟", "Đàn cá nhỏ bơi lội dưới nước"),
    CLOUD("Mây Trắng", "☁️", "Mây bồng bềnh bồng bềnh")
}

data class DrawingStroke(
    val points: List<StrokePoint>,
    val color: Color,
    val width: Float,
    val isEraser: Boolean = false,
    val isRainbow: Boolean = false,
    val brushType: String = "PENCIL"
)

data class PlacedSticker(
    val id: String,
    val emoji: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1.2f,
    val rotation: Float = 0f
)

enum class ColoringTemplate(val title: String, val icon: String, val description: String) {
    BLANK("Trang Trắng", "📝", "Thỏa sức vẽ tự do nè!"),
    FLOWER("Bông Hoa", "🌸", "Tô màu bông hoa rực rỡ"),
    KITTY("Mèo Con", "🐱", "Chú mèo con tinh nghịch"),
    PUPPY("Cún Con", "🐶", "Chú cún con đáng yêu"),
    CAR("Ô Tô", "🚗", "Xe hơi chạy bon bon"),
    STAR("Ngôi Sao", "⭐", "Ngôi sao may mắn lấp lánh")
}

data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val vx: Float,
    val vy: Float,
    var alpha: Float = 1.0f,
    val emoji: String? = null
)

data class SavedDrawing(
    val id: String,
    val title: String,
    val strokes: List<DrawingStroke>,
    val stickers: List<PlacedSticker>,
    val template: ColoringTemplate,
    val timestamp: Long
)

data class KidsBadge(
    val id: String,
    val title: String,
    val desc: String,
    val emoji: String,
    val isUnlocked: Boolean = false
)

// --- Moshi Models for Gemini API Response ---
@JsonClass(generateAdapter = true)
data class PointJson(val x: Float, val y: Float)

@JsonClass(generateAdapter = true)
data class StrokeJson(val points: List<PointJson>)

@JsonClass(generateAdapter = true)
data class DrawingJson(val title: String, val strokes: List<StrokeJson>)
