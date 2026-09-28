package com.pledge.app.ui.screens

import android.content.Intent
import android.widget.Toast
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
    var isJudgeSheetOpen by remember { mutableStateOf(false) }
    var isHowItWorksOpen by remember { mutableStateOf(false) }
    var showTipBanner by remember { mutableStateOf(true) }
    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Explore, 2: Ranks, 3: Vault
    var selectedChipIndex by remember { mutableIntStateOf(0) }

    // Habit Presets
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
            title = "10k Steps Daily Marathon",
            sensorType = SensorType.HEALTH_STEPS,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 10000.0,
            unit = "steps",
            isLimitCeiling = false
        )
    }

    val screenDetoxRule = remember {
        HabitRule(
            id = "screen_detox",
            title = "Social Media Detox (<2h)",
            sensorType = SensorType.SCREEN_DETOX,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 2.0,
            unit = "hours",
            isLimitCeiling = true
        )
    }

    val gymWorkoutRule = remember {
        HabitRule(
            id = "gym_workout",
            title = "Daily 45m Gym Workout",
            sensorType = SensorType.HEALTH_STEPS,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 6000.0,
            unit = "steps",
            isLimitCeiling = false
        )
    }

    // Pulse animation for primary CTA button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        containerColor = BgVoid,
        bottomBar = {
            // Apple Translucent Tab Bar
            Surface(
                color = SurfaceCard.copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
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
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedNavTab = index
                                }
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) AppleGreen else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppleGreen else TextMuted
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
        ) {
            // 1. TOP APP HEADER (Apple Navigation Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo Mark + Text + Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .border(0.5.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Logo",
                            tint = AppleGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row {
                            Text(
                                text = "boz",
                                fontFamily = PlusJakartaSans,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "PLEDGE",
                                fontFamily = PlusJakartaSans,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppleGreen,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppleGreen)
                            )
                            Text(
                                text = "Seeker Hardware Protected",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Header Actions: Guide + Connect (Clean Apple HIG 2-Capsule Header)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Guide Icon Button (36x36 circular button)
                    Surface(
                        color = SurfaceCard,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isHowItWorksOpen = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "💡", fontSize = 15.sp)
                        }
                    }

                    // Connect Wallet Button Capsule
                    val isConnected = walletAddress != null && !walletAddress.contains("SeekerPledgeDemo")
                    Surface(
                        color = if (isConnected) AppleGreen.copy(alpha = 0.12f) else SurfaceCard,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isConnected) AppleGreen.copy(alpha = 0.35f) else BorderSubtle
                        ),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConnectWallet()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) AppleGreen else AppleRed)
                            )
                            Text(
                                text = if (isConnected) "${walletAddress?.take(4)}...${walletAddress?.takeLast(4)}" else "Seeker...7xK2",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) AppleGreen else TextPrimary
                            )
                        }
                    }
                }
            }

            // Main Content Area switched by selectedNavTab
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedNavTab) {
                    0 -> {
                        // TAB 0: ACTIVE DASHBOARD
                        ActiveTabContent(
                            state = state,
                            currentSteps = currentSteps,
                            currentInternetMins = currentInternetMins,
                            isClockingIn = isClockingIn,
                            isDemoMode = isDemoMode,
                            showTipBanner = showTipBanner,
                            onDismissTipBanner = { showTipBanner = false },
                            onOpenHowItWorks = { isHowItWorksOpen = true },
                            onClockIn = onClockIn,
                            onCreateNewPledge = onCreateNewPledge,
                            onSelectPreset = onSelectPreset,
                            onToggleFreshState = onToggleFreshState,
                            internetDetoxRule = internetDetoxRule,
                            early6amRule = early6amRule,
                            stepGoalRule = stepGoalRule,
                            screenDetoxRule = screenDetoxRule,
                            pulseScale = pulseScale,
                            selectedChipIndex = selectedChipIndex,
                            onSelectChipIndex = { selectedChipIndex = it }
                        )
                    }
                    1 -> {
                        // TAB 1: EXPLORE / CATALOG OF BLUEPRINTS
                        ExploreTabContent(
                            state = state,
                            onCreateCustom = onCreateNewPledge,
                            onSelectRule = { rule ->
                                onSelectPreset(rule)
                                selectedNavTab = 0 // Switch to active tab so user sees it right away
                            },
                            internetDetoxRule = internetDetoxRule,
                            early6amRule = early6amRule,
                            stepGoalRule = stepGoalRule,
                            screenDetoxRule = screenDetoxRule,
                            gymWorkoutRule = gymWorkoutRule
                        )
                    }
                    2 -> {
                        // TAB 2: RANKS / COMMUNITY LEADERBOARD
                        RanksTabContent()
                    }
                    3 -> {
                        // TAB 3: VAULT / ESCROW & SOULBOUND cNFT BADGES
                        VaultTabContent(
                            walletAddress = walletAddress,
                            stakedAmount = state.totalAmountSKR,
                            skrBalance = skrBalance,
                            onSettle = onSettle
                        )
                    }
                }
            }
        }
    }

    // Modal 1: 30-Second Interactive "How It Works" Walkthrough
    if (isHowItWorksOpen) {
        HowItWorksModal(
            onDismiss = { isHowItWorksOpen = false }
        )
    }

    // Modal 2: Hackathon Judge Controls Bottom Sheet
    if (isJudgeSheetOpen) {
        JudgeModalBottomSheet(
            currentInternetMins = currentInternetMins,
            currentSteps = currentSteps,
            isDemoMode = isDemoMode,
            onManualInternetChange = onManualInternetChange,
            onManualStepsChange = onManualStepsChange,
            onFastForwardDay = onFastForwardDay,
            onToggleFreshState = onToggleFreshState,
            onDismiss = { isJudgeSheetOpen = false }
        )
    }
}

// -----------------------------------------------------------------------------
// DAY 0 ONBOARDING COMPONENTS
// -----------------------------------------------------------------------------
@Composable
fun Day0BlueprintCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = AppleGreen,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Stake & Start",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun Day0OnboardingContent(
    onCreateCustom: () -> Unit,
    onSelectPreset: (HabitRule) -> Unit,
    onToggleDemo: () -> Unit,
    internetDetoxRule: HabitRule,
    early6amRule: HabitRule,
    stepGoalRule: HabitRule
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Badge
                Surface(
                    color = AppleGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AppleGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "HARDWARE-ATTESTED ESCROW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppleGreen,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Lock Collateral.\nBuild Unstoppable Habits.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Seeker phone sensors continuously certify your physical discipline. Miss a day, and your staked \$SKR burns forever on Solana. Choose a verified blueprint below to start Day 1:",
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Blueprint 1: Odd-Days Internet Detox
                Day0BlueprintCard(
                    title = "Odd-Days Internet Detox",
                    subtitle = "NetworkStats • <1h on Mon/Wed/Fri/Sun • 2,500 \$SKR",
                    icon = Icons.Default.Language,
                    accentColor = AppleTeal,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectPreset(internetDetoxRule)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Blueprint 2: The 6:00 AM Club
                Day0BlueprintCard(
                    title = "The 6:00 AM Club",
                    subtitle = "Hardware NTP Clock • Wake up <06:00 AM • 5,000 \$SKR",
                    icon = Icons.Default.Alarm,
                    accentColor = AppleOrange,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectPreset(early6amRule)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Blueprint 3: 10,000 Steps Daily
                Day0BlueprintCard(
                    title = "10,000 Steps Daily",
                    subtitle = "Health Connect • Daily Step Goal • 1,000 \$SKR",
                    icon = Icons.Default.DirectionsRun,
                    accentColor = AppleGreen,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectPreset(stepGoalRule)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Compose Custom Rule Button
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCreateCustom()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+ Compose Custom Smart Rule",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Executive Hackathon Judge Sandbox Card
                Surface(
                    color = AppleTeal.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, AppleTeal.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleDemo()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "HACKATHON JUDGE SANDBOX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppleTeal,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Instant-Load Day 2 Escrow Simulation",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Skip day 1 onboarding to test live hardware sensor telemetry, Seed Vault Ed25519 proof signing, and slashing rules.",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Launch Live Simulation →",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTeal
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 0: ACTIVE DASHBOARD CONTENT
// -----------------------------------------------------------------------------
@Composable
fun ActiveTabContent(
    state: CommitmentState,
    currentSteps: Int,
    currentInternetMins: Int,
    isClockingIn: Boolean,
    isDemoMode: Boolean,
    showTipBanner: Boolean,
    onDismissTipBanner: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    onClockIn: () -> Unit,
    onCreateNewPledge: () -> Unit,
    onSelectPreset: (HabitRule) -> Unit,
    onToggleFreshState: () -> Unit = {},
    internetDetoxRule: HabitRule,
    early6amRule: HabitRule,
    stepGoalRule: HabitRule,
    screenDetoxRule: HabitRule,
    pulseScale: Float,
    selectedChipIndex: Int,
    onSelectChipIndex: (Int) -> Unit
) {
    if (state.startTimestamp == 0L) {
        Day0OnboardingContent(
            onCreateCustom = onCreateNewPledge,
            onSelectPreset = onSelectPreset,
            onToggleDemo = onToggleFreshState,
            internetDetoxRule = internetDetoxRule,
            early6amRule = early6amRule,
            stepGoalRule = stepGoalRule
        )
        return
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val isInternetSensor = state.rule.sensorType == SensorType.NETWORK_DATA
    val isWakeUpSensor = state.rule.sensorType == SensorType.WAKE_UP_CLOCK
    val currentVal = if (isInternetSensor) currentInternetMins else if (isWakeUpSensor) 5 else currentSteps
    val limitVal = if (isInternetSensor) state.rule.thresholdLimit.toInt() else if (isWakeUpSensor) 6 else state.targetSteps
    val progressPercent = if (limitVal > 0) ((currentVal.toFloat() / limitVal.toFloat()) * 100).toInt().coerceIn(0, 100) else 63
    val isWithinLimit = if (state.rule.isLimitCeiling) currentVal <= limitVal else currentVal >= limitVal

    val dailyBurn = if (state.totalDays > 0) state.totalAmountSKR / state.totalDays else 357.14
    val secsLeft = state.secondsLeftInCurrentDay
    val countdownStr = if (isDemoMode) {
        String.format("%02ds Left Today", secsLeft)
    } else {
        val h = secsLeft / 3600
        val m = (secsLeft % 3600) / 60
        val s = secsLeft % 60
        String.format("%02dh %02dm %02ds", h, m, s)
    }

    val currentDay = state.currentDayIndex + 1
    val totalDays = if (state.totalDays > 0) state.totalDays else 7
    val canClockIn = isWithinLimit
    val isAlreadyClockedInToday = state.isTodayClockedIn

    val chips = listOf(
        "<1h Net (Odd Days)",
        "6:00 AM Club (5k)",
        "10k Steps Daily (1k)",
        "+ Custom Plan"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Helpful Orientation Tip Banner (Apple Tips style)
        if (showTipBanner) {
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "💡", fontSize = 13.sp)
                            Text(
                                text = "Quick Orientation Guide",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleGreen
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDismissTipBanner() }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Active Habit: ${state.rule.title}. Phone hardware sensors monitor your progress without cheating. When today's goal is met, tap the verification button to lock in your day.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How it works (interactive 3-step walkthrough) →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleGreen,
                        modifier = Modifier.clickable { onOpenHowItWorks() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Developer / Judge Sandbox Reset Bar
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFreshState()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppleTeal)
                    )
                    Text(
                        text = "DEVELOPER / JUDGE SANDBOX",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Reset to Fresh Day 0 ↺",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Studio Action Row (Composer & 6 AM Club)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Rule Composer
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCreateNewPledge()
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Composer",
                            tint = AppleTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Rule Composer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Build custom sensor rules & stake",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            // Card 2: 6 AM Club
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectPreset(early6amRule)
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarm",
                            tint = AppleOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "6 AM Club",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wake up before 6 AM • 5k stake",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Habit Chips Carousel (Apple Segmented Pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEachIndexed { index, title ->
                val isSelected = selectedChipIndex == index
                Surface(
                    color = if (isSelected) AppleGreen else SurfaceCard,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectChipIndex(index)
                        when (index) {
                            0 -> onSelectPreset(internetDetoxRule)
                            1 -> onSelectPreset(early6amRule)
                            2 -> onSelectPreset(stepGoalRule)
                            3 -> onCreateNewPledge()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (index == 0) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (index == 1) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Urgency Loss Aversion Banner (Apple Alert Card)
        Surface(
            color = AppleRed.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
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
                            tint = AppleRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Daily Window Deadline",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleRed
                        )
                    }
                    Text(
                        text = "If missed: ${String.format("%.1f", dailyBurn)} \$SKR burned",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = AppleRed.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (countdownStr.isNotEmpty()) countdownStr else "04h 28m 14s",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Commitment Card (Apple Fitness Activity Widget)
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Top Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isInternetSensor) Icons.Default.Language else if (isWakeUpSensor) Icons.Default.Alarm else Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = AppleTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = state.rule.title.uppercase(),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTeal,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ApplePurple,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${state.totalAmountSKR.toInt()} \$SKR STAKED",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApplePurple,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Metric Display (Apple Fitness typography)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "$currentVal",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = state.rule.unit,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Text(
                    text = if (isInternetSensor) {
                        "used of $limitVal mins max allowed today"
                    } else if (isWakeUpSensor) {
                        "Wake-up time recorded (Target: before 06:00 AM)"
                    } else {
                        "steps achieved of $limitVal daily target"
                    },
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Meter Progress Bar Track & Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (progressPercent / 100f).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppleGreen, AppleTeal)
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
                        text = if (isWithinLimit) "✓ Daily Sensor Goal Achieved • Proof Ready" else "⚠️ Limit Exceeded • Forfeit at Midnight",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWithinLimit) AppleGreen else AppleRed
                    )
                    Text(
                        text = "$progressPercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWithinLimit) AppleGreen else AppleRed
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smart Rule Inset Details Box
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Schedule", fontSize = 11.5.sp, color = TextSecondary)
                            Text(
                                text = if (isInternetSensor) "Mon, Wed, Fri, Sun (Odd Days)" else "Daily All Days",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Constraint", fontSize = 11.5.sp, color = TextSecondary)
                            Text(
                                text = state.rule.title,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Attestation", fontSize = 11.5.sp, color = TextSecondary)
                            Text(
                                text = "Seed Vault Enclave • SHA-256 Digest #84201",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeline Card (Day Dots)
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAY $currentDay OF $totalDays",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
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
                                        isClockedIn -> AppleGreen.copy(alpha = 0.2f)
                                        isCurrent -> AppleGreen
                                        isPast -> AppleRed.copy(alpha = 0.2f)
                                        else -> SurfaceElevated
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isClockedIn) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = AppleGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            } else if (isPast) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Failed",
                                    tint = AppleRed,
                                    modifier = Modifier.size(11.dp)
                                )
                            } else {
                                Text(
                                    text = "${i + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.Black else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PRIMARY ACTION BUTTON (Solid Apple Fitness Green Capsule)
        Surface(
            color = when {
                isAlreadyClockedInToday -> AppleGreen.copy(alpha = 0.15f)
                canClockIn -> AppleGreen
                else -> SurfaceElevated
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = canClockIn && !isAlreadyClockedInToday && !isClockingIn) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClockIn()
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isClockingIn) {
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
                            imageVector = if (isAlreadyClockedInToday) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (canClockIn && !isAlreadyClockedInToday) Color.Black else if (isAlreadyClockedInToday) AppleGreen else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    isAlreadyClockedInToday -> "✓ Daily Proof Anchored on Solana"
                                    canClockIn -> "Sign & Anchor Daily Proof"
                                    else -> "Monitoring in Progress"
                                },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canClockIn && !isAlreadyClockedInToday) Color.Black else if (isAlreadyClockedInToday) AppleGreen else TextMuted
                            )
                            Text(
                                text = when {
                                    isAlreadyClockedInToday -> "Escrow milestone unlocked • Seed Vault Sig #5Kz8..."
                                    canClockIn -> "Seed Vault Ed25519 Claim • Records sensor hash"
                                    else -> "Maintain limit to unlock daily milestone signature"
                                },
                                fontSize = 9.5.sp,
                                color = if (canClockIn && !isAlreadyClockedInToday) Color.Black.copy(alpha = 0.75f) else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cryptographic Proof Explanation Note
        Surface(
            color = SurfaceCard.copy(alpha = 0.7f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AppleTeal,
                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                )
                Text(
                    text = "Hardware sensors track telemetry automatically. Your Seed Vault key cryptographically signs the attestation digest to release your daily escrow milestone without centralized oracles.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SHARE CUSTOM PLAN AS SOLANA BLINK BUTTON
        Surface(
            color = SurfaceDeep,
            shape = RoundedCornerShape(14.dp),
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
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// -----------------------------------------------------------------------------
// TAB 1: EXPLORE / CATALOG CONTENT (Apple Inset Grouped)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTabContent(
    state: CommitmentState,
    onCreateCustom: () -> Unit,
    onSelectRule: (HabitRule) -> Unit,
    internetDetoxRule: HabitRule,
    early6amRule: HabitRule,
    stepGoalRule: HabitRule,
    screenDetoxRule: HabitRule,
    gymWorkoutRule: HabitRule
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    var pendingSwitchRule by remember { mutableStateOf<HabitRule?>(null) }

    val handleBlueprintClick: (HabitRule) -> Unit = { rule ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (state.startTimestamp == 0L || state.rule.id == rule.id) {
            onSelectRule(rule)
        } else {
            pendingSwitchRule = rule
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Catalog Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Commitment Catalog",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Hardware-attested habit blueprints. Pick one to activate.",
                    fontSize = 11.5.sp,
                    color = TextSecondary
                )
            }

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(50),
                modifier = Modifier.clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCreateCustom()
                }
            ) {
                Text(
                    text = "+ Custom",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTeal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Inset Grouped Card Container
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ExploreCardRow(
                    title = "Odd-Days Internet Detox (<1h)",
                    subtitle = "NetworkStats • Mon/Wed/Fri/Sun • 2,500 \$SKR",
                    icon = Icons.Default.Language,
                    accentColor = AppleTeal,
                    isActive = state.rule.id == internetDetoxRule.id,
                    onActionClick = { handleBlueprintClick(internetDetoxRule) }
                )
                Divider(color = BorderSubtle, thickness = 0.5.dp)

                ExploreCardRow(
                    title = "The 6:00 AM Club (High Stakes)",
                    subtitle = "Hardware NTP Clock • 7 Days • Stake 5,000 \$SKR",
                    icon = Icons.Default.Alarm,
                    accentColor = AppleOrange,
                    isActive = state.rule.id == early6amRule.id,
                    onActionClick = { handleBlueprintClick(early6amRule) }
                )
                Divider(color = BorderSubtle, thickness = 0.5.dp)

                ExploreCardRow(
                    title = "10,000 Steps Daily Marathon",
                    subtitle = "Health Connect Sensors • 7 Days • Stake 1,000 \$SKR",
                    icon = Icons.Default.DirectionsWalk,
                    accentColor = AppleGreen,
                    isActive = state.rule.id == stepGoalRule.id,
                    onActionClick = { handleBlueprintClick(stepGoalRule) }
                )
                Divider(color = BorderSubtle, thickness = 0.5.dp)

                ExploreCardRow(
                    title = "Social Media Detox (<2h)",
                    subtitle = "Android UsageStats • 14 Days • Stake 1,500 \$SKR",
                    icon = Icons.Default.Tune,
                    accentColor = ApplePurple,
                    isActive = state.rule.id == screenDetoxRule.id,
                    onActionClick = { handleBlueprintClick(screenDetoxRule) }
                )
                Divider(color = BorderSubtle, thickness = 0.5.dp)

                ExploreCardRow(
                    title = "Daily 45m Gym Workout",
                    subtitle = "HeartRateRecord Sensors • 7 Days • Stake 750 \$SKR",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = AppleTeal,
                    isActive = state.rule.id == gymWorkoutRule.id,
                    onActionClick = { handleBlueprintClick(gymWorkoutRule) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info footnote
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💡", fontSize = 14.sp)
                Text(
                    text = "Tapping 'Activate' immediately syncs the rule with your phone hardware sensors and redirects you to the active commitment view.",
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Seeker Hardware Telemetry Pipeline Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = AppleTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Seeker Hardware Telemetry Pipeline",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetrySpecBox(
                        title = "NetworkStatsManager",
                        desc = "Cellular UID metrics sealed directly from Android OS kernel.",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetrySpecBox(
                        title = "Health Connect",
                        desc = "Cryptographically stamped steps with hardware pedometer.",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetrySpecBox(
                        title = "Hardware NTP Clock",
                        desc = "Tamper-proof real-time clock isolated from local time spoofing.",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetrySpecBox(
                        title = "Android UsageStats",
                        desc = "Foreground app timestamps certified by OS subsystem.",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Solana Smart Escrow Guarantees Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AppleGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Solana Smart Escrow Guarantees",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                EscrowGuaranteeRow(
                    badge = "100% Non-Custodial PDA",
                    text = "Funds are locked in program-derived accounts. No dev backdoors or withdrawal privileges exist."
                )
                Spacer(modifier = Modifier.height(8.dp))
                EscrowGuaranteeRow(
                    badge = "Deflationary Slashing Burn",
                    text = "Failed daily verification thresholds automatically trigger on-chain token burns."
                )
                Spacer(modifier = Modifier.height(8.dp))
                EscrowGuaranteeRow(
                    badge = "Seed Vault Biometrics",
                    text = "Daily clock-in transactions are executed within Seeker Secure Execution Environment."
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Active Escrow Protection Confirmation Sheet
    if (pendingSwitchRule != null) {
        val targetRule = pendingSwitchRule!!
        ModalBottomSheet(
            onDismissRequest = { pendingSwitchRule = null },
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AppleOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Active Escrow In Progress",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleOrange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You currently have ${state.totalAmountSKR.toInt()} \$SKR staked in \"${state.rule.title}\" (Day ${state.currentDayIndex + 1} of ${state.totalDays}).\n\nActivating \"${targetRule.title}\" will switch your active tracking track for this session.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val r = targetRule
                        pendingSwitchRule = null
                        onSelectRule(r)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleTeal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Switch Active Track",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        pendingSwitchRule = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Keep Current Escrow",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ExploreCardRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isActive: Boolean,
    onActionClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onActionClick()
            }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // App Store style Pill Button
        Surface(
            color = if (isActive) AppleGreen else SurfaceElevated,
            shape = RoundedCornerShape(50),
            modifier = Modifier.clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onActionClick()
            }
        ) {
            Text(
                text = if (isActive) "ACTIVE" else "ACTIVATE",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.Black else AppleBlue,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 2: RANKS / COMMUNITY LEADERBOARD CONTENT
// -----------------------------------------------------------------------------
@Composable
fun RanksTabContent() {
    val scrollState = rememberScrollState()
    var selectedEpoch by remember { mutableIntStateOf(0) } // 0: Weekly, 1: 30-Day, 2: All-Time

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Seeker Protocol Ranks",
                fontFamily = PlusJakartaSans,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Proof-of-Action sovereign community leaderboard",
                fontSize = 11.5.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3-Metric Protocol Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Active Staked", fontSize = 10.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "284.5K",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleGreen,
                        fontFamily = PlusJakartaSans
                    )
                    Text(text = "1,280 Nodes", fontSize = 9.5.sp, color = TextMuted)
                }
            }

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Deflation Burned", fontSize = 10.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "48,250",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleRed,
                        fontFamily = PlusJakartaSans
                    )
                    Text(text = "Forfeited 🔥", fontSize = 9.5.sp, color = TextMuted)
                }
            }

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Success Rate", fontSize = 10.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "94.2%",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleTeal,
                        fontFamily = PlusJakartaSans
                    )
                    Text(text = "Attested ✨", fontSize = 9.5.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Epoch Filter Pills
        Surface(
            color = SurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {
                listOf("Weekly Sprint", "30-Day Epoch", "All-Time Genesis").forEachIndexed { idx, label ->
                    val isSel = selectedEpoch == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSel) SurfaceCard else Color.Transparent)
                            .clickable { selectedEpoch = idx }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Proof-of-Action Verified Nodes",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Rank 1
        LeaderboardRow(
            rank = "1",
            name = "toly.sol",
            subtitle = "14,200 avg steps • 0 burns",
            streak = "28d STREAK",
            streakColor = AppleYellow,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 2
        LeaderboardRow(
            rank = "2",
            name = "mert_helius",
            subtitle = "11,800 avg steps • 0 burns",
            streak = "21d STREAK",
            streakColor = Color.White,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 3
        LeaderboardRow(
            rank = "3",
            name = "raj_gokal",
            subtitle = "6:00 AM Wake-Up • 5,000 \$SKR",
            streak = "18d STREAK",
            streakColor = AppleOrange,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 4
        LeaderboardRow(
            rank = "4",
            name = "solana_sensei",
            subtitle = "10K Steps Daily • 2,500 \$SKR",
            streak = "14d STREAK",
            streakColor = AppleTeal,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 5
        LeaderboardRow(
            rank = "5",
            name = "dr_crypto",
            subtitle = "Odd Internet Detox • 1,500 \$SKR",
            streak = "11d STREAK",
            streakColor = AppleTeal,
            isUser = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Pinned Standing Card
        Surface(
            color = AppleGreen.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, AppleGreen.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR NODE STANDING",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleGreen,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "Rank #42 • Top 4%",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleGreen
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "SEEKER-ED25519-7XK2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "2,500 \$SKR Staked • Day 2 / 7", fontSize = 11.sp, color = TextSecondary)
                    }
                    Surface(
                        color = AppleGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "100% On-Track",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live On-Chain Activity Stream Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AppleGreen)
                    )
                    Text(
                        text = "REAL-TIME HARDWARE ATTESTATIONS",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleGreen,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ActivityStreamItem(
                        author = "toly.sol",
                        action = "Signed Step Verification (12,410 steps)",
                        time = "2m ago • Slot 298092451"
                    )
                    Divider(color = BorderSubtle, thickness = 0.5.dp)
                    ActivityStreamItem(
                        author = "mert_helius",
                        action = "Odd-Days Internet Detox Checked (<42m)",
                        time = "7m ago • Slot 298092388"
                    )
                    Divider(color = BorderSubtle, thickness = 0.5.dp)
                    ActivityStreamItem(
                        author = "solana_sensei",
                        action = "6:00 AM NTP Hardware Attestation",
                        time = "14m ago • Slot 298092210"
                    )
                    Divider(color = BorderSubtle, thickness = 0.5.dp)
                    ActivityStreamItem(
                        author = "node_98b1",
                        action = "Threshold Missed: 357.14 \$SKR Burned",
                        time = "28m ago • Slot 298091804",
                        isSlash = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footnote
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AppleGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "All ranks are cryptographically proven via Seeker hardware signatures and verifiable on Solana.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LeaderboardRow(
    rank: String,
    name: String,
    subtitle: String,
    streak: String,
    streakColor: Color,
    isUser: Boolean
) {
    Surface(
        color = if (isUser) AppleGreen.copy(alpha = 0.08f) else SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isUser) AppleGreen.copy(alpha = 0.35f) else BorderSubtle
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                "1" -> AppleYellow.copy(alpha = 0.2f)
                                "2" -> Color.White.copy(alpha = 0.2f)
                                "3" -> AppleOrange.copy(alpha = 0.2f)
                                else -> SurfaceElevated
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (rank) {
                            "1" -> AppleYellow
                            "2" -> Color.White
                            "3" -> AppleOrange
                            else -> TextSecondary
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) AppleGreen else TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = if (isUser) AppleGreen else SurfaceElevated,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = streak,
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color.Black else streakColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 3: VAULT / ESCROW & BADGES CONTENT
// -----------------------------------------------------------------------------
@Composable
fun VaultTabContent(
    walletAddress: String?,
    stakedAmount: Double,
    skrBalance: Double,
    onSettle: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val openSolscan: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://solscan.io"))
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Opening Solscan...", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Titanium Executive Vault Profile Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Certified Chip
                Surface(
                    color = AppleGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, AppleGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AppleGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "SEEKER SEED VAULT CERTIFIED",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppleGreen,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (walletAddress != null && !walletAddress.contains("SeekerPledgeDemo")) {
                        "${walletAddress.take(8)}...${walletAddress.takeLast(6)}"
                    } else {
                        "Seeker...7xK2 (Solana Devnet)"
                    },
                    fontFamily = PlusJakartaSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${stakedAmount.toInt()} \$SKR Locked in Smart Escrow",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleGreen
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Available Wallet Balance: ${String.format("%.2f", skrBalance)} \$SKR",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dual Action Bar: Settle + Solscan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = ApplePurple,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSettle()
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Settle & Claim Escrow",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(46.dp)
                            .clickable { openSolscan() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Solscan ↗",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats 2-Col
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Total Returned", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1,450 \$SKR",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleGreen,
                        fontFamily = PlusJakartaSans
                    )
                }
            }

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Total Burned", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100 \$SKR",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleRed,
                        fontFamily = PlusJakartaSans
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Soulbound cNFTs Header
        Text(
            text = "Soulbound Proof-of-Action cNFTs",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Badge 1: Data Master
            BadgeCard(
                title = "Data Master",
                subtitle = "Mobile Internet Detox",
                tag = "Rare cNFT",
                icon = Icons.Default.Language,
                color = AppleTeal,
                modifier = Modifier.weight(1f)
            )

            // Badge 2: 6 AM Club
            BadgeCard(
                title = "6 AM Club Hero",
                subtitle = "Consistent Early Riser",
                tag = "Epic cNFT",
                icon = Icons.Default.Alarm,
                color = AppleOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Badge 3: 10k Steps Pioneer
            BadgeCard(
                title = "10k Pioneer",
                subtitle = "10k Steps Marathon",
                tag = "Legendary cNFT",
                icon = Icons.Default.DirectionsWalk,
                color = AppleGreen,
                modifier = Modifier.weight(1f)
            )

            // Badge 4: Iron Will
            BadgeCard(
                title = "Iron Will Genesis",
                subtitle = "Zero Violations Streak",
                tag = "Mythic cNFT",
                icon = Icons.Default.Shield,
                color = ApplePurple,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Immutable Smart Contract Audit Trail Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AppleTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Immutable Smart Contract Audit Trail",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuditLedgerRow(
                        title = "Genesis Non-Custodial Escrow Locked",
                        meta = "PDA Anchor Vault • 2,500 \$SKR • Slot 298092440",
                        status = "Confirmed ✓"
                    )
                    Divider(color = BorderSubtle, thickness = 0.5.dp)
                    AuditLedgerRow(
                        title = "Day 1 Hardware Proof Certified",
                        meta = "Proof-of-Action Ed25519 • 0 burns • Slot 298092422",
                        status = "Confirmed ✓"
                    )
                    Divider(color = BorderSubtle, thickness = 0.5.dp)
                    AuditLedgerRow(
                        title = "Seed Vault Nonce Verification",
                        meta = "Hardware Nonce Verification • Slot 298092401",
                        status = "Confirmed ✓"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun BadgeCard(
    title: String,
    subtitle: String,
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
                    .border(0.5.dp, color.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = tag,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Solana Explorer ↗",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleTeal,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://explorer.solana.com"))
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }
    }
}

@Composable
fun TelemetrySpecBox(
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTeal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 10.5.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun EscrowGuaranteeRow(
    badge: String,
    text: String
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = badge,
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppleGreen
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                fontSize = 10.5.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun ActivityStreamItem(
    author: String,
    action: String,
    time: String,
    isSlash: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = author,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSlash) AppleRed else TextPrimary
                )
                Text(
                    text = action,
                    fontSize = 11.sp,
                    color = if (isSlash) AppleRed else TextSecondary
                )
            }
            Text(
                text = time,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun AuditLedgerRow(
    title: String,
    meta: String,
    status: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = meta,
                fontSize = 10.5.sp,
                color = TextSecondary
            )
        }
        Surface(
            color = AppleGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AppleGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 30-SECOND INTERACTIVE "HOW IT WORKS" MODAL
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowItWorksModal(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                Column {
                    Text(
                        text = "💡 How bozPLEDGE Works",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hardware-attested cryptographic habit accountability",
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = AppleTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Seeker Enclave",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "3-LAYER SEEKER SECURITY ARCHITECTURE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Layer 1
            HowItWorksStepCard(
                stepNumber = "L1",
                stepTitle = "Android Kernel & Sensor Telemetry",
                stepDesc = "Direct integration with Android OS NetworkStatsManager and Health Connect sensor pipelines. Telemetry runs isolated in hardware-backed services without manual reporting.",
                badgeColor = AppleTeal,
                icon = Icons.Default.Bolt
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Layer 2
            HowItWorksStepCard(
                stepNumber = "L2",
                stepTitle = "Local SHA-256 Digest & Session Nonce",
                stepDesc = "Daily metrics are sealed into an immutable digest: SHA-256(EpochDay + SensorMetric + Nonce + PDA). Protects against replay attacks and spoofed timestamps.",
                badgeColor = AppleOrange,
                icon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Layer 3
            HowItWorksStepCard(
                stepNumber = "L3",
                stepTitle = "Seed Vault Ed25519 Hardware Enclave",
                stepDesc = "Private keys never leave the secure hardware enclave. Biometric signing authorizes the milestone claim instruction directly to the Solana smart contract escrow.",
                badgeColor = AppleGreen,
                icon = Icons.Default.Shield
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ECONOMIC RULES & ESCROW LOGIC",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rule 1: 100% returned
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(18.dp))
                    Column {
                        Text(text = "100% Collateral Returned", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Complete all days to withdraw 100% of your staked \$SKR + earn Soulbound cNFTs.", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rule 2: Deflationary burn
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = AppleRed, modifier = Modifier.size(18.dp))
                    Column {
                        Text(text = "Deflationary On-Chain Burn", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Miss a daily milestone deadline, and that day's proportional collateral is burned forever.", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Button: Got it!
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Understood, Continue",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun HowItWorksStepCard(
    stepNumber: String,
    stepTitle: String,
    stepDesc: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = SurfaceDeep,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.16f))
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stepTitle,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stepDesc,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// JUDGE CONTROLS BOTTOM SHEET
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JudgeModalBottomSheet(
    currentInternetMins: Int,
    currentSteps: Int,
    isDemoMode: Boolean,
    onManualInternetChange: (Int) -> Unit,
    onManualStepsChange: (Int) -> Unit,
    onFastForwardDay: () -> Unit,
    onToggleFreshState: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "Next Day (+1d)", fontFamily = PlusJakartaSans, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFreshState()
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "Reset Demo", fontFamily = PlusJakartaSans, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
