package com.vladrip.ifchat.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.vladrip.ifchat.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.util.Locale

object FormatHelper {

    private fun format(dateTime: LocalDateTime, pattern: String): String {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern))
    }

    @Composable
    fun lastOnline(dateTime: LocalDateTime?) = lastOnline(dateTime, LocalContext.current)

    fun lastOnline(dateTime: LocalDateTime?, context: Context): String {
        if (dateTime == null) return context.getString(R.string.last_online_recently)
        val now = LocalDateTime.now()
        val weekOfMonth = WeekFields.of(Locale.getDefault()).weekOfMonth()
        return when {
            dateTime.year < now.year -> {
                val yearDiff = now.year - dateTime.year
                return if (yearDiff == 1)
                    context.getString(R.string.last_online, context.getString(R.string.a_year_ago))
                else context.getString(R.string.last_online, "$yearDiff ${context.getString(R.string.years_ago)}")
            }

            dateTime.month.value != now.month.value ||
                    dateTime.get(weekOfMonth) != dateTime.get(weekOfMonth) ->
                context.getString(R.string.last_online_at, format(dateTime, "MMM dd, hh:mm"))

            dateTime.dayOfMonth != now.dayOfMonth ->
                context.getString(R.string.last_online_at, format(dateTime, "EEE, hh:mm"))

            dateTime.hour != now.hour ->
                context.getString(R.string.last_online_at, format(dateTime, "hh:mm"))

            dateTime.minute != now.minute ->
                context.getString(
                    R.string.last_online,
                    "${dateTime.minute} ${context.getString(R.string.ago)}"
                )

            else -> return "online"
        }
    }

    fun formatLastSent(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val weekOfMonth = WeekFields.of(Locale.getDefault()).weekOfMonth()
        val formatter: DateTimeFormatter = when {
            dateTime.year != now.year ->
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

            dateTime.month.value != now.month.value ||
                    dateTime.get(weekOfMonth) != now.get(weekOfMonth) ->
                DateTimeFormatter.ofPattern("MMM dd")

            dateTime.dayOfYear != now.dayOfYear ->
                DateTimeFormatter.ofPattern("EEE")

            else -> DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.UK)
        }
        return dateTime.format(formatter)
    }

    fun formatMessageSentAt(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    }

    fun formatDateSeparator(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val formatter: DateTimeFormatter = when {
            dateTime.year != now.year ->
                DateTimeFormatter.ofPattern("LLLL d, yyyy")

            else -> DateTimeFormatter.ofPattern("LLLL d")
        }
        return dateTime.format(formatter).replaceFirstChar { c -> c.uppercase() }
    }
}