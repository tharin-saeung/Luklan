package com.commu.luklan.ui.ocr

import com.commu.luklan.ui.medicine.MedicineFormState

// Extracted OCR parsing logic moved here so it can be tested/iterated independently.
fun parseOcrToForm(text: String): MedicineFormState {
    if (text.isBlank()) return MedicineFormState()

    // Work on a mutable copy and progressively remove matched fragments
    var working = text.replace("\r", "\n")

    // Helpers
    fun removeMatch(m: MatchResult?) {
        if (m == null) return
        working = working.replaceFirst(m.value, " ")
    }

    // 1) Extract explicit HH:MM times
    val timeRegex = "\\b([01]?\\d|2[0-3]):[0-5]\\d\\b".toRegex()
    val foundTimes = mutableListOf<String>()
    timeRegex.findAll(working).forEach { mr ->
        val t = mr.value.padStart(5, '0')
        if (!foundTimes.contains(t)) foundTimes.add(t)
        removeMatch(mr)
    }

    // 2) Keyword times (Thai words)
    val keywordMap = mapOf(
        "ตอนเช้า" to "08:00",
        "เช้า" to "08:00",
        "กลางวัน" to "12:00",
        "เที่ยง" to "12:00",
        "เย็น" to "18:00",
        "ก่อนนอน" to "21:00",
        "ก่อนอาหาร" to "07:30",
        "หลังอาหาร" to "13:00"
    )
    for ((k, v) in keywordMap) {
        if (working.contains(k, ignoreCase = true) && !foundTimes.contains(v)) {
            foundTimes.add(v)
            working = working.replaceFirst(Regex("(?i)" + Regex.escape(k)), " ")
        }
    }

    // 3) Fallback for '8 โมง' like tokens
    if (foundTimes.isEmpty()) {
        val hourWord = "(\\b(\\d{1,2})\\s*(โมง|นาฬิกา)\\b)".toRegex()
        val hw = hourWord.find(working)
        if (hw != null) {
            val h = hw.groupValues[2].toIntOrNull() ?: 0
            val hh = h.toString().padStart(2, '0')
            val guess = "$hh:00"
            foundTimes.add(guess)
            removeMatch(hw)
        }
    }

    // 4) Dosage / amount per dose (e.g., '1 เม็ด', '1/2', '500 mg')
    val dosageRegex = "(\\d+(?:[\\/.]\\d+)?(?:\\s*\\/\\s*\\d+)?)(?:\\s*)(mg|g|ml|เม็ด|แผง|แท็บเล็ต|tab)?".toRegex(RegexOption.IGNORE_CASE)
    val dosageMatch = dosageRegex.find(working)
    val amountPerDose = dosageMatch?.value?.trim() ?: ""
    val dosage = dosageMatch?.groups?.get(1)?.value ?: ""
    val unit = dosageMatch?.groups?.get(2)?.value?.lowercase() ?: run {
        when {
            working.contains("mg", ignoreCase = true) -> "mg"
            working.contains("เม็ด") -> "เม็ด"
            working.contains("ml", ignoreCase = true) -> "ml"
            else -> ""
        }
    }
    removeMatch(dosageMatch)

    // 5) Quantity (total) e.g., '(30 เม็ด)', 'จำนวน 1 แผง', '1 แผง'
    var quantity = ""
    val quantityRegex1 = "\\((\\d+)\\s*(เม็ด|แผง|ขวด|กล่อง|หลอด)\\)".toRegex(RegexOption.IGNORE_CASE)
    val q1 = quantityRegex1.find(working)
    if (q1 != null) {
        quantity = q1.groups[1]?.value ?: ""
        removeMatch(q1)
    } else {
        val quantityRegex2 = "จำนวน\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE)
        val q2 = quantityRegex2.find(working)
        if (q2 != null) {
            quantity = q2.groups[1]?.value ?: ""
            removeMatch(q2)
        } else {
            val q3 = "(\\d+)\\s*(แผง|เม็ด|กล่อง|ขวด)".toRegex(RegexOption.IGNORE_CASE).find(working)
            if (q3 != null) {
                quantity = q3.groups[1]?.value ?: ""
                removeMatch(q3)
            }
        }
    }

    // 6) Frequency (วันละ X ครั้ง, สัปดาห์ละ X ครั้ง, เดือนละ X ครั้ง)
    var frequency = ""
    var timeUnit = "วัน"
    var frequencyCount = 0
    val freqPatterns = listOf(
        "วันละ\\s*(\\d+)\\s*ครั้ง",
        "สัปดาห์ละ\\s*(\\d+)\\s*ครั้ง",
        "เดือนละ\\s*(\\d+)\\s*ครั้ง",
        "(\\d+)\\s*ครั้ง\\s*ต่อ\\s*สัปดาห์",
        "(\\d+)\\s*ครั้ง\\s*ต่อ\\s*เดือน"
    )
    for (p in freqPatterns) {
        val r = p.toRegex(RegexOption.IGNORE_CASE)
        val m = r.find(working)
        if (m != null) {
            val cnt = m.groups[1]?.value?.toIntOrNull() ?: 0
            frequencyCount = cnt
            when {
                p.startsWith("วัน") -> timeUnit = "วัน"
                p.startsWith("สัปดาห์") -> timeUnit = "สัปดาห์"
                p.startsWith("เดือน") -> timeUnit = "เดือน"
                p.contains("สัปดาห์") -> timeUnit = "สัปดาห์"
                p.contains("เดือน") -> timeUnit = "เดือน"
            }
            frequency = when (timeUnit) {
                "วัน" -> "วันละ ${frequencyCount} ครั้ง"
                else -> "${timeUnit}ละ ${frequencyCount} ครั้ง"
            }
            removeMatch(m)
            break
        }
    }

    // 7) Expiry / dates
    val dateRegex = "\\b(\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4})\\b".toRegex()
    val dateMatch = dateRegex.find(working)
    val thaiMonthRegex = """(\d{1,2})\s+(ม\.?ค\.?|ก\.?พ\.?|มี\.?ค\.?|เม\.?ย\.?|พ\.?ค\.?|มิ\.?ย\.?|ก\.?ค\.?|ส\.?ค\.?|ก\.?ย\.?|ต\.?ค\.?|พ\.?ย\.?|ธ\.?ค\.? )\s*(\d{2,4})""".toRegex(RegexOption.IGNORE_CASE)
    val thaiDateMatch = thaiMonthRegex.find(working)
    val expiry = dateMatch?.value ?: thaiDateMatch?.value ?: ""
    removeMatch(dateMatch)
    removeMatch(thaiDateMatch)

    // 8) Remove phone numbers, emails and common contact/address lines
    val phoneRegex = "(?:0\\d{1,2}[- ]?\\d{3}[- ]?\\d{4}|02[- ]?\\d{3}[- ]?\\d{4}|\\+?\\d{2,4}[- ]?\\d{6,8})".toRegex()
    val emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}".toRegex()
    phoneRegex.findAll(working).forEach { m -> removeMatch(m) }
    emailRegex.findAll(working).forEach { m -> removeMatch(m) }

    // Remove common address-like tokens / labels to avoid them being selected as name
    val addressTokens = listOf("ถ.", "ถนน", "แขวง", "เขต", "ต.", "อ.", "อำเภอ", "จังหวัด", "กรุงเทพ", "เบอร์", "โทร", "TEL", "E-mail", "HN", "VN", "LINE", "Line", "โรงพยาบาล", "Hosp", "Address")
    for (tok in addressTokens) {
        val r = Regex("(?i)" + Regex.escape(tok))
        working = working.replace(r, " ")
    }

    // 9) After removals, pick the best name token: prefer proximity to dosage then filtered fallback
    fun isNoiseToken(t: String): Boolean {
        val lower = t.lowercase()
        val noiseWords = listOf("ผู้ใช้ยา", "ใช้ตามแพทย์สั่งเท่านั้น", "สรรพคุณ", "วิธีใช้", "รับประทาน", "ก่อนอาหาร", "หลังอาหาร", "เช้า", "เย็น", "กลางวัน", "วันละ", "ครั้ง", "จำนวน", "เม็ด", "แผง", "แพทย์", "line", "official", "email", "tel")
        if (phoneRegex.matches(t) || emailRegex.matches(t)) return true
        if (lower.any { it.isDigit() } && lower.length > 30) return true
        if (noiseWords.any { lower.contains(it) }) return true
        if (t.length <= 1) return true
        return false
    }

    val original = text
    var nameCandidate: String? = null

    // Simple approach: prefer the longest English-like token (allow digits and '-')
    // 1) check lines that contain explicit medicine indicators (e.g., 'ยา', 'อยา', 'drug', 'drugs')
    // 2) if none, check tokens near the dosage position
    // 3) fallback to the longest English token in the whole text
    // 4) if still none, fallback to the longest Thai token

    val medIndicators = listOf("ยา", "อยา", "drug", "drugs", "tablet", "tab", "mg", "เม็ด", "แผง")
    val englishTokenRegex = Regex("[A-Za-z0-9\\-']+")
    val thaiTokenRegex = Regex("[\\u0E00-\\u0E7F]+")

    fun isEnglishCandidate(tok: String): Boolean {
        val t = tok.trim('\'', '"')
        if (t.length < 2) return false
        if (phoneRegex.matches(t) || emailRegex.matches(t)) return false
        if (isNoiseToken(t)) return false
        // require at least 2 latin letters to avoid codes like B11
        val latinCount = Regex("[A-Za-z]").findAll(t).count()
        if (latinCount < 2) return false
        return true
    }

    // 1) scan medicine-indicator lines first
    val lines = original.split('\n')
    val medLineCandidates = mutableListOf<String>()
    for (line in lines) {
        val low = line.lowercase()
        if (medIndicators.any { low.contains(it) }) {
            englishTokenRegex.findAll(line).forEach { m ->
                val tok = m.value
                if (isEnglishCandidate(tok)) medLineCandidates.add(tok)
            }
        }
    }
    if (medLineCandidates.isNotEmpty()) {
        nameCandidate = medLineCandidates.maxByOrNull { it.length }
    }

    // 2) tokens near dosage
    if (nameCandidate == null && dosageMatch != null) {
        val dosagePos = dosageMatch.range.first
        val proximityWindow = 200
        val nearby = mutableListOf<Pair<String, Int>>()
        englishTokenRegex.findAll(original).forEach { m ->
            val tok = m.value
            val pos = m.range.first
            val dist = kotlin.math.abs(pos - dosagePos)
            if (dist <= proximityWindow && isEnglishCandidate(tok)) nearby.add(Pair(tok, dist))
        }
        if (nearby.isNotEmpty()) {
            // prefer closest then longest
            nameCandidate = nearby.sortedWith(compareBy({ it.second }, { -it.first.length })).first().first
        }
    }

    // 3) global longest English token
    if (nameCandidate == null) {
        val allEnglish = englishTokenRegex.findAll(original).map { it.value }.filter { isEnglishCandidate(it) }.toList()
        if (allEnglish.isNotEmpty()) nameCandidate = allEnglish.maxByOrNull { it.length }
    }

    // 4) fallback to longest Thai token
    if (nameCandidate == null) {
        val thaiTokens = thaiTokenRegex.findAll(original).map { it.value }.filter { !isNoiseToken(it) }.toList()
        if (thaiTokens.isNotEmpty()) nameCandidate = thaiTokens.maxByOrNull { it.length }
    }

    if (nameCandidate == null) {
        val cleaned = working.replace(Regex("[^0-9A-Za-z\\u0E00-\\u0E7F\\s\\-:/']"), " ")
        val tokens = cleaned.split(Regex("[\\n\\s,\\t\\/\\|\\(\\)]+")).map { it.trim().trim('.') }.filter { it.isNotEmpty() }

        val letterRegex = Regex("\\p{L}")
        val validCharsRegex = Regex("^[\\p{L}0-9\\-']+$")

        val candidates = tokens.filter { t ->
            val token = t.trim()
            if (token.length < 2) return@filter false
            if (phoneRegex.matches(token) || emailRegex.matches(token)) return@filter false
            if (isNoiseToken(token)) return@filter false
            if (!letterRegex.containsMatchIn(token)) return@filter false
            if (!validCharsRegex.matches(token)) return@filter false
            true
        }

        // Prefer the longest sensible token (allows letters, digits, '-' and apostrophe),
        // fallback to a more permissive selection if nothing found.
        nameCandidate = candidates.maxByOrNull { it.length }
            ?: tokens.filter { t -> !isNoiseToken(t) && !phoneRegex.matches(t) && !emailRegex.matches(t) }.maxByOrNull { it.length }
            ?: tokens.firstOrNull()
    }

    val name = nameCandidate ?: ""

    val timesList = foundTimes.toList()
    val primaryTime = timesList.firstOrNull() ?: ""

    // If frequency wasn't detected, derive a friendly frequency string from times count
    val finalFrequency = if (frequency.isNotBlank()) frequency else when (timesList.size) {
        0 -> ""
        1 -> "ทุกวัน"
        2 -> "เช้า/เย็น"
        3 -> "เช้า/กลางวัน/เย็น"
        else -> "วันละ ${timesList.size} ครั้ง"
    }

    return MedicineFormState(
        name = name,
        dosage = dosage,
        unit = unit,
        time = primaryTime,
        times = timesList,
        quantity = quantity,
        frequency = finalFrequency,
        expiryDate = expiry,
        timeUnit = timeUnit,
        frequencyCount = frequencyCount,
        amountPerDose = amountPerDose
    )
}
