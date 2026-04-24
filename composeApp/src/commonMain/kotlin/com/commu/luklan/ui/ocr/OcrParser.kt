package com.commu.luklan.ui.ocr

import com.commu.luklan.ui.medicine.MedicineFormState

fun parseOcrToForm(text: String): MedicineFormState {
    if (text.isBlank()) return MedicineFormState()

    var working = text.replace("\r", "\n")

    fun removeMatch(m: MatchResult?) {
        if (m == null) return
        working = working.replaceFirst(m.value, " ")
    }

    // 1) HH:MM times
    val timeRegex = "\\b([01]?\\d|2[0-3]):[0-5]\\d\\b".toRegex()
    val foundTimes = mutableListOf<String>()
    timeRegex.findAll(working).forEach { mr ->
        val t = mr.value.padStart(5, '0')
        if (!foundTimes.contains(t)) foundTimes.add(t)
        removeMatch(mr)
    }

    // 2) Keyword times
    val keywordMap = mapOf(
        "ตอนเช้า" to "08:00", "เช้า" to "08:00",
        "กลางวัน" to "12:00", "เที่ยง" to "12:00",
        "เย็น" to "18:00", "ก่อนนอน" to "21:00",
        "ก่อนอาหาร" to "07:30", "หลังอาหาร" to "13:00"
    )
    for ((k, v) in keywordMap) {
        if (working.contains(k, ignoreCase = true) && !foundTimes.contains(v)) {
            foundTimes.add(v)
            working = working.replaceFirst(Regex("(?i)" + Regex.escape(k)), " ")
        }
    }

    // 4) Dosage
    val dosageRegex = "(\\d+(?:[\\/.]\\d+)?(?:\\s*\\/\\s*\\d+)?)(?:\\s*)(mg|g|ml|เม็ด|แผง|แท็บเล็ต|tab|หลอด|ขวด)?".toRegex(RegexOption.IGNORE_CASE)
    val dosageMatch = dosageRegex.find(working)
    val dosage = dosageMatch?.groups?.get(1)?.value ?: ""
    val unit = dosageMatch?.groups?.get(2)?.value?.lowercase() ?: run {
        when {
            working.contains("mg", ignoreCase = true) -> "mg"
            working.contains("เม็ด") -> "เม็ด"
            working.contains("ml", ignoreCase = true) -> "ml"
            working.contains("หลอด") -> "หลอด"
            else -> ""
        }
    }
    removeMatch(dosageMatch)

    // Detect Category
    val category = when {
        working.contains("น้ำ", ignoreCase = true) || working.contains("ml", ignoreCase = true) -> "น้ำ"
        working.contains("ครีม", ignoreCase = true) || working.contains("ทา") || working.contains("cream", ignoreCase = true) -> "ครีม"
        working.contains("เหน็บ", ignoreCase = true) || working.contains("suppository", ignoreCase = true) -> "เหน็บ"
        working.contains("ฉีด", ignoreCase = true) || working.contains("inject", ignoreCase = true) -> "ฉีด"
        working.contains("เม็ด") || working.contains("tablet", ignoreCase = true) -> "เม็ด"
        working.contains("แคปซูล") || working.contains("capsule", ignoreCase = true) -> "แคปซูล"
        else -> ""
    }

    // 5) Quantity (currentAmount)
    var quantity = ""
    val quantityRegex1 = "\\((\\d+)\\s*(เม็ด|แผง|ขวด|กล่อง|หลอด|แท่ง|ชิ้น)\\)".toRegex(RegexOption.IGNORE_CASE)
    val q1 = quantityRegex1.find(working)
    if (q1 != null) {
        quantity = q1.groups[1]?.value ?: ""
        removeMatch(q1)
    } else {
        val q2 = "จำนวน\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE).find(working)
        if (q2 != null) {
            quantity = q2.groups[1]?.value ?: ""
            removeMatch(q2)
        } else {
            val q3 = "(\\d+)\\s*(แผง|เม็ด|กล่อง|ขวด|หลอด)".toRegex(RegexOption.IGNORE_CASE).find(working)
            if (q3 != null) {
                quantity = q3.groups[1]?.value ?: ""
                removeMatch(q3)
            }
        }
    }

    // 9) Name picking logic (Simplified for space)
    val phoneRegex = "(?:0\\d{1,2}[- ]?\\d{3}[- ]?\\d{4}|02[- ]?\\d{3}[- ]?\\d{4}|\\+?\\d{2,4}[- ]?\\d{6,8})".toRegex()
    val emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}".toRegex()
    
    val addressTokens = listOf("ถ.", "ถนน", "แขวง", "เขต", "ต.", "อ.", "อำเภอ", "จังหวัด", "กรุงเทพ", "เบอร์", "โทร", "TEL", "โรงพยาบาล", "Hosp")
    for (tok in addressTokens) working = working.replace(Regex("(?i)" + Regex.escape(tok)), " ")

    fun isNoise(t: String) = t.lowercase() in listOf("ผู้ใช้ยา", "สรรพคุณ", "วิธีใช้", "รับประทาน", "ก่อนอาหาร", "หลังอาหาร", "เช้า", "เย็น", "กลางวัน", "เม็ด") || t.length <= 1

    val englishTokenRegex = Regex("[A-Za-z0-9\\-']+")
    val nameCandidate = englishTokenRegex.findAll(working).map { it.value }.filter { it.length > 2 && !isNoise(it) }.maxByOrNull { it.length } ?: ""

    return MedicineFormState(
        name = nameCandidate,
        dosage = dosage,
        unit = unit,
        category = category,
        currentAmount = quantity,
        times = foundTimes.sorted()
    )
}
