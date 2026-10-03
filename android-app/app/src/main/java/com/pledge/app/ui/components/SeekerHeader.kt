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
                    color = if (isValidAddress) Color(0xFF131A29) else Color(0xFF1A1324),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isValidAddress) SolanaNeonMint.copy(alpha = 0.6f) else SolanaNeonPurple.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConnectWallet()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (isValidAddress) SolanaNeonMint else SolanaNeonPurple,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isValidAddress) "${walletAddress?.take(4)}...${walletAddress?.takeLast(4)}" else "Connect Wallet",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isValidAddress) SolanaNeonMint else Color(0xFFFFD60A)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isValidAddress) SolanaNeonMint else SolanaNeonPurple,
                            modifier = Modifier.size(12.dp)
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
        // ROW 2: UNIFIED CYBER-SOLANA GLASS COMMAND CARD
        // -------------------------------------------------------------
        Surface(
            color = Color(0xFF090C16),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.2.dp,
                SolanaGlassBorderGradient
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF131728).copy(alpha = 0.9f),
                                Color(0xFF080A12)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Main Top Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. SOL Devnet Gas Tile
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SolanaElectricCyan.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaElectricCyan.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = SolanaElectricCyan,
                                        modifier = Modifier.size(13.dp).padding(2.dp)
                                    )
                                }
                                Text(
                                    text = "SOL DEVNET GAS",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFA0B4D0),
                                    letterSpacing = 0.7.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.3f", solBalance),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "SOL",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolanaElectricCyan,
                                    modifier = Modifier.padding(bottom = 2.dp)
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
                                        .background(if (solBalance > 0.0) SolanaNeonMint else Color(0xFFFF9F0A))
                                )
                                Text(
                                    text = if (solBalance > 0.0) "Ready for Gas" else "Needs Gas Airdrop",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (solBalance > 0.0) SolanaNeonMint else Color(0xFFFF9F0A)
                                )
                            }
                        }

                        // Vertical Luminous Solana Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            SolanaNeonPurple.copy(alpha = 0.3f),
                                            SolanaElectricCyan.copy(alpha = 0.6f),
                                            SolanaNeonMint.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 2. $SKR Staked Collateral Tile
                        Column(
                            modifier = Modifier.weight(1.25f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = com.pledge.app.R.drawable.app_logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    text = "STAKE ASSET",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFA0B4D0),
                                    letterSpacing = 0.7.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%,.0f", skrBalance),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                    color = SolanaNeonMint
                                )
                                Text(
                                    text = "\$SKR",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolanaNeonMint,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            Text(
                                text = "SPL Token • Escrow Asset",
                                fontSize = 9.sp,
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
                            // Solana Gradient Faucet Pill (+10k $SKR & SOL Gas)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF14F195).copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SolanaSignatureGradient)
                                    .clickable(enabled = !isRequestingAirdrop) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onRequestAirdrop()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isRequestingAirdrop) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(13.dp),
                                            color = Color(0xFF030D08),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Minting...",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF030D08)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFF030D08),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "+10k & Gas",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF030D08)
                                        )
                                    }
                                }
                            }

                            // Refresh Button (Glass with Solana Cyan glow)
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
                                color = Color(0xFF101424),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaElectricCyan.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isRefreshingBalances) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onRefreshBalances()
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Balances",
                                        tint = SolanaElectricCyan,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .rotate(if (isRefreshingBalances) rotation else 0f)
                                    )
                                }
                            }
                        }
                    }

                    // Hairline separator inside the same card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0x22FFFFFF))
                    )

                    // Integrated Sub-Strip (Program, Mint, Wallet Switcher)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SolanaElectricCyan))
                            Text(
                                text = "PROGRAM: 68c1...FRcd ↗",
                                fontSize = 9.sp,
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
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SolanaNeonMint))
                            Text(
                                text = "MINT: F4L7...qfgQ ↗",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaNeonMint,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Switch Wallet Action Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConnectWallet()
                            }
                        ) {
                            Text(
                                text = if (isValidAddress) "WALLET: ${walletAddress?.take(4)}...${walletAddress?.takeLast(4)} ⇄" else "CONNECT WALLET ⇄",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC48BFF),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
