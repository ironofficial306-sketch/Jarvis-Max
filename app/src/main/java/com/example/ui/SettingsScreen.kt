package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ZoyaForegroundService
import com.example.ui.components.FuturisticLiveBackground
import com.example.voice.Persona
import com.example.voice.VoiceData
import com.example.voice.VoiceManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ZoyaPrefs", Context.MODE_PRIVATE) }

    val voiceManager = remember { VoiceManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    // Persona
    var selectedPersona by remember {
        val saved = prefs.getString("active_persona", "ZOYA") ?: "ZOYA"
        mutableStateOf(if (saved == "JARVIS") Persona.JARVIS else Persona.ZOYA)
    }

    // Language
    var selectedLangCode by remember {
        mutableStateOf(prefs.getString("selected_language", "hi") ?: "hi")
    }

    val currentLangConfig = remember(selectedLangCode) {
        VoiceData.getLanguageByCode(selectedLangCode)
    }

    val voiceList = remember(selectedPersona) {
        if (selectedPersona == Persona.ZOYA) VoiceData.ZOYA_VOICES else VoiceData.JARVIS_VOICES
    }

    var selectedVoiceId by remember(selectedPersona) {
        val key = if (selectedPersona == Persona.ZOYA) "zoya_voice" else "jarvis_voice"
        val defaultId = if (selectedPersona == Persona.ZOYA) "zoya_aoede" else "jarvis_fenrir"
        mutableStateOf(prefs.getString(key, defaultId) ?: defaultId)
    }

    val currentVoiceOption = remember(selectedVoiceId, selectedPersona) {
        VoiceData.getVoiceById(selectedVoiceId, selectedPersona)
    }

    var pitchValue by remember(selectedVoiceId) {
        mutableFloatStateOf(
            prefs.getFloat("pitch_${selectedVoiceId}", currentVoiceOption.defaultPitch)
        )
    }

    var speedValue by remember(selectedVoiceId) {
        mutableFloatStateOf(
            prefs.getFloat("speed_${selectedVoiceId}", currentVoiceOption.defaultSpeed)
        )
    }

    val isTestingVoice by voiceManager.isTestingVoice.collectAsState()

    // Keyword Spotting
    var wakeWordEnabled by remember {
        mutableStateOf(prefs.getBoolean("wake_word_enabled", true))
    }
    var sensitivity by remember {
        mutableFloatStateOf(prefs.getFloat("wake_word_sensitivity", 0.75f))
    }
    var customWakeWord by remember {
        mutableStateOf(prefs.getString("custom_wake_word", "Hey Zoya") ?: "Hey Zoya")
    }
    var hapticFeedback by remember {
        mutableStateOf(prefs.getBoolean("wake_word_haptic", true))
    }

    // API Key
    var apiKey by remember {
        mutableStateOf(prefs.getString("api_key", "") ?: "")
    }
    var showApiKeyText by remember { mutableStateOf(false) }

    val presetWakeWords = listOf("Hey Zoya", "Hello Zoya", "Zoya", "Jarvis", "Computer")

    fun saveSettings() {
        prefs.edit()
            .putString("active_persona", selectedPersona.id)
            .putString("selected_language", selectedLangCode)
            .putString(
                if (selectedPersona == Persona.ZOYA) "zoya_voice" else "jarvis_voice",
                selectedVoiceId
            )
            .putFloat("pitch_${selectedVoiceId}", pitchValue)
            .putFloat("speed_${selectedVoiceId}", speedValue)
            .putBoolean("wake_word_enabled", wakeWordEnabled)
            .putFloat("wake_word_sensitivity", sensitivity)
            .putString("custom_wake_word", customWakeWord)
            .putBoolean("wake_word_haptic", hapticFeedback)
            .putString("api_key", apiKey)
            .apply()

        val service = ZoyaForegroundService.activeService
        service?.updateVoiceSettings()
        service?.reloadKeywordSettings()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FuturisticLiveBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "JARVIS SETTINGS MATRIX",
                                color = Color(0xFF00E5FF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Voice, Language & Detection System",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                voiceManager.stopVoiceTest()
                                saveSettings()
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                selectedPersona = Persona.ZOYA
                                selectedLangCode = "hi"
                                wakeWordEnabled = true
                                sensitivity = 0.75f
                                customWakeWord = "Hey Zoya"
                                hapticFeedback = true
                                saveSettings()
                                Toast.makeText(context, "Settings reset to default", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Defaults",
                                tint = Color(0xFFFF80AB)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. AI Persona Section
                SettingsSectionCard(
                    title = "AI Assistant Persona",
                    icon = Icons.Default.RecordVoiceOver
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PersonaCard(
                            modifier = Modifier.weight(1f),
                            title = "Zoya (Female AI)",
                            subtitle = "Warm & Empathetic Voice",
                            icon = "♀️",
                            accentColor = Color(0xFFFF4081),
                            isSelected = selectedPersona == Persona.ZOYA,
                            onClick = {
                                voiceManager.stopVoiceTest()
                                selectedPersona = Persona.ZOYA
                                saveSettings()
                            }
                        )

                        PersonaCard(
                            modifier = Modifier.weight(1f),
                            title = "Jarvis (Cinematic AI)",
                            subtitle = "Deep & Powerful Male Persona",
                            icon = "♂️",
                            accentColor = Color(0xFF00E5FF),
                            isSelected = selectedPersona == Persona.JARVIS,
                            onClick = {
                                voiceManager.stopVoiceTest()
                                selectedPersona = Persona.JARVIS
                                saveSettings()
                            }
                        )
                    }
                }

                // 2. Language Matrix Section
                SettingsSectionCard(
                    title = "Language Matrix (भाषा चयन)",
                    icon = Icons.Default.Language
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VoiceData.LANGUAGES.forEach { lang ->
                            val isSelected = selectedLangCode == lang.code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.25f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        voiceManager.stopVoiceTest()
                                        selectedLangCode = lang.code
                                        saveSettings()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${lang.nativeName} (${lang.nameInEnglish})",
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 3. Voice Selection & Voice Test
                SettingsSectionCard(
                    title = "Voice Engine & Vocal Tuning",
                    icon = Icons.Default.Mic
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        voiceList.forEach { voice ->
                            val isSelected = selectedVoiceId == voice.id
                            val cardBg = if (isSelected) {
                                if (selectedPersona == Persona.ZOYA) Color(0xFFFF4081).copy(alpha = 0.15f)
                                else Color(0xFF00E5FF).copy(alpha = 0.15f)
                            } else Color.White.copy(alpha = 0.04f)

                            val borderCol = if (isSelected) {
                                if (selectedPersona == Persona.ZOYA) Color(0xFFFF4081)
                                else Color(0xFF00E5FF)
                            } else Color.White.copy(alpha = 0.1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                                    .clickable {
                                        voiceManager.stopVoiceTest()
                                        selectedVoiceId = voice.id
                                        saveSettings()
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                voiceManager.stopVoiceTest()
                                                selectedVoiceId = voice.id
                                                saveSettings()
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = if (selectedPersona == Persona.ZOYA) Color(0xFFFF4081) else Color(0xFF00E5FF),
                                                unselectedColor = Color.White.copy(alpha = 0.4f)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = voice.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = voice.description,
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (voice.isFemale) "♀ Female" else "♂ Deep Male",
                                        color = if (voice.isFemale) Color(0xFFFF80AB) else Color(0xFF80D8FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Pitch / Speed Sliders
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pitch Tuning:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("${String.format("%.2f", pitchValue)}x", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = pitchValue,
                                onValueChange = {
                                    pitchValue = it
                                    saveSettings()
                                },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00E5FF),
                                    activeTrackColor = Color(0xFF00E5FF)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Speed Rate:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("${String.format("%.2f", speedValue)}x", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = speedValue,
                                onValueChange = {
                                    speedValue = it
                                    saveSettings()
                                },
                                valueRange = 0.7f..1.3f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00E5FF),
                                    activeTrackColor = Color(0xFF00E5FF)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Voice Test Button
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (isTestingVoice) {
                                    voiceManager.stopVoiceTest()
                                } else {
                                    voiceManager.testVoice(
                                        persona = selectedPersona,
                                        language = currentLangConfig,
                                        voiceOption = currentVoiceOption,
                                        customPitch = pitchValue,
                                        customSpeed = speedValue
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTestingVoice) Color(0xFFFF1744) else Color(0xFF7C4DFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (isTestingVoice) "⏹️ Stop Voice Test" else "▶️ Voice Test (${currentLangConfig.nativeName})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 4. Keyword Spotting Section
                SettingsSectionCard(
                    title = "Keyword Spotting (Wake Word)",
                    icon = Icons.Default.RecordVoiceOver
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF241530).copy(alpha = 0.6f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Listen for Wake Word",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                if (wakeWordEnabled) "Background trigger active" else "Detection paused",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = {
                                wakeWordEnabled = it
                                saveSettings()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFFF4081)
                            )
                        )
                    }

                    AnimatedVisibility(visible = wakeWordEnabled, enter = fadeIn(), exit = fadeOut()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = customWakeWord,
                                onValueChange = {
                                    customWakeWord = it
                                    saveSettings()
                                },
                                label = { Text("Custom Trigger Phrase", color = Color(0xFFFFD1DC)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF80AB),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E1428),
                                    unfocusedContainerColor = Color(0xFF1E1428)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Sensitivity Slider
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF241530).copy(alpha = 0.6f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Detection Sensitivity:", color = Color.White, fontSize = 13.sp)
                                    Text("${(sensitivity * 100).roundToInt()}%", color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = sensitivity,
                                    onValueChange = {
                                        sensitivity = it
                                        saveSettings()
                                    },
                                    valueRange = 0.40f..0.98f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFF4081),
                                        activeTrackColor = Color(0xFFFF80AB)
                                    )
                                )
                            }
                        }
                    }
                }

                // 5. Gemini Live API Key Section
                SettingsSectionCard(
                    title = "Gemini Live API Key",
                    icon = Icons.Default.Key
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = {
                                apiKey = it
                                saveSettings()
                            },
                            label = { Text("API Key (AIza...)", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            visualTransformation = if (showApiKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKeyText = !showApiKeyText }) {
                                    Icon(
                                        imageVector = if (showApiKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF80AB),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E1428),
                                unfocusedContainerColor = Color(0xFF1E1428)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF80AB).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFFD1DC)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Get Gemini API Key (AI Studio)")
                        }
                    }
                }

                // 6. Automation & Accessibility
                SettingsSectionCard(
                    title = "Device Automation & Accessibility",
                    icon = Icons.Default.Accessibility
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                            contentColor = Color(0xFFD1C4E9)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Accessibility Settings", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0E25).copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            content()
        }
    }
}

@Composable
private fun PersonaCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) accentColor.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(icon, fontSize = 18.sp)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}
