package com.example.tasko.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val dateTimeFmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

fun formatDate(millis: Long): String = dateFmt.format(Date(millis))
fun formatDateTime(millis: Long): String = dateTimeFmt.format(Date(millis))
