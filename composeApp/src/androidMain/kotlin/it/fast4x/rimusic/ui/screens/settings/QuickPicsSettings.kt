package it.fast4x.rimusic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import app.kreate.android.R
import app.kreate.android.Settings
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.styling.Dimensions
import kotlinx.coroutines.Dispatchers
import me.knighthat.utils.Toaster

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun  QuickPicsSettings() {
    var playEventType by Settings.QUICK_PICKS_TYPE
    var showTips by Settings.QUICK_PICKS_SHOW_TIPS
    var showRelatedAlbums by Settings.QUICK_PICKS_SHOW_RELATED_ALBUMS
    var showSimilarArtists by Settings.QUICK_PICKS_SHOW_RELATED_ARTISTS
    var showNewAlbumsArtists by Settings.QUICK_PICKS_SHOW_NEW_ALBUMS_ARTISTS
    var showNewAlbums by Settings.QUICK_PICKS_SHOW_NEW_ALBUMS
    var showPlaylistMightLike by Settings.QUICK_PICKS_SHOW_MIGHT_LIKE_PLAYLISTS
    var showMoodsAndGenres by Settings.QUICK_PICKS_SHOW_MOODS_AND_GENRES
    var showMonthlyPlaylistInQuickPicks by Settings.QUICK_PICKS_SHOW_MONTHLY_PLAYLISTS
    var showCharts by Settings.QUICK_PICKS_SHOW_CHARTS
    var enableQuickPicksPage by Settings.QUICK_PICKS_PAGE
    var clearEvents by remember { mutableStateOf(false) }
    if (clearEvents) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_all_playback_events),
            onDismiss = { clearEvents = false },
            onConfirm = {
                Database.asyncTransaction {
                    eventTable.deleteAll()
                    Toaster.done()
                }
            }
        )
    }

    //var isEnabledDiscoveryLangCode by rememberPreference(isEnabledDiscoveryLangCodeKey,   true)

    //var showActionsBar by rememberPreference(showActionsBarKey, true)
    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if( NavigationBarPosition.Right.isCurrent() )
                    Dimensions.contentWidthRightBar
                else
                    1f
            )
            .verticalScroll(rememberScrollState())
            /*
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )

             */
    ) {
        HeaderWithIcon(
            title = if (!isYouTubeLoggedIn()) stringResource(R.string.quick_picks) else stringResource(R.string.home),
            iconId = if (!isYouTubeLoggedIn()) R.drawable.sparkles else R.drawable.ytmusic,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        SwitchSettingEntry(
            title = stringResource(R.string.enable_quick_picks_page),
            text = "",
            isChecked = enableQuickPicksPage,
            onCheckedChange = {
                enableQuickPicksPage = it
            }
        )

        //SettingsGroupSpacer()
        /*
        SwitchSettingEntry(
            title = stringResource(R.string.show_actions_bar),
            text = "",
            isChecked = showActionsBar,
            onCheckedChange = {
                showActionsBar = it
            }
        )
         */

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.tips)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.tips),
            isChecked = showTips,
            onCheckedChange = {
                showTips = it
            }
        )

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.charts)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.charts),
            isChecked = showCharts,
            onCheckedChange = {
                showCharts = it
            }
        )

        AnimatedVisibility(
            visible = showTips,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(100)),
        ) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.tips),
                selectedValue = playEventType,
                onValueSelected = { playEventType = it },
                valueText = { it.text }
            )
        }

        //SettingsGroupSpacer()

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.related_albums)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.related_albums),
            isChecked = showRelatedAlbums,
            onCheckedChange = {
                showRelatedAlbums = it
            }
        )

        //SettingsGroupSpacer()

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.similar_artists)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.similar_artists),
            isChecked = showSimilarArtists,
            onCheckedChange = {
                showSimilarArtists = it
            }
        )


        //SettingsGroupSpacer()

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.new_albums_of_your_artists)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.new_albums_of_your_artists),
            isChecked = showNewAlbumsArtists,
            onCheckedChange = {
                showNewAlbumsArtists = it
            }
        )

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.new_albums)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.new_albums),
            isChecked = showNewAlbums,
            onCheckedChange = {
                showNewAlbums = it
            }
        )

        //SettingsGroupSpacer()

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.playlists_you_might_like)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.playlists_you_might_like),
            isChecked = showPlaylistMightLike,
            onCheckedChange = {
                showPlaylistMightLike = it
            }
        )

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.moods_and_genres)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.moods_and_genres),
            isChecked = showMoodsAndGenres,
            onCheckedChange = {
                showMoodsAndGenres = it
            }
        )

        SwitchSettingEntry(
            title = "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}",
            text = stringResource(R.string.disable_if_you_do_not_want_to_see) + " " +stringResource(R.string.monthly_playlists),
            isChecked = showMonthlyPlaylistInQuickPicks,
            onCheckedChange = {
                showMonthlyPlaylistInQuickPicks = it
            }
        )

        /*
        SwitchSettingEntry(
            title = stringResource(R.string.enable_language_in_discovery),
            text = stringResource(R.string.if_possible_allows_discovery_content_language),
            isChecked = isEnabledDiscoveryLangCode,
            onCheckedChange = {
                isEnabledDiscoveryLangCode = it
            }
        )
        ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
         */

        val eventsCount by remember {
            Database.eventTable
                    .countAll()
        }.collectAsState( 0L, Dispatchers.IO )

        SettingsEntry(
            title = stringResource(R.string.reset_quick_picks),
            text = if (eventsCount > 0) {
                stringResource(R.string.delete_playback_events, eventsCount)
            } else {
                stringResource(R.string.quick_picks_are_cleared)
            },
            isEnabled = eventsCount > 0,
            onClick = { clearEvents = true }
        )
        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }
}
