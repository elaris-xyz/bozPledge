package com.pledge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun SeedVaultAuthCard(
    stakedAmount: Double,
    canClockIn: Boolean,
    isAlreadyClockedInToday: Boolean,
    isClockingIn: Boolean,
    currentDay: Int,
    totalDays: Int,
    onClockIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "bioPulse")
    
    // Pulse animation for concentric rings
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha1"
    )

    Surface(
        color = Color(0xFF0F1018),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        shadowElevation = 14.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF151624),
                            Color(0xFF0B0C12),
                            Color(0xFF07080A)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "SEED VAULT AUTHORIZATION",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "COMMIT HABIT (${stakedAmount.toInt()} \$SKR ESCROW)",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Biometric Scanner Flex Row (Left Meta, Center Fingerprint, Right Meta)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Meta
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "BIOMETRIC\nAUTHORIZATION\nESCROW",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            lineHeight = 11.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Center Concentric Fingerprint Core
                    Box(
                        modifier = Modifier
                            .size(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Concentric Ripple Ring 1
                        if (canClockIn && !isAlreadyClockedInToday) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .scale(pulseScale1)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        SolanaNeonMint.copy(alpha = pulseAlpha1),
                                        CircleShape
                                    )
                            )
                        }

                        // Fingerprint Main Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            SolanaElectricCyan.copy(alpha = 0.35f),
                                            SolanaNeonMint.copy(alpha = 0.15f),
                                            Color(0xFF141726)
                                        )
                                    )
                                )
                                .border(
                                    2.dp,
                                    if (isAlreadyClockedInToday) SolanaNeonMint else SolanaNeonMint,
                                    CircleShape
                                )
                                .clickable(enabled = canClockIn && !isAlreadyClockedInToday && !isClockingIn) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onClockIn()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isClockingIn) {
                                CircularProgressIndicator(
                                    color = SolanaNeonMint,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isAlreadyClockedInToday) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                                    contentDescription = "Seed Vault Biometric",
                                    tint = if (isAlreadyClockedInToday) SolanaNeonMint else SolanaNeonMint,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }

                    // Right Meta
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "SECURED BY\nSOLANA\nSEED VAULT 🔒",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            lineHeight = 11.5.sp,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glowing Luxury Sign & Commit CTA Button (Solana Signature Gradient)
                Surface(
                    color = when {
                        isAlreadyClockedInToday -> SolanaNeonMint.copy(alpha = 0.15f)
                        canClockIn -> Color.Transparent
                        else -> Color(0xFF101322)
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isAlreadyClockedInToday -> SolanaNeonMint.copy(alpha = 0.6f)
                            canClockIn -> SolanaNeonMint
                            else -> BorderSubtle
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(enabled = canClockIn && !isAlreadyClockedInToday && !isClockingIn) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClockIn()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (canClockIn && !isAlreadyClockedInToday) {
                                    Modifier.background(SolanaSignatureGradient)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAlreadyClockedInToday) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (canClockIn && !isAlreadyClockedInToday) SolanaButtonTextDark else if (isAlreadyClockedInToday) SolanaNeonMint else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when {
                                        isAlreadyClockedInToday -> "✓ Day $currentDay Proof Certified on Solana"
                                        canClockIn -> "SIGN & COMMIT"
                                        else -> "Monitoring in Progress"
                                    },
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = if (canClockIn && !isAlreadyClockedInToday) SolanaButtonTextDark else if (isAlreadyClockedInToday) SolanaNeonMint else TextMuted
                                )
                            }
                            Text(
                                text = when {
                                    isAlreadyClockedInToday -> "Escrow milestone unlocked • Seed Vault Sig #5Kz8..."
                                    canClockIn -> "Seed Vault Ed25519 Claim • Records sensor hash"
                                    else -> "Maintain limit to unlock daily milestone signature"
                                },
                                fontSize = 9.sp,
                                color = if (canClockIn && !isAlreadyClockedInToday) Color.Black.copy(alpha = 0.75f) else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
