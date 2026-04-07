package org.owlcode.edutrack

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform