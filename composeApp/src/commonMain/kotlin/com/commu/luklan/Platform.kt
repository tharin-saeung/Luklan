package com.commu.luklan

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform