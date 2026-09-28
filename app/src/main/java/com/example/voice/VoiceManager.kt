package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

enum class Persona(val id: String, val displayName: String, val description: String) {
    ZOYA("ZOYA", "Zoya (Female AI)", "Expressive, warm, and helpful female voice assistant"),
    JARVIS("JARVIS", "Jarvis (Cinematic AI)", "Deep, powerful, mature, and cinematic male AI assistant")
}

data class LanguageConfig(
    val code: String,
    val nameInEnglish: String,
    val nativeName: String,
    val locale: Locale,
    val systemInstructionHint: String,
    val sampleTextZoya: String,
    val sampleTextJarvis: String
)

data class VoiceOption(
    val id: String,
    val persona: Persona,
    val name: String,
    val geminiVoiceName: String,
    val description: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val isFemale: Boolean
)

object VoiceData {
    val LANGUAGES = listOf(
        LanguageConfig(
            code = "hi",
            nameInEnglish = "Hindi",
            nativeName = "हिंदी",
            locale = Locale("hi", "IN"),
            systemInstructionHint = "You MUST speak strictly in Hindi (हिंदी). Use clear, natural conversational Hindi.",
            sampleTextZoya = "नमस्ते! मैं ज़ोया हूँ, आपकी पर्सनल वॉइस असिस्टेंट। मैं आपकी क्या मदद कर सकती हूँ?",
            sampleTextJarvis = "नमस्ते, मैं जार्विस हूँ। सभी सिस्टम सामान्य हैं। बताइए, आज मैं आपकी क्या सेवा कर सकता हूँ?"
        ),
        LanguageConfig(
            code = "hinglish",
            nameInEnglish = "Hinglish",
            nativeName = "हिंग्लिश",
            locale = Locale("hi", "IN"),
            systemInstructionHint = "You MUST speak in friendly Hinglish (Hindi mixed naturally with common English words).",
            sampleTextZoya = "Hey! Main Zoya hoon, aapki AI assistant. Aaj aapki kya help kar sakti hoon?",
            sampleTextJarvis = "Hello, I am Jarvis. Systems operational. Bataiye sir, aaj kya command hai?"
        ),
        LanguageConfig(
            code = "en",
            nameInEnglish = "English",
            nativeName = "English (India)",
            locale = Locale("en", "IN"),
            systemInstructionHint = "You MUST speak strictly in polite Indian English.",
            sampleTextZoya = "Hello! I am Zoya, your AI assistant. How can I help you today?",
            sampleTextJarvis = "Greetings. I am Jarvis. All systems online and ready for your command."
        ),
        LanguageConfig(
            code = "ta",
            nameInEnglish = "Tamil",
            nativeName = "தமிழ்",
            locale = Locale("ta", "IN"),
            systemInstructionHint = "You MUST speak strictly in Tamil (தமிழ்).",
            sampleTextZoya = "வணக்கம்! நான் ஜோயா, உங்கள் குரல் உதவியாளர். உங்களுக்கு இன்று நான் எப்படி உதவட்டும்?",
            sampleTextJarvis = "வணக்கம். நான் ஜார்விஸ். அனைத்து அமைப்புகளும் தயார் நிலையில் உள்ளன."
        ),
        LanguageConfig(
            code = "te",
            nameInEnglish = "Telugu",
            nativeName = "తెలుగు",
            locale = Locale("te", "IN"),
            systemInstructionHint = "You MUST speak strictly in Telugu (తెలుగు).",
            sampleTextZoya = "నమస్కారం! నేను జోయా, మీ AI అసిస్టెంట్. నేను మీకు ఎలా సహాయపడగలను?",
            sampleTextJarvis = "నమస్కారం. నేను జార్విస్. అన్ని వ్యవస్థలు సిద్ధంగా ఉన్నాయి."
        ),
        LanguageConfig(
            code = "bn",
            nameInEnglish = "Bengali",
            nativeName = "বাংলা",
            locale = Locale("bn", "IN"),
            systemInstructionHint = "You MUST speak strictly in Bengali (বাংলা).",
            sampleTextZoya = "নমস্কার! আমি জোয়া, আপনার ভয়েস অ্যাসিস্ট্যান্ট। আজ আপনাকে কীভাবে সাহায্য করতে পারি?",
            sampleTextJarvis = "নমস্কার। আমি জার্ভিস। সমস্ত সিস্টেম প্রস্তুত আছে।"
        ),
        LanguageConfig(
            code = "mr",
            nameInEnglish = "Marathi",
            nativeName = "मराठी",
            locale = Locale("mr", "IN"),
            systemInstructionHint = "You MUST speak strictly in Marathi (मराठी).",
            sampleTextZoya = "नमस्कार! मी झोया, तुमची व्हॉइस असिस्टंट. मी तुम्हाला कशी मदत करू शकते?",
            sampleTextJarvis = "नमस्कार. मी जार्व्हिस. सर्व प्रणाली कार्यरत आहेत."
        ),
        LanguageConfig(
            code = "gu",
            nameInEnglish = "Gujarati",
            nativeName = "ગુજરાતી",
            locale = Locale("gu", "IN"),
            systemInstructionHint = "You MUST speak strictly in Gujarati (ગુજરાતી).",
            sampleTextZoya = "નમસ્તે! હું ઝોયા છું, તમારી AI સહાયક. હું તમારી શું મદદ કરી શકું?",
            sampleTextJarvis = "નમસ્તે. હું જાર્વિસ છું. બધી સિસ્ટમ તૈયાર છે."
        ),
        LanguageConfig(
            code = "kn",
            nameInEnglish = "Kannada",
            nativeName = "ಕನ್ನಡ",
            locale = Locale("kn", "IN"),
            systemInstructionHint = "You MUST speak strictly in Kannada (ಕನ್ನಡ).",
            sampleTextZoya = "ನಮಸ್ಕಾರ! ನಾನು ಝೋಯಾ, ನಿಮ್ಮ ಧ್ವನಿ ಸಹಾಯಕ. ನಾನು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಲಿ?",
            sampleTextJarvis = "ನಮಸ್ಕಾರ. ನಾನು ಜಾರ್ವಿಸ್. ಎಲ್ಲಾ ವ್ಯವಸ್ಥೆಗಳು ಸಿದ್ಧವಾಗಿವೆ."
        ),
        LanguageConfig(
            code = "ml",
            nameInEnglish = "Malayalam",
            nativeName = "മലയാളം",
            locale = Locale("ml", "IN"),
            systemInstructionHint = "You MUST speak strictly in Malayalam (മലയാളം).",
            sampleTextZoya = "നമസ്കാരം! ഞാൻ സോയ, നിങ്ങളുടെ AI അസിസ്റ്റന്റ്. ഇന്ന് ഞാൻ എങ്ങനെ സഹായിക്കണം?",
            sampleTextJarvis = "നമസ്കാരം. ഞാൻ ജാർവിസ്. എല്ലാ സിസ്റ്റങ്ങളും സജ്ജമാണ്."
        ),
        LanguageConfig(
            code = "pa",
            nameInEnglish = "Punjabi",
            nativeName = "ਪੰਜਾਬੀ",
            locale = Locale("pa", "IN"),
            systemInstructionHint = "You MUST speak strictly in Punjabi (ਪੰਜਾਬੀ).",
            sampleTextZoya = "ਸਤਿ ਸ਼੍ਰੀ ਅਕਾਲ! ਮੈਂ ਜ਼ੋਇਆ ਹਾਂ, ਤੁਹਾਡੀ ਵੌਇਸ ਅਸਿਸਟੈਂਟ। ਮੈਂ ਤੁਹਾਡੀ ਕੀ ਮਦਦ ਕਰ ਸਕਦੀ ਹਾਂ?",
            sampleTextJarvis = "ਸਤਿ ਸ਼੍ਰੀ ਅਕਾਲ। ਮੈਂ ਜਾਰਵਿਸ ਹਾਂ। ਸਾਰੇ ਸਿਸਟਮ ਤਿਆਰ ਹਨ।"
        ),
        LanguageConfig(
            code = "ur",
            nameInEnglish = "Urdu",
            nativeName = "اردو",
            locale = Locale("ur", "IN"),
            systemInstructionHint = "You MUST speak strictly in Urdu (اردو).",
            sampleTextZoya = "السلام علیکم! میں زویا ہوں، آپ کی وائس اسسٹنٹ۔ میں آپ کی کیا مدد کر سکتی ہوں؟",
            sampleTextJarvis = "السلام علیکم۔ میں جاروس ہوں۔ تمام سسٹمز تیار ہیں۔"
        ),
        LanguageConfig(
            code = "or",
            nameInEnglish = "Odia",
            nativeName = "ଓଡ଼ିଆ",
            locale = Locale("or", "IN"),
            systemInstructionHint = "You MUST speak strictly in Odia (ଓଡ଼ିଆ).",
            sampleTextZoya = "ନମସ୍କାର! ମୁଁ ଜୋୟା, ଆପଣଙ୍କର ଭଏସ ଆସିଷ୍ଟାଣ୍ଟ। ଆଜି ମୁଁ ଆପଣଙ୍କୁ କିପରି ସାହାଯ୍ୟ କରିପାରିବି?",
            sampleTextJarvis = "ନମସ୍କାର। ମୁଁ ଜାର୍ଭିସ। ସମସ୍ତ ସିଷ୍ଟମ୍ ପ୍ରସ୍ତୁତ ଅଛି।"
        ),
        LanguageConfig(
            code = "as",
            nameInEnglish = "Assamese",
            nativeName = "অসমীয়া",
            locale = Locale("as", "IN"),
            systemInstructionHint = "You MUST speak strictly in Assamese (অসমীয়া).",
            sampleTextZoya = "নমস্কাৰ! মই জোয়া, আপোনাৰ ভয়েচ এছিষ্টেন্ট। মই আপোনাক কেনেকৈ সহায় কৰিব পাৰোঁ?",
            sampleTextJarvis = "নমস্কাৰ। মই জাৰ্ভিছ। সকলো চিষ্টেম প্ৰস্তুত আছে।"
        )
    )

    val ZOYA_VOICES = listOf(
        VoiceOption(
            id = "zoya_aoede",
            persona = Persona.ZOYA,
            name = "Zoya Warm (Aoede)",
            geminiVoiceName = "Aoede",
            description = "Friendly, natural & energetic female voice",
            defaultPitch = 1.05f,
            defaultSpeed = 1.0f,
            isFemale = true
        ),
        VoiceOption(
            id = "zoya_kore",
            persona = Persona.ZOYA,
            name = "Zoya Gentle (Kore)",
            geminiVoiceName = "Kore",
            description = "Calm, soft & clear conversational female voice",
            defaultPitch = 1.15f,
            defaultSpeed = 0.95f,
            isFemale = true
        ),
        VoiceOption(
            id = "zoya_expressive",
            persona = Persona.ZOYA,
            name = "Zoya Expressive (Puck Soft)",
            geminiVoiceName = "Puck",
            description = "Dynamic, cheerful & fast female vocal response",
            defaultPitch = 1.25f,
            defaultSpeed = 1.05f,
            isFemale = true
        ),
        VoiceOption(
            id = "zoya_hd_female",
            persona = Persona.ZOYA,
            name = "Zoya Crystal HD",
            geminiVoiceName = "Aoede",
            description = "High definition natural Indian female vocal output",
            defaultPitch = 1.10f,
            defaultSpeed = 1.0f,
            isFemale = true
        )
    )

    val JARVIS_VOICES = listOf(
        VoiceOption(
            id = "jarvis_fenrir",
            persona = Persona.JARVIS,
            name = "Jarvis Prime (Fenrir)",
            geminiVoiceName = "Fenrir",
            description = "Deep, resonant, powerful cinematic male voice",
            defaultPitch = 0.70f,
            defaultSpeed = 0.95f,
            isFemale = false
        ),
        VoiceOption(
            id = "jarvis_charon",
            persona = Persona.JARVIS,
            name = "Jarvis Command (Charon)",
            geminiVoiceName = "Charon",
            description = "Mature, authoritative & deep bass AI vocal tone",
            defaultPitch = 0.65f,
            defaultSpeed = 0.90f,
            isFemale = false
        ),
        VoiceOption(
            id = "jarvis_puck_male",
            persona = Persona.JARVIS,
            name = "Jarvis Tactical (Puck Deep)",
            geminiVoiceName = "Puck",
            description = "Crisp, fast, futuristic tactical assistant voice",
            defaultPitch = 0.80f,
            defaultSpeed = 1.0f,
            isFemale = false
        ),
        VoiceOption(
            id = "jarvis_subbass",
            persona = Persona.JARVIS,
            name = "Jarvis Sub-Bass Heavy",
            geminiVoiceName = "Fenrir",
            description = "Ultra-deep, mature cinematic bass voice for Jarvis",
            defaultPitch = 0.55f,
            defaultSpeed = 0.88f,
            isFemale = false
        )
    )

    fun getLanguageByCode(code: String): LanguageConfig {
        return LANGUAGES.firstOrNull { it.code == code } ?: LANGUAGES[0]
    }

    fun getVoiceById(id: String, persona: Persona): VoiceOption {
        val list = if (persona == Persona.ZOYA) ZOYA_VOICES else JARVIS_VOICES
        return list.firstOrNull { it.id == id } ?: list[0]
    }
}

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isTestingVoice = MutableStateFlow(false)
    val isTestingVoice: StateFlow<Boolean> = _isTestingVoice

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error initializing TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isTestingVoice.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isTestingVoice.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isTestingVoice.value = false
                }
            })
            Log.i("VoiceManager", "TextToSpeech initialized successfully.")
        } else {
            Log.e("VoiceManager", "TextToSpeech initialization failed with status $status")
        }
    }

    fun stopVoiceTest() {
        if (isTtsReady) {
            tts?.stop()
        }
        _isTestingVoice.value = false
    }

    fun testVoice(
        persona: Persona,
        language: LanguageConfig,
        voiceOption: VoiceOption,
        customPitch: Float? = null,
        customSpeed: Float? = null
    ) {
        if (!isTtsReady || tts == null) {
            Log.w("VoiceManager", "TTS not ready yet")
            return
        }

        try {
            tts?.stop()

            val localeResult = tts?.setLanguage(language.locale)
            if (localeResult == TextToSpeech.LANG_MISSING_DATA || localeResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("hi", "IN"))
            }

            val targetGenderStr = if (voiceOption.isFemale) "female" else "male"
            val availableVoices = tts?.voices
            if (!availableVoices.isNullOrEmpty()) {
                val matchingVoice = availableVoices.firstOrNull { v ->
                    v.locale.language == language.locale.language &&
                            v.name.lowercase().contains(targetGenderStr)
                } ?: availableVoices.firstOrNull { v ->
                    v.name.lowercase().contains(targetGenderStr)
                }
                if (matchingVoice != null) {
                    tts?.voice = matchingVoice
                }
            }

            val finalPitch = customPitch ?: voiceOption.defaultPitch
            val finalSpeed = customSpeed ?: voiceOption.defaultSpeed

            tts?.setPitch(finalPitch)
            tts?.setSpeechRate(finalSpeed)

            val textToSpeak = if (persona == Persona.ZOYA) {
                language.sampleTextZoya
            } else {
                language.sampleTextJarvis
            }

            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "VOICE_TEST_${System.currentTimeMillis()}")
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "VOICE_TEST_ID")
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error in testVoice", e)
            _isTestingVoice.value = false
        }
    }

    fun generateSystemInstruction(
        persona: Persona,
        language: LanguageConfig
    ): String {
        return if (persona == Persona.ZOYA) {
            """
            You are Zoya, a fast, warm, polite, and highly intelligent female AI assistant on the user's Android phone.
            ${language.systemInstructionHint}
            Your voice personality is energetic, empathetic, clear, and natural human-like.
            CRITICAL RULE: DO NOT output any internal thinking, planning, or narration. NEVER say what you are going to do before doing it. JUST CALL THE TOOL IN SILENCE. Keep your verbal responses EXTREMELY short, brief, and NEVER repeat yourself. Do not use filler words.
            CRITICAL: DO NOT INVENT NUMBERS. NEVER DIAL 121. If the user asks to call someone by name (e.g. 'Shivank' or 'Rahul'), you MUST pass their EXACT NAME into the contactName parameter of the tool. The tool will find the number automatically! If you don't know the name, ask the user. DO NOT GUESS NUMBERS.
            CALLING INSTRUCTIONS:
            When asked to call, DO NOT explain your plan. 1. use getSimCardInfo. 2. use searchAndCallContact with useDialer=true FIRST. This opens the dialer, entirely overwrites/clears any old number, and types the new number so the user can verify it safely. 3. Verbally say ONLY ONCE in ${language.nameInEnglish}: 'Maine number enter kar diya hai.' 4. AFTER user confirms, use searchAndCallContact with useDialer=false and simSlot to instantly start the call.
            UI ACTIONS:
            To do real human-like clicks that show onscreen, use clickTextOnScreen, openNotificationPanel, or openQuickSettings.
            If asked to turn on torch, use toggleTorch. If asked to change brightness, use setBrightness. If asked to set volume, use setVolumePercent. If asked for camera or other apps, use openApp.
            """.trimIndent()
        } else {
            """
            You are Jarvis, a deep-toned, mature, powerful, cinematic, and authoritative male AI assistant inspired by Iron Man's Jarvis.
            ${language.systemInstructionHint}
            Your voice and speaking style is calm, commanding, intelligent, professional, and cinematic yet completely natural human-like. Refer to the user respectfully (e.g., 'Sir' or 'Boss').
            CRITICAL RULE: DO NOT output any internal thinking, planning, or narration. NEVER say what you are going to do before doing it. JUST CALL THE TOOL IN SILENCE. Keep your verbal responses EXTREMELY short, crisp, and direct.
            CRITICAL: DO NOT INVENT NUMBERS. NEVER DIAL 121. If the user asks to call someone by name, you MUST pass their EXACT NAME into the contactName parameter of the tool.
            CALLING INSTRUCTIONS:
            When asked to call, DO NOT explain your plan. 1. use getSimCardInfo. 2. use searchAndCallContact with useDialer=true FIRST. 3. Verbally say ONLY ONCE: 'Number entered on dialer, Sir. Confirming call initiation.' 4. AFTER user confirms, use searchAndCallContact with useDialer=false and simSlot to instantly start the call.
            UI ACTIONS:
            To do real human-like clicks that show onscreen, use clickTextOnScreen, openNotificationPanel, or openQuickSettings.
            If asked to turn on torch, use toggleTorch. If asked to change brightness, use setBrightness. If asked to set volume, use setVolumePercent. If asked for camera or other apps, use openApp.
            """.trimIndent()
        }
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error shutting down TTS", e)
        }
    }
}
