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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.data.HabitRule
import com.pledge.app.data.SensorType
import com.pledge.app.ui.theme.*

@Composable
fun StepProgressCard(
    currentSteps: Int,
    targetSteps: Int,
    isClockedInToday: Boolean,
    habitRule: HabitRule = HabitRule(),
    modifier: Modifier = Modifier
) {
    val isCeiling = habitRule.isLimitCeiling
    val currentVal = if (habitRule.sensorType == SensorType.NETWORK_DATA) 38f else currentSteps.toFloat()
    val targetVal = if (habitRule.sensorType == SensorType.NETWORK_DATA) habitRule.thresholdLimit.toFloat() else targetSteps.toFloat()

    val progress = if (targetVal > 0f) {
        if (isCeiling) {
            (currentVal / targetVal).coerceIn(0f, 1f)
        } else {
            (currentVal / targetVal).coerceIn(0f, 1f)
        }
    } else 0f

    val isPassing = if (isCeiling) currentVal <= targetVal else currentVal >= targetVal

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "ProgressAnimation"
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (habitRule.sensorType) {
                        SensorType.NETWORK_DATA -> Icons.Default.Language
                        SensorType.WAKE_UP_CLOCK -> Icons.Default.Schedule
                        else -> Icons.Default.DirectionsWalk
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Sensor",
                        tint = SolanaTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = habitRule.sensorType.apiSource.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = SolanaTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
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
                } else {
                    Surface(
                        color = if (isPassing) SolanaGreen.copy(alpha = 0.15f) else FlameBurn.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isPassing) "CONDITIONS MET" else "LIMIT EXCEEDED",
                            color = if (isPassing) SolanaGreen else FlameBurn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Display Metric
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (habitRule.sensorType == SensorType.NETWORK_DATA) {
                    Text(
                        text = "38",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "mins used",
                        fontSize = 18.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else if (habitRule.sensorType == SensorType.WAKE_UP_CLOCK) {
                    Text(
                        text = "05:48",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AM",
                        fontSize = 20.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = String.format("%,d", currentSteps),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "steps",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val subText = when (habitRule.sensorType) {
                SensorType.NETWORK_DATA -> "Quota: Max ${habitRule.thresholdLimit.toInt()} mins on Odd Days (Mon, Wed, Fri, Sun)"
                SensorType.WAKE_UP_CLOCK -> "Rule: Clock In before 06:00 AM sharp"
                else -> "Daily Target: %,d steps required".format(targetSteps)
            }

            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isPassing) listOf(SolanaTeal, SolanaGreen) else listOf(FlameBurn, Color(0xFFFF453A))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}% of budget used",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = if (isPassing) "Safe for Clock-In" else "Breach Risk",
                    fontSize = 12.sp,
                    color = if (isPassing) SolanaGreen else FlameBurn,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
