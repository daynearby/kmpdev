package com.example.kmpdev.app

import com.example.rt.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}