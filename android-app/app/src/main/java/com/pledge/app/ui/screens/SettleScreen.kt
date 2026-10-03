package com.pledge.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.data.CommitmentState
import com.pledge.app.ui.theme.*

@Composable
fun SettleScreen(
    state: CommitmentState,
    isSettling: Boolean,
    onSettleConfirmed: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Icon(
            imageVector = if (state.completedDays == state.totalDays) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
            contentDescription = "Status",
            tint = if (state.completedDays == state.totalDays) SolanaGreen else FlameBurn,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CHALLENGE CONCLUDED",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Completed ${state.completedDays} of ${state.totalDays} Days",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Breakdown Card (Apple Inset Grouped style)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Refund to Wallet", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "${state.refundAmountSKR.toInt()} \$SKR",
                        color = AppleGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Burned On-Chain", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "${state.burnAmountSKR.toInt()} \$SKR",
                        color = AppleRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = DarkBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Deflationary Rule: Forfeited tokens are sent to SPL Burn Instruction. No developer or third party receives these tokens.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!state.settled) {
            val isSuccess = state.completedDays == state.totalDays
            Surface(
                color = if (isSuccess) Color.Transparent else Color(0xFF241014),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSuccess) Color(0xFF14F195).copy(alpha = 0.8f) else AppleRed.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isSuccess) Modifier.background(SolanaSignatureGradient) else Modifier
                    )
                    .clickable(enabled = !isSettling) {
                        onSettleConfirmed()
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSettling) {
                        CircularProgressIndicator(
                            color = if (isSuccess) SolanaButtonTextDark else Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Execute Settle On-Chain",
                            fontWeight = FontWeight.Black,
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            color = if (isSuccess) SolanaButtonTextDark else Color.White
                        )
                    }
                }
            }
        } else {
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolanaNeonPurple.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(SolanaNeonPurple, Color(0xFF6B26DF))
                        )
                    )
                    .clickable {
                        onDone()
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start Next Commitment",
                        fontWeight = FontWeight.Black,
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
}
