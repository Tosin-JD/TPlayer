package com.tosin.musicplayer.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Centralized icon mapping using Google Material Icons Rounded.
 */
object AppIcons {
    // ── Navigation & Playback ──
    val Play: ImageVector get() = Icons.Rounded.PlayArrow
    val Pause: ImageVector get() = Icons.Rounded.Pause
    val Stop: ImageVector get() = Icons.Rounded.Stop
    val SkipPrevious: ImageVector get() = Icons.Rounded.SkipPrevious
    val SkipNext: ImageVector get() = Icons.Rounded.SkipNext

    // ── Shuffle & Repeat ──
    val Shuffle: ImageVector get() = Icons.Rounded.Shuffle
    val ShuffleOn: ImageVector get() = Icons.Rounded.ShuffleOn
    val Repeat: ImageVector get() = Icons.Rounded.Repeat
    val RepeatOne: ImageVector get() = Icons.Rounded.RepeatOne
    val RepeatOneOn: ImageVector get() = Icons.Rounded.RepeatOne
    val LooksOne: ImageVector get() = Icons.Rounded.LooksOne
    val ParallelRightArrows: ImageVector
        get() = ImageVector.Builder(
            name = "ParallelRightArrows",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Top Arrow stem
                moveTo(4.5f, 7.5f)
                lineTo(18.5f, 7.5f)
                // Top Arrow head
                moveTo(14.5f, 4.5f)
                lineTo(18.5f, 7.5f)
                lineTo(14.5f, 10.5f)

                // Bottom Arrow stem
                moveTo(4.5f, 16.5f)
                lineTo(18.5f, 16.5f)
                // Bottom Arrow head
                moveTo(14.5f, 13.5f)
                lineTo(18.5f, 16.5f)
                lineTo(14.5f, 19.5f)
            }
        }.build()

    // ── Music & Media ──
    val MusicNote: ImageVector get() = Icons.Rounded.MusicNote
    val Album: ImageVector get() = Icons.Rounded.Album
    val Lyrics: ImageVector get() = Icons.Rounded.Speaker
    val Queue: ImageVector get() = Icons.Rounded.QueueMusic
    val Equalizer: ImageVector get() = Icons.Rounded.Equalizer
    val LibraryMusic: ImageVector get() = Icons.Rounded.LibraryMusic
    val GraphicEq: ImageVector get() = Icons.Rounded.GraphicEq
    val Waves: ImageVector get() = Icons.Rounded.Waves

    // ── Library & Organization ──
    val Folder: ImageVector get() = Icons.Rounded.Folder
    val FolderOpen: ImageVector get() = Icons.Rounded.FolderOpen
    val FolderOff: ImageVector get() = Icons.Rounded.FolderOff
    val Person: ImageVector get() = Icons.Rounded.Person
    val Label: ImageVector get() = Icons.Rounded.Label

    // ── Actions ──
    val Search: ImageVector get() = Icons.Rounded.Search
    val SearchOff: ImageVector get() = Icons.Rounded.SearchOff
    val Add: ImageVector get() = Icons.Rounded.Add
    val Remove: ImageVector get() = Icons.Rounded.Remove
    val Close: ImageVector get() = Icons.Rounded.Close
    val Check: ImageVector get() = Icons.Rounded.Check
    val Edit: ImageVector get() = Icons.Rounded.Edit
    val Delete: ImageVector get() = Icons.Rounded.Delete
    val DeleteOutline: ImageVector get() = Icons.Rounded.DeleteOutline
    val MoreVert: ImageVector get() = Icons.Rounded.MoreVert
    val DragHandle: ImageVector get() = Icons.Rounded.DragHandle
    val Sort: ImageVector get() = Icons.Rounded.Sort
    val FilterList: ImageVector get() = Icons.Rounded.FilterList

    // ── Arrows & Navigation ──
    val ArrowUp: ImageVector get() = Icons.Rounded.ArrowUpward
    val ArrowDown: ImageVector get() = Icons.Rounded.ArrowDownward
    val ArrowBack: ImageVector get() = Icons.AutoMirrored.Rounded.ArrowBack
    val ArrowForward: ImageVector get() = Icons.AutoMirrored.Rounded.ArrowForward
    val KeyboardArrowDown: ImageVector get() = Icons.Rounded.KeyboardArrowDown
    val KeyboardDoubleArrowLeft: ImageVector get() = Icons.Rounded.KeyboardDoubleArrowLeft
    val KeyboardDoubleArrowRight: ImageVector get() = Icons.Rounded.KeyboardDoubleArrowRight
    val RestartAlt: ImageVector get() = Icons.Rounded.RestartAlt
    val Restore: ImageVector get() = Icons.Rounded.Restore

    // ── Status & Feedback ──
    val Info: ImageVector get() = Icons.Rounded.Info
    val Warning: ImageVector get() = Icons.Rounded.Warning
    val WarningAmber: ImageVector get() = Icons.Rounded.WarningAmber

    // ── Settings & Configuration ──
    val Settings: ImageVector get() = Icons.Rounded.Settings
    val Palette: ImageVector get() = Icons.Rounded.Palette
    val Notifications: ImageVector get() = Icons.Rounded.Notifications
    val Sync: ImageVector get() = Icons.Rounded.Sync
    val Schedule: ImageVector get() = Icons.Rounded.Schedule
    val Storage: ImageVector get() = Icons.Rounded.Storage
    val History: ImageVector get() = Icons.Rounded.History
    val Timer: ImageVector get() = Icons.Rounded.Timer
    val Speed: ImageVector get() = Icons.Rounded.Speed
    val Tune: ImageVector get() = Icons.Rounded.Tune
    val ViewList: ImageVector get() = Icons.Rounded.ViewList
    val VisibilityOff: ImageVector get() = Icons.Rounded.VisibilityOff

    // ── Appearance ──
    val DarkMode: ImageVector get() = Icons.Rounded.DarkMode
    val AutoAwesome: ImageVector get() = Icons.Rounded.AutoAwesome
    val TextFields: ImageVector get() = Icons.Rounded.TextFields
    val FormatSize: ImageVector get() = Icons.Rounded.FormatSize

    // ── Charts & Stats ──
    val BarChart: ImageVector get() = Icons.Rounded.BarChart

    // ── Playback Controls ──
    val PlayCircle: ImageVector get() = Icons.Rounded.PlayCircle
    val PauseCircleOutline: ImageVector get() = Icons.Rounded.PauseCircleOutline
    val RemoveCircleOutline: ImageVector get() = Icons.Rounded.RemoveCircleOutline
    val Timelapse: ImageVector get() = Icons.Rounded.Timelapse

    // ── Favorites ──
    val Favorite: ImageVector get() = Icons.Rounded.Favorite
    val FavoriteBorder: ImageVector get() = Icons.Rounded.FavoriteBorder

    // ── Misc ──
    val PlaylistPlay: ImageVector get() = Icons.Rounded.PlaylistPlay
    val VolumeOff: ImageVector get() = Icons.AutoMirrored.Rounded.VolumeOff
}
