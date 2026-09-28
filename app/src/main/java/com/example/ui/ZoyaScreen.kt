package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ZoyaForegroundService
import com.example.live.ZoyaState
import com.example.ui.components.FuturisticLiveBackground
import com.example.ui.components.JarvisArcReactorOrb
import com.example.voice.VoiceData

@Composable
fun ZoyaScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToChat = { navController.navigate("chat") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("chat") {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ZoyaPrefs", Context.MODE_PRIVATE) }
    
    var apiKey by remember { mutableStateOf(prefs.getString("api_key", "") ?: "") }
    var showApiKeyDialog by remember { mutableStateOf(apiKey.isEmpty()) }
    var zoyaState by remember { mutableStateOf(ZoyaForegroundService.currentState) }
    var serviceStarted by remember { mutableStateOf(ZoyaForegroundService.activeService != null) }

    val currentPersonaStr = prefs.getString("active_persona", "ZOYA") ?: "ZOYA"
    val currentLangCode = prefs.getString("selected_language", "hi") ?: "hi"
    val currentLangConfig = remember(currentLangCode) { VoiceData.getLanguageByCode(currentLangCode) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.RECORD_AUDIO] == true) {
            val intent = Intent(context, ZoyaForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
            serviceStarted = true
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        ZoyaForegroundService.onStateChange = { state ->
            zoyaState = state
        }
    }

    // Infinite rotation for settings gear icon
    val infiniteTransition = rememberInfiniteTransition(label = "SettingsGear")
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gearRotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // UHD Animated Live Background
        FuturisticLiveBackground(state = zoyaState)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Z.O.Y.A.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = 3.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            // Status Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (serviceStarted) Color(0xFF00E676) else Color(0xFFFF1744))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (serviceStarted) "ONLINE" else "OFFLINE",
                                color = if (serviceStarted) Color(0xFF00E676) else Color.Red.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        // Single Animated Futuristic Settings Button
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .testTag("settings_button")
                                .padding(end = 12.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "JARVIS Settings Matrix",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(gearRotation)
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // CENTRAL ELEMENT: Futuristic Arc-Reactor Orb
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(320.dp)
                ) {
                    JarvisArcReactorOrb(state = zoyaState)
                }

                // Below Orb: Status Pill & Active Persona Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Glassmorphic Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF140B24).copy(alpha = 0.85f))
                            .border(
                                1.dp,
                                when (zoyaState) {
                                    ZoyaState.LISTENING -> Color(0xFFFF4081)
                                    ZoyaState.THINKING -> Color(0xFFFF9100)
                                    ZoyaState.SPEAKING -> Color(0xFF00E676)
                                    ZoyaState.IDLE -> Color(0xFF00E5FF).copy(alpha = 0.5f)
                                },
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (zoyaState) {
                                            ZoyaState.LISTENING -> Color(0xFFFF4081)
                                            ZoyaState.THINKING -> Color(0xFFFF9100)
                                            ZoyaState.SPEAKING -> Color(0xFF00E676)
                                            ZoyaState.IDLE -> Color(0xFF00E5FF)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (zoyaState) {
                                    ZoyaState.LISTENING -> "LISTENING TO COMMAND..."
                                    ZoyaState.THINKING -> "PROCESSING DATA MATRIX..."
                                    ZoyaState.SPEAKING -> "TRANSMITTING RESPONSE..."
                                    ZoyaState.IDLE -> if (serviceStarted) "AWAITING VOICE COMMAND" else "SYSTEM IDLE"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Small Persona & Language Info Badge
                    Text(
                        text = "${if (currentPersonaStr == "JARVIS") "🤖 JARVIS" else "♀️ ZOYA"} • ${currentLangConfig.nativeName.uppercase()}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Bottom Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!serviceStarted) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("start_zoya_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(28.dp),
                            onClick = {
                                if (apiKey.isEmpty()) {
                                    showApiKeyDialog = true
                                } else {
                                    val hasMic = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    val hasContacts = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    val hasPhone = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                    if (hasMic && hasContacts && hasPhone) {
                                        val intent = Intent(context, ZoyaForegroundService::class.java)
                                        ContextCompat.startForegroundService(context, intent)
                                        serviceStarted = true
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.RECORD_AUDIO,
                                                android.Manifest.permission.READ_CONTACTS,
                                                android.Manifest.permission.CALL_PHONE
                                            )
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("INITIALIZE Z.O.Y.A.", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
                        }
                    } else {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF1744).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFF80AB)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF1744).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(28.dp),
                            onClick = {
                                val intent = Intent(context, ZoyaForegroundService::class.java)
                                context.stopService(intent)
                                serviceStarted = false
                            }
                        ) {
                            Text("DISCONNECT SESSION", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                        }
                    }

                    // Terminal / Log View Icon Button
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "View System Logs",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showApiKeyDialog) {
        var tempKey by remember { mutableStateOf(apiKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key Required", color = Color.White) },
            containerColor = Color(0xFF1B0E25),
            text = {
                Column {
                    Text("Enter your Gemini API key to activate Z.O.Y.A.", color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            if (tempKey.isNotEmpty()) {
                                androidx.compose.material3.IconButton(onClick = { tempKey = "" }) {
                                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putString("api_key", tempKey).apply()
                        apiKey = tempKey
                        showApiKeyDialog = false
                    }
                ) {
                    Text("Save Key", color = Color(0xFF00E5FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun ChatScreen(onNavigateBack: () -> Unit) {
    val liveSessionManager = ZoyaForegroundService.activeService?.liveSessionManager
    val messages = liveSessionManager?.messages?.collectAsState(initial = emptyList())?.value ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        FuturisticLiveBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "TERMINAL DIAGNOSTICS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "STATUS: ${ZoyaForegroundService.currentState.name}",
                    color = Color(0xFF00E5FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        Text(
                            text = message,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
