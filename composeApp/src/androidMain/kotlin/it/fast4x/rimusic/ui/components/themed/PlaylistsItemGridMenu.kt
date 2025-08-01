package it.fast4x.rimusic.ui.components.themed

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import app.kreate.android.R
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.items.PlaylistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.isNetworkConnected
import it.fast4x.rimusic.utils.semiBold
import kotlinx.coroutines.Dispatchers

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@UnstableApi
@ExperimentalAnimationApi
@Composable
fun PlaylistsItemGridMenu(
    navController: NavController,
    onDismiss: () -> Unit,
    onSelectUnselect: (() -> Unit)? = null,
    onSelect: (() -> Unit)? = null,
    onUncheck: (() -> Unit)? = null,
    playlist: PlaylistPreview? = null,
    modifier: Modifier = Modifier,
    onPlayNext: (() -> Unit)? = null,
    onDeleteSongsNotInLibrary: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onImportOnlinePlaylist: (() -> Unit)? = null,
    onAddToPreferites: (() -> Unit)? = null,
    showonAddToPreferitesYoutube: Boolean = false,
    onAddToPreferitesYoutube: (() -> Unit)? = null,
    onAddToPlaylist: ((PlaylistPreview) -> Unit)? = null,
    showOnSyncronize: Boolean = false,
    showLinkUnlink: Boolean = false,
    onSyncronize: (() -> Unit)? = null,
    onRenumberPositions: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onRename: (() -> Unit)? = null,
    showonListenToYT: Boolean = false,
    onListenToYT: (() -> Unit)? = null,
    onExport: (() -> Unit)? = null,
    onImport: (() -> Unit)? = null,
    onImportFavorites: (() -> Unit)? = null,
    onEditThumbnail: (() -> Unit)? = null,
    onResetThumbnail: (() -> Unit)? = null,
    onGoToPlaylist: ((Long) -> Unit)? = null,
    onLinkUnlink: (() -> Unit)? = null,
    disableScrollingText: Boolean
    ) {
    var isViewingPlaylists by remember {
        mutableStateOf(false)
    }

    var height by remember {
        mutableStateOf(0.dp)
    }

    val binder = LocalPlayerServiceBinder.current
    val thumbnailSizeDp = Dimensions.thumbnails.song + 20.dp
    val thumbnailSizePx = thumbnailSizeDp.px
    val thumbnailArtistSizeDp = Dimensions.thumbnails.song + 10.dp
    val thumbnailArtistSizePx = thumbnailArtistSizeDp.px


    AnimatedContent(
        targetState = isViewingPlaylists,
        transitionSpec = {
            val animationSpec = tween<IntOffset>(400)
            val slideDirection = if (targetState) AnimatedContentTransitionScope.SlideDirection.Left
            else AnimatedContentTransitionScope.SlideDirection.Right

            slideIntoContainer(slideDirection, animationSpec) togetherWith
                    slideOutOfContainer(slideDirection, animationSpec)
        }, label = ""
    ) { currentIsViewingPlaylists ->
        if (currentIsViewingPlaylists) {
            val context = LocalContext.current
            val playlistPreviews by remember {
                Database.playlistTable.sortPreviewsByName()
            }.collectAsState( emptyList(), Dispatchers.IO )

            val pinnedPlaylists = playlistPreviews.filter {
                it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                        && if (isNetworkConnected(context)) !(it.playlist.isYoutubePlaylist && !it.playlist.isEditable) else !it.playlist.isYoutubePlaylist
            }

            val youtubePlaylists = playlistPreviews.filter { it.playlist.isEditable && it.playlist.isYoutubePlaylist && !it.playlist.name.startsWith(PINNED_PREFIX) }

            val unpinnedPlaylists = playlistPreviews.filter {
                !it.playlist.name.startsWith(PINNED_PREFIX, 0, true) &&
                        !it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true) &&
                        !it.playlist.isYoutubePlaylist
            }

            var isCreatingNewPlaylist by rememberSaveable {
                mutableStateOf(false)
            }

            if (isCreatingNewPlaylist && onAddToPlaylist != null) {
                InputTextDialog(
                    onDismiss = { isCreatingNewPlaylist = false },
                    title = stringResource(R.string.enter_the_playlist_name),
                    value = "",
                    placeholder = stringResource(R.string.enter_the_playlist_name),
                    setValue = { text ->
                        onDismiss()
                        Database.asyncTransaction {
                            val pId = playlistTable.insert( Playlist(name = text) )
                            onAddToPlaylist(
                                PlaylistPreview(
                                    Playlist(
                                        id = pId,
                                        name = text
                                    ),
                                    0
                                )
                            )
                        }
                    }
                )
            }

            BackHandler {
                isViewingPlaylists = false
            }

            Menu(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { isViewingPlaylists = false },
                        icon = R.drawable.chevron_back,
                        color = colorPalette().textSecondary,
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .size(20.dp)
                    )

                    if (onAddToPlaylist != null) {
                        SecondaryTextButton(
                            text = stringResource(R.string.new_playlist),
                            onClick = { isCreatingNewPlaylist = true },
                            alternative = true
                        )
                    }
                }

                if (pinnedPlaylists.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.pinned_playlists),
                        style = typography().m.semiBold,
                        modifier = modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        pinnedPlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = R.drawable.add_in_playlist,
                                text = cleanPrefix(playlistPreview.playlist.name),
                                secondaryText = "${playlistPreview.songCount} " + stringResource(
                                    R.string.songs
                                ),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(
                                        PlaylistPreview(
                                            playlistPreview.playlist,
                                            playlistPreview.songCount
                                        )
                                    )
                                },
                                trailingContent = {
                                    if (playlistPreview.playlist.isYoutubePlaylist) {
                                        Image(
                                            painter = painterResource(R.drawable.ytmusic),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(
                                                Color.Red.copy(0.75f).compositeOver(Color.White)
                                            ),
                                            modifier = Modifier
                                                .size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        icon = R.drawable.open,
                                        color = colorPalette().text,
                                        onClick = {
                                            if (onGoToPlaylist != null) {
                                                onGoToPlaylist(playlistPreview.playlist.id)
                                                onDismiss()
                                            }
                                            NavRoutes.localPlaylist.navigateHere( navController, playlistPreview.playlist.id )
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                if (youtubePlaylists.isNotEmpty() && isNetworkConnected(context)) {
                    BasicText(
                        text = stringResource(R.string.ytm_playlists),
                        style = typography().m.semiBold,
                        modifier = Modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        youtubePlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = R.drawable.add_in_playlist,
                                text = cleanPrefix(playlistPreview.playlist.name),
                                secondaryText = "${playlistPreview.songCount} " + stringResource(R.string.songs),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(
                                        PlaylistPreview(
                                            playlistPreview.playlist,
                                            playlistPreview.songCount
                                        )
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        icon = R.drawable.open,
                                        color = colorPalette().text,
                                        onClick = {
                                            if (onGoToPlaylist != null) {
                                                onGoToPlaylist(playlistPreview.playlist.id)
                                                onDismiss()
                                            }
                                            NavRoutes.localPlaylist.navigateHere( navController, playlistPreview.playlist.id )
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                if (unpinnedPlaylists.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.playlists),
                        style = typography().m.semiBold,
                        modifier = modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        unpinnedPlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = R.drawable.add_in_playlist,
                                text = cleanPrefix(playlistPreview.playlist.name),
                                secondaryText = "${playlistPreview.songCount} " + stringResource(
                                    R.string.songs
                                ),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(
                                        PlaylistPreview(
                                            playlistPreview.playlist,
                                            playlistPreview.songCount
                                        )
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        icon = R.drawable.open,
                                        color = colorPalette().text,
                                        onClick = {
                                            if (onGoToPlaylist != null) {
                                                onGoToPlaylist(playlistPreview.playlist.id)
                                                onDismiss()
                                            }
                                            NavRoutes.localPlaylist.navigateHere( navController, playlistPreview.playlist.id )
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } else {
            val selectText = "${stringResource(R.string.item_select)}/${stringResource(R.string.item_deselect)}"
            val colorPalette = colorPalette()

            GridMenu(
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp + WindowInsets.systemBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
                topContent = {
                    if (playlist != null) {
                        PlaylistItem(
                            playlist = playlist,
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
                            modifier = Modifier.height(90.dp),
                            disableScrollingText = disableScrollingText,
                            isYoutubePlaylist = playlist.playlist.isYoutubePlaylist,
                            isEditable = playlist.playlist.isEditable
                        )
                    }
                }
            ) {

                onSelectUnselect?.let { onSelectUnselect ->
                    GridMenuItem(
                        icon = R.drawable.checked_filled,
                        title = R.string.item_select,
                        titleString = selectText,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onSelectUnselect()
                        }
                    )
                }

                onPlayNext?.let { onPlayNext ->
                    GridMenuItem(
                        icon = R.drawable.play_skip_forward,
                        title = R.string.play_next,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onPlayNext()
                        }
                    )
                }

                onDeleteSongsNotInLibrary?.let { onDeleteSongsNotInLibrary ->
                    GridMenuItem(
                        icon = R.drawable.trash,
                        title = R.string.delete_songs_not_in_library,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onDeleteSongsNotInLibrary()
                        }
                    )
                }

                onEnqueue?.let { onEnqueue ->
                    GridMenuItem(
                        icon = R.drawable.enqueue,
                        title = R.string.enqueue,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onEnqueue()
                        }
                    )
                }

                if (showOnSyncronize) onSyncronize?.let { onSyncronize ->
                    GridMenuItem(
                        icon = R.drawable.sync,
                        title = R.string.sync,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onSyncronize()
                        }
                    )
                }

                if (showLinkUnlink) onLinkUnlink?.let { onLinkUnlink ->
                    GridMenuItem(
                        icon = R.drawable.link,
                        title = if (playlist?.playlist?.isYoutubePlaylist == true) R.string.unlink_from_ytm else R.string.unlink_from_yt,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onLinkUnlink()
                        }
                    )
                }

                onImportOnlinePlaylist?.let { onImportOnlinePlaylist ->
                    GridMenuItem(
                        icon = R.drawable.add_in_playlist,
                        title = R.string.import_playlist,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onImportOnlinePlaylist()
                        }
                    )
                }

                if (onAddToPreferites != null)
                    GridMenuItem(
                        icon = R.drawable.heart,
                        title = R.string.add_to_favorites,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onAddToPreferites()
                        }
                    )

                if (showonAddToPreferitesYoutube) {
                    if (onAddToPreferitesYoutube != null)
                        GridMenuItem(
                            icon = R.drawable.ytmusic,
                            title = R.string.add_rimusic_to_ytm_favorites,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onAddToPreferitesYoutube()
                            }
                        )
                }

                onAddToPlaylist?.let { onAddToPlaylist ->
                    GridMenuItem(
                        icon = R.drawable.add_in_playlist,
                        title = R.string.add_to_playlist,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            isViewingPlaylists = true
                        }
                    )
                }

                onRename?.let { onRename ->
                    GridMenuItem(
                        icon = R.drawable.title_edit,
                        title = R.string.rename,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onRename()
                        }
                    )
                }

                onDelete?.let { onDelete ->
                    GridMenuItem(
                        icon = R.drawable.trash,
                        title = R.string.delete,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onDelete()
                        }
                    )
                }

                onRenumberPositions?.let { onRenumberPositions ->
                    GridMenuItem(
                        icon = R.drawable.position,
                        title = R.string.renumber_songs_positions,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onRenumberPositions()
                        }
                    )
                }

                if (showonListenToYT) onListenToYT?.let { onListenToYT ->
                    GridMenuItem(
                        icon = R.drawable.play,
                        title = R.string.listen_on_youtube,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onListenToYT()
                        }
                    )
                }

                onExport?.let { onExport ->
                    GridMenuItem(
                        icon = R.drawable.export_outline,
                        title = R.string.export_playlist,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onExport()
                        }
                    )
                }

                onImport?.let { onImport ->
                    GridMenuItem(
                        icon = R.drawable.import_outline,
                        title = R.string.import_playlist,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onImport()
                        }
                    )
                }

                onImportFavorites?.let { onImport ->
                    GridMenuItem(
                        icon = R.drawable.import_outline,
                        title = R.string.import_favorites,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onImportFavorites()
                        }
                    )
                }

                onEditThumbnail?.let { onEditThumbnail ->
                    GridMenuItem(
                        icon = R.drawable.image,
                        title = R.string.edit_thumbnail,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onEditThumbnail()
                        }
                    )
                }

                onResetThumbnail?.let { onResetThumbnail ->
                    GridMenuItem(
                        icon = R.drawable.image,
                        title = R.string.reset_thumbnail,
                        colorIcon = colorPalette.text,
                        colorText = colorPalette.text,
                        onClick = {
                            onDismiss()
                            onResetThumbnail()
                        }
                    )
                }
            }

        }
    }

}