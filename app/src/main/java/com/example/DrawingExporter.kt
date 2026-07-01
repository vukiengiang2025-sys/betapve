package com.example

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object DrawingExporter {

    fun exportToBitmap(
        context: Context,
        strokes: List<DrawingStroke>,
        stickers: List<PlacedSticker>,
        template: ColoringTemplate,
        width: Int,
        height: Int
    ): Bitmap {
        // Create an empty high-quality bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val androidCanvas = android.graphics.Canvas(bitmap)
        
        // Fill base with pure white
        androidCanvas.drawColor(android.graphics.Color.WHITE)
        
        val composeCanvas = androidx.compose.ui.graphics.Canvas(androidCanvas)
        val drawScope = CanvasDrawScope()
        
        drawScope.draw(
            density = Density(context),
            layoutDirection = LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = Size(width.toFloat(), height.toFloat())
        ) {
            // 1. Draw Template
            TemplateDrawer.drawTemplate(template, this)
            
            // 2. Draw Strokes
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
                        // Special brushed shapes guide
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
        
        // 3. Draw stickers using raw Android Canvas text rendering on top
        stickers.forEach { sticker ->
            androidCanvas.save()
            androidCanvas.translate(sticker.x, sticker.y)
            androidCanvas.rotate(sticker.rotation)
            androidCanvas.scale(sticker.scale, sticker.scale)
            
            val paint = Paint().apply {
                textSize = 54f // base emoji size
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            // Vertical offset to make it center nicely on (0,0)
            androidCanvas.drawText(sticker.emoji, 0f, paint.textSize * 0.33f, paint)
            androidCanvas.restore()
        }
        
        return bitmap
    }

    private fun drawGlitterStar(scope: DrawScope, x: Float, y: Float, color: Color) {
        val starPath = Path().apply {
            moveTo(x, y - 10f)
            quadraticTo(x, y, x + 10f, y)
            quadraticTo(x, y, x, y + 10f)
            quadraticTo(x, y, x - 10f, y)
            quadraticTo(x, y, x, y - 10f)
        }
        scope.drawPath(starPath, color)
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
        scope.drawCircle(Color(0xFFFFD54F), r * 0.8f, Offset(x, y))
    }

    private fun drawBubbleBrush(scope: DrawScope, x: Float, y: Float) {
        scope.drawCircle(Color(0xFF80DEEA).copy(alpha = 0.35f), 12f, Offset(x, y))
        scope.drawCircle(Color.White.copy(alpha = 0.8f), 3f, Offset(x - 4f, y - 4f))
        scope.drawCircle(Color(0xFFE0F7FA), 12f, Offset(x, y), style = Stroke(width = 1.2f))
    }

    private fun drawLeafBrush(scope: DrawScope, x: Float, y: Float, color: Color) {
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
        scope.drawCircle(printColor, 6f, Offset(x, y + 3f))
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

    // Save Bitmap to MediaStore (Device Gallery)
    fun saveToDeviceStorage(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "Be_Hoa_Si_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BeHoaSi")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/BeHoaSi"
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val imageFile = File(dir, filename)
                fos = FileOutputStream(imageFile)
                imageUri = Uri.fromFile(imageFile)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
