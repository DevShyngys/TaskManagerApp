package com.example.tasko.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.tasko.data.db.TaskEntity
import com.example.tasko.vm.TaskViewModel
import com.example.tasko.workers.ReminderScheduler
import com.example.tasko.ui.components.DateTimePickerRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    vm: TaskViewModel,
    onSaved: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var remindAt by remember { mutableStateOf<Long?>(null) }

    val canSave = title.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New task",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                        leadingIcon = { Icon(Icons.Outlined.Title, contentDescription = null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Outlined.Notes, contentDescription = null) },
                        minLines = 3
                    )

                    Divider()

                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Alarm, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reminder", style = MaterialTheme.typography.titleSmall)
                    }

                    DateTimePickerRow(
                        label = "Remind at",
                        valueMillis = remindAt,
                        onChange = { remindAt = it },
                        modifier = Modifier.fillMaxWidth(),
                        withTime = true
                    )

                    Text(
                        "If reminder time is set, you’ll get a notification.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = {
                    if (!canSave) return@Button

                    vm.save(
                        TaskEntity(
                            title = title.trim(),
                            description = description.trim(),
                            remindAt = remindAt
                        )
                    ) { newId ->
                        if (remindAt != null) {
                            ReminderScheduler.schedule(
                                context,
                                newId,
                                title.trim(),
                                remindAt!!
                            )
                        }
                        onSaved()
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Save task")
            }

        }
    }
}
