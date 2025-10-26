package com.example.ia4_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.ia4_2.ui.theme.Ia4_2Theme

class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private val _autoMode = MutableStateFlow(false)
    val autoMode = _autoMode.asStateFlow()

    private val _interval = MutableStateFlow(3_000L)
    val interval = _interval.asStateFlow()

    private var autoJob: Job? = null

    fun increment() {
        _count.value += 1
    }

    fun decrement() {
        _count.value -= 1
    }

    fun reset() {
        _count.value = 0
    }

    fun toggleAutoMode() {
        val newState = !_autoMode.value
        _autoMode.value = newState
        if (newState) startAutoIncrement() else stopAutoIncrement()
    }

    private fun startAutoIncrement() {
        autoJob = viewModelScope.launch {
            while (true) {
                delay(_interval.value)
                _count.value += 1
            }
        }
    }

    private fun stopAutoIncrement() {
        autoJob?.cancel()
        autoJob = null
    }

    fun setInterval(seconds: Long) {
        _interval.value = seconds * 1000
        if (_autoMode.value) {
            stopAutoIncrement()
            startAutoIncrement()
        }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<CounterViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CounterApp(viewModel)
        }
    }
}

@Composable
fun CounterApp(viewModel: CounterViewModel) {
    var showSettings by remember { mutableStateOf(false) }
    if (showSettings) {
        SettingsScreen(
            onBack = { showSettings = false },
            viewModel = viewModel
        )
    } else {
        CounterScreen(
            viewModel = viewModel,
            onOpenSettings = { showSettings = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(viewModel: CounterViewModel, onOpenSettings: () -> Unit) {
    val count by viewModel.count.collectAsState()
    val autoMode by viewModel.autoMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Counter", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onOpenSettings) {
                        Text(
                            "Settings",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAutoMode() },
                containerColor = if (autoMode)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary
            ) {
                Text(if (autoMode) "STOP" else "AUTO")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Count: $count", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.decrement() }) { Text("-1") }
                Button(onClick = { viewModel.increment() }) { Text("+1") }
                Button(onClick = { viewModel.reset() }) { Text("Reset") }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Auto Mode: ${if (autoMode) "ON" else "OFF"}",
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: CounterViewModel, onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val intervalMs by viewModel.interval.collectAsState()
    var intervalText by remember { mutableStateOf((intervalMs / 1000).toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Back",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Auto Increment Interval (seconds):", fontWeight = FontWeight.Bold)

            TextField(
                value = intervalText,
                onValueChange = { intervalText = it },
                label = { Text("Seconds") },
                singleLine = true
            )

            Button(onClick = {
                val newInterval = intervalText.toLongOrNull()
                if (newInterval != null && newInterval > 0) {
                    viewModel.setInterval(newInterval)
                    scope.launch {
                        snackbarHostState.showSnackbar("Custom interval has been saved")
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Please enter a valid number")
                    }
                }
            }) {
                Text("Save Interval")
            }
        }
    }
}
