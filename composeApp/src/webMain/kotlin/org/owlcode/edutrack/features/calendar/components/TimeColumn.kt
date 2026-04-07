package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Altura en dp de una franja de 1 hora — constante compartida con WeekView y DayView */
const val HOUR_HEIGHT_DP = 64

@Composable
fun TimeColumn(
    startHour: Int = 7,
    endHour: Int   = 22,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.width(52.dp)) {
        (startHour..endHour).forEach { hour ->
            Box(
                modifier         = Modifier
                    .height(HOUR_HEIGHT_DP.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text     = "${hour.toString().padStart(2, '0')}:00",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp, top = 2.dp)
                )
            }
        }
    }
}

