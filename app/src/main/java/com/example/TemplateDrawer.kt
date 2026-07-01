package com.example

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

object TemplateDrawer {

    private val strokeColor = Color(0xFF37474F) // Dark blue gray for cute solid outlines
    private val lineThickness = 8f // Bold lines suitable for kids

    fun drawTemplate(template: ColoringTemplate, scope: DrawScope) {
        val w = scope.size.width
        val h = scope.size.height
        val cx = w / 2f
        val cy = h / 2.2f // slightly higher than true center to leave room for controls

        when (template) {
            ColoringTemplate.BLANK -> {
                // Draw nothing, clean canvas
            }
            ColoringTemplate.FLOWER -> {
                drawFlower(scope, cx, cy)
            }
            ColoringTemplate.KITTY -> {
                drawKitty(scope, cx, cy)
            }
            ColoringTemplate.PUPPY -> {
                drawPuppy(scope, cx, cy)
            }
            ColoringTemplate.CAR -> {
                drawCar(scope, cx, cy)
            }
            ColoringTemplate.STAR -> {
                drawStar(scope, cx, cy)
            }
        }
    }

    private fun drawFlower(scope: DrawScope, cx: Float, cy: Float) {
        // 1. Draw Green Stem and Leaves
        val stemPath = Path().apply {
            moveTo(cx, cy)
            quadraticTo(cx - 20f, cy + 180f, cx - 10f, cy + 300f)
        }
        scope.drawPath(
            path = stemPath,
            color = Color(0xFF81C784), // Light green stem guide
            style = Stroke(width = lineThickness * 1.5f)
        )

        // Leaf 1 (Left)
        val leafLeft = Path().apply {
            moveTo(cx - 10f, cy + 150f)
            cubicTo(cx - 80f, cy + 100f, cx - 120f, cy + 180f, cx - 10f, cy + 200f)
        }
        scope.drawPath(path = leafLeft, color = Color(0xFF4CAF50), style = Stroke(width = lineThickness))

        // Leaf 2 (Right)
        val leafRight = Path().apply {
            moveTo(cx - 8f, cy + 210f)
            cubicTo(cx + 80f, cy + 170f, cx + 110f, cy + 240f, cx - 8f, cy + 260f)
        }
        scope.drawPath(path = leafRight, color = Color(0xFF4CAF50), style = Stroke(width = lineThickness))

        // 2. Draw 5 Petals
        val petalRadius = 75f
        val petalOffsets = listOf(
            Offset(0f, -petalRadius - 10f),       // Top
            Offset(-petalRadius, -petalRadius/2), // Top-Left
            Offset(petalRadius, -petalRadius/2),  // Top-Right
            Offset(-petalRadius + 15f, petalRadius), // Bottom-Left
            Offset(petalRadius - 15f, petalRadius)  // Bottom-Right
        )

        for (offset in petalOffsets) {
            scope.drawCircle(
                color = strokeColor,
                radius = petalRadius,
                center = Offset(cx + offset.x, cy + offset.y),
                style = Stroke(width = lineThickness)
            )
        }

        // 3. Draw Center Circle
        val centerRadius = 85f
        scope.drawCircle(
            color = strokeColor,
            radius = centerRadius,
            center = Offset(cx, cy),
            style = Stroke(width = lineThickness)
        )

        // 4. Face details inside center circle (Happy face!)
        // Eyes
        scope.drawCircle(color = strokeColor, radius = 8f, center = Offset(cx - 30f, cy - 15f))
        scope.drawCircle(color = strokeColor, radius = 8f, center = Offset(cx + 30f, cy - 15f))

        // Cheeks
        scope.drawCircle(color = Color(0xFFFF8A80), radius = 10f, center = Offset(cx - 50f, cy))
        scope.drawCircle(color = Color(0xFFFF8A80), radius = 10f, center = Offset(cx + 50f, cy))

        // Smile
        val mouthPath = Path().apply {
            moveTo(cx - 20f, cy + 15f)
            quadraticTo(cx, cy + 40f, cx + 20f, cy + 15f)
        }
        scope.drawPath(path = mouthPath, color = strokeColor, style = Stroke(width = lineThickness - 2f))
    }

    private fun drawKitty(scope: DrawScope, cx: Float, cy: Float) {
        val headWidth = 280f
        val headHeight = 220f

        // Ears
        val leftEar = Path().apply {
            moveTo(cx - headWidth / 2.3f, cy - headHeight / 3f)
            lineTo(cx - headWidth / 2f, cy - headHeight * 0.9f)
            lineTo(cx - headWidth / 4f, cy - headHeight / 2.2f)
        }
        val rightEar = Path().apply {
            moveTo(cx + headWidth / 2.3f, cy - headHeight / 3f)
            lineTo(cx + headWidth / 2f, cy - headHeight * 0.9f)
            lineTo(cx + headWidth / 4f, cy - headHeight / 2.2f)
        }
        scope.drawPath(path = leftEar, color = strokeColor, style = Stroke(width = lineThickness))
        scope.drawPath(path = rightEar, color = strokeColor, style = Stroke(width = lineThickness))

        // Head outline
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(cx - headWidth / 2f, cy - headHeight / 2f),
            size = Size(headWidth, headHeight),
            cornerRadius = CornerRadius(90f, 90f),
            style = Stroke(width = lineThickness)
        )

        // Eyes (Big cute circles with little white glints inside)
        scope.drawCircle(color = strokeColor, radius = 22f, center = Offset(cx - 65f, cy - 10f))
        scope.drawCircle(color = strokeColor, radius = 22f, center = Offset(cx + 65f, cy - 10f))
        scope.drawCircle(color = Color.White, radius = 6f, center = Offset(cx - 70f, cy - 15f))
        scope.drawCircle(color = Color.White, radius = 6f, center = Offset(cx + 60f, cy - 15f))

        // Blush cheeks
        scope.drawCircle(color = Color(0xFFFFCDD2), radius = 18f, center = Offset(cx - 95f, cy + 25f))
        scope.drawCircle(color = Color(0xFFFFCDD2), radius = 18f, center = Offset(cx + 95f, cy + 25f))

        // Cute Nose (small rounded triangle)
        val nosePath = Path().apply {
            moveTo(cx - 15f, cy + 15f)
            lineTo(cx + 15f, cy + 15f)
            lineTo(cx, cy + 28f)
            close()
        }
        scope.drawPath(path = nosePath, color = strokeColor, style = Stroke(width = lineThickness - 2f))

        // Whiskers
        scope.drawLine(strokeColor, Offset(cx - headWidth / 2.1f, cy + 10f), Offset(cx - headWidth / 1.5f, cy + 5f), strokeWidth = lineThickness - 2f)
        scope.drawLine(strokeColor, Offset(cx - headWidth / 2.1f, cy + 30f), Offset(cx - headWidth / 1.5f, cy + 30f), strokeWidth = lineThickness - 2f)

        scope.drawLine(strokeColor, Offset(cx + headWidth / 2.1f, cy + 10f), Offset(cx + headWidth / 1.5f, cy + 5f), strokeWidth = lineThickness - 2f)
        scope.drawLine(strokeColor, Offset(cx + headWidth / 2.1f, cy + 30f), Offset(cx + headWidth / 1.5f, cy + 30f), strokeWidth = lineThickness - 2f)

        // Cute kitten mouth curves
        val mouthLeft = Path().apply {
            moveTo(cx, cy + 28f)
            quadraticTo(cx - 15f, cy + 45f, cx - 30f, cy + 35f)
        }
        val mouthRight = Path().apply {
            moveTo(cx, cy + 28f)
            quadraticTo(cx + 15f, cy + 45f, cx + 30f, cy + 35f)
        }
        scope.drawPath(path = mouthLeft, color = strokeColor, style = Stroke(width = lineThickness - 2f))
        scope.drawPath(path = mouthRight, color = strokeColor, style = Stroke(width = lineThickness - 2f))
    }

    private fun drawPuppy(scope: DrawScope, cx: Float, cy: Float) {
        val headRadius = 130f

        // Ears (Floppy hanging ears)
        val leftEar = Path().apply {
            moveTo(cx - headRadius + 15f, cy - 30f)
            cubicTo(cx - headRadius - 80f, cy + 20f, cx - headRadius - 20f, cy + 180f, cx - headRadius + 40f, cy + 80f)
        }
        val rightEar = Path().apply {
            moveTo(cx + headRadius - 15f, cy - 30f)
            cubicTo(cx + headRadius + 80f, cy + 20f, cx + headRadius + 20f, cy + 180f, cx + headRadius - 40f, cy + 80f)
        }
        scope.drawPath(path = leftEar, color = strokeColor, style = Stroke(width = lineThickness))
        scope.drawPath(path = rightEar, color = strokeColor, style = Stroke(width = lineThickness))

        // Round head
        scope.drawCircle(color = strokeColor, radius = headRadius, center = Offset(cx, cy), style = Stroke(width = lineThickness))

        // Eyes (Big doggy eyes)
        scope.drawCircle(color = strokeColor, radius = 20f, center = Offset(cx - 45f, cy - 20f))
        scope.drawCircle(color = strokeColor, radius = 20f, center = Offset(cx + 45f, cy - 20f))
        scope.drawCircle(color = Color.White, radius = 7f, center = Offset(cx - 50f, cy - 25f))
        scope.drawCircle(color = Color.White, radius = 7f, center = Offset(cx + 40f, cy - 25f))

        // Cute Eyebrows
        scope.drawLine(strokeColor, Offset(cx - 65f, cy - 55f), Offset(cx - 35f, cy - 50f), strokeWidth = lineThickness - 3f)
        scope.drawLine(strokeColor, Offset(cx + 65f, cy - 55f), Offset(cx + 35f, cy - 50f), strokeWidth = lineThickness - 3f)

        // Snout circle
        scope.drawCircle(color = strokeColor, radius = 45f, center = Offset(cx, cy + 40f), style = Stroke(width = lineThickness - 2f))

        // Puppy Nose (Big button nose)
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(cx - 20f, cy + 15f),
            size = Size(40f, 25f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Tongue sticking out!
        val tonguePath = Path().apply {
            moveTo(cx - 15f, cy + 65f)
            quadraticTo(cx, cy + 110f, cx + 15f, cy + 65f)
            close()
        }
        scope.drawPath(path = tonguePath, color = Color(0xFFFF5252)) // Painted Red Tongue
        scope.drawPath(path = tonguePath, color = strokeColor, style = Stroke(width = lineThickness - 3f))

        // Mouth dividers
        scope.drawLine(strokeColor, Offset(cx, cy + 40f), Offset(cx, cy + 65f), strokeWidth = lineThickness - 2f)
    }

    private fun drawCar(scope: DrawScope, cx: Float, cy: Float) {
        val carW = 380f
        val carH = 140f

        // 1. Car upper cabin (roof)
        val cabinPath = Path().apply {
            moveTo(cx - carW * 0.35f, cy - 20f)
            lineTo(cx - carW * 0.2f, cy - carH * 0.7f)
            lineTo(cx + carW * 0.2f, cy - carH * 0.7f)
            lineTo(cx + carW * 0.38f, cy - 20f)
        }
        scope.drawPath(path = cabinPath, color = strokeColor, style = Stroke(width = lineThickness))

        // Windows divider
        scope.drawLine(strokeColor, Offset(cx, cy - carH * 0.7f), Offset(cx, cy - 20f), strokeWidth = lineThickness - 2f)

        // 2. Car main body
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(cx - carW / 2f, cy - 20f),
            size = Size(carW, carH),
            cornerRadius = CornerRadius(40f, 40f),
            style = Stroke(width = lineThickness)
        )

        // Headlights (cute circles on left and right)
        scope.drawCircle(color = strokeColor, radius = 18f, center = Offset(cx - carW / 2f, cy + 30f), style = Stroke(width = lineThickness - 2f))
        scope.drawCircle(color = strokeColor, radius = 12f, center = Offset(cx + carW / 2f, cy + 30f), style = Stroke(width = lineThickness - 2f))

        // 3. Wheels
        val wheelRadius = 45f
        val wheelY = cy + carH - 20f
        scope.drawCircle(color = strokeColor, radius = wheelRadius, center = Offset(cx - carW * 0.28f, wheelY))
        scope.drawCircle(color = Color.White, radius = 18f, center = Offset(cx - carW * 0.28f, wheelY))

        scope.drawCircle(color = strokeColor, radius = wheelRadius, center = Offset(cx + carW * 0.28f, wheelY))
        scope.drawCircle(color = Color.White, radius = 18f, center = Offset(cx + carW * 0.28f, wheelY))
    }

    private fun drawStar(scope: DrawScope, cx: Float, cy: Float) {
        val outerRadius = 180f
        val innerRadius = 80f
        val points = 5
        val starPath = Path()

        var angle = -Math.PI / 2
        val angleIncrement = Math.PI / points

        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = (cx + Math.cos(angle) * r).toFloat()
            val y = (cy + Math.sin(angle) * r).toFloat()

            if (i == 0) {
                starPath.moveTo(x, y)
            } else {
                starPath.lineTo(x, y)
            }
            angle += angleIncrement
        }
        starPath.close()

        // Draw the main large star
        scope.drawPath(path = starPath, color = strokeColor, style = Stroke(width = lineThickness))

        // Cute face inside star
        scope.drawCircle(color = strokeColor, radius = 10f, center = Offset(cx - 30f, cy - 10f))
        scope.drawCircle(color = strokeColor, radius = 10f, center = Offset(cx + 30f, cy - 10f))
        scope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx - 32f, cy - 13f))
        scope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx + 28f, cy - 13f))

        // Blush
        scope.drawCircle(color = Color(0xFFFF8A80), radius = 12f, center = Offset(cx - 50f, cy + 10f))
        scope.drawCircle(color = Color(0xFFFF8A80), radius = 12f, center = Offset(cx + 50f, cy + 10f))

        // Smile mouth
        val mouthPath = Path().apply {
            moveTo(cx - 15f, cy + 15f)
            quadraticTo(cx, cy + 32f, cx + 15f, cy + 15f)
        }
        scope.drawPath(path = mouthPath, color = strokeColor, style = Stroke(width = lineThickness - 2f))

        // Draw little twinkling star outline in top-right
        val smallStarX = cx + 220f
        val smallStarY = cy - 150f
        val smallStarPath = Path()
        var smallAngle = -Math.PI / 2
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) 40f else 18f
            val x = (smallStarX + Math.cos(smallAngle) * r).toFloat()
            val y = (smallStarY + Math.sin(smallAngle) * r).toFloat()
            if (i == 0) smallStarPath.moveTo(x, y) else smallStarPath.lineTo(x, y)
            smallAngle += angleIncrement
        }
        smallStarPath.close()
        scope.drawPath(path = smallStarPath, color = strokeColor, style = Stroke(width = lineThickness - 2f))
    }
}
