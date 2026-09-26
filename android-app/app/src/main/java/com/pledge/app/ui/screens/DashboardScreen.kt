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
            // Bottom Navigation Dock with 4 fully functional tabs
            Surface(
                color = SurfaceDeep.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedNavTab = index
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) SolanaMint.copy(alpha = 0.16f) else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) SolanaMint else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
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
        ) {
            // 1. TOP APP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        SolanaMint.copy(alpha = 0.22f),
                                        SolanaPurple.copy(alpha = 0.28f)
                                    )
                                )
                            )
                            .border(1.dp, SolanaMint.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Logo",
                            tint = SolanaMint,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Row {
                        Text(
                            text = "boz",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "PLEDGE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SolanaMint,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // Header Actions: "How It Works" + "Judge" + "Connect"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // How It Works Pill Button
                    Surface(
                        color = GoldGenesis.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldGenesis.copy(alpha = 0.35f)),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isHowItWorksOpen = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "راهنما",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldGenesis
                            )
                        }
                    }

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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Judge",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) SolanaMint else CrimsonBurn)
                            )
                            Text(
                                text = if (isConnected) "${walletAddress?.take(4)}...${walletAddress?.takeLast(4)}" else "Connect",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
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
                            currentRuleId = state.rule.id,
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
    internetDetoxRule: HabitRule,
    early6amRule: HabitRule,
    stepGoalRule: HabitRule,
    screenDetoxRule: HabitRule,
    pulseScale: Float,
    selectedChipIndex: Int,
    onSelectChipIndex: (Int) -> Unit
) {
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
        // Helpful Orientation Tip Banner for new users
        if (showTipBanner) {
            Surface(
                color = SolanaPurple.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaPurple.copy(alpha = 0.35f)),
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
                            Text(text = "💡", fontSize = 14.sp)
                            Text(
                                text = "راهنمای سریع این صفحه",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolanaMint
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
                        text = "هدف فعال شما: ${state.rule.title} است. سنسور گوشی عملکرد شما را می‌سنجد. اگر هدف روزانه رعایت شده باشد، دکمه تایید سبز می‌شود و با یک لمس امتیاز روز را ثبت می‌کنید.",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "چطور کار می‌کنه؟ (مشاهده ویدیویی/گرافیکی) →",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolanaMint,
                            modifier = Modifier.clickable { onOpenHowItWorks() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Hardware Telemetry Status Bar
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
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SolanaMint)
                    )
                    Text(
                        text = when (state.rule.sensorType) {
                            SensorType.NETWORK_DATA -> "سنسور دیتای شبکه • فعال و متصل"
                            SensorType.WAKE_UP_CLOCK -> "ساعت سخت‌افزاری • متصل"
                            else -> "سنسور گام‌شمار • متصل"
                        },
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
                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaTeal.copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCreateNewPledge()
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp, 12.dp)) {
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
                            text = "طراحی چالش دلخواه",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "تنظیم سنسور و وثیقه شخصی",
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
                Column(modifier = Modifier.padding(12.dp, 12.dp)) {
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
                            text = "باشگاه سحرخیزی",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "بیداری قبل از ۶ صبح با ۵k وثیقه",
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Habit Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEachIndexed { index, title ->
                val isSelected = selectedChipIndex == index
                Surface(
                    color = if (isSelected) SolanaTeal.copy(alpha = 0.14f) else SurfaceDeep,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SolanaTeal else BorderSubtle
                    ),
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
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (isSelected) GoldGenesis else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Urgency Loss Aversion Banner (Daily Window Deadline)
        Surface(
            color = CrimsonBurn.copy(alpha = 0.09f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBurn.copy(alpha = 0.25f)),
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
                            text = "مهلت ثبت امروز / Daily Deadline",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CrimsonBurn
                        )
                    }
                    Text(
                        text = "در صورت نقض: ${String.format("%.1f", dailyBurn)} \$SKR سوزانده می‌شود",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = CrimsonBurn.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBurn.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (countdownStr.isNotEmpty()) countdownStr else "04h 28m 14s",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CrimsonBurn,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Commitment Card (Primary Focal Point)
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Top Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge 1: Sensor & Schedule
                    Surface(
                        color = SolanaTeal.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SolanaTeal.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isInternetSensor) Icons.Default.Language else if (isWakeUpSensor) Icons.Default.Alarm else Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = SolanaTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = state.rule.title.uppercase(),
                                fontSize = 9.5.sp,
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
                                text = "${state.totalAmountSKR.toInt()} \$SKR وثیقه",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolanaPurple,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Metric Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "$currentVal",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = state.rule.unit,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Text(
                    text = if (isInternetSensor) {
                        "مصرف شده از سقف مجاز $limitVal دقیقه برای امروز"
                    } else if (isWakeUpSensor) {
                        "ساعت بیداری ثبت‌شده (سقف مجاز: قبل از 06:00 AM)"
                    } else {
                        "ثبت‌شده از هدف روزانه $limitVal قدم"
                    },
                    fontSize = 12.5.sp,
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
                        text = if (isWithinLimit) "✓ در محدوده مجاز • آماده ثبت وضعیت" else "⚠️ فراتر از حد مجاز • در معرض جریمه",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWithinLimit) SolanaMint else CrimsonBurn
                    )
                    Text(
                        text = "$progressPercent%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWithinLimit) SolanaMint else CrimsonBurn
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                            Text(text = "زمان‌بندی: ", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (isInternetSensor) "روزهای فرد (دوشنبه، چهارشنبه، جمعه، یکشنبه)" else "همه روزها (روزانه)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Row {
                            Text(text = "سنسور: ", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = state.rule.sensorType.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolanaMint
                            )
                        }
                        Row {
                            Text(text = "تایید سخت‌افزاری: ", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "Seeker Ed25519 Nonce #84201 (ضد تقلب)",
                                fontSize = 11.sp,
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
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "روز $currentDay از $totalDays",
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

        // PRIMARY ACTION BUTTON: VERIFY & CHECK-IN
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
                .height(60.dp)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
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
                                    isAlreadyClockedInToday -> "وضعیت امروز تایید و ثبت شد (VERIFIED)"
                                    canClockIn -> "ثبت و تایید موفقیت امروز (VERIFY & CHECK-IN)"
                                    else -> "هدف امروز هنوز رعایت نشده است"
                                },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp,
                                color = if (canClockIn && !isAlreadyClockedInToday) BgVoid else if (isAlreadyClockedInToday) SolanaMint else TextMuted
                            )
                        }
                        if (canClockIn && !isAlreadyClockedInToday) {
                            Text(
                                text = "تایید سنسور سخت‌افزاری با امضای امن Seed Vault بدون کارمزد",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BgVoid.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
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
                    text = "اشتراک‌گذاری به عنوان Solana Blink در توییتر/ایکس",
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
// TAB 1: EXPLORE / CATALOG CONTENT
// -----------------------------------------------------------------------------
@Composable
fun ExploreTabContent(
    currentRuleId: String,
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
                    text = "کاتالوگ چالش‌های اثبات‌شده",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "عادات هوشمند متصل به سنسورهای واقعی سخت‌افزار Seeker",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Surface(
                color = SolanaTeal.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaTeal.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCreateCustom()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = SolanaTeal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "+ دلخواه",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaTeal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Blueprint 1: Odd-Days Internet Detox
        ExploreCardItem(
            title = "سم‌زدایی اینترنت روزهای فرد (<1h)",
            subtitle = "NetworkStats • دوشنبه/چهارشنبه/جمعه/یکشنبه • ۲,۵۰۰ \$SKR",
            icon = Icons.Default.Language,
            accentColor = SolanaTeal,
            isActive = currentRuleId == internetDetoxRule.id,
            onActionClick = { onSelectRule(internetDetoxRule) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Blueprint 2: The 6:00 AM Club
        ExploreCardItem(
            title = "باشگاه سحرخیزی ۶:۰۰ صبح (High Stakes)",
            subtitle = "ساعت سخت‌افزاری سیستم NTP • ۷ روز • ۵,۰۰۰ \$SKR وثیقه",
            icon = Icons.Default.Alarm,
            accentColor = GoldGenesis,
            isActive = currentRuleId == early6amRule.id,
            onActionClick = { onSelectRule(early6amRule) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Blueprint 3: 10,000 Steps Marathon
        ExploreCardItem(
            title = "ماراتن ۱۰,۰۰۰ قدم روزانه",
            subtitle = "سنسور گام‌شمار Health Connect • ۷ روز • ۱,۰۰۰ \$SKR وثیقه",
            icon = Icons.Default.DirectionsWalk,
            accentColor = SolanaMint,
            isActive = currentRuleId == stepGoalRule.id,
            onActionClick = { onSelectRule(stepGoalRule) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Blueprint 4: Social Media Detox
        ExploreCardItem(
            title = "ترک و محدودیت شبکه‌های اجتماعی (<2h)",
            subtitle = "سنسور زمان برنامه UsageStats • ۱۴ روز • ۱,۵۰۰ \$SKR وثیقه",
            icon = Icons.Default.Tune,
            accentColor = SolanaPurple,
            isActive = currentRuleId == screenDetoxRule.id,
            onActionClick = { onSelectRule(screenDetoxRule) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Blueprint 5: Daily 45m Gym Workout
        ExploreCardItem(
            title = "ورزش روزانه باشگاه ۴۵ دقیقه‌ای",
            subtitle = "سنسور ضربان قلب و کالری • ۷ روز • ۷۵۰ \$SKR وثیقه",
            icon = Icons.Default.LocalFireDepartment,
            accentColor = SolanaTeal,
            isActive = currentRuleId == gymWorkoutRule.id,
            onActionClick = { onSelectRule(gymWorkoutRule) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info foot note
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💡", fontSize = 14.sp)
                Text(
                    text = "با لمس دکمه «فعال‌سازی»، چالش بلافاصله روی سنسورهای گوشی شما فعال شده و به تب تعهد فعال منتقل می‌شوید.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ExploreCardItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isActive: Boolean,
    onActionClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) accentColor.copy(alpha = 0.5f) else BorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onActionClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = if (isActive) accentColor.copy(alpha = 0.18f) else accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActive) accentColor else accentColor.copy(alpha = 0.35f)
                ),
                modifier = Modifier.clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onActionClick()
                }
            ) {
                Text(
                    text = if (isActive) "فعال (ACTIVE)" else "فعال‌سازی",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 2: RANKS / COMMUNITY LEADERBOARD CONTENT
// -----------------------------------------------------------------------------
@Composable
fun RanksTabContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "رتبه‌بندی پروتکل Seeker (Protocol Ranks)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = "اثبات پایبندی جامعه کاربران بدون امکان تقلب",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2-Col Protocol Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBurn.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "مجموع سوخت پروتکل", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "48,250 \$SKR",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = CrimsonBurn,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "جریمه افراد بدقول 🔥", fontSize = 10.sp, color = TextMuted)
                }
            }

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaMint.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "نرخ پایبندی جامعه", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "92.4%",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = SolanaMint,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "موفقیت در تعهدات ✨", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "برترین‌های تعهد و استقامت",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Rank 1
        LeaderboardRow(
            rank = "1",
            name = "Toly.sol",
            subtitle = "میانگین ۱۴,۲۰۰ قدم • ۰ سوختگی",
            streak = "28d STREAK",
            streakColor = GoldGenesis,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 2
        LeaderboardRow(
            rank = "2",
            name = "Mert_Helius",
            subtitle = "میانگین ۱۱,۸۰۰ قدم • ۰ سوختگی",
            streak = "21d STREAK",
            streakColor = GoldGenesis,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 3 (You)
        LeaderboardRow(
            rank = "3",
            name = "شما (Seeker Genesis)",
            subtitle = "۲,۵۰۰ \$SKR وثیقه • سم‌زدایی اینترنت",
            streak = "4d STREAK",
            streakColor = SolanaMint,
            isUser = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 4
        LeaderboardRow(
            rank = "4",
            name = "Raj_Gokal",
            subtitle = "سحرخیزی ۶:۰۰ صبح • ۵,۰۰۰ \$SKR وثیقه",
            streak = "12d STREAK",
            streakColor = SolanaTeal,
            isUser = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rank 5
        LeaderboardRow(
            rank = "5",
            name = "Austin_Federa",
            subtitle = "سم‌زدایی اینترنت • ۰ سوختگی",
            streak = "9d STREAK",
            streakColor = SolanaTeal,
            isUser = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Footnote
        Surface(
            color = SurfaceDeep,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
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
                    tint = SolanaMint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "کلیه رتبه‌ها بر اساس امضاهای رمزنگاری‌شده سخت‌افزار Seeker ثبت شده و قابل دستکاری نیستند.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
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
        color = if (isUser) SolanaMint.copy(alpha = 0.06f) else SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUser) SolanaMint.copy(alpha = 0.45f) else BorderSubtle
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = rank,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isUser) SolanaMint else if (rank == "1") GoldGenesis else TextSecondary
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUser) SolanaMint.copy(alpha = 0.2f) else SurfaceDeep
                        )
                        .border(
                            1.dp,
                            if (isUser) SolanaMint else BorderSubtle,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Check else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isUser) SolanaMint else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.5.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = streakColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, streakColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = streak,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = streakColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Vault Profile Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SolanaMint.copy(alpha = 0.12f))
                        .border(1.dp, SolanaMint.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SolanaMint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (walletAddress != null && !walletAddress.contains("SeekerPledgeDemo")) {
                        "${walletAddress.take(8)}...${walletAddress.takeLast(6)}"
                    } else {
                        "Seeker...7xK2 (متصل به Devnet)"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )

                Text(
                    text = "${stakedAmount.toInt()} \$SKR در گاوصندوق هوشمند وثیقه شده",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SolanaMint,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "موجودی آزاد کیف‌پول: ${String.format("%.1f", skrBalance)} \$SKR",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Button: Settle & Claim
        Surface(
            color = SolanaPurple,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "برداشت و تسویه تعهدات (Settle & Claim)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "کل وثیقه بازگشته", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1,450 \$SKR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = SolanaMint,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "کل مبالغ جریمه‌شده", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100 \$SKR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CrimsonBurn,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Soulbound cNFTs Header
        Text(
            text = "نشان‌های افتخار غیرقابل انتقال (Soulbound cNFTs)",
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
                subtitle = "سم‌زدایی موفق اینترنت",
                tag = "cNFT Verified ✓",
                icon = Icons.Default.Language,
                color = SolanaTeal,
                modifier = Modifier.weight(1f)
            )

            // Badge 2: 6 AM Club
            BadgeCard(
                title = "6 AM Club Hero",
                subtitle = "سحرخیزی پیوسته",
                tag = "cNFT Verified ✓",
                icon = Icons.Default.Alarm,
                color = GoldGenesis,
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
                subtitle = "ماراتن ۱۰ هزار قدم",
                tag = "cNFT Verified ✓",
                icon = Icons.Default.DirectionsWalk,
                color = SolanaMint,
                modifier = Modifier.weight(1f)
            )

            // Badge 4: Iron Will
            BadgeCard(
                title = "Iron Will Genesis",
                subtitle = "اراده آهنین بدون نقض",
                tag = "Genesis cNFT ✓",
                icon = Icons.Default.Shield,
                color = SolanaPurple,
                modifier = Modifier.weight(1f)
            )
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
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
                    .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
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
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = tag,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
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
                        text = "💡 bozPLEDGE چطور کار می‌کنه؟",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "راهنمای ۳۰ ثانیه‌ای برای موفقیت در اهداف و مسابقه",
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = SolanaMint.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "۳ مرحله ساده",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolanaMint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1
            HowItWorksStepCard(
                stepNumber = "۱",
                stepTitle = "شرط‌بندی روی اراده خودت (Stake Deposit)",
                stepDesc = "برای هر عادت (ترک اینترنت، ورزش یا سحرخیزی)، مقداری توکن \$SKR در گاوصندوق قرارداد هوشمند وثیقه می‌گذاری تا انگیزه قطعی داشته باشی و بهانه‌تراشی نکنی.",
                badgeColor = SolanaPurple,
                icon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 2
            HowItWorksStepCard(
                stepNumber = "۲",
                stepTitle = "راستی‌آزمایی با سنسور گوشی (Hardware Proof)",
                stepDesc = "نیازی به ثبت دستی نیست و امکان دروغ گفتن وجود نداره! سنسورهای واقعی گوشی (مصرف اینترنت، گام‌شمار، ساعت بیداری) عملکردت رو اتوماتیک و ضد تقلب تایید می‌کنن.",
                badgeColor = SolanaTeal,
                icon = Icons.Default.Bolt
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 3
            HowItWorksStepCard(
                stepNumber = "۳",
                stepTitle = "پاداش و نشان یا جریمه (Win or Burn)",
                stepDesc = "اگر به قولت عمل کنی، کل پولت به همراه نشان افتخار Soulbound cNFT برمی‌گرده. اما اگر بدقولی کنی، بخشی از توکن‌های وثیقه سوزانده میشه!",
                badgeColor = SolanaMint,
                icon = Icons.Default.EmojiEvents
            )

            Spacer(modifier = Modifier.height(18.dp))

            // CTA Button: Got it!
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolanaMint),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "متوجه شدم، شروع کنیم! (Got It, Let's Start)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    color = BgVoid
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
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
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
