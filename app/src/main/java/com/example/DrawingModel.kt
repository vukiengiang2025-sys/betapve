package com.example

import androidx.compose.ui.graphics.Color

data class StrokePoint(val x: Float, val y: Float)

data class DrawingStroke(
    val points: List<StrokePoint>,
    val color: Color,
    val width: Float,
    val isEraser: Boolean = false,
    val isRainbow: Boolean = false
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
