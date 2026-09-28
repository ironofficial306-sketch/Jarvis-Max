package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ZoyaForegroundService
import com.example.voice.LanguageConfig
import com.example.voice.Persona
import com.example.voice.VoiceData
import com.example.voice.VoiceManager
import com.example.voice.VoiceOption

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ZoyaPrefs", Context.MODE_PRIVATE) }
    
    val voiceManager = remember { VoiceManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    var selectedPersona by remember {
        val saved = prefs.getString("active_persona", "ZOYA") ?: "ZOYA"
        mutableStateOf(if (saved == "JARVIS") Persona.JARVIS else Persona.ZOYA)
    }

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

    Dialog(
        onDismissRequest = {
            voiceManager.stopVoiceTest()
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.4f),
                            Color(0xFF7C4DFF).copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            color = Color(0xFF141424)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "आवाज़ व भाषा सेटिंग्स",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multiple Voices & Languages System",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            voiceManager.stopVoiceTest()
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(32.dp)
                    ) {
                        Text("✕", color = Color.White, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Persona Selection
                Text(
                    text = "1. AI Persona चुनें (Choose Persona):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PersonaCard(
                        modifier = Modifier.weight(1f),
                        title = "Zoya (Female AI)",
                        subtitle = "Warm, Empathetic Female Voice",
                        icon = "♀️",
                        accentColor = Color(0xFFFF4081),
                        isSelected = selectedPersona == Persona.ZOYA,
                        onClick = {
                            voiceManager.stopVoiceTest()
                            selectedPersona = Persona.ZOYA
                        }
                    )

                    PersonaCard(
                        modifier = Modifier.weight(1f),
                        title = "Jarvis (Cinematic AI)",
                        subtitle = "Deep, Mature Male Persona",
                        icon = "♂️",
                        accentColor = Color(0xFF00E5FF),
                        isSelected = selectedPersona == Persona.JARVIS,
                        onClick = {
                            voiceManager.stopVoiceTest()
                            selectedPersona = Persona.JARVIS
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Language Selection
                Text(
                    text = "2. भाषा चुनें (Select Language):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                                    else Color.White.copy(alpha = 0.06f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    voiceManager.stopVoiceTest()
                                    selectedLangCode = lang.code
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: Voice Options
                Text(
                    text = "3. Voice Options (${selectedPersona.displayName}):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Voice Fine-Tuning Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "4. Voice Tone Tuning (आवाज़ ट्यूनिंग):",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Pitch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pitch (आवाज़ का भारी/पतलापन):", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("${String.format("%.2f", pitchValue)}x", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = pitchValue,
                        onValueChange = { pitchValue = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )

                    // Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speed (गति):", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("${String.format("%.2f", speedValue)}x", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speedValue,
                        onValueChange = { speedValue = it },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 5: Voice Test Button
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_test_button"),
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
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isTestingVoice) "⏹️ आवाज़ रोकें (Stop Test)" else "▶️ Voice Test (आवाज़ टेस्ट करें)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isTestingVoice) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔊 Realistic natural voice testing in progress (${currentLangConfig.nativeName})...",
                        color = Color(0xFF69F0AE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 6: Apply & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            voiceManager.stopVoiceTest()
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("रद्द करें (Cancel)", color = Color.White.copy(alpha = 0.8f))
                    }

                    Button(
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("apply_voice_button"),
                        onClick = {
                            voiceManager.stopVoiceTest()

                            // Save to SharedPreferences
                            prefs.edit()
                                .putString("active_persona", selectedPersona.id)
                                .putString("selected_language", selectedLangCode)
                                .putString(
                                    if (selectedPersona == Persona.ZOYA) "zoya_voice" else "jarvis_voice",
                                    selectedVoiceId
                                )
                                .putFloat("pitch_${selectedVoiceId}", pitchValue)
                                .putFloat("speed_${selectedVoiceId}", speedValue)
                                .apply()

                            // Notify Active Service to update live session voice
                            val service = ZoyaForegroundService.activeService
                            service?.updateVoiceSettings()

                            Toast.makeText(
                                context,
                                "${selectedPersona.displayName} selected in ${currentLangConfig.nativeName}! Voice updated.",
                                Toast.LENGTH_SHORT
                            ).show()

                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("लागू करें (Apply Voice)", fontWeight = FontWeight.Bold)
                    }
                }
            }
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
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) accentColor.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
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
                Text(icon, fontSize = 20.sp)
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
                fontSize = 13.sp,
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
