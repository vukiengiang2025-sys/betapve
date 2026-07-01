package com.example

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun BubbleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4CAF50),
    borderColor: Color = Color(0xFF2E7D32),
    contentColor: Color = Color.White,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1.0f, label = "ButtonScale")
    val shadowOffset by animateDpAsState(targetValue = if (isPressed) 1.dp else 4.dp, label = "ShadowOffset")

    Box(
        modifier = modifier
            .scale(scale)
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
        // Drop Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowOffset)
                .background(borderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        )
        // Main Button body
        Row(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(20.dp))
                .border(3.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

@Composable
fun ToyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFF81C784),
    shadowColor: Color = Color(0xFFE8F5E9),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Drop Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 6.dp)
                .background(shadowColor, RoundedCornerShape(24.dp))
                .border(2.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        )
        // Main Card Surface
        Column(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(24.dp))
                .border(3.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PandaSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    bubbleColor: Color = Color.White,
    borderColor: Color = Color(0xFF4CAF50),
    textColor: Color = Color(0xFF2E7D32)
) {
    Box(modifier = modifier.padding(bottom = 12.dp)) {
        // Drop Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 5.dp)
                .background(borderColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        )
        Column(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(20.dp))
                .border(3.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ToyPopup(
    onDismissRequest: () -> Unit,
    title: String,
    emoji: String = "✨",
    borderColor: Color = Color(0xFF4CAF50),
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 8.dp)
                    .background(borderColor.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            )
            // Dialogue Box
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .border(4.dp, borderColor, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    confirmButton()
                }
            }
        }
    }
}
