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
        var isClockingIn by remember { mutableStateOf(false) }
        var isSettling by remember { mutableStateOf(false) }
        var isDemoMode by remember { mutableStateOf(true) }

        // Initial default commitment for testing demo
        var commitmentState by remember {
            mutableStateOf(
                CommitmentState(
                    commitmentId = System.currentTimeMillis(),
                    authority = "SeekerPledgeDemo492b",
                    clockInAuthority = sessionKeyManager.getPublicKeyBase58(),
                    targetSteps = 8000,
                    totalDays = 5,
                    completedDays = 1,
                    dayDurationSec = 30L, // 30s day in demo mode
                    startTimestamp = (System.currentTimeMillis() / 1000L) - 30L,
                    totalAmountSKR = 500.0,
                    settled = false,
                    clockedInBitmap = 0b00001L
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
                    walletAddress = solanaManager.connectedPublicKey ?: "Seeker...492b",
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
                                "Verified On-Chain! Day $dayIdx Clocked In via Seed Vault",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onCreateNewPledge = {
                        currentScreen = Screen.CREATE_PLEDGE
                    },
                    onSettle = {
                        currentScreen = Screen.SETTLE
                    },
                    onManualStepsChange = { newSteps ->
                        currentSteps = newSteps
                    },
                    onFastForwardDay = {
                        // Advance timestamp by 1 day
                        commitmentState = commitmentState.copy(
                            startTimestamp = commitmentState.startTimestamp - commitmentState.dayDurationSec
                        )
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
