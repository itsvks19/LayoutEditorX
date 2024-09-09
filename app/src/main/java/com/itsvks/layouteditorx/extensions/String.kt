package com.itsvks.layouteditorx.extensions

import java.io.File

fun String.isValidHex() = matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))

fun String.toFile() = File(this)