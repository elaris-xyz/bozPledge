package com.pledge.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun HabitCommitmentHeroCard(
    ruleTitle: String,
    streakDays: Int = 14,
    currentVal: Int,
    limitVal: Int,
    unit: String,
    progressPercent: Int,
    stakedSKR: Double,
    timeRemainingStr: String,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Surface(
        color = Color(0xFF10121C),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SolanaNeonMint.copy(alpha = 0.65f)),
        shadowElevation = 14.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161828),
                            Color(0xFF0C0D16),
                            Color(0xFF07080D)
                        )
                    )
                )
        ) {
            // Top Ambient Neon Line Accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                SolanaNeonMint.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isTablet) 24.dp else 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card Main Title
                Text(
                    text = "HABIT COMMITMENT",
                    fontFamily = PlusJakartaSans,
                    fontSize = if (isTablet) 14.sp else 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isTablet) {
                    // TABLET EXPANSIVE DUAL-PANE ROW (Left: Circular Ring, Right: Sensor Telemetry & Stats)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        // Left: Giant Glowing Circular Meter
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularRingMeter(
                                progressPercent = progressPercent,
                                currentVal = currentVal,
                                limitVal = limitVal,
                                unit = unit,
                                sizeDp = 200
                            )
                        }

                        // Right: Rich Live Sensor Telemetry Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Substat: Daily Habit & Streak
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "DAILY HABIT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = ruleTitle.uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    color = AppleOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, AppleOrange.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "$streakDays DAYS STREAK",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleOrange,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            // Substat: Staked & Time Remaining
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "STAKED ESCROW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.6.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SolanaElectricCyan)
                                        )
                                        Text(
                                            text = "${stakedSKR.toInt()} \$SKR",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TIME REMAINING",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = if (timeRemainingStr.isNotEmpty()) timeRemainingStr else "86400s Left Today",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolanaNeonMint,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Telemetry Box
                            Surface(
                                color = Color(0xFF141724),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "✓ Daily Sensor Goal Achieved • Proof Ready",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolanaNeonMint
                                    )
                                    Text(
                                        text = "Seed Vault Enclave • SHA-256 Hardware Attestation",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // MOBILE VERTICAL STACK
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "DAILY HABIT:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = ruleTitle.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "STREAK:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "$streakDays DAYS STREAK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppleOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Circular Ring Meter
                    Box(
                        modifier = Modifier.size(186.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularRingMeter(
                            progressPercent = progressPercent,
                            currentVal = currentVal,
                            limitVal = limitVal,
                            unit = unit,
                            sizeDp = 186
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Substats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "STAKED:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.6.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SolanaElectricCyan)
                                    )
                                Text(
                                    text = "${stakedSKR.toInt()} \$SKR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TIME REMAINING:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = if (timeRemainingStr.isNotEmpty()) timeRemainingStr else "07h 38m 12s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaNeonMint,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularRingMeter(
    progressPercent: Int,
    currentVal: Int,
    limitVal: Int,
    unit: String,
    sizeDp: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeW = 14.dp.toPx()
        val arcPadding = strokeW / 2
        val diameter = size.minDimension - strokeW
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(arcPadding, arcPadding)

        // 1. Background Track
        drawArc(
            color = Color(0xFF1B1D2A),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // 2. Neon Sweep Gradient Arc
        val sweep = ((progressPercent / 100f).coerceIn(0.01f, 1f)) * 360f
        val neonBrush = Brush.sweepGradient(
            0.0f to SolanaNeonMint,
            0.4f to SolanaElectricCyan,
            0.85f to SolanaNeonPurple,
            1.0f to SolanaNeonMint
        )

        drawArc(
            brush = neonBrush,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }

    // Ring Center Info
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$progressPercent%",
            fontFamily = PlusJakartaSans,
            fontSize = if (sizeDp > 190) 42.sp else 38.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-1.5).sp,
            lineHeight = 38.sp
        )
        Text(
            text = "COMPLETE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "$currentVal of $limitVal $unit",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SolanaElectricCyan,
            fontFamily = FontFamily.Monospace
        )
    }
}
