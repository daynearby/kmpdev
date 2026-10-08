package com.example.rt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform