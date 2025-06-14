package app.kreate.android.themed.common.screens.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kreate.android.R
import app.kreate.android.Settings
import app.kreate.android.themed.common.component.settings.SettingComponents
import it.fast4x.rimusic.enums.AlbumSwipeAction
import it.fast4x.rimusic.enums.PlaylistSwipeAction
import it.fast4x.rimusic.enums.QueueSwipeAction
import it.fast4x.rimusic.ui.screens.settings.SwitchSettingEntry

@Composable
fun SwipeActionSettings() {
    var isSwipeToActionEnabled by Settings.ENABLE_SWIPE_ACTION
    SwitchSettingEntry(
        title = stringResource(R.string.swipe_to_action),
        text = stringResource(R.string.activate_the_action_menu_by_swiping_the_song_left_or_right),
        isChecked = isSwipeToActionEnabled,
        onCheckedChange = { isSwipeToActionEnabled = it }
    )

    AnimatedVisibility(visible = isSwipeToActionEnabled) {
        Column(
            modifier = Modifier.padding(start = 25.dp)
        ) {
            SettingComponents.EnumEntry(
                Settings.QUEUE_SWIPE_LEFT_ACTION,
                R.string.queue_and_local_playlists_left_swipe
            )
            SettingComponents.EnumEntry(
                Settings.QUEUE_SWIPE_RIGHT_ACTION,
                R.string.queue_and_local_playlists_right_swipe
            )
            SettingComponents.EnumEntry(
                Settings.PLAYLIST_SWIPE_LEFT_ACTION,
                R.string.playlist_left_swipe
            )
            SettingComponents.EnumEntry(
                Settings.PLAYLIST_SWIPE_RIGHT_ACTION,
                R.string.playlist_right_swipe
            )
            SettingComponents.EnumEntry(
                Settings.ALBUM_SWIPE_LEFT_ACTION,
                R.string.album_left_swipe
            )
            SettingComponents.EnumEntry(
                Settings.ALBUM_SWIPE_RIGHT_ACTION,
                R.string.album_right_swipe
            )
        }
    }
}