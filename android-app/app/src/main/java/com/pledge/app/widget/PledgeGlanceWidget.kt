package com.pledge.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.color.ColorProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class PledgeGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFF13151F)))
                    .cornerRadius(20.dp)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "bozPLEDGE : CLOCK IN",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF14F195)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Text(
                    text = "8,420",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "of 8,000 steps",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF9AA0B4)),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Text(
                    text = "Verified Today",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF14F195)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class PledgeGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PledgeGlanceWidget()
}
