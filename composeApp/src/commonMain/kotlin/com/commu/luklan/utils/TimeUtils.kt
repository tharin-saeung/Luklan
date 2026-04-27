package com.commu.luklan.utils

fun formatTimeForDisplay(timeStr: String): String {
    // remove 0 from hour that have 1 digit (1-9:xx) except 0 (00:xx)
    if (timeStr.startsWith("0") && timeStr.length >= 2 && timeStr[1] != '0') {
        return timeStr.substring(1)
    }
    return timeStr
}

fun formatTimeListForDisplay(times: List<String>): String {
    return times.joinToString(", ") { formatTimeForDisplay(it) }
}
