package com.garsontech.rubiesafrica

fun Int.toBoolean() = if (this == 1) true else false

fun Boolean.toInt() = if (this == true) 1 else 0

fun Boolean.toIntReversed() = if (this == true) 0 else 1