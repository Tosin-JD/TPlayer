package com.tosin.musicplayer.ui.state

import android.content.Context
import com.tosin.musicplayer.data.models.Song

enum class StorageScope(val label: String) {
    Internal("Internal"),
    SdCard("SD Card"),
    Both("Both")
}

fun Song.isInternalStorage(): Boolean {
    val path = folderPath?.lowercase().orEmpty()
    return path.isBlank() ||
        !path.contains("/storage/") ||
        path.contains("/storage/emulated/0") ||
        path.contains("/storage/self/primary")
}

fun Song.isSdCardStorage(): Boolean {
    val path = folderPath?.lowercase().orEmpty()
    return path.contains("/storage/") &&
        !path.contains("/storage/emulated/0") &&
        !path.contains("/storage/self/primary") &&
        !path.isBlank()
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
