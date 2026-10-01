package com.pledge.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun ConnectWalletScreen(
    savedWalletAddress: String?,
    onConnectWallet: () -> Unit,
    onConfirmJudgeKeypair: () -> Unit,
    onEnterApp: () -> Unit,
    onDisconnectWallet: () -> Unit,
    isConnecting: Boolean = false,
    errorMessage: String? = null,
    showNoWalletDialog: Boolean = false,
    onDismissNoWalletDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    var showJudgeConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgVoid),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .fillMaxHeight(),
            color = BgVoid
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Brand Logo Mark (Flat Solana Seeker Hex Shield)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141726))
                        .border(1.5.dp, SolanaNeonMint, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Seeker Shield",
                        tint = SolanaNeonMint,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Brand Title & Network Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SEEKER",
                            fontFamily = PlusJakartaSans,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = Color.White
                        )
                        Text(
                            text = "bozPLEDGE",
                            fontFamily = PlusJakartaSans,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolanaNeonMint
                        )
                    }

                    Surface(
                        color = SolanaElectricCyan.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, SolanaElectricCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SolanaElectricCyan)
                            )
                            Text(
                                text = "Solana Devnet Edition",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaElectricCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (savedWalletAddress != null)
                            "Wallet session active on Solana Devnet. Tap below to enter the protocol."
                        else
                            "Sovereign Habit & Proof-of-Action Protocol on Solana Mobile.\nConnect a Devnet wallet to begin evaluation.",
                        fontSize = 13.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // -------------------------------------------------------------
                // CASE 1: WALLET ALREADY CONNECTED (RESUME SESSION)
                // -------------------------------------------------------------
                if (savedWalletAddress != null) {
                    Surface(
                        color = Color(0xFF101322),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SolanaNeonMint.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SolanaNeonMint)
                                    )
                                    Text(
                                        text = "Connected Session",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolanaNeonMint
                                    )
                                }

                                Surface(
                                    color = Color(0xFF1B2032),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Devnet",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolanaElectricCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Address Card
                            Surface(
                                color = Color(0xFF090A12),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = SolanaNeonMint,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Public Key Authority:",
                                                fontSize = 10.5.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "${savedWalletAddress.take(8)}...${savedWalletAddress.takeLast(8)}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFF141724),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(savedWalletAddress))
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = SolanaElectricCyan,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Copy",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SolanaElectricCyan
                                            )
                                        }
                                    }
                                }
                            }

                            // Primary CTA: Enter bozPLEDGE
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEnterApp()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SolanaNeonMint,
                                    contentColor = Color(0xFF030D08)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Enter bozPLEDGE",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Secondary: Disconnect / Switch
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDisconnectWallet()
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Disconnect / Switch Wallet",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // CASE 2: FIRST TIME ONBOARDING (CONNECT OPTIONS)
                // -------------------------------------------------------------
                if (savedWalletAddress == null) {
                    // 3 Flat Architecture Feature Highlights
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureRow(
                            icon = Icons.Default.Lock,
                            title = "1. Hardware-Attested Escrow",
                            subtitle = "Daily physical discipline certified directly by on-device sensors."
                        )
                        FeatureRow(
                            icon = Icons.Default.Tune,
                            title = "2. 1-Tap Daily Clock-In",
                            subtitle = "Sub-second Ed25519 session signing eliminates repetitive wallet popups."
                        )
                        FeatureRow(
                            icon = Icons.Default.Fingerprint,
                            title = "3. Zero-Profit Deflationary Sink",
                            subtitle = "Missed days permanently burn \$SKR on-chain. No one profits from failure."
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Connection Button: MWA 2.0
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConnectWallet()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolanaNeonMint,
                            contentColor = Color(0xFF030D08)
                        ),
                        enabled = !isConnecting
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF030D08),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp)
                                )
                                Text(
                                    text = "Connect Solana Wallet (MWA)",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Secondary Connection Button: Instant Judge Devnet Keypair
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showJudgeConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0E101D),
                            contentColor = SolanaElectricCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SolanaElectricCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SolanaElectricCyan,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Instant Judge Devnet Keypair (1-Click)",
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = Color(0xFF261014),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE04343))
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFF6B6B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Compliance
                Text(
                    text = "Requires Solana Mobile Stack (MWA 2.0) • Program ID: PLEDGE...1111",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOG 1: JUDGE DEVNET KEYPAIR CONFIRMATION
    // -------------------------------------------------------------
    if (showJudgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showJudgeConfirmDialog = false },
            containerColor = Color(0xFF131626),
            titleContentColor = Color.White,
            textContentColor = TextSecondary,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SolanaNeonMint,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Initialize Judge Devnet Wallet",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Initialize an on-device Ed25519 keypair funded with 2,500 \$SKR on Solana Devnet?\n\nThis enables full evaluation of on-chain escrow, daily clock-in, and slashing without setting up an external wallet.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Surface(
                        color = Color(0xFF090A12),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cluster: Solana Devnet • RPC: api.devnet.solana.com",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = SolanaElectricCyan,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showJudgeConfirmDialog = false
                        onConfirmJudgeKeypair()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolanaNeonMint,
                        contentColor = Color(0xFF030D08)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Initialize & Enter bozPLEDGE",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showJudgeConfirmDialog = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }

    // -------------------------------------------------------------
    // DIALOG 2: NO WALLET FOUND (PROMPT TO INSTALL OR USE JUDGE KEYPAIR)
    // -------------------------------------------------------------
    if (showNoWalletDialog) {
        AlertDialog(
            onDismissRequest = onDismissNoWalletDialog,
            containerColor = Color(0xFF131626),
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = SolanaElectricCyan,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "No Solana Wallet Found",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "No Solana Mobile Wallet Adapter compatible wallet (Phantom or Solflare) was detected.\n\nYou can install Phantom from Google Play or instantly evaluate using the built-in Judge Devnet Keypair.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissNoWalletDialog()
                        onConfirmJudgeKeypair()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolanaNeonMint,
                        contentColor = Color(0xFF030D08)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Use Judge Keypair", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissNoWalletDialog()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://phantom.app/download"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Get Phantom", color = SolanaElectricCyan)
                }
            }
        )
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        color = Color(0xFF101322),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161A2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SolanaElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
