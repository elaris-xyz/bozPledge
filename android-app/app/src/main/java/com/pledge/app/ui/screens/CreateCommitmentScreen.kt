package com.pledge.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommitmentScreen(
    userSkrBalance: Double,
    onBack: () -> Unit,
    onSubmitCommitment: (totalDays: Int, targetSteps: Int, stakeAmountSKR: Double, isDemoMode: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDays by remember { mutableStateOf(7) }
    var selectedSteps by remember { mutableStateOf(8000) }
    var stakeAmount by remember { mutableStateOf(300.0) }
    var isDemoMode by remember { mutableStateOf(true) }

    val daysOptions = listOf(3, 7, 14, 30)
    val stepsOptions = listOf(5000, 8000, 10000, 12000)

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NEW COMMITMENT",
                        fontWeight = FontWeight.Bold,
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
            // 1. Duration Selector
            Text(
                text = "COMMITMENT DURATION",
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
                daysOptions.forEach { days ->
                    val isSelected = selectedDays == days
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SolanaGreen else DarkBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = if (isSelected) SolanaGreen.copy(alpha = 0.15f) else DarkSurface,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedDays = days },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$days Days",
                            color = if (isSelected) SolanaGreen else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Daily Step Goal
            Text(
                text = "DAILY STEP TARGET",
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
                stepsOptions.forEach { steps ->
                    val isSelected = selectedSteps == steps
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SolanaPurple else DarkBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = if (isSelected) SolanaPurple.copy(alpha = 0.15f) else DarkSurface,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedSteps = steps },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${steps / 1000}k",
                            color = if (isSelected) SolanaPurple else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Staking Amount ($SKR)
            Text(
                text = "STAKE AMOUNT IN \$SKR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stakeAmount.toInt()} \$SKR",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Balance: ${userSkrBalance.toInt()} \$SKR",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = stakeAmount.toFloat(),
                        onValueChange = { stakeAmount = it.toDouble() },
                        valueRange = 50f..1000f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = SolanaPurple,
                            activeTrackColor = SolanaPurple,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Demo Mode Toggle
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
                            text = "Compresses 24h into 30 seconds for quick hackathon judging & emulator testing.",
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

            // 5. Burn Warning Notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FlameBurn.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = FlameBurn.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Burn",
                        tint = FlameBurn,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "From your failure, NO ONE profits. Not even us. Any forfeited stake is permanently burned on-chain.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Deposit & Authorize Button
            Button(
                onClick = {
                    onSubmitCommitment(selectedDays, selectedSteps, stakeAmount, isDemoMode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolanaGreen),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Sign",
                    tint = DarkBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STAKE \$SKR & CLOCK IN",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = DarkBackground
                )
            }
        }
    }
}
