package com.pledge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun SeekerHeader(
    walletAddress: String?,
    solBalance: Double = 0.0,
    skrBalance: Double = 0.0,
    onConnectWallet: () -> Unit,
    onOpenJudgeLab: () -> Unit,
    onRequestAirdrop: () -> Unit = {},
    onRefreshBalances: () -> Unit = {},
    isRequestingAirdrop: Boolean = false,
    isRefreshingBalances: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Address verification helper
    val isValidAddress = remember(walletAddress) {
        !walletAddress.isNullOrBlank() &&
                walletAddress.length in 32..44 &&
                !walletAddress.contains("SeekerPledgeDemo") &&
                !walletAddress.startsWith("SeekerWallet_")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // -------------------------------------------------------------
        // ROW 1: BRAND LOGO, TITLE & TOP STATUS BADGES
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = com.pledge.app.R.drawable.app_logo),
                    contentDescription = "bozPledge Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolanaNeonMint
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                // Devnet Badge
                Surface(
                    color = SolanaElectricCyan.copy(alpha = 0.12f),
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

                // Wallet Address Pill (Clickable to view/switch wallet)
                Surface(
                    color = if (isValidAddress) SolanaNeonMint.copy(alpha = 0.12f) else Color(0xFF141724),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (isValidAddress) SolanaNeonMint.copy(alpha = 0.45f) else BorderSubtle
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
                                .background(if (isValidAddress) SolanaNeonMint else Color(0xFFFF9F0A))
                        )
                        Text(
                            text = if (isValidAddress) "${walletAddress?.take(4)}...${walletAddress?.takeLast(4)}" else "Connect Wallet",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isValidAddress) SolanaNeonMint else Color(0xFFFFD60A)
                        )
                    }
                }

                // Judge Lab Shortcut
                Surface(
                    color = SolanaNeonPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaNeonPurple.copy(alpha = 0.45f)),
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
                            tint = SolanaNeonPurple,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Judge Lab",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBF80FF)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // ROW 2: REDESIGNED CYBER-SOLANA GLASS BALANCE RIBBON
        // -------------------------------------------------------------
        Surface(
            color = Color(0xFF0D101C),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        SolanaNeonPurple.copy(alpha = 0.5f),
                        SolanaElectricCyan.copy(alpha = 0.35f),
                        SolanaNeonMint.copy(alpha = 0.6f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. SOL Gas Balance Pillar
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = SolanaElectricCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "SOL DEVNET GAS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8FA0B8),
                                letterSpacing = 0.6.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.3f", solBalance),
                                fontFamily = PlusJakartaSans,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "SOL",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolanaElectricCyan,
                                modifier = Modifier.padding(bottom = 1.5.dp)
                            )
                        }

                        Text(
                            text = if (solBalance > 0.0) "Ready for Tx" else "Needs Gas",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (solBalance > 0.0) SolanaElectricCyan else Color(0xFFFF9F0A)
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0x22FFFFFF))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 2. $SKR Staked Collateral Pillar
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.pledge.app.R.drawable.app_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = "STAKE ASSET",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8FA0B8),
                                letterSpacing = 0.6.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%,.0f", skrBalance),
                                fontFamily = PlusJakartaSans,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = SolanaNeonMint
                            )
                            Text(
                                text = "\$SKR",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolanaNeonMint,
                                modifier = Modifier.padding(bottom = 1.5.dp)
                            )
                        }

                        Text(
                            text = "SPL Token Mint",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 3. Action Buttons (Faucet + Refresh)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Faucet Button (Solana Gradient Pill)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(listOf(SolanaNeonPurple, SolanaNeonMint))
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            SolanaNeonPurple.copy(alpha = 0.85f),
                                            SolanaNeonMint.copy(alpha = 0.95f)
                                        )
                                    )
                                )
                                .clickable(enabled = !isRequestingAirdrop) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRequestAirdrop()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isRequestingAirdrop) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Minting...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF030D08),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "+10k \$SKR",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF030D08)
                                    )
                                }
                            }
                        }

                        // Sync / Refresh Button
                        val infiniteTransition = rememberInfiniteTransition(label = "spin")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spinRot"
                        )

                        Surface(
                            color = Color(0xFF141724),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isRefreshingBalances) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onRefreshBalances()
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Balances",
                                    tint = SolanaElectricCyan,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(if (isRefreshingBalances) rotation else 0f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // ROW 3: LIVE SOLANA DEVNET TELEMETRY & VERIFIED MINT LINKS
        // -------------------------------------------------------------
        Surface(
            color = Color(0xFF070912),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF141726)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Program ID link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://explorer.solana.com/address/68c1eNdHAfNWJhCWhtqumiqzYyFcDNLkfgwKwdFwFRcd?cluster=devnet")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SolanaElectricCyan))
                    Text(
                        text = "PROGRAM: 68c1...FRcd ↗",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaElectricCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // SPL Token Mint link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://explorer.solana.com/address/F4L7W4qgFAuU2iyg5ePBENXTHeZyTPor4tJMfhUHqfgQ?cluster=devnet")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SolanaNeonMint))
                    Text(
                        text = "MINT: F4L7...qfgQ ↗",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaNeonMint,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
