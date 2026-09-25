package com.pledge.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.data.CommitmentState
import com.pledge.app.data.HabitRule
import com.pledge.app.data.SchedulePreset
import com.pledge.app.data.SensorType
import com.pledge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: CommitmentState,
    currentSteps: Int,
    currentInternetMins: Int = 38,
    walletAddress: String?,
    skrBalance: Double,
    isClockingIn: Boolean,
    isDemoMode: Boolean,
    onClockIn: () -> Unit,
    onCreateNewPledge: () -> Unit,
    onSelectPreset: (HabitRule) -> Unit = {},
    onSettle: () -> Unit,
    onManualStepsChange: (Int) -> Unit,
    onManualInternetChange: (Int) -> Unit = {},
    onFastForwardDay: () -> Unit,
    onConnectWallet: () -> Unit = {},
    onToggleFreshState: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    var isJudgeSheetOpen by remember { mutableStateOf(false) }
    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Explore, 2: Ranks, 3: Vault
    var selectedChipIndex by remember { mutableIntStateOf(0) }

    // Predefined Habit Presets
    val internetDetoxRule = remember {
        HabitRule(
            id = "odd_internet",
            title = "Odd Days Internet Detox",
            sensorType = SensorType.NETWORK_DATA,
            schedulePreset = SchedulePreset.ODD_DAYS,
            activeDaysOfWeek = setOf(1, 3, 5, 7),
            thresholdLimit = 60.0,
            unit = "mins",
            isLimitCeiling = true
        )
    }

    val early6amRule = remember {
        HabitRule(
            id = "early_6am",
            title = "6:00 AM Wake-Up Club",
            sensorType = SensorType.WAKE_UP_CLOCK,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 6.0,
            unit = "AM",
            isLimitCeiling = true
        )
    }

    val stepGoalRule = remember {
        HabitRule(
            id = "steps_10k",
            title = "10k Steps Daily",
            sensorType = SensorType.HEALTH_STEPS,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 10000.0,
            unit = "steps",
            isLimitCeiling = false
        )
    }

    val chips = listOf(
        "<1h Net (Odd Days)",
        "6:00 AM Club (5k)",
        "10k Steps Daily (1k)",
        "+ Custom Plan"
    )

    // Pulse animation for button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        containerColor = BgVoid,
        bottomBar = {
            // Bottom Navigation Dock
            Surface(
                color = SurfaceDeep.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems = listOf(
                        Triple(0, "Active", Icons.Default.Bolt),
                        Triple(1, "Explore", Icons.Default.Explore),
                        Triple(2, "Ranks", Icons.Default.EmojiEvents),
                        Triple(3, "Vault", Icons.Default.Shield)
                    )

                    navItems.forEach { (index, title, icon) ->
                        val isSelected = selectedNavTab == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedNavTab = index
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) SolanaMint else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SolanaMint else TextMuted
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // 1. TOP APP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo Mark + Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        SolanaMint.copy(alpha = 0.2f),
                                        SolanaPurple.copy(alpha = 0.25f)
                                    )
                                )
                            )
                            .border(1.dp, SolanaMint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Logo",
                            tint = SolanaMint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row {
                        Text(
                            text = "boz",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "PLEDGE",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            color = SolanaMint,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // Header Actions: Judge Button + Connect Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Judge Button
                    Surface(
                        color = SurfaceDeep,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isJudgeSheetOpen = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Judge",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Judge",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }

                    // Connect Wallet Button
                    val isConnected = walletAddress != null && !walletAddress.contains("SeekerPledgeDemo")
                    Surface(
                        color = if (isConnected) SolanaMint.copy(alpha = 0.12f) else SurfaceDeep,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isConnected) SolanaMint.copy(alpha = 0.35f) else BorderSubtle
                        ),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConnectWallet()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) SolanaMint else CrimsonBurn)
                            )
                            Text(
                                text = if (isConnected) "${walletAddress?.take(4)}...${walletAddress?.takeLast(4)}" else "Connect",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. HARDWARE TELEMETRY STATUS BAR
            Surface(
                color = SurfaceDeep,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SolanaMint)
                        )
                        Text(
                            text = if (state.rule.sensorType == SensorType.NETWORK_DATA) "NetworkStats Active • Synced" else "Health Connect • Synced",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "Slot #294025 • 28ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. STUDIO ACTION ROW (Rule Composer & 6 AM Club)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Rule Composer
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SolanaTeal.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCreateNewPledge()
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp, 14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Composer",
                                tint = SolanaTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Rule Composer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Custom sensor rules (e.g. <1h Net on Odd Days)",
                            fontSize = 10.5.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Card 2: 6 AM Club
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectPreset(early6amRule)
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp, 14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alarm",
                                tint = GoldGenesis,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "6 AM Club",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wake-up before 06:00 AM & stake 5,000 \$SKR",
                            fontSize = 10.5.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. HORIZONTAL HABIT CHIPS CAROUSEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEachIndexed { index, title ->
                    val isSelected = selectedChipIndex == index
                    Surface(
                        color = if (isSelected) SolanaTeal.copy(alpha = 0.12f) else SurfaceDeep,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SolanaTeal else BorderSubtle
                        ),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedChipIndex = index
                            when (index) {
                                0 -> onSelectPreset(internetDetoxRule)
                                1 -> onSelectPreset(early6amRule)
                                2 -> onSelectPreset(stepGoalRule)
                                3 -> onCreateNewPledge()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (index == 0) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (isSelected) SolanaTeal else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else if (index == 1) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isSelected) GoldGenesis else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. URGENCY LOSS AVERSION BANNER (Daily Window Deadline)
            val dailyBurn = if (state.totalDays > 0) state.totalAmountSKR / state.totalDays else 357.14
            val secsLeft = state.secondsLeftInCurrentDay
            val countdownStr = if (isDemoMode) {
                val s = secsLeft % 60
                val m = (secsLeft / 60) % 60
                String.format("%02ds Left Today", secsLeft)
            } else {
                val h = secsLeft / 3600
                val m = (secsLeft % 3600) / 60
                val s = secsLeft % 60
                String.format("%02dh %02dm %02ds", h, m, s)
            }

            Surface(
                color = CrimsonBurn.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBurn.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fire",
                                tint = CrimsonBurn,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Daily Window Deadline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CrimsonBurn
                            )
                        }
                        Text(
                            text = "If missed: ${String.format("%.2f", dailyBurn)} \$SKR burned",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        color = CrimsonBurn.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBurn.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (countdownStr.isNotEmpty()) countdownStr else "04h 28m 14s",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CrimsonBurn,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. HERO COMMITMENT CARD (Primary Focal Point)
            val isInternetSensor = state.rule.sensorType == SensorType.NETWORK_DATA
            val currentVal = if (isInternetSensor) currentInternetMins else currentSteps
            val limitVal = if (isInternetSensor) state.rule.thresholdLimit.toInt() else state.targetSteps
            val progressPercent = if (limitVal > 0) ((currentVal.toFloat() / limitVal.toFloat()) * 100).toInt().coerceIn(0, 100) else 63
            val isWithinLimit = if (state.rule.isLimitCeiling) currentVal <= limitVal else currentVal >= limitVal

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Top Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge 1: Sensor & Schedule
                        Surface(
                            color = SolanaTeal.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolanaTeal.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInternetSensor) Icons.Default.Language else Icons.Default.DirectionsWalk,
                                    contentDescription = null,
                                    tint = SolanaTeal,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isInternetSensor) "NETWORKSTATS ODD DAYS" else "HEALTH CONNECT DAILY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolanaTeal,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Badge 2: Staked Amount
                        Surface(
                            color = SolanaPurple.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolanaPurple.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = SolanaPurple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${state.totalAmountSKR.toInt()} \$SKR STAKED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolanaPurple,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Big Metric Display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$currentVal",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            lineHeight = 46.sp
                        )
                        Text(
                            text = state.rule.unit,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = if (isInternetSensor) "used of $limitVal mins max allowed today" else "of $limitVal steps target for today",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Meter Progress Bar Track & Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SurfaceDeep)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (progressPercent / 100f).coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SolanaPurple, SolanaMint)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Meter Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWithinLimit) "Within Limit! Ready to Clock In" else "Limit Exceeded! At Risk",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWithinLimit) SolanaMint else CrimsonBurn
                        )
                        Text(
                            text = "$progressPercent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWithinLimit) SolanaMint else CrimsonBurn
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Smart Rule Details Box (Attestation Box)
                    Surface(
                        color = SurfaceDeep,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row {
                                Text(text = "Schedule: ", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = if (isInternetSensor) "Mon, Wed, Fri, Sun (Odd Days)" else "Daily All Days",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Row {
                                Text(text = "Constraint: ", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = if (isInternetSensor) "Max 1.0 hr Internet / Mobile Data" else "Minimum 10,000 steps",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Row {
                                Text(text = "Attestation: ", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "Hardware Attested Nonce #84201",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. TIMELINE CARD (Day Dots)
            val currentDay = state.currentDayIndex + 1
            val totalDays = if (state.totalDays > 0) state.totalDays else 7

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAY $currentDay OF $totalDays",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0 until totalDays) {
                            val isClockedIn = (state.clockedInBitmap and (1L shl i)) != 0L
                            val isPast = i < state.currentDayIndex
                            val isCurrent = i == state.currentDayIndex

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isClockedIn -> SolanaMint.copy(alpha = 0.18f)
                                            isCurrent -> SolanaPurple.copy(alpha = 0.35f)
                                            isPast -> CrimsonBurn.copy(alpha = 0.2f)
                                            else -> SurfaceDeep
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            isClockedIn -> SolanaMint
                                            isCurrent -> SolanaPurple
                                            isPast -> CrimsonBurn.copy(alpha = 0.5f)
                                            else -> BorderSubtle
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isClockedIn) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = SolanaMint,
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else if (isPast) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Failed",
                                        tint = CrimsonBurn,
                                        modifier = Modifier.size(11.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${i + 1}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. PRIMARY CTA BUTTON: CLOCK IN (SEED VAULT)
            val canClockIn = isWithinLimit
            val isAlreadyClockedInToday = state.isTodayClockedIn

            Surface(
                color = when {
                    isAlreadyClockedInToday -> SolanaMint.copy(alpha = 0.15f)
                    canClockIn -> SolanaMint
                    else -> SurfaceDeep
                },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAlreadyClockedInToday) SolanaMint else Color.Transparent
                ),
                shadowElevation = if (canClockIn && !isAlreadyClockedInToday) 14.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(if (canClockIn && !isAlreadyClockedInToday) pulseScale else 1f)
                    .clickable(enabled = canClockIn && !isAlreadyClockedInToday && !isClockingIn) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClockIn()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isClockingIn) {
                        CircularProgressIndicator(
                            color = BgVoid,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAlreadyClockedInToday) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (canClockIn && !isAlreadyClockedInToday) BgVoid else if (isAlreadyClockedInToday) SolanaMint else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when {
                                    isAlreadyClockedInToday -> "TODAY CLOCKED IN (VERIFIED)"
                                    canClockIn -> "CLOCK IN (SEED VAULT)"
                                    else -> "GOAL NOT MET YET"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = if (canClockIn && !isAlreadyClockedInToday) BgVoid else if (isAlreadyClockedInToday) SolanaMint else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 9. SHARE CUSTOM PLAN AS SOLANA BLINK BUTTON
            Surface(
                color = SurfaceDeep,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Just locked 2,500 \$SKR in @bozPledge on @solanamobile Seeker to detox mobile internet on odd days! Verify on-chain via Solana Blink: https://dial.to/?action=solana-action:https://pledge.app/api/blink/odd_internet"
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share as Solana Blink")
                        context.startActivity(shareIntent)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Custom Plan as Solana Blink on X",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 10. HACKATHON JUDGE CONTROLS MODAL BOTTOM SHEET
    if (isJudgeSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isJudgeSheetOpen = false },
            containerColor = SurfaceCard,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderMedium) },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
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
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Judge",
                            tint = SolanaTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "HACKATHON JUDGE CONTROLS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = CrimsonBurn.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBurn.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (isDemoMode) "30s Day Timer" else "24h Production Mode",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonBurn,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Internet minutes override slider
                Text(
                    text = "Simulate Network Data Usage: $currentInternetMins mins (Limit: 60 mins)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = currentInternetMins.toFloat(),
                    onValueChange = { onManualInternetChange(it.toInt()) },
                    valueRange = 10f..90f,
                    colors = SliderDefaults.colors(
                        thumbColor = SolanaTeal,
                        activeTrackColor = SolanaTeal,
                        inactiveTrackColor = SurfaceDeep
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step count override slider
                Text(
                    text = "Simulate Health Connect Steps: $currentSteps steps",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = currentSteps.toFloat(),
                    onValueChange = { onManualStepsChange(it.toInt()) },
                    valueRange = 1000f..15000f,
                    colors = SliderDefaults.colors(
                        thumbColor = SolanaMint,
                        activeTrackColor = SolanaMint,
                        inactiveTrackColor = SurfaceDeep
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fast forward & Reset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onFastForwardDay()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Fast-Forward Day", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFreshState()
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Reset / Fresh State", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
