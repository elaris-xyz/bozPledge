package com.pledge.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun ClockInButton(
    isClockedIn: Boolean,
    canClockIn: Boolean,
    isLoading: Boolean,
    onClockInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (canClockIn && !isClockedIn) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonBrush = when {
        isClockedIn -> Brush.horizontalGradient(listOf(SuccessGreen, SuccessGreen))
        canClockIn -> Brush.horizontalGradient(listOf(SolanaPurple, SolanaGreen))
        else -> Brush.horizontalGradient(listOf(DarkBorder, DarkBorder))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(pulseScale)
            .clip(RoundedCornerShape(20.dp))
            .background(buttonBrush)
            .clickable(enabled = canClockIn && !isClockedIn && !isLoading) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClockInClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = TextPrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isClockedIn) Icons.Default.CheckCircle else Icons.Default.LockClock,
                    contentDescription = "Clock In",
                    tint = if (canClockIn || isClockedIn) Color.White else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when {
                        isClockedIn -> "CLOCKED IN ON-CHAIN"
                        canClockIn -> "CLOCK IN (1-TAP SEED VAULT)"
                        else -> "STEP GOAL NOT MET"
                    },
                    color = if (canClockIn || isClockedIn) Color.White else TextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
