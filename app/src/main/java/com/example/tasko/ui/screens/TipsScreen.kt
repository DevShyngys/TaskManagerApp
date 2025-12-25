package com.example.tasko.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tasko.data.db.TaskEntity
import com.example.tasko.vm.TipsViewModel
import com.example.tasko.vm.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    tipsVm: TipsViewModel,
    taskVm: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val state by tipsVm.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { tipsVm.load() }

    Box(modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { tipsVm.load() }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reload")
                }
            }

            when {
                state.loading -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                state.error != null -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Failed to load 😕", style = MaterialTheme.typography.titleMedium)
                            Text(state.error ?: "")
                            Button(onClick = { tipsVm.load() }) { Text("Try again") }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(state.tips, key = { it.title }) { tip ->
                            TipCard(
                                title = tip.title,
                                body = tip.body,
                                onAdd = {
                                    taskVm.save(
                                        TaskEntity(
                                            title = tip.title.trim(),
                                            description = tip.body.trim(),
                                            remindAt = null
                                        )
                                    )
                                    scope.launch {
                                        snack.showSnackbar("Added as task ✅")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun TipCard(
    title: String,
    body: String,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onAdd,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add as task")
                }
            }
        }
    }
}
