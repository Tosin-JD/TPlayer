package com.tosin.musicplayer.ui.state

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import com.tosin.musicplayer.data.models.Song

enum class StorageScope(val label: String) {
    Internal("Internal"),
    SdCard("SD Card"),
    Both("Both")
}

fun Song.isInternalStorage(): Boolean {
    // Primary check: use volumeName if available (reliable on Android 10+)
    if (volumeName != null) {
        return volumeName == "external_primary" || volumeName == "internal"
    }
    // Fallback: path-based detection for older Android versions
    val path = folderPath?.lowercase().orEmpty()
    return path.isBlank() ||
        !path.contains("/storage/") ||
        path.contains("/storage/emulated") ||
        path.contains("/storage/self")
}

fun Song.isSdCardStorage(): Boolean {
    // Primary check: use volumeName if available
    if (volumeName != null) {
        return volumeName != "external_primary" && volumeName != "internal"
    }
    // Fallback: path-based detection
    val path = folderPath?.lowercase().orEmpty()
    return path.contains("/storage/") &&
        !path.contains("/storage/emulated") &&
        !path.contains("/storage/self") &&
        path.isNotBlank()
}

fun Song.matchesStorageScope(scope: StorageScope): Boolean {
    return when (scope) {
        StorageScope.Internal -> isInternalStorage()
        StorageScope.SdCard -> isSdCardStorage()
        StorageScope.Both -> true
    }
}

fun Context.hasRemovableStorage(): Boolean {
    val storageManager = getSystemService(android.os.storage.StorageManager::class.java) ?: return false
    return storageManager.storageVolumes.any { it.isRemovable }
}

