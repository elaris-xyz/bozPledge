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
import androidx.compose.ui.graphics.Brush
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
    onManualAddressSubmit: (String) -> Unit,
    onConfirmDevnetKeypair: () -> Unit,
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

    var showManualAddressDialog by remember { mutableStateOf(false) }
    var showDevnetConfirmDialog by remember { mutableStateOf(false) }
    var manualAddressInput by remember { mutableStateOf("") }
    var manualAddressError by remember { mutableStateOf<String?>(null) }

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
                            "کیف‌پول شما متصل است. برای ادامه فعالیت یا ثبت تعهد جدید، وارد برنامه شوید."
                        else
                            "پروتکل ضمانت عادات با گواهی سنسورهای سخت‌افزاری سولانا.\nبرای شروع، ابتدا کیف‌پول خود را متصل یا مشخص نمایید.",
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
                                        text = "کیف‌پول متصل (Connected Session)",
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
                                                text = "آدرس عمومی حساب:",
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
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(text = "کپی", fontSize = 10.5.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }

                            // Primary CTA: Enter App directly!
                            Surface(
                                color = SolanaNeonMint,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onEnterApp()
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(SolanaNeonMint, SolanaElectricCyan)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "ورود به برنامه (Enter bozPLEDGE)",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            // Secondary: Disconnect / Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onDisconnectWallet()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تغییر یا قطع اتصال کیف‌پول (Disconnect / Switch)",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                } else {
                    // -------------------------------------------------------------
                    // CASE 2: NO WALLET CONNECTED (INITIAL ONBOARDING)
                    // -------------------------------------------------------------

                    // 3 Flat Architecture Feature Cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FlatFeatureRow(
                            icon = Icons.Default.Lock,
                            iconTint = SolanaNeonMint,
                            title = "۱. اتصال امن با تایید کاربر",
                            description = "اتصال مستقیم به Seed Vault یا Phantom روی شبکه Devnet بدون نیاز به اشتراک کلید خصوصی."
                        )

                        FlatFeatureRow(
                            icon = Icons.Default.Tune,
                            iconTint = SolanaElectricCyan,
                            title = "۲. انتخاب تعهد و استیک توکن",
                            description = "انتخاب تارگت‌های تاییدشده (سحرخیزی ۶ صبح، ۱۰ هزار قدم، دی‌تاکس اینترنت) یا ساخت قانون دلخواه."
                        )

                        FlatFeatureRow(
                            icon = Icons.Default.Fingerprint,
                            iconTint = SolanaNeonPurple,
                            title = "۳. تصدیق سنسورهای فیزیکی تبلت",
                            description = "سنسورهای دستگاه اثبات فیزیکی را ثبت می‌کنند. با انجام روزانه، استیک خود را حفظ و cNFT دریافت کنید."
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (errorMessage != null) {
                        Surface(
                            color = AppleRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, AppleRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppleRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = errorMessage,
                                    fontSize = 12.sp,
                                    color = AppleRed
                                )
                            }
                        }
                    }

                    // Button 1: Connect via Solana MWA (Mobile Wallet Adapter)
                    Surface(
                        color = SolanaNeonMint,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(enabled = !isConnecting) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConnectWallet()
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SolanaNeonMint, SolanaElectricCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Connect Solana Wallet (MWA)",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Button 2: Manual Public Key Entry
                    Surface(
                        color = Color(0xFF141724),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                manualAddressInput = ""
                                manualAddressError = null
                                showManualAddressDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = SolanaElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ورود دستی آدرس ولت (Manual Public Key)",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Button 3: Devnet Sandbox Keypair with Explicit Confirmation
                    Surface(
                        color = Color(0xFF0F111B),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showDevnetConfirmDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SolanaNeonMint.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تایید و ورود با حساب آزمایشی Devnet",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = "Requires Solana Mobile Wallet Adapter (Seed Vault / Phantom / Solflare) • Devnet",
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // DIALOG 1: MANUAL ADDRESS INPUT MODAL
    // -------------------------------------------------------------------------
    if (showManualAddressDialog) {
        AlertDialog(
            onDismissRequest = { showManualAddressDialog = false },
            containerColor = Color(0xFF141726),
            title = {
                Text(
                    text = "ورود دستی آدرس عمومی سولانا",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "آدرس عمومی (Public Key) حساب سولانا خود را وارد یا پیست کنید:",
                        fontSize = 12.5.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = manualAddressInput,
                        onValueChange = {
                            manualAddressInput = it
                            manualAddressError = null
                        },
                        placeholder = { Text("مثال: 9xQe... یا Phantom Address", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolanaNeonMint,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (manualAddressError != null) {
                        Text(
                            text = manualAddressError!!,
                            color = AppleRed,
                            fontSize = 11.5.sp
                        )
                    }

                    // Quick Paste Button
                    Surface(
                        color = Color(0xFF1C2033),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                manualAddressInput = clip.trim()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = SolanaElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(text = "پیست از کلیپ‌بورد", fontSize = 11.sp, color = SolanaElectricCyan)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = manualAddressInput.trim()
                        if (trimmed.length < 32 || trimmed.length > 44) {
                            manualAddressError = "طول آدرس پابلیک‌کی نامعتبر است (باید بین ۳۲ تا ۴۴ کاراکتر باشد)"
                        } else {
                            showManualAddressDialog = false
                            onManualAddressSubmit(trimmed)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolanaNeonMint)
                ) {
                    Text(text = "تایید و اتصال", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddressDialog = false }) {
                    Text(text = "انصراف", color = TextSecondary)
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // DIALOG 2: DEVNET KEYPAIR EXPLICIT CONFIRMATION MODAL
    // -------------------------------------------------------------------------
    if (showDevnetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDevnetConfirmDialog = false },
            containerColor = Color(0xFF141726),
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
                    text = "تایید اتصال با حساب تستی دِونت",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "آیا مایلید با یک حساب امن تستی Devnet با موجودی ۱,۲۵۰ \$SKR متصل شوید و این حساب برای ورودهای بعدی شما ذخیره شود؟",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Surface(
                        color = Color(0xFF090A12),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SeekerDevnet_Escrow_Keypair",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SolanaElectricCyan,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDevnetConfirmDialog = false
                        onConfirmDevnetKeypair()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolanaNeonMint)
                ) {
                    Text(text = "تایید و ورود به برنامه", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevnetConfirmDialog = false }) {
                    Text(text = "انصراف", color = TextSecondary)
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // DIALOG 3: NO WALLET FOUND (OFFER CHOICES TRANSPARENTLY)
    // -------------------------------------------------------------------------
    if (showNoWalletDialog) {
        AlertDialog(
            onDismissRequest = onDismissNoWalletDialog,
            containerColor = Color(0xFF141726),
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SolanaElectricCyan,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "کیف‌پول موبایلی یافت نشد",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "هیچ برنامه کیف‌پول سولانا (مانند فانتوم یا سولفلیر) روی این دستگاه یافت نشد. مایلید چگونه متصل شوید؟",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Surface(
                        color = Color(0xFF1A1E2F),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissNoWalletDialog()
                                showManualAddressDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = SolanaElectricCyan, modifier = Modifier.size(16.dp))
                            Text(text = "۱. ورود آدرس عمومی کیف‌پول دستی", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Surface(
                        color = Color(0xFF1A1E2F),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissNoWalletDialog()
                                onConfirmDevnetKeypair()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolanaNeonMint, modifier = Modifier.size(16.dp))
                            Text(text = "۲. تایید اتصال با حساب تستی دِونت", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Surface(
                        color = Color(0xFF1A1E2F),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=app.phantom"))
                                    context.startActivity(playIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=app.phantom"))
                                    context.startActivity(webIntent)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = AppleOrange, modifier = Modifier.size(16.dp))
                            Text(text = "۳. نصب کیف‌پول Phantom از گوگل‌پلی", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismissNoWalletDialog) {
                    Text(text = "بستن", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FlatFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF10121C),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161928))
                    .border(0.5.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
