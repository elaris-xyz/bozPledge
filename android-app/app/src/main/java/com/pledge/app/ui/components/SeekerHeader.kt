package com.pledge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
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
        // Row 1: Brand & Status Header (Devnet Edition)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141624))
                        .border(1.dp, SolanaNeonMint.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Seeker Brand",
                        tint = SolanaNeonMint,
                        modifier = Modifier.size(20.dp)
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
                            fontSize = 11.5.sp,
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
                            text = "Hardware Protected Enclave • Devnet",
                            fontSize = 9.5.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top Status Badges Row (Devnet, Wallet, Judge Lab)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Devnet Pill
                Surface(
                    color = SolanaElectricCyan.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaElectricCyan.copy(alpha = 0.45f))
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
                                .background(SolanaElectricCyan)
                        )
                        Text(
                            text = "Devnet",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaElectricCyan
                        )
                    }
                }

                // Wallet Address Pill
                Surface(
                    color = if (isConnected) SolanaNeonMint.copy(alpha = 0.12f) else Color(0xFF141624),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (isConnected) SolanaNeonMint.copy(alpha = 0.4f) else BorderSubtle
                    ),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConnectWallet()
                    }
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
                                .background(if (isConnected) SolanaNeonMint else AppleGreen)
                        )
                        Text(
                            text = if (isConnected) "${walletAddress?.take(4)}...${walletAddress?.takeLast(4)}" else "Seek...7xK2",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) SolanaNeonMint else TextPrimary
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
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = SolanaTeal,
                            modifier = Modifier.size(11.dp)
                        )
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

        // Row 2: Live Solana Devnet Telemetry Bar (Clickable to Explorer)
        val context = androidx.compose.ui.platform.LocalContext.current
        Surface(
            color = Color(0xFF10121C),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaElectricCyan.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://explorer.solana.com/address/68c1eNdHAfNWJhCWhtqumiqzYyFcDNLkfgwKwdFwFRcd?cluster=devnet")
                    )
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(SolanaElectricCyan)
                    )
                    Text(
                        text = "SOLANA DEVNET: ACTIVE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaElectricCyan,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PROGRAM: PLEDGE...1111",
                        fontSize = 8.5.sp,
                        color = SolanaNeonMint,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "EXPLORER ↗",
                        fontSize = 8.sp,
                        color = SolanaElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
