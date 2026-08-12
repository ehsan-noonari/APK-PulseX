package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun LiveMarketStatusBar(
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            currentTime = sdf.format(Date())
            delay(1000L)
        }
    }

    val calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)
    val timeInMinutes = hour * 60 + minute

    val isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY
    val (statusText, statusColor) = when {
        isWeekend -> "Market Closed" to Color(0xFFEF4444)
        timeInMinutes in 240..570 -> "Pre-Market" to Color(0xFFF59E0B)
        timeInMinutes in 570..960 -> "Market Open" to Color(0xFF10B981)
        timeInMinutes in 960..1200 -> "After Hours" to Color(0xFF3B82F6)
        else -> "Market Closed" to Color(0xFFEF4444)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PulseXColors.GlassCardBg)
            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "Last Updated: $currentTime",
                color = PulseXColors.Outline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
