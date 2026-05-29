package com.tosin.musicplayer.ui.extensions

import com.tosin.musicplayer.R

fun String?.orDefaultAlbumArt(): Any = if (isNullOrEmpty()) R.drawable.album_art else this
