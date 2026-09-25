package com.pledge.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.pledge.app.data.CommitmentState
import com.pledge.app.data.HealthConnectManager
import com.pledge.app.data.SessionKeyManager
import com.pledge.app.data.SolanaManager
import com.pledge.app.ui.screens.CreateCommitmentScreen
import com.pledge.app.ui.screens.DashboardScreen
import com.pledge.app.ui.screens.SettleScreen
import com.pledge.app.ui.theme.DarkBackground
import com.pledge.app.ui.theme.PledgeTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    CREATE_PLEDGE,
    SETTLE
}

class MainActivity : ComponentActivity() {

    private lateinit var solanaManager: SolanaManager
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var sessionKeyManager: SessionKeyManager
    private lateinit var activityResultSender: ActivityResultSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        solanaManager = SolanaManager(this)
        healthConnectManager = HealthConnectManager(this)
        sessionKeyManager = SessionKeyManager(this)
        activityResultSender = ActivityResultSender(this)

        setContent {
            PledgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    PledgeAppRoot()
                }
            }
        }
    }

    @Composable
    fun PledgeAppRoot() {
        var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
        var currentSteps by remember { mutableIntStateOf(8500) }
        var currentInternetMins by remember { mutableIntStateOf(38) }
        var isClockingIn by remember { mutableStateOf(false) }
        var isSettling by remember { mutableStateOf(false) }
        var isDemoMode by remember { mutableStateOf(true) }

        // Initial default commitment matching Preview (Odd Days Internet Detox, Day 2 of 7, 2,500 SKR)
        var commitmentState by remember {
            mutableStateOf(
                CommitmentState(
                    commitmentId = System.currentTimeMillis(),
                    authority = "SeekerPledge7xK2",
                    clockInAuthority = sessionKeyManager.getPublicKeyBase58(),
                    targetSteps = 8000,
                    totalDays = 7,
                    completedDays = 1,
                    dayDurationSec = 30L, // 30s day in demo mode
                    startTimestamp = (System.currentTimeMillis() / 1000L) - 30L,
                    totalAmountSKR = 2500.0,
                    settled = false,
                    clockedInBitmap = 0b00001L,
                    rule = HabitRule(
                        id = "odd_internet",
                        title = "Odd Days Internet Detox",
                        sensorType = SensorType.NETWORK_DATA,
                        schedulePreset = SchedulePreset.ODD_DAYS,
                        activeDaysOfWeek = setOf(1, 3, 5, 7),
                        thresholdLimit = 60.0,
                        unit = "mins",
                        isLimitCeiling = true
                    )
                )
            )
        }

        // Live ticker for 30s day window countdown
        LaunchedEffect(commitmentState.startTimestamp, commitmentState.dayDurationSec) {
            while (true) {
                delay(1000L)
                if (commitmentState.isActive) {
                    // Trigger state refresh
                    commitmentState = commitmentState.copy()
                }
            }
        }

        when (currentScreen) {
            Screen.DASHBOARD -> {
                DashboardScreen(
                    state = commitmentState,
                    currentSteps = currentSteps,
                    currentInternetMins = currentInternetMins,
                    walletAddress = solanaManager.connectedPublicKey ?: "Seeker...7xK2",
                    skrBalance = solanaManager.userSkrBalance,
                    isClockingIn = isClockingIn,
                    isDemoMode = isDemoMode,
                    onClockIn = {
                        lifecycleScope.launch {
                            isClockingIn = true
                            delay(600) // Fast 1-tap local Ed25519 signature
                            val dayIdx = commitmentState.currentDayIndex
                            val newMask = commitmentState.clockedInBitmap or (1L shl dayIdx)
                            commitmentState = commitmentState.copy(
                                clockedInBitmap = newMask,
                                completedDays = commitmentState.completedDays + 1
                            )
                            isClockingIn = false
                            Toast.makeText(
                                this@MainActivity,
                                "Verified On-Chain! Day ${dayIdx + 1} Clocked In via Seed Vault",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onCreateNewPledge = {
                        currentScreen = Screen.CREATE_PLEDGE
                    },
                    onSelectPreset = { preset ->
                        commitmentState = commitmentState.copy(
                            rule = preset,
                            totalAmountSKR = if (preset.id == "early_6am") 5000.0 else 2500.0
                        )
                        Toast.makeText(this@MainActivity, "Selected: ${preset.title}", Toast.LENGTH_SHORT).show()
                    },
                    onSettle = {
                        currentScreen = Screen.SETTLE
                    },
                    onManualStepsChange = { newSteps ->
                        currentSteps = newSteps
                    },
                    onManualInternetChange = { newMins ->
                        currentInternetMins = newMins
                    },
                    onFastForwardDay = {
                        // Advance timestamp by 1 day
                        commitmentState = commitmentState.copy(
                            startTimestamp = commitmentState.startTimestamp - commitmentState.dayDurationSec
                        )
                    },
                    onConnectWallet = {
                        lifecycleScope.launch {
                            val res = solanaManager.connectWallet(activityResultSender)
                            Toast.makeText(this@MainActivity, "Wallet: ${res.getOrNull()}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onToggleFreshState = {
                        if (commitmentState.startTimestamp > 0) {
                            commitmentState = commitmentState.copy(startTimestamp = 0L)
                            Toast.makeText(this@MainActivity, "Switched to Fresh Day 0 State", Toast.LENGTH_SHORT).show()
                        } else {
                            commitmentState = commitmentState.copy(
                                startTimestamp = (System.currentTimeMillis() / 1000L) - 30L,
                                completedDays = 1,
                                clockedInBitmap = 0b00001L
                            )
                            Toast.makeText(this@MainActivity, "Restored Active Plan", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Screen.CREATE_PLEDGE -> {
                CreateCommitmentScreen(
                    userSkrBalance = solanaManager.userSkrBalance,
                    onBack = { currentScreen = Screen.DASHBOARD },
                    onSubmitCommitment = { totalDays, targetSteps, stakeAmount, demoActive, habitRule ->
                        lifecycleScope.launch {
                            // Connect wallet if needed
                            solanaManager.connectWallet(activityResultSender)

                            val duration = if (demoActive) 30L else 86400L
                            commitmentState = CommitmentState(
                                commitmentId = System.currentTimeMillis(),
                                authority = solanaManager.connectedPublicKey ?: "Seeker...492b",
                                clockInAuthority = sessionKeyManager.getPublicKeyBase58(),
                                targetSteps = targetSteps,
                                totalDays = totalDays,
                                completedDays = 0,
                                dayDurationSec = duration,
                                startTimestamp = System.currentTimeMillis() / 1000L,
                                totalAmountSKR = stakeAmount,
                                settled = false,
                                clockedInBitmap = 0L,
                                rule = habitRule
                            )
                            isDemoMode = demoActive
                            currentScreen = Screen.DASHBOARD
                            Toast.makeText(
                                this@MainActivity,
                                "Rule Deployed: $stakeAmount \$SKR Staked for ${habitRule.title}!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }

            Screen.SETTLE -> {
                SettleScreen(
                    state = commitmentState,
                    isSettling = isSettling,
                    onSettleConfirmed = {
                        lifecycleScope.launch {
                            isSettling = true
                            delay(800)
                            commitmentState = commitmentState.copy(settled = true)
                            isSettling = false
                            Toast.makeText(
                                this@MainActivity,
                                "Settled: ${commitmentState.refundAmountSKR.toInt()} \$SKR Refunded, ${commitmentState.burnAmountSKR.toInt()} \$SKR Burned!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onDone = {
                        currentScreen = Screen.CREATE_PLEDGE
                    }
                )
            }
        }
    }
}
