package com.pledge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pledge.app.ui.theme.*

@Composable
fun DemoControlsBar(
    isDemoMode: Boolean,
    manualSteps: Int,
    secondsLeftInDay: Long,
    onManualStepsChange: (Int) -> Unit,
    onFastForwardDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SolanaPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Demo Mode",
                        tint = SolanaTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HACKATHON JUDGE CONTROLS",
                        color = SolanaTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = FlameBurn.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "30s Day Timer: ${secondsLeftInDay}s",
                        color = FlameBurn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Manual Step Override (Emulator Test): $manualSteps steps",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Slider(
                value = manualSteps.toFloat(),
                onValueChange = { onManualStepsChange(it.toInt()) },
                valueRange = 0f..15000f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = SolanaGreen,
                    activeTrackColor = SolanaGreen,
                    inactiveTrackColor = DarkBorder
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slide above goal to Clock In instantly",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                OutlinedButton(
                    onClick = onFastForwardDay,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolanaTeal),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Fast Forward",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Next Day", fontSize = 11.sp)
                }
            }
        }
    }
}
