package com.pledge.app.ui.components

import androidx.compose.foundation.Image
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
    isRequestingAirdrop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isConnected = walletAddress != null && !walletAddress.contains("SeekerPledgeDemo")
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                Image(
                    painter = painterResource(id = com.pledge.app.R.drawable.app_logo),
                    contentDescription = "bozPledge Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
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

        // Row 2: Live On-Chain Balances Bar ($SKR Token + SOL Gas + Instant Faucet)
        Surface(
            color = Color(0xFF0F111D),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2238)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Real $SKR Token Balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = com.pledge.app.R.drawable.app_logo),
                        contentDescription = "SKR Token",
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%,.0f", skrBalance)} \$SKR",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolanaNeonMint
                            )
                        }
                        Text(
                            text = "bozPledge SKR (Devnet)",
                            fontSize = 8.5.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Middle: Real SOL Gas Balance
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.3f", solBalance)} SOL",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaElectricCyan
                    )
                    Text(
                        text = "Devnet Gas",
                        fontSize = 8.sp,
                        color = TextSecondary
                    )
                }

                // Right: Request 10k $SKR Faucet Button
                Surface(
                    color = if (isRequestingAirdrop) Color(0xFF1E2238) else SolanaTeal.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (isRequestingAirdrop) TextSecondary else SolanaTeal.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clickable(enabled = !isRequestingAirdrop) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRequestAirdrop()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isRequestingAirdrop) "⏳ Minting..." else "💧 +10k \$SKR",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRequestingAirdrop) Color.White else SolanaTeal
                        )
                    }
                }
            }
        }

        // Row 3: Live Solana Devnet Telemetry & Verified Mint Explorer Link
        Surface(
            color = Color(0xFF0A0C14),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF161928)),
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
                        fontSize = 8.sp,
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
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaNeonMint,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
