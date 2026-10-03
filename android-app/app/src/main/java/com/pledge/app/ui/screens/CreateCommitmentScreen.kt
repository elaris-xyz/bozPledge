package com.pledge.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.data.HabitRule
import com.pledge.app.data.SchedulePreset
import com.pledge.app.data.SensorType
import com.pledge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommitmentScreen(
    userSkrBalance: Double,
    onBack: () -> Unit,
    onSubmitCommitment: (totalDays: Int, targetSteps: Int, stakeAmountSKR: Double, isDemoMode: Boolean, habitRule: HabitRule) -> Unit,
    modifier: Modifier = Modifier
) {
    var isComposerTab by remember { mutableStateOf(false) }

    // Presets
    var selectedPresetIndex by remember { mutableStateOf(0) }
    val presets = listOf(
        HabitRule(
            id = "odd_internet",
            title = "Odd Days Internet Detox",
            sensorType = SensorType.NETWORK_DATA,
            schedulePreset = SchedulePreset.ODD_DAYS,
            activeDaysOfWeek = setOf(1, 3, 5, 7),
            thresholdLimit = 60.0,
            unit = "mins",
            isLimitCeiling = true
        ),
        HabitRule(
            id = "early_6am",
            title = "6:00 AM Wake-Up Club",
            sensorType = SensorType.WAKE_UP_CLOCK,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 6.0,
            unit = "AM",
            isLimitCeiling = true
        ),
        HabitRule(
            id = "steps_10k",
            title = "10,000 Steps Daily",
            sensorType = SensorType.HEALTH_STEPS,
            schedulePreset = SchedulePreset.DAILY,
            activeDaysOfWeek = (1..7).toSet(),
            thresholdLimit = 10000.0,
            unit = "steps",
            isLimitCeiling = false
        ),
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
    )

    // Composer custom state
    var customSensor by remember { mutableStateOf(SensorType.NETWORK_DATA) }
    var customSchedule by remember { mutableStateOf(SchedulePreset.ODD_DAYS) }
    var customLimitText by remember { mutableStateOf("1.0") }
    var customUnit by remember { mutableStateOf("Hours") }
    var customStakeText by remember { mutableStateOf("2500") }
    var selectedDays by remember { mutableStateOf(7) }
    var isDemoMode by remember { mutableStateOf(true) }

    val activeRule = if (isComposerTab) {
        val limit = customLimitText.toDoubleOrNull() ?: 1.0
        HabitRule(
            id = "custom_rule",
            title = "Custom: ${customSensor.displayName}",
            sensorType = customSensor,
            schedulePreset = customSchedule,
            activeDaysOfWeek = if (customSchedule == SchedulePreset.ODD_DAYS) setOf(1, 3, 5, 7) else if (customSchedule == SchedulePreset.EVEN_DAYS) setOf(2, 4, 6) else (1..7).toSet(),
            thresholdLimit = limit,
            unit = customUnit,
            isLimitCeiling = (customSensor == SensorType.NETWORK_DATA || customSensor == SensorType.SCREEN_DETOX || customSensor == SensorType.WAKE_UP_CLOCK)
        )
    } else {
        presets[selectedPresetIndex]
    }

    val stakeAmount = if (isComposerTab) {
        customStakeText.toDoubleOrNull() ?: 2500.0
    } else {
        when (selectedPresetIndex) {
            0 -> 2500.0
            1 -> 5000.0
            2 -> 1000.0
            else -> 1000.0
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sovereign Escrow Studio",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Hardware-Attested Smart Contract Deployer",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgVoid)
            )
        },
        containerColor = BgVoid
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 840.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
            // Apple Segmented Control: Verified Blueprints vs Custom Rule Engine
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isComposerTab) SurfaceCard else Color.Transparent)
                            .clickable { isComposerTab = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Verified Blueprints",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (!isComposerTab) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = if (!isComposerTab) TextPrimary else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isComposerTab) SurfaceCard else Color.Transparent)
                            .clickable { isComposerTab = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Smart Rule Studio",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isComposerTab) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = if (isComposerTab) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!isComposerTab) {
                // Preset habits list (Apple Inset Grouped)
                Text(
                    text = "SELECT HABIT BLUEPRINT",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        presets.forEachIndexed { index, preset ->
                            val isSelected = selectedPresetIndex == index
                            val presetStake = when(index) {
                                0 -> "2,500 \$SKR"
                                1 -> "5,000 \$SKR"
                                2 -> "1,000 \$SKR"
                                else -> "1,000 \$SKR"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPresetIndex = index }
                                    .background(if (isSelected) AppleGreen.copy(alpha = 0.08f) else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.title,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) AppleGreen else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${preset.sensorType.apiSource} • ${preset.schedulePreset.displayName}",
                                        fontSize = 11.5.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = presetStake,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = if (isSelected) AppleGreen else TextSecondary
                                )
                            }
                            if (index < presets.size - 1) {
                                Divider(color = BorderSubtle, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            } else {
                // Executive Rule Composer (No numbered survey headers)
                Text(
                    text = "HARDWARE TELEMETRY PIPELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorType.values().take(2).forEach { sensor ->
                        val isSel = customSensor == sensor
                        Surface(
                            color = if (isSel) AppleGreen.copy(alpha = 0.12f) else SurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSel) AppleGreen.copy(alpha = 0.5f) else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { customSensor = sensor }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sensor.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) AppleGreen else TextPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorType.values().drop(2).forEach { sensor ->
                        val isSel = customSensor == sensor
                        Surface(
                            color = if (isSel) AppleGreen.copy(alpha = 0.12f) else SurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSel) AppleGreen.copy(alpha = 0.5f) else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { customSensor = sensor }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sensor.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) AppleGreen else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enforcement Cadence
                Text(
                    text = "ENFORCEMENT CADENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        SchedulePreset.ODD_DAYS to "Odd Days",
                        SchedulePreset.EVEN_DAYS to "Even Days",
                        SchedulePreset.DAILY to "Daily (7d)"
                    ).forEach { (sched, label) ->
                        val isSel = customSchedule == sched
                        Surface(
                            color = if (isSel) AppleTeal.copy(alpha = 0.12f) else SurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSel) AppleTeal.copy(alpha = 0.5f) else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { customSchedule = sched }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) AppleTeal else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Limit & Staking Inputs
                Text(
                    text = "DAILY VERIFICATION THRESHOLD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customLimitText,
                        onValueChange = { customLimitText = it },
                        label = { Text("Target Limit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleTeal,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        )
                    )

                    OutlinedTextField(
                        value = customUnit,
                        onValueChange = { customUnit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleTeal,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Staking & Collateral
                Text(
                    text = "ESCROW COLLATERAL DEPOSIT (\$SKR)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Staking Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1000" to "1K", "2500" to "2.5K", "5000" to "5K", "10000" to "10K").forEach { (amount, label) ->
                        val isSel = customStakeText == amount
                        Surface(
                            color = if (isSel) AppleGreen.copy(alpha = 0.15f) else SurfaceCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSel) AppleGreen.copy(alpha = 0.4f) else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { customStakeText = amount }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$label \$SKR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) AppleGreen else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customStakeText,
                    onValueChange = { customStakeText = it },
                    label = { Text("Custom Amount (\$SKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleGreen,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Demo Mode Sandbox Card
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Hackathon Fast-Forward Mode",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AppleTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Compresses 24h into 30s for rapid judge evaluation & verification.",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = { isDemoMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = AppleTeal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Executive Smart Contract Specifications Card (Replaces student IF-THEN code box)
            val dailyLoss = String.format("%.2f", stakeAmount / 7)
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AppleTeal,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "SMART ESCROW SPECIFICATIONS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppleTeal,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Contract Spec Table
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContractSpecRow(
                            label = "Sensor Verification",
                            value = "${activeRule.sensorType.displayName} < ${activeRule.thresholdLimit} ${activeRule.unit}"
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ContractSpecRow(
                            label = "Active Cadence",
                            value = activeRule.schedulePreset.displayName
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ContractSpecRow(
                            label = "Escrow PDA Stake",
                            value = "${String.format("%,.0f", stakeAmount)} \$SKR"
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ContractSpecRow(
                            label = "Daily Slashing Risk",
                            value = "$dailyLoss \$SKR burned per failure",
                            valueColor = AppleRed
                        )
                        Divider(color = BorderSubtle, thickness = 0.5.dp)
                        ContractSpecRow(
                            label = "Enclave Authorization",
                            value = "Seed Vault Ed25519 Hardware Sign",
                            valueColor = AppleGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Deploy Button (Solana Signature Gradient Capsule)
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF14F195).copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SolanaSignatureGradient)
                    .clickable {
                        val stepsTarget = activeRule.thresholdLimit.toInt()
                        onSubmitCommitment(selectedDays, stepsTarget, stakeAmount, isDemoMode, activeRule)
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DEPLOY SMART ESCROW (${String.format(java.util.Locale.US, "%,.0f", stakeAmount)} \$SKR)",
                        color = Color(0xFF030D08),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "100% Non-Custodial Anchor PDA • Deflationary On-Chain Slashing",
                    fontSize = 10.5.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
}

@Composable
fun ContractSpecRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
