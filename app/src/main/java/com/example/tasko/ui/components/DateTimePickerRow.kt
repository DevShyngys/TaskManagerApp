package com.example.tasko.ui.components

import android.app.TimePickerDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tasko.ui.util.formatDate
import com.example.tasko.ui.util.formatDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerRow(
    label: String,
    valueMillis: Long?,
    onChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    withTime: Boolean = true
) {
    val context = LocalContext.current

    var showDate by remember { mutableStateOf(false) }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = valueMillis ?: System.currentTimeMillis()
    )

    if (showDate) {
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = dateState.selectedDateMillis
                        showDate = false
                        if (selectedDate == null) return@TextButton

                        if (!withTime) {
                            onChange(atStartOfDay(selectedDate))
                        } else {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            val hour = cal.get(Calendar.HOUR_OF_DAY)
                            val minute = cal.get(Calendar.MINUTE)

                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    val out = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, m)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    onChange(out)
                                },
                                hour,
                                minute,
                                true
                            ).show()
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    val text = when {
        valueMillis == null -> "Not set"
        withTime -> formatDateTime(valueMillis)
        else -> formatDate(valueMillis)
    }

    OutlinedButton(
        onClick = { showDate = true },
        modifier = modifier
    ) {
        Text("$label: $text")
    }
}

private fun atStartOfDay(millis: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}
