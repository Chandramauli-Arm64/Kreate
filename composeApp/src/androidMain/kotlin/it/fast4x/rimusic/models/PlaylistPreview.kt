package it.fast4x.rimusic.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class PlaylistPreview(
    @Embedded val playlist: Playlist,
    val songCount: Int,
    val thumbnailUrl: String? = null
)

