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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
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

        // Breakdown Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Refund to Wallet:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "${state.refundAmountSKR.toInt()} \$SKR",
                        color = SolanaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Burned On-Chain:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "${state.burnAmountSKR.toInt()} \$SKR",
                        color = FlameBurn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = DarkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Deflationary Rule: Forfeited tokens are sent to SPL Burn Instruction. No developer or third party receives these tokens.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        if (!state.settled) {
            Button(
                onClick = onSettleConfirmed,
                colors = ButtonDefaults.buttonColors(containerColor = if (state.completedDays == state.totalDays) SolanaGreen else FlameBurn),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                if (isSettling) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "EXECUTE SETTLE ON-CHAIN",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DarkBackground
                    )
                }
            }
        } else {
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(
                    text = "START NEXT COMMITMENT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
