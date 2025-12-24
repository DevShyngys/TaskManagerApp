package com.example.tasko.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasko.data.db.TaskEntity
import com.example.tasko.ui.util.formatDate
import com.example.tasko.ui.util.formatDateTime
import com.example.tasko.vm.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CalTab { TODAY, UPCOMING, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    vm: TaskViewModel,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.uiState.collectAsState()
    var tab by remember { mutableStateOf(CalTab.TODAY) }

    val now = System.currentTimeMillis()
    val startToday = startOfDay(now)
    val startTomorrow = startToday + DAY_MS

    val tasks = state.tasks

    val filtered = remember(tasks, tab, startToday, startTomorrow) {
        when (tab) {
            CalTab.TODAY ->
                tasks.filter { it.remindAt != null && it.remindAt in startToday until startTomorrow }
                    .sortedBy { it.remindAt }

            CalTab.UPCOMING ->
                tasks.filter { it.remindAt != null && it.remindAt >= startTomorrow }
                    .sortedBy { it.remindAt }

            CalTab.ALL ->
                tasks.filter { it.remindAt != null }
                    .sortedBy { it.remindAt }
        }
    }

    val grouped = remember(filtered) {
        filtered.groupBy { startOfDay(it.remindAt!!) }.toSortedMap()
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabRow(selectedTabIndex = tab.ordinal) {
            Tab(selected = tab == CalTab.TODAY, onClick = { tab = CalTab.TODAY }, text = { Text("Today") })
            Tab(selected = tab == CalTab.UPCOMING, onClick = { tab = CalTab.UPCOMING }, text = { Text("Upcoming") })
            Tab(selected = tab == CalTab.ALL, onClick = { tab = CalTab.ALL }, text = { Text("All") })
        }

        if (grouped.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "No reminders ✨",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Add date & time to a task and it will appear here.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("Set a reminder time on a task to see it in Calendar.")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                grouped.forEach { (dayStart, list) ->
                    val header = dayHeader(dayStart, startToday, startTomorrow)

                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(header, style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatDate(dayStart),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    items(list, key = { it.id }) { task ->
                        ReminderCard(task = task, onOpen = { onOpen(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(task: TaskEntity, onOpen: () -> Unit) {
    val timeText = task.remindAt?.let { formatTime(it) } ?: ""

    val indicatorColor = if (task.isDone) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.padding(14.dp)) {

            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .padding(end = 12.dp)
            ) {
                Surface(
                    color = indicatorColor,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxSize()
                ) {}
            }

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )

                    if (timeText.isNotBlank()) {
                        AssistChip(onClick = {}, label = { Text(timeText) })
                    }
                }

                if (task.description.isNotBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(if (task.isDone) "Done" else "Active") }
                    )
                    AssistChip(onClick = {}, label = { Text("Reminder set") })
                }

                if (task.remindAt != null) {
                    Text(
                        "Remind at: ${formatDateTime(task.remindAt!!)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


private const val DAY_MS = 24L * 60 * 60 * 1000

private fun dayHeader(dayStart: Long, startToday: Long, startTomorrow: Long): String {
    return when (dayStart) {
        startToday -> "Today"
        startTomorrow -> "Tomorrow"
        else -> "Upcoming"
    }
}

private fun startOfDay(millis: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun formatTime(millis: Long): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    return fmt.format(Date(millis))
}
