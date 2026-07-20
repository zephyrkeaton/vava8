package com.vava8.app.util

import java.util.concurrent.TimeUnit

object TimeUtils {
    fun relative(tsSeconds: Long): String {
        if (tsSeconds <= 0) return ""
        val now = System.currentTimeMillis()
        val then = tsSeconds * 1000
        val diff = (now - then).coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            hours < 24 -> "${hours}小时前"
            days < 30 -> "${days}天前"
            else -> {
                val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
                sdf.format(java.util.Date(then))
            }
        }
    }

    fun formatCount(n: Int): String = when {
        n >= 10000 -> String.format("%.1fw", n / 10000f)
        n >= 1000 -> String.format("%.1fk", n / 1000f)
        else -> n.toString()
    }
}
