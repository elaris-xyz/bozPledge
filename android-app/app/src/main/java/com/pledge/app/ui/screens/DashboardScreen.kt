package com.pledge.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.data.CommitmentState
import com.pledge.app.ui.components.ClockInButton
import com.pledge.app.ui.components.DemoControlsBar
import com.pledge.app.ui.components.StepProgressCard
import com.pledge.app.ui.theme.*

@Composable
fun DashboardScreen(
    state: CommitmentState,
    currentSteps: Int,
    walletAddress: String?,
    skrBalance: Double,
    isClockingIn: Boolean,
    isDemoMode: Boolean,
    onClockIn: () -> Unit,
    onCreateNewPledge: () -> Unit,
    onSettle: () -> Unit,
    onManualStepsChange: (Int) -> Unit,
    onFastForwardDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "bozPLEDGE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Clock In on Solana Mobile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SolanaGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Wallet & Balance Badge
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = "Wallet",
                        tint = SolanaPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${skrBalance.toInt()} \$SKR",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isActive && !state.settled) {
            // Empty State: No active commitment
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Pledge",
                        tint = SolanaPurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Active Commitment",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stake \$SKR against your daily physical step goal. Complete your walk, Clock In daily on-chain with Seed Vault, or your forfeited stake gets burned.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onCreateNewPledge,
                        colors = ButtonDefaults.buttonColors(containerColor = SolanaGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Pledge",
                            tint = DarkBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START NEW COMMITMENT",
                            color = DarkBackground,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        } else {
            // Active Commitment Dashboard
            // 1. Stats Bar (Day progress & Staked Amount)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CURRENT DAY",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Day ${state.currentDayIndex + 1} of ${state.totalDays}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STAKED IN ESCROW",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.totalAmountSKR.toInt()} \$SKR",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolanaPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Day Status Dots
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHALLENGE TIMELINE",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0 until state.totalDays) {
                            val isClockedIn = (state.clockedInBitmap and (1L shl i)) != 0L
                            val isPast = i < state.currentDayIndex
                            val isCurrent = i == state.currentDayIndex

                            val dotColor = when {
                                isClockedIn -> SolanaGreen
                                isPast -> FlameBurn
                                isCurrent -> SolanaPurple
                                else -> DarkSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(dotColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isClockedIn) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = DarkBackground,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (isPast) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Failed",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${i + 1}",
                                        fontSize = 10.sp,
                                        color = if (isCurrent) Color.White else TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Hero Step Progress
            StepProgressCard(
                currentSteps = currentSteps,
                targetSteps = state.targetSteps,
                isClockedInToday = state.isTodayClockedIn
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Hero Clock In Button
            val canClockIn = currentSteps >= state.targetSteps
            ClockInButton(
                isClockedIn = state.isTodayClockedIn,
                canClockIn = canClockIn,
                isLoading = isClockingIn,
                onClockInClick = onClockIn
            )

            // If challenge completed, show Settle Button
            if (state.currentDayIndex >= state.totalDays || state.settled) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSettle,
                    colors = ButtonDefaults.buttonColors(containerColor = FlameBurn),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "SETTLE COMMITMENT & BURN FORFEITED TOKENS",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Hackathon Judge Demo Controls Bar
        DemoControlsBar(
            isDemoMode = isDemoMode,
            manualSteps = currentSteps,
            secondsLeftInDay = state.secondsLeftInCurrentDay,
            onManualStepsChange = onManualStepsChange,
            onFastForwardDay = onFastForwardDay
        )
    }
}
