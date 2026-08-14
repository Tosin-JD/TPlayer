package com.tosin.musicplayer.ui.state

import java.util.Calendar

enum class StatsRange(val label: String) {
    Today("Today"),
    ThisWeek("This Week"),
    ThisMonth("This Month"),
    ThisYear("This Year"),
    AllTime("All Time")
}

enum class SortBy(val label: String) {
    PLAY_COUNT("By Plays"),
    DURATION("By Minutes")
}

fun startTimeForRange(range: StatsRange): Long = when (range) {
    StatsRange.Today -> getStartOfToday()
    StatsRange.ThisWeek -> getStartOfWeek()
    StatsRange.ThisMonth -> getStartOfMonth()
    StatsRange.ThisYear -> getStartOfYear()
    StatsRange.AllTime -> 0L
}

private fun getStartOfToday(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun getStartOfWeek(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun getStartOfMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun getStartOfYear(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
