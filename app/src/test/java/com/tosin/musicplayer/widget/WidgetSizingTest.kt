package com.tosin.musicplayer.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetSizingTest {

    private fun createOptions(width: Int, height: Int): Bundle {
        val bundle = mockk<Bundle>(relaxed = true)
        every { bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0) } returns width
        every { bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0) } returns height
        return bundle
    }

    @Test
    fun testDedicatedCompactProvider() {
        val result = WidgetSizing.resolveSize(null, "com.tosin.musicplayer.widget.CompactWidgetProvider")
        assertEquals(WidgetSizing.SizeMode.COMPACT, result)
    }

    @Test
    fun testDedicatedFullscreenProvider() {
        val result = WidgetSizing.resolveSize(null, "com.tosin.musicplayer.widget.FullscreenWidgetProvider")
        assertEquals(WidgetSizing.SizeMode.FULLSCREEN, result)
    }

    @Test
    fun testNullOptionsDefaultsToSmall() {
        val result = WidgetSizing.resolveSize(null, null)
        assertEquals(WidgetSizing.SizeMode.SMALL, result)
    }

    @Test
    fun testZeroDimensionsDefaultsToSmall() {
        val options = createOptions(0, 0)
        val result = WidgetSizing.resolveSize(options, null)
        assertEquals(WidgetSizing.SizeMode.SMALL, result)
    }

    @Test
    fun testFullscreenDimensions() {
        val options = createOptions(300, 300)
        val result = WidgetSizing.resolveSize(options, null)
        assertEquals(WidgetSizing.SizeMode.FULLSCREEN, result)
    }

    @Test
    fun testTinyDimensions() {
        val options = createOptions(100, 100)
        val result = WidgetSizing.resolveSize(options, null)
        assertEquals(WidgetSizing.SizeMode.TINY, result)
    }

    @Test
    fun testCompactBarDimensions() {
        val options = createOptions(250, 60)
        val result = WidgetSizing.resolveSize(options, null)
        assertEquals(WidgetSizing.SizeMode.COMPACT, result)
    }

    @Test
    fun testLargeDimensions() {
        val options = createOptions(320, 200)
        val result = WidgetSizing.resolveSize(options, null)
        assertEquals(WidgetSizing.SizeMode.LARGE, result)
    }

    @Test
    fun testMediumDimensions() {
        val options = createOptions(240, 140)
        val result = WidgetSizing.resolveSize(options, null)
        assertEquals(WidgetSizing.SizeMode.MEDIUM, result)
    }
}
