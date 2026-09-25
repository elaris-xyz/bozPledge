package com.pledge.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            title = "App Detox (< 2h)",
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
            2 -> 500.0
            else -> 1000.0
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "bozPledge STUDIO",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Mode Toggle (Presets vs Rule Composer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isComposerTab) SolanaTeal.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { isComposerTab = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ PRESETS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (!isComposerTab) SolanaTeal else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isComposerTab) SolanaGreen.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { isComposerTab = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧠 RULE COMPOSER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isComposerTab) SolanaGreen else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isComposerTab) {
                // Preset habits list
                Text(
                    text = "SELECT HABIT BLUEPRINT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                presets.forEachIndexed { index, preset ->
                    val isSelected = selectedPresetIndex == index
                    val presetStake = when(index) {
                        0 -> "2,500 \$SKR"
                        1 -> "5,000 \$SKR"
                        2 -> "500 \$SKR"
                        else -> "1,000 \$SKR"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SolanaTeal else DarkBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedPresetIndex = index },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SolanaTeal.copy(alpha = 0.08f) else DarkSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${preset.sensorType.apiSource} • ${preset.schedulePreset.displayName}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = presetStake,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (isSelected) SolanaTeal else TextMuted
                            )
                        }
                    }
                }
            } else {
                // Advanced Rule Composer (IFTTT)
                Text(
                    text = "1. TELEMETRY / SENSOR SOURCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorType.values().take(2).forEach { sensor ->
                        val isSel = customSensor == sensor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(if (isSel) 2.dp else 1.dp, if (isSel) SolanaGreen else DarkBorder, RoundedCornerShape(14.dp))
                                .background(if (isSel) SolanaGreen.copy(alpha = 0.15f) else DarkSurface, RoundedCornerShape(14.dp))
                                .clickable { customSensor = sensor },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sensor.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) SolanaGreen else TextSecondary)
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(if (isSel) 2.dp else 1.dp, if (isSel) SolanaGreen else DarkBorder, RoundedCornerShape(14.dp))
                                .background(if (isSel) SolanaGreen.copy(alpha = 0.15f) else DarkSurface, RoundedCornerShape(14.dp))
                                .clickable { customSensor = sensor },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sensor.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) SolanaGreen else TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Schedule selection
                Text(
                    text = "2. SCHEDULE PRESET",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(SchedulePreset.ODD_DAYS, SchedulePreset.EVEN_DAYS, SchedulePreset.DAILY).forEach { sched ->
                        val isSel = customSchedule == sched
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(if (isSel) 2.dp else 1.dp, if (isSel) SolanaTeal else DarkBorder, RoundedCornerShape(12.dp))
                                .background(if (isSel) SolanaTeal.copy(alpha = 0.15f) else DarkSurface, RoundedCornerShape(12.dp))
                                .clickable { customSchedule = sched },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                when (sched) {
                                    SchedulePreset.ODD_DAYS -> "Odd Days"
                                    SchedulePreset.EVEN_DAYS -> "Even Days"
                                    else -> "Daily"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) SolanaTeal else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Limit & Staking Inputs
                Text(
                    text = "3. LIMIT & FREE-FORM COLLATERAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customLimitText,
                        onValueChange = { customLimitText = it },
                        label = { Text("Max Target") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolanaTeal,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    OutlinedTextField(
                        value = customStakeText,
                        onValueChange = { customStakeText = it },
                        label = { Text("Stake \$SKR") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolanaGreen,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Demo Mode Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SolanaTeal.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demo Mode (30s Days)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SolanaTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Compresses 24h into 30 seconds for fast hackathon judging & verification.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = { isDemoMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SolanaTeal,
                            checkedTrackColor = SolanaTeal.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Smart Rule Logic Specification Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SolanaGreen.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "// GENERATED ON-CHAIN SPECIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SolanaTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val dailyLoss = (stakeAmount / 7).toInt()
                    val ruleDesc = "IF (${activeRule.sensorType.displayName} <= ${activeRule.thresholdLimit} ${activeRule.unit}) ON (${activeRule.schedulePreset.displayName})\nTHEN Unlock Daily Clock-In\nELSE Burn $dailyLoss \$SKR from PDA Escrow"

                    Text(
                        text = ruleDesc,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Deploy Button
            Button(
                onClick = {
                    val stepsTarget = activeRule.thresholdLimit.toInt()
                    onSubmitCommitment(selectedDays, stepsTarget, stakeAmount, isDemoMode, activeRule)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolanaGreen)
            ) {
                Text(
                    text = "DEPLOY SMART RULE (${stakeAmount.toInt()} \$SKR)",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Funds locked in Solana Anchor PDA. 100% SPL Burn upon breach.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
