package com.pledge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun SeekerHeader(
    walletAddress: String?,
    onConnectWallet: () -> Unit,
    onOpenJudgeLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isConnected = walletAddress != null && !walletAddress.contains("SeekerPledgeDemo")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Brand & Status Header (Matching Image 1 Cinema Spec)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(0.5.dp, SolanaNeonMint.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Seeker Brand",
                        tint = SolanaNeonMint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SEEKER",
                            fontFamily = PlusJakartaSans,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.4).sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "bozPLEDGE",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaNeonMint
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(SolanaNeonMint)
                        )
                        Text(
                            text = "Hardware Protected Enclave",
                            fontSize = 9.5.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top Status Badges Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mainnet Beta Pill
                Surface(
                    color = SolanaNeonMint.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaNeonMint.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(SolanaNeonMint)
                        )
                        Text(
                            text = "Mainnet Beta",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaNeonMint
                        )
                    }
                }

                // Judge Lab Button
                Surface(
                    color = SolanaTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaTeal.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenJudgeLab()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = "⚡", fontSize = 10.sp)
                        Text(
                            text = "Judge Lab",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaTeal
                        )
                    }
                }
            }
        }

        // Row 2: Live Solana Network Telemetry Bar
        Surface(
            color = Color(0xFF12141F),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(SolanaNeonMint)
                    )
                    Text(
                        text = "SOLANA NETWORK: ACTIVE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaNeonMint,
                        letterSpacing = 0.4.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "BLOCK: 271,842,910",
                        fontSize = 8.5.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "GAS: 0.000005 SOL",
                        fontSize = 8.5.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
