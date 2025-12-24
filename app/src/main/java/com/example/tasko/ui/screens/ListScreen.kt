package com.example.tasko.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tasko.data.db.TaskEntity
import com.example.tasko.ui.theme.OnSuccess
import com.example.tasko.ui.theme.Success
import com.example.tasko.vm.TaskFilter
import com.example.tasko.vm.TaskViewModel
import com.example.tasko.workers.ReminderScheduler
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.tasko.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    vm: TaskViewModel,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showCompleted by rememberSaveable { mutableStateOf(true) }
    var sheetTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showClearCompletedDialog by remember { mutableStateOf(false) }
    var deleteDialogTask by remember { mutableStateOf<TaskEntity?>(null) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val now = System.currentTimeMillis()

    val activeTasks = remember(state.tasks, now) {
        state.tasks
            .filter { !it.isDone }
            .sortedWith(
                compareBy<TaskEntity>(
                    { !it.isPinned },
                    { !isOverdue(it, now) },
                    { it.remindAt == null },
                    { it.remindAt ?: Long.MAX_VALUE }
                )
            )
    }

    val pinnedTasks = remember(activeTasks) { activeTasks.filter { it.isPinned } }
    val normalTasks = remember(activeTasks) { activeTasks.filter { !it.isPinned } }

    val doneTasks = remember(state.tasks) { state.tasks.filter { it.isDone } }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = state.query,
            onValueChange = vm::setQuery,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            label = { Text("Search tasks") },
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrettyChip("All", state.filter == TaskFilter.ALL) { vm.setFilter(TaskFilter.ALL) }
            PrettyChip("Active", state.filter == TaskFilter.ACTIVE) { vm.setFilter(TaskFilter.ACTIVE) }
            PrettyChip("Done", state.filter == TaskFilter.DONE) { vm.setFilter(TaskFilter.DONE) }
        }

        if (state.tasks.isEmpty()) {
            EmptyState()
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {

            if (pinnedTasks.isNotEmpty()) {
                item { SectionHeader("Pinned", "${pinnedTasks.size} task(s)") }

                items(pinnedTasks, key = { it.id }) { task ->
                    SwipeTaskCard(
                        task = task,
                        onOpen = { onOpen(task.id) },
                        onToggleDone = { newValue -> vm.toggleDone(task.id, newValue) },
                        onDelete = { deleted ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ReminderScheduler.cancel(context, deleted.id)
                            vm.delete(deleted)

                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "Task deleted",
                                    actionLabel = "UNDO",
                                    withDismissAction = true
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    vm.save(deleted) { newId ->
                                        if (deleted.remindAt != null) {
                                            ReminderScheduler.schedule(
                                                context,
                                                newId,
                                                deleted.title,
                                                deleted.remindAt!!
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        haptic = haptic,
                        onLongPress = { sheetTask = task }
                    )
                }

                item { Spacer(Modifier.height(6.dp)) }
            }

            if (normalTasks.isNotEmpty()) {
                item { SectionHeader("Active", "${normalTasks.size} task(s)") }

                items(normalTasks, key = { it.id }) { task ->
                    SwipeTaskCard(
                        task = task,
                        onOpen = { onOpen(task.id) },
                        onToggleDone = { newValue -> vm.toggleDone(task.id, newValue) },
                        onDelete = { deleted ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ReminderScheduler.cancel(context, deleted.id)
                            vm.delete(deleted)

                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "Task deleted",
                                    actionLabel = "UNDO",
                                    withDismissAction = true
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    vm.save(deleted) { newId ->
                                        if (deleted.remindAt != null) {
                                            ReminderScheduler.schedule(
                                                context,
                                                newId,
                                                deleted.title,
                                                deleted.remindAt!!
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        haptic = haptic,
                        onLongPress = { sheetTask = task }
                    )
                }
            }

            if (doneTasks.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { showCompleted = !showCompleted },
                                    onLongClick = { }
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (showCompleted) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("Completed", style = MaterialTheme.typography.titleMedium)
                                    Text("${doneTasks.size} task(s)", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            TextButton(
                                enabled = showCompleted,
                                onClick = { showClearCompletedDialog = true }
                            ) {
                                Text("Clear")
                            }
                        }

                        if (showCompleted) {
                            Spacer(Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                doneTasks.forEach { task ->
                                    key(task.id) {
                                        SwipeTaskCard(
                                            task = task,
                                            onOpen = { onOpen(task.id) },
                                            onToggleDone = { newValue -> vm.toggleDone(task.id, newValue) },
                                            onDelete = { deleted ->
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                ReminderScheduler.cancel(context, deleted.id)
                                                vm.delete(deleted)

                                                scope.launch {
                                                    val res = snackbarHostState.showSnackbar(
                                                        message = "Task deleted",
                                                        actionLabel = "UNDO",
                                                        withDismissAction = true
                                                    )
                                                    if (res == SnackbarResult.ActionPerformed) {
                                                        vm.save(deleted) { newId ->
                                                            if (deleted.remindAt != null) {
                                                                ReminderScheduler.schedule(
                                                                    context,
                                                                    newId,
                                                                    deleted.title,
                                                                    deleted.remindAt!!
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            haptic = haptic,
                                            onLongPress = { sheetTask = task }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (sheetTask != null) {
        ModalBottomSheet(onDismissRequest = { sheetTask = null }) {
            val task = sheetTask!!

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(task.title, style = MaterialTheme.typography.titleLarge)

                OutlinedButton(
                    onClick = {
                        vm.save(task.copy(isPinned = !task.isPinned))
                        sheetTask = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (task.isPinned) "Unpinned" else "Pinned"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(if (task.isPinned) "Unpin task" else "Pin task")
                }

                OutlinedButton(
                    onClick = {
                        deleteDialogTask = task
                        sheetTask = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete task")
                }


                Text("Snooze", style = MaterialTheme.typography.titleMedium)

                SnoozeRow("10 minutes") {
                    val newTime = snoozeMillis(SnoozeOption.MIN_10, System.currentTimeMillis())
                    applySnooze(vm, context, task, newTime)
                    sheetTask = null
                    scope.launch { snackbarHostState.showSnackbar("Snoozed 10 minutes") }
                }

                SnoozeRow("1 hour") {
                    val newTime = snoozeMillis(SnoozeOption.HOUR_1, System.currentTimeMillis())
                    applySnooze(vm, context, task, newTime)
                    sheetTask = null
                    scope.launch { snackbarHostState.showSnackbar("Snoozed 1 hour") }
                }

                SnoozeRow("Tomorrow 09:00") {
                    val newTime = snoozeMillis(SnoozeOption.TOMORROW_9, System.currentTimeMillis())
                    applySnooze(vm, context, task, newTime)
                    sheetTask = null
                    scope.launch { snackbarHostState.showSnackbar("Snoozed to tomorrow") }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
    if (showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showClearCompletedDialog = false },
            title = { Text("Clear completed?") },
            text = { Text("This will permanently delete all completed tasks.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCompletedDialog = false

                        doneTasks.forEach { t ->
                            ReminderScheduler.cancel(context, t.id)
                            vm.delete(t)
                        }

                        scope.launch {
                            snackbarHostState.showSnackbar("Completed tasks cleared")
                        }
                    }
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCompletedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val toDelete = deleteDialogTask
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteDialogTask = null },
            title = { Text("Delete task?") },
            text = { Text("“${toDelete.title}” will be deleted. You can UNDO from snackbar.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteDialogTask = null

                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        ReminderScheduler.cancel(context, toDelete.id)
                        vm.delete(toDelete)

                        scope.launch {
                            val res = snackbarHostState.showSnackbar(
                                message = "Task deleted",
                                actionLabel = "UNDO",
                                withDismissAction = true
                            )
                            if (res == SnackbarResult.ActionPerformed) {
                                vm.save(toDelete) { newId ->
                                    if (toDelete.remindAt != null) {
                                        ReminderScheduler.schedule(
                                            context,
                                            newId,
                                            toDelete.title,
                                            toDelete.remindAt!!
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogTask = null }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
private fun SwipeTaskCard(
    task: TaskEntity,
    onOpen: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onLongPress: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete(task)
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleDone(!task.isDone)
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isSwiping =
                dismissState.currentValue != SwipeToDismissBoxValue.Settled ||
                        dismissState.targetValue != SwipeToDismissBoxValue.Settled

            if (!isSwiping) {
                Box(Modifier.fillMaxSize())
                return@SwipeToDismissBox
            }

            val dir = dismissState.dismissDirection
            val isToggle = dir == SwipeToDismissBoxValue.StartToEnd

            val progress by animateFloatAsState(
                targetValue = dismissState.progress.coerceIn(0f, 1f),
                label = "swipeProgress"
            )

            val shape = RoundedCornerShape(12.dp)
            val bg = if (isToggle) Success else MaterialTheme.colorScheme.error
            val fg = if (isToggle) OnSuccess else MaterialTheme.colorScheme.onError
            val fillFraction = (0.12f + 0.88f * progress).coerceIn(0.12f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .clip(shape)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillFraction)
                        .align(if (isToggle) Alignment.CenterStart else Alignment.CenterEnd)
                        .background(bg)
                )

                Row(
                    modifier = Modifier
                        .align(if (isToggle) Alignment.CenterStart else Alignment.CenterEnd)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isToggle) Icons.Outlined.CheckCircle else Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = fg
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isToggle) {
                            if (task.isDone) "Restore" else "Done"
                        } else {
                            "Delete"
                        },
                        color = fg,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        content = {
            TaskCard(
                task = task,
                onToggleDone = onToggleDone,
                onOpen = onOpen,
                onLongPress = onLongPress
            )
        }
    )
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggleDone: (Boolean) -> Unit,
    onOpen: () -> Unit,
    onLongPress: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.55f else 1f,
        label = "doneAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onLongPress
            )
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.isPinned && !task.isDone) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    Text(
                        task.title,
                        style = if (task.isDone)
                            MaterialTheme.typography.bodyLarge
                        else
                            MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!task.isDone) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("Active") })
                        if (task.remindAt != null) {
                            AssistChip(onClick = {}, label = { Text("Reminder") })
                        }
                        if (isOverdue(task, System.currentTimeMillis())) {
                            AssistChip(onClick = {}, label = { Text("Overdue") })
                        }
                    }
                }
            }

            RadioButton(
                selected = task.isDone,
                onClick = { onToggleDone(!task.isDone) }
            )
        }
    }
}

@Composable
private fun SnoozeRow(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PrettyChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(text) })
}

@Composable
private fun EmptyState() {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No tasks yet ✨", style = MaterialTheme.typography.titleLarge)
            Image(
                painter = painterResource(id = R.drawable.empty_tasks),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(10.dp))

            Text(
                "Tap the + button to create your first task. Add a reminder to get notified.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun isOverdue(task: TaskEntity, now: Long): Boolean {
    val t = task.remindAt ?: return false
    return !task.isDone && t < now
}

private enum class SnoozeOption { MIN_10, HOUR_1, TOMORROW_9 }

private fun snoozeMillis(option: SnoozeOption, now: Long): Long {
    return when (option) {
        SnoozeOption.MIN_10 -> now + 10 * 60_000L
        SnoozeOption.HOUR_1 -> now + 60 * 60_000L
        SnoozeOption.TOMORROW_9 -> tomorrowAt9amMillis(now)
    }
}

private fun tomorrowAt9amMillis(now: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = now }
    c.add(Calendar.DAY_OF_YEAR, 1)
    c.set(Calendar.HOUR_OF_DAY, 9)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun applySnooze(
    vm: TaskViewModel,
    context: android.content.Context,
    task: TaskEntity,
    newTime: Long
) {
    ReminderScheduler.cancel(context, task.id)
    vm.save(task.copy(remindAt = newTime)) { newId ->
        ReminderScheduler.schedule(context, newId, task.title, newTime)
    }
}
