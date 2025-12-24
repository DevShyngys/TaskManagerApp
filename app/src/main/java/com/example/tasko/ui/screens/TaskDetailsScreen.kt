package com.example.tasko.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.tasko.data.db.TaskEntity
import com.example.tasko.vm.TaskViewModel
import com.example.tasko.workers.ReminderScheduler
import com.example.tasko.ui.components.DateTimePickerRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: Long,
    vm: TaskViewModel,
    onBack: () -> Unit
) {
    var task by remember { mutableStateOf<TaskEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        task = vm.getById(taskId)
    }

    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var title by remember { mutableStateOf(task!!.title) }
    var description by remember { mutableStateOf(task!!.description) }
    var remindAt by remember { mutableStateOf(task!!.remindAt) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Task Details") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            DateTimePickerRow(
                label = "Remind at",
                valueMillis = remindAt,
                onChange = { remindAt = it },
                modifier = Modifier.fillMaxWidth(),
                withTime = true
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val updated = task!!.copy(
                        title = title.trim(),
                        description = description.trim(),
                        remindAt = remindAt
                    )

                    vm.save(updated) {
                        if (remindAt != null) {
                            ReminderScheduler.schedule(
                                context,
                                updated.id,
                                updated.title,
                                remindAt!!
                            )
                        } else {
                            ReminderScheduler.cancel(context, updated.id)
                        }
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }

            OutlinedButton(
                onClick = {
                    ReminderScheduler.cancel(context, task!!.id)
                    vm.delete(task!!)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    }
}
