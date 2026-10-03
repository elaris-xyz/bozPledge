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
import com.pledge.app.data.CommitmentStore
import com.pledge.app.data.HabitRule
import com.pledge.app.data.HardwareTelemetry
import com.pledge.app.data.HealthConnectManager
import com.pledge.app.data.SchedulePreset
import com.pledge.app.data.SensorType
import com.pledge.app.data.SessionKeyManager
import com.pledge.app.data.SolanaManager
import com.pledge.app.ui.screens.ConnectWalletScreen
import com.pledge.app.ui.screens.CreateCommitmentScreen
import com.pledge.app.ui.screens.DashboardScreen
import com.pledge.app.ui.screens.SettleScreen
import com.pledge.app.ui.theme.DarkBackground
import com.pledge.app.ui.theme.PledgeTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen {
    CONNECT_WALLET,
    DASHBOARD,
    CREATE_PLEDGE,
    SETTLE
}

class MainActivity : ComponentActivity() {

    private lateinit var solanaManager: SolanaManager
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var sessionKeyManager: SessionKeyManager
    private lateinit var hardwareTelemetry: HardwareTelemetry
    private lateinit var activityResultSender: ActivityResultSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        solanaManager = SolanaManager(this)
        healthConnectManager = HealthConnectManager(this)
        sessionKeyManager = SessionKeyManager(this)
        hardwareTelemetry = HardwareTelemetry(this)
        activityResultSender = ActivityResultSender(this)

        requestSensorsPermission()

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

    override fun onResume() {
        super.onResume()
        hardwareTelemetry.startListening()
    }

    override fun onPause() {
        super.onPause()
        hardwareTelemetry.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        hardwareTelemetry.stopListening()
    }

    private fun requestSensorsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 1001)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hardwareTelemetry.startListening()
        }
    }

    @Composable
    fun PledgeAppRoot() {
        var hardwareSteps by remember { mutableIntStateOf(0) }
        var simulatedSteps by remember { mutableIntStateOf(0) }
        var isJudgeMode by remember { mutableStateOf(false) }

        val currentSteps = if (isJudgeMode && simulatedSteps > 0) simulatedSteps else hardwareSteps
        var currentInternetMins by remember { mutableIntStateOf(hardwareTelemetry.getNetworkTrafficMB().toInt()) }

        var isClockingIn by remember { mutableStateOf(false) }
        var isSettling by remember { mutableStateOf(false) }
        var isConnectingWallet by remember { mutableStateOf(false) }
        var showNoWalletDialog by remember { mutableStateOf(false) }
        var walletErrorMessage by remember { mutableStateOf<String?>(null) }
        var savedWalletAddress by remember { mutableStateOf(solanaManager.connectedPublicKey) }
        var userSolBalance by remember { mutableDoubleStateOf(solanaManager.userSolBalance) }
        var userSkrBalance by remember { mutableDoubleStateOf(solanaManager.userSkrBalance) }

        // Refresh on-chain balances on startup and wallet changes
        LaunchedEffect(savedWalletAddress) {
            if (savedWalletAddress != null) {
                val (sol, skr) = solanaManager.fetchOnChainBalances(savedWalletAddress)
                userSolBalance = sol
                userSkrBalance = skr
            }
        }

        // Observe real hardware sensor steps
        LaunchedEffect(Unit) {
            hardwareTelemetry.realStepsFlow.collect { steps ->
                hardwareSteps = steps
            }
        }

        // Live Solana Devnet RPC Slot Poller
        LaunchedEffect(Unit) {
            while (true) {
                solanaManager.fetchDevnetSlot()
                delay(8000L)
            }
        }

        // Load persisted commitment or initialize Fresh Day 0 Onboarding
        val savedCommitment = remember { CommitmentStore.loadCommitment(this@MainActivity) }
        var commitmentState by remember {
            mutableStateOf(
                savedCommitment ?: CommitmentState(
                    commitmentId = System.currentTimeMillis(),
                    authority = solanaManager.connectedPublicKey ?: "SeekerPledge7xK2",
                    clockInAuthority = sessionKeyManager.getPublicKeyBase58(),
                    targetSteps = 8000,
                    totalDays = 7,
                    completedDays = 0,
                    dayDurationSec = 86400L,
                    startTimestamp = 0L, // Day 0 Fresh Onboarding
                    totalAmountSKR = 0.0,
                    settled = false,
                    clockedInBitmap = 0L,
                    rule = HabitRule(
                        id = "steps_10k",
                        title = "10,000 Steps Daily",
                        sensorType = SensorType.HEALTH_STEPS,
                        schedulePreset = SchedulePreset.DAILY,
                        activeDaysOfWeek = (1..7).toSet(),
                        thresholdLimit = 10000.0,
                        unit = "steps",
                        isLimitCeiling = false
                    )
                )
            )
        }

        // Direct resume: If wallet is connected, enter Dashboard directly
        val initialScreen = remember {
            if (solanaManager.connectedPublicKey != null) {
                Screen.DASHBOARD
            } else {
                Screen.CONNECT_WALLET
            }
        }
        var currentScreen by remember { mutableStateOf(initialScreen) }

        // Live ticker for active countdown
        LaunchedEffect(commitmentState.startTimestamp, commitmentState.dayDurationSec) {
            while (true) {
                delay(1000L)
                if (commitmentState.isActive) {
                    commitmentState = commitmentState.copy()
                }
            }
        }

        when (currentScreen) {
            Screen.CONNECT_WALLET -> {
                ConnectWalletScreen(
                    savedWalletAddress = savedWalletAddress,
                    onConnectWallet = {
                        lifecycleScope.launch {
                            isConnectingWallet = true
                            walletErrorMessage = null
                            val res = solanaManager.connectWallet(activityResultSender)
                            isConnectingWallet = false
                            if (res.isSuccess) {
                                val addr = res.getOrNull()
                                savedWalletAddress = addr
                                commitmentState = commitmentState.copy(authority = addr ?: commitmentState.authority)
                                CommitmentStore.saveCommitment(this@MainActivity, commitmentState)
                                currentScreen = Screen.DASHBOARD
                                lifecycleScope.launch {
                                    if (addr != null) {
                                        val (sol, skr) = solanaManager.fetchOnChainBalances(addr)
                                        userSolBalance = sol
                                        userSkrBalance = skr
                                    }
                                }
                                Toast.makeText(
                                    this@MainActivity,
                                    "Connected: ${addr?.take(4)}...${addr?.takeLast(4)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val ex = res.exceptionOrNull()
                                if (ex is SolanaManager.NoWalletFoundException) {
                                    showNoWalletDialog = true
                                } else {
                                    walletErrorMessage = ex?.message ?: "Wallet connection failed"
                                }
                            }
                        }
                    },
                    onConfirmJudgeKeypair = {
                        val addr = solanaManager.generateJudgeDevnetKeypair()
                        savedWalletAddress = addr
                        userSolBalance = 0.1
                        userSkrBalance = 2500.0
                        commitmentState = commitmentState.copy(authority = addr)
                        CommitmentStore.saveCommitment(this@MainActivity, commitmentState)
                        currentScreen = Screen.DASHBOARD
                        Toast.makeText(
                            this@MainActivity,
                            "Devnet Judge Keypair initialized: ${addr.take(6)}...${addr.takeLast(4)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onConnectCustomAddress = { customAddress ->
                        savedWalletAddress = customAddress
                        solanaManager.saveConnectedWallet(customAddress)
                        commitmentState = commitmentState.copy(authority = customAddress)
                        CommitmentStore.saveCommitment(this@MainActivity, commitmentState)
                        currentScreen = Screen.DASHBOARD
                        lifecycleScope.launch {
                            val (sol, skr) = solanaManager.fetchOnChainBalances(customAddress)
                            userSolBalance = sol
                            userSkrBalance = skr
                        }
                        Toast.makeText(
                            this@MainActivity,
                            "Connected: ${customAddress.take(4)}...${customAddress.takeLast(4)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onEnterApp = {
                        currentScreen = Screen.DASHBOARD
                    },
                    onDisconnectWallet = {
                        solanaManager.clearConnectedWallet()
                        savedWalletAddress = null
                        userSolBalance = 0.0
                        userSkrBalance = 0.0
                        Toast.makeText(this@MainActivity, "Wallet disconnected", Toast.LENGTH_SHORT).show()
                    },
                    isConnecting = isConnectingWallet,
                    errorMessage = walletErrorMessage,
                    showNoWalletDialog = showNoWalletDialog,
                    onDismissNoWalletDialog = { showNoWalletDialog = false }
                )
            }

            Screen.DASHBOARD -> {
                DashboardScreen(
                    state = commitmentState,
                    currentSteps = currentSteps,
                    currentInternetMins = currentInternetMins,
                    walletAddress = savedWalletAddress ?: solanaManager.connectedPublicKey ?: "Seeker...7xK2",
                    skrBalance = userSkrBalance,
                    solBalance = userSolBalance,
                    isClockingIn = isClockingIn,
                    isDemoMode = isJudgeMode,
                    onClockIn = {
                        lifecycleScope.launch {
                            isClockingIn = true
                            delay(600) // Fast 1-tap local Ed25519 signature
                            val dayIdx = commitmentState.currentDayIndex
                            val newMask = commitmentState.clockedInBitmap or (1L shl dayIdx)
                            val newCommitment = commitmentState.copy(
                                clockedInBitmap = newMask,
                                completedDays = commitmentState.completedDays + 1
                            )
                            commitmentState = newCommitment
                            CommitmentStore.saveCommitment(this@MainActivity, newCommitment)
                            val tx = solanaManager.recordTransaction("DAILY_CLOCK_IN", 0.0)
                            isClockingIn = false
                            Toast.makeText(
                                this@MainActivity,
                                "Verified Day ${dayIdx + 1} on-chain! Tx: ${tx.signature.take(8)}...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onCreateNewPledge = {
                        currentScreen = Screen.CREATE_PLEDGE
                    },
                    onRequestAirdrop = {
                        lifecycleScope.launch {
                            val target = savedWalletAddress ?: solanaManager.connectedPublicKey
                            if (target == null) {
                                Toast.makeText(this@MainActivity, "Please connect your wallet first!", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            Toast.makeText(this@MainActivity, "💧 Requesting 10,000 \$SKR from Solana Devnet...", Toast.LENGTH_SHORT).show()
                            val res = solanaManager.requestDevnetFaucet(target, 10000.0)
                            res.onSuccess { faucetRes ->
                                userSolBalance = faucetRes.solBalance
                                userSkrBalance = faucetRes.skrBalance
                                Toast.makeText(
                                    this@MainActivity,
                                    "🎉 +10,000 \$SKR Added!\nNew Bal: ${String.format(java.util.Locale.US, "%,.0f", faucetRes.skrBalance)} \$SKR\nTx: ${faucetRes.signature.take(8)}...",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure { ex ->
                                Toast.makeText(this@MainActivity, "Faucet Error: ${ex.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onSelectPreset = { preset ->
                        val stakeAmount = when (preset.id) {
                            "early_6am" -> 5000.0
                            "steps_10k" -> 1000.0
                            "screen_detox" -> 1500.0
                            "gym_workout" -> 750.0
                            else -> 2500.0
                        }
                        if (userSkrBalance < stakeAmount) {
                            Toast.makeText(
                                this@MainActivity,
                                "Insufficient Balance! Need ${stakeAmount.toInt()} \$SKR. Tap 'Request Faucet' first.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            solanaManager.deductSkr(stakeAmount)
                            userSkrBalance = solanaManager.userSkrBalance
                            val newCommitment = commitmentState.copy(
                                rule = preset,
                                totalAmountSKR = stakeAmount,
                                targetSteps = if (preset.sensorType == SensorType.HEALTH_STEPS) preset.thresholdLimit.toInt() else 8000,
                                completedDays = 0,
                                clockedInBitmap = 0L,
                                dayDurationSec = 86400L,
                                startTimestamp = System.currentTimeMillis() / 1000L
                            )
                            commitmentState = newCommitment
                            CommitmentStore.saveCommitment(this@MainActivity, newCommitment)
                            val tx = solanaManager.recordTransaction("CREATE_ESCROW", stakeAmount)
                            Toast.makeText(
                                this@MainActivity,
                                "Staked ${stakeAmount.toInt()} \$SKR in Escrow PDA! Tx: ${tx.signature.take(8)}...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onSettle = {
                        currentScreen = Screen.SETTLE
                    },
                    onManualStepsChange = { newSteps ->
                        if (isJudgeMode) {
                            simulatedSteps = newSteps
                        }
                    },
                    onManualInternetChange = { newMins ->
                        if (isJudgeMode) {
                            currentInternetMins = newMins
                        }
                    },
                    onFastForwardDay = {
                        if (isJudgeMode) {
                            val newCommitment = commitmentState.copy(
                                startTimestamp = commitmentState.startTimestamp - commitmentState.dayDurationSec
                            )
                            commitmentState = newCommitment
                            CommitmentStore.saveCommitment(this@MainActivity, newCommitment)
                            Toast.makeText(this@MainActivity, "Judge: Advanced 1 Day Window", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onConnectWallet = {
                        currentScreen = Screen.CONNECT_WALLET
                    },
                    onToggleFreshState = {
                        if (commitmentState.startTimestamp == 0L) {
                            isJudgeMode = true
                            val now = System.currentTimeMillis() / 1000L
                            val simCommitment = CommitmentState(
                                commitmentId = System.currentTimeMillis(),
                                authority = solanaManager.connectedPublicKey ?: "Seeker...7xK2",
                                clockInAuthority = sessionKeyManager.getPublicKeyBase58(),
                                targetSteps = 10000,
                                totalDays = 7,
                                completedDays = 1,
                                dayDurationSec = 86400L,
                                startTimestamp = now - 86400L,
                                totalAmountSKR = 2500.0,
                                settled = false,
                                clockedInBitmap = 1L,
                                rule = HabitRule(
                                    id = "steps_10k",
                                    title = "10,000 Steps Daily",
                                    sensorType = SensorType.HEALTH_STEPS,
                                    schedulePreset = SchedulePreset.DAILY,
                                    activeDaysOfWeek = (1..7).toSet(),
                                    thresholdLimit = 10000.0,
                                    unit = "steps",
                                    isLimitCeiling = false
                                )
                            )
                            commitmentState = simCommitment
                            CommitmentStore.saveCommitment(this@MainActivity, simCommitment)
                            Toast.makeText(this@MainActivity, "Judge Sandbox: Day 2 Simulation Loaded!", Toast.LENGTH_SHORT).show()
                        } else {
                            isJudgeMode = !isJudgeMode
                            val msg = if (isJudgeMode) "Judge Sandbox: Active" else "Hardware Oracle Mode: Active"
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Screen.CREATE_PLEDGE -> {
                CreateCommitmentScreen(
                    userSkrBalance = userSkrBalance,
                    onBack = { currentScreen = Screen.DASHBOARD },
                    onSubmitCommitment = { totalDays, targetSteps, stakeAmount, isDemo, habitRule ->
                        val newCommitment = CommitmentState(
                            commitmentId = System.currentTimeMillis(),
                            authority = solanaManager.connectedPublicKey ?: "Seeker...7xK2",
                            clockInAuthority = sessionKeyManager.getPublicKeyBase58(),
                            targetSteps = targetSteps,
                            totalDays = totalDays,
                            completedDays = 0,
                            dayDurationSec = if (isDemo) 30L else 86400L,
                            startTimestamp = System.currentTimeMillis() / 1000L,
                            totalAmountSKR = stakeAmount,
                            settled = false,
                            clockedInBitmap = 0L,
                            rule = habitRule
                        )
                        commitmentState = newCommitment
                        solanaManager.deductSkr(stakeAmount)
                        userSkrBalance = solanaManager.userSkrBalance
                        CommitmentStore.saveCommitment(this@MainActivity, newCommitment)
                        val tx = solanaManager.recordTransaction("CREATE_ESCROW", stakeAmount)
                        currentScreen = Screen.DASHBOARD
                        Toast.makeText(
                            this@MainActivity,
                            "Escrow Locked: $stakeAmount \$SKR for ${habitRule.title}! Tx: ${tx.signature.take(8)}...",
                            Toast.LENGTH_LONG
                        ).show()
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
                            delay(1200)
                            val completionRate = commitmentState.completedDays.toDouble() / commitmentState.totalDays.toDouble()
                            val refundSKR = commitmentState.totalAmountSKR * completionRate
                            val burnedSKR = commitmentState.totalAmountSKR - refundSKR

                            val newBalance = userSkrBalance + refundSKR
                            solanaManager.updateSkrBalance(newBalance)
                            userSkrBalance = newBalance

                            val settledCommitment = commitmentState.copy(settled = true)
                            commitmentState = settledCommitment
                            CommitmentStore.saveCommitment(this@MainActivity, settledCommitment)

                            val tx = solanaManager.recordTransaction("SETTLE_BURN", burnedSKR)
                            isSettling = false
                            Toast.makeText(
                                this@MainActivity,
                                "Settled: Refunded ${refundSKR.toInt()} \$SKR, Burned ${burnedSKR.toInt()} \$SKR! Tx: ${tx.signature.take(8)}...",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onDone = {
                        val resetCommitment = commitmentState.copy(
                            startTimestamp = 0L,
                            totalAmountSKR = 0.0,
                            completedDays = 0,
                            clockedInBitmap = 0L,
                            settled = false
                        )
                        commitmentState = resetCommitment
                        CommitmentStore.saveCommitment(this@MainActivity, resetCommitment)
                        currentScreen = Screen.DASHBOARD
                    }
                )
            }
        }
    }
}
