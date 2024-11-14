package com.vladrip.ifchat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform