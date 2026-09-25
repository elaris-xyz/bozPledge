package com.pledge.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun StepProgressCard(
    currentSteps: Int,
    targetSteps: Int,
    isClockedInToday: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (targetSteps > 0) {
        (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "StepProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Steps",
                        tint = SolanaGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S PHYSICAL PROOF",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                if (isClockedInToday) {
                    Surface(
                        color = SolanaGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "VERIFIED ON-CHAIN",
                            color = SolanaGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Step Number
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%,d", currentSteps),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = " / " + String.format("%,d", targetSteps) + " steps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp, start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SolanaPurple, SolanaGreen)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (progress >= 1f) "Goal Achieved! Ready to Clock In" else "${((1f - progress) * targetSteps).toInt()} steps remaining",
                    color = if (progress >= 1f) SolanaGreen else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
