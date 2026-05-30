package com.tosin.musicplayer.ui.state

import com.tosin.musicplayer.data.models.Song

enum class StorageScope(val label: String) {
    Internal("Internal"),
    SdCard("SD Card"),
    Both("Both")
}

fun Song.isInternalStorage(): Boolean {
    val path = folderPath?.lowercase().orEmpty()
    return path.contains("/storage/emulated/0") || path.contains("/storage/self/primary")
}

fun Song.isSdCardStorage(): Boolean {
    val path = folderPath?.lowercase().orEmpty()
    return path.contains("/storage/") && !isInternalStorage() && !path.contains("/emulated/0")
}

fun Song.matchesStorageScope(scope: StorageScope): Boolean {
    return when (scope) {
        StorageScope.Internal -> isInternalStorage()
        StorageScope.SdCard -> isSdCardStorage()
        StorageScope.Both -> true
    }
}

