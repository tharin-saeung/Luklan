package com.commu.luklan.ui.ocr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.commu.luklan.data.getVisionRepository
import com.commu.luklan.platform.pickImageFromDevice
import com.commu.luklan.ui.medicine.MedicineFormState
import com.commu.luklan.ui.theme.LuklanTypography
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.commu.luklan.utils.getCurrentTimeMillis
import com.commu.luklan.ui.ocr.parseOcrToForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScanScreen(
    onBack: () -> Unit,
    onProceedToAdd: () -> Unit
) {
    val visionRepository = remember { getVisionRepository() }
    val scope = rememberCoroutineScope()
    var ready by remember { mutableStateOf(false) }

    var extractedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Kick off a lightweight background warm-up so first upload is less likely to fail
    LaunchedEffect(Unit) {
        // best-effort warm up; if it fails we still allow the user to try
        try {
            visionRepository.warmUp()
        } catch (_: Throwable) {
        }
        ready = true
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("สแกนฉลากยา", style = LuklanTypography.h3) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back") }
                })
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Column(modifier = Modifier
                .fillMaxSize()) {
            // Buttons to pick image or use sample/paste
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    // Try to pick image via platform API - may be unimplemented on some platforms
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            println("OCR: pickImage - start")
                            val tPickStart = getCurrentTimeMillis()
                            var bytes = pickImageFromDevice()
                            val pickElapsed = getCurrentTimeMillis() - tPickStart
                            println("OCR: pickImage - returned bytes=${bytes?.size}")
                            // If the picker returned null almost immediately, it's likely a transient
                            // presentation/gesture issue — retry once after a short delay.
                            if (bytes == null && pickElapsed < 500L) {
                                println("OCR: pickImage - quick null returned in ${pickElapsed}ms, retrying once")
                                kotlinx.coroutines.delay(300)
                                val tRetryStart = getCurrentTimeMillis()
                                bytes = pickImageFromDevice()
                                val retryElapsed = getCurrentTimeMillis() - tRetryStart
                                println("OCR: pickImage - retry returned bytes=${bytes?.size} (took ${retryElapsed}ms)")
                            }
                            if (bytes != null) {
                                    println("OCR: recognizeText - start")
                                    val t0 = getCurrentTimeMillis()
                                    val result = withTimeoutOrNull(12000L) { visionRepository.recognizeText(bytes) }
                                    val took = getCurrentTimeMillis() - t0
                                    if (result == null) {
                                        error = "OCR timed out after ${took}ms"
                                        println("OCR: recognizeText - timed out after ${took}ms")
                                    } else {
                                        result.onSuccess {
                                            extractedText = it
                                            println("OCR: recognizeText - success in ${took}ms")
                                        }
                                        result.onFailure {
                                            error = it.message ?: "Failed to OCR"
                                            println("OCR: recognizeText - failure in ${took}ms: ${it.message}")
                                        }
                                    }
                            } else {
                                    error = "No image selected (picker returned null). Check permissions or try again."
                                    println("OCR: pickImage returned null - possible presenter/permission issue")
                            }
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            isLoading = false
                        }
                    }
                }, enabled = ready && !isLoading) { Text(if (!ready) "กำลังเตรียมระบบ..." else "เลือกภาพ/ถ่ายรูป") }

                Button(onClick = {
                    // Use sample text flow (quick demo). Replace with actual sample bytes if available.
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            // For quick demo we ask user to paste or we simulate a result
                            extractedText = "Paracetamol 500mg 1 เม็ด ทุกวัน"
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            isLoading = false
                        }
                    }
                }) { Text("ตัวอย่าง") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("ผลลัพธ์จาก OCR:")
            OutlinedTextField(
                value = extractedText,
                onValueChange = {}, // read-only
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f),
                readOnly = true,
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            if (error != null) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                // Parse extractedText into a simple form and store it
                val parsed = parseOcrToForm(extractedText)
                OcrResultStore.lastForm = parsed
                onProceedToAdd()
            }, enabled = extractedText.isNotBlank()) {
                Text("นำผลไปเพิ่มยา")
            }
            }

            // Loading overlay
            if (isLoading) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x80000000)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// // Very small heuristic parser: first word(s) -> name, look for first number -> dosage
// fun parseOcrToForm(text: String): MedicineFormState {
//     if (text.isBlank()) return MedicineFormState()

//     // Work on a mutable copy and progressively remove matched fragments
//     var working = text.replace("\r", "\n")

//     // Helpers
//     fun removeMatch(m: MatchResult?) {
//         if (m == null) return
//         working = working.replaceFirst(m.value, " ")
//     }

//     // 1) Extract explicit HH:MM times
//     val timeRegex = "\\b([01]?\\d|2[0-3]):[0-5]\\d\\b".toRegex()
//     val foundTimes = mutableListOf<String>()
//     timeRegex.findAll(working).forEach { mr ->
//         val t = mr.value.padStart(5, '0')
//         if (!foundTimes.contains(t)) foundTimes.add(t)
//         removeMatch(mr)
//     }

//     // 2) Keyword times (Thai words)
//     val keywordMap = mapOf(
//         "ตอนเช้า" to "08:00",
//         "เช้า" to "08:00",
//         "กลางวัน" to "12:00",
//         "เที่ยง" to "12:00",
//         "เย็น" to "18:00",
//         "ก่อนนอน" to "21:00",
//         "ก่อนอาหาร" to "07:30",
//         "หลังอาหาร" to "13:00"
//     )
//     for ((k, v) in keywordMap) {
//         if (working.contains(k, ignoreCase = true) && !foundTimes.contains(v)) {
//             foundTimes.add(v)
//             working = working.replaceFirst(Regex("(?i)" + Regex.escape(k)), " ")
//         }
//     }

//     // 3) Fallback for '8 โมง' like tokens
//     if (foundTimes.isEmpty()) {
//         val hourWord = "(\\b(\\d{1,2})\\s*(โมง|นาฬิกา)\\b)".toRegex()
//         val hw = hourWord.find(working)
//         if (hw != null) {
//             val h = hw.groupValues[2].toIntOrNull() ?: 0
//             val hh = h.toString().padStart(2, '0')
//             val guess = "$hh:00"
//             foundTimes.add(guess)
//             removeMatch(hw)
//         }
//     }

//     // 4) Dosage / amount per dose (e.g., '1 เม็ด', '1/2', '500 mg')
//     val dosageRegex = "(\\d+(?:[\\/.]\\d+)?(?:\\s*\\/\\s*\\d+)?)(?:\\s*)(mg|g|ml|เม็ด|แผง|แท็บเล็ต|tab)?".toRegex(RegexOption.IGNORE_CASE)
//     val dosageMatch = dosageRegex.find(working)
//     val amountPerDose = dosageMatch?.value?.trim() ?: ""
//     val dosage = dosageMatch?.groups?.get(1)?.value ?: ""
//     val unit = dosageMatch?.groups?.get(2)?.value?.lowercase() ?: run {
//         when {
//             working.contains("mg", ignoreCase = true) -> "mg"
//             working.contains("เม็ด") -> "เม็ด"
//             working.contains("ml", ignoreCase = true) -> "ml"
//             else -> ""
//         }
//     }
//     removeMatch(dosageMatch)

//     // 5) Quantity (total) e.g., '(30 เม็ด)', 'จำนวน 1 แผง', '1 แผง'
//     var quantity = ""
//     val quantityRegex1 = "\\((\\d+)\\s*(เม็ด|แผง|ขวด|กล่อง|หลอด)\\)".toRegex(RegexOption.IGNORE_CASE)
//     val q1 = quantityRegex1.find(working)
//     if (q1 != null) {
//         quantity = q1.groups[1]?.value ?: ""
//         removeMatch(q1)
//     } else {
//         val quantityRegex2 = "จำนวน\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE)
//         val q2 = quantityRegex2.find(working)
//         if (q2 != null) {
//             quantity = q2.groups[1]?.value ?: ""
//             removeMatch(q2)
//         } else {
//             val q3 = "(\\d+)\\s*(แผง|เม็ด|กล่อง|ขวด)".toRegex(RegexOption.IGNORE_CASE).find(working)
//             if (q3 != null) {
//                 quantity = q3.groups[1]?.value ?: ""
//                 removeMatch(q3)
//             }
//         }
//     }

//     // 6) Frequency (วันละ X ครั้ง, สัปดาห์ละ X ครั้ง, เดือนละ X ครั้ง)
//     var frequency = ""
//     var timeUnit = "วัน"
//     var frequencyCount = 0
//     val freqPatterns = listOf(
//         "วันละ\\s*(\\d+)\\s*ครั้ง",
//         "สัปดาห์ละ\\s*(\\d+)\\s*ครั้ง",
//         "เดือนละ\\s*(\\d+)\\s*ครั้ง",
//         "(\\d+)\\s*ครั้ง\\s*ต่อ\\s*สัปดาห์",
//         "(\\d+)\\s*ครั้ง\\s*ต่อ\\s*เดือน"
//     )
//     for (p in freqPatterns) {
//         val r = p.toRegex(RegexOption.IGNORE_CASE)
//         val m = r.find(working)
//         if (m != null) {
//             val cnt = m.groups[1]?.value?.toIntOrNull() ?: 0
//             frequencyCount = cnt
//             when {
//                 p.startsWith("วัน") -> timeUnit = "วัน"
//                 p.startsWith("สัปดาห์") -> timeUnit = "สัปดาห์"
//                 p.startsWith("เดือน") -> timeUnit = "เดือน"
//                 p.contains("สัปดาห์") -> timeUnit = "สัปดาห์"
//                 p.contains("เดือน") -> timeUnit = "เดือน"
//             }
//             frequency = when (timeUnit) {
//                 "วัน" -> "วันละ ${frequencyCount} ครั้ง"
//                 else -> "${timeUnit}ละ ${frequencyCount} ครั้ง"
//             }
//             removeMatch(m)
//             break
//         }
//     }

//     // 7) Expiry / dates
//     val dateRegex = "\\b(\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4})\\b".toRegex()
//     val dateMatch = dateRegex.find(working)
//     val thaiMonthRegex = """(\d{1,2})\s+(ม\.?ค\.?|ก\.?พ\.?|มี\.?ค\.?|เม\.?ย\.?|พ\.?ค\.?|มิ\.?ย\.?|ก\.?ค\.?|ส\.?ค\.?|ก\.?ย\.?|ต\.?ค\.?|พ\.?ย\.?|ธ\.?ค\.? )\s*(\d{2,4})""".toRegex(RegexOption.IGNORE_CASE)
//     val thaiDateMatch = thaiMonthRegex.find(working)
//     val expiry = dateMatch?.value ?: thaiDateMatch?.value ?: ""
//     removeMatch(dateMatch)
//     removeMatch(thaiDateMatch)

//     // 8) Remove phone numbers, emails and common contact/address lines
//     val phoneRegex = "(?:0\\d{1,2}[- ]?\\d{3}[- ]?\\d{4}|02[- ]?\\d{3}[- ]?\\d{4}|\\+?\\d{2,4}[- ]?\\d{6,8})".toRegex()
//     val emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}".toRegex()
//     phoneRegex.findAll(working).forEach { m -> removeMatch(m) }
//     emailRegex.findAll(working).forEach { m -> removeMatch(m) }

//     // Remove common address-like tokens / labels to avoid them being selected as name
//     val addressTokens = listOf("ถ.", "ถนน", "แขวง", "เขต", "ต.", "ต.", "อ.", "อำเภอ", "จังหวัด", "กรุงเทพ", "เบอร์", "โทร", "TEL", "E-mail", "HN", "VN", "LINE", "Line", "โรงพยาบาล", "Hosp", "Address")
//     for (tok in addressTokens) {
//         val r = Regex("(?i)" + Regex.escape(tok))
//         working = working.replace(r, " ")
//     }

//     // 9) After removals, pick the best name token: prefer proximity to dosage then filtered fallback
//     fun isNoiseToken(t: String): Boolean {
//         val lower = t.lowercase()
//         val noiseWords = listOf("ผู้ใช้ยา", "ใช้ตามแพทย์สั่งเท่านั้น", "สรรพคุณ", "วิธีใช้", "รับประทาน", "ก่อนอาหาร", "หลังอาหาร", "เช้า", "เย็น", "กลางวัน", "วันละ", "ครั้ง", "จำนวน", "เม็ด", "แผง", "แพทย์", "line", "official", "email", "tel")
//         if (phoneRegex.matches(t) || emailRegex.matches(t)) return true
//         if (lower.any { it.isDigit() } && lower.length > 30) return true
//         if (noiseWords.any { lower.contains(it) }) return true
//         if (t.length <= 1) return true
//         return false
//     }

//     val original = text
//     var nameCandidate: String? = null
//     if (dosageMatch != null) {
//         val startIndex = dosageMatch.range.first
//         val before = if (startIndex > 0) original.substring(0, startIndex) else original
//         val candidateLines = before.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }
//         for (i in candidateLines.size - 1 downTo 0) {
//             val line = candidateLines[i].replace(Regex("[^0-9A-Za-z\\u0E00-\\u0E7F\\s\\-]"), " ").trim()
//             if (line.isNotEmpty() && !isNoiseToken(line)) {
//                 val parts = line.split('\t', ',', '/', '-', '|').map { it.trim() }.filter { it.isNotEmpty() }
//                 val firstPart = parts.firstOrNull() ?: line
//                 val subtokens = firstPart.split(' ').map { it.trim() }.filter { it.isNotEmpty() }
//                 val pick = subtokens.filter { s -> s.any { it.isLetter() } }.maxByOrNull { it.length }
//                 if (pick != null && !isNoiseToken(pick)) {
//                     nameCandidate = pick
//                     break
//                 }
//             }
//         }
//     }

//     if (nameCandidate == null) {
//         val cleaned = working.replace(Regex("[^0-9A-Za-z\\u0E00-\\u0E7F\\s\\-:/]"), " ")
//         val tokens = cleaned.split('\n', ' ', '\t').map { it.trim().trim('.') }.filter { it.isNotEmpty() }
//         val phoneTest = phoneRegex
//         val filtered = tokens
//             .filter { t -> !phoneTest.matches(t) && t.length >= 2 && t.any { it.isLetter() } }
//             .filter { t -> !isNoiseToken(t) }
//         nameCandidate = filtered.maxByOrNull { it.length } ?: tokens.firstOrNull()
//     }

//     val name = nameCandidate ?: ""

//     val timesList = foundTimes.toList()
//     val primaryTime = timesList.firstOrNull() ?: ""

//     // If frequency wasn't detected, derive a friendly frequency string from times count
//     val finalFrequency = if (frequency.isNotBlank()) frequency else when (timesList.size) {
//         0 -> ""
//         1 -> "ทุกวัน"
//         2 -> "เช้า/เย็น"
//         3 -> "เช้า/กลางวัน/เย็น"
//         else -> "วันละ ${timesList.size} ครั้ง"
//     }

//     return MedicineFormState(
//         name = name,
//         dosage = dosage,
//         unit = unit,
//         time = primaryTime,
//         times = timesList,
//         quantity = quantity,
//         frequency = finalFrequency,
//         expiryDate = expiry,
//         timeUnit = timeUnit,
//         frequencyCount = frequencyCount,
//         amountPerDose = amountPerDose
//     )
// }
