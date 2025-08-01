package it.fast4x.rimusic.service.modern

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_LOCK
import android.app.WallpaperManager.FLAG_SYSTEM
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink.DefaultAudioProcessorChain
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionToken
import app.kreate.android.Preferences
import app.kreate.android.R
import app.kreate.android.service.createDataSourceFactory
import app.kreate.android.service.newpipe.NewPipeDownloader
import app.kreate.android.utils.innertube.CURRENT_LOCALE
import app.kreate.android.utils.innertube.toMediaItem
import app.kreate.android.widget.Widget
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.MoreExecutors
import it.fast4x.innertube.models.NavigationEndpoint
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.MainActivity
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.enums.ExoPlayerCacheLocation
import it.fast4x.rimusic.enums.ExoPlayerDiskCacheMaxSize
import it.fast4x.rimusic.enums.NotificationButtons
import it.fast4x.rimusic.enums.NotificationType
import it.fast4x.rimusic.enums.PresetsReverb
import it.fast4x.rimusic.enums.QueueLoopType
import it.fast4x.rimusic.enums.WallpaperType
import it.fast4x.rimusic.extensions.audiovolume.AudioVolumeObserver
import it.fast4x.rimusic.extensions.audiovolume.OnAudioVolumeChangedListener
import it.fast4x.rimusic.extensions.connectivity.AndroidConnectivityObserverLegacy
import it.fast4x.rimusic.extensions.discord.updateDiscordPresence
import it.fast4x.rimusic.isHandleAudioFocusEnabled
import it.fast4x.rimusic.models.Event
import it.fast4x.rimusic.models.PersistentQueue
import it.fast4x.rimusic.models.PersistentSong
import it.fast4x.rimusic.models.QueuedMediaItem
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.asMediaItem
import it.fast4x.rimusic.service.BitmapProvider
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.MyDownloadService
import it.fast4x.rimusic.utils.CoilBitmapLoader
import it.fast4x.rimusic.utils.TimerJob
import it.fast4x.rimusic.utils.activityPendingIntent
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.broadCastPendingIntent
import it.fast4x.rimusic.utils.collect
import it.fast4x.rimusic.utils.fadeInEffect
import it.fast4x.rimusic.utils.fadeOutEffect
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.isAtLeastAndroid10
import it.fast4x.rimusic.utils.isAtLeastAndroid6
import it.fast4x.rimusic.utils.isAtLeastAndroid7
import it.fast4x.rimusic.utils.isAtLeastAndroid8
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.mediaItems
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.setGlobalVolume
import it.fast4x.rimusic.utils.timer
import it.fast4x.rimusic.utils.toggleRepeatMode
import it.fast4x.rimusic.utils.toggleShuffleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.knighthat.innertube.model.InnertubeSong
import me.knighthat.utils.Toaster
import org.schabi.newpipe.extractor.NewPipe
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import android.os.Binder as AndroidBinder
import me.knighthat.innertube.Innertube as NewInnertube


const val LOCAL_KEY_PREFIX = "local:"

val MediaItem.isLocal get() = mediaId.startsWith(LOCAL_KEY_PREFIX)
val Song.isLocal get() = id.startsWith(LOCAL_KEY_PREFIX)

@UnstableApi
class PlayerServiceModern : MediaLibraryService(),
    Player.Listener,
    PlaybackStatsListener.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener,
    OnAudioVolumeChangedListener {

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mediaSession: MediaLibrarySession
    private var mediaLibrarySessionCallback: MediaLibrarySessionCallback =
        MediaLibrarySessionCallback(this, Database, MyDownloadHelper)
    lateinit var player: ExoPlayer
    lateinit var cache: Cache
    lateinit var downloadCache: Cache
    private lateinit var audioVolumeObserver: AudioVolumeObserver
    private lateinit var bitmapProvider: BitmapProvider
    private var volumeNormalizationJob: Job? = null
    private var isPersistentQueueEnabled: Boolean = false
    private var isclosebackgroundPlayerEnabled = false
    private var audioManager: AudioManager? = null
    private var audioDeviceCallback: AudioDeviceCallback? = null
    private lateinit var downloadListener: DownloadManager.Listener

    var loudnessEnhancer: LoudnessEnhancer? = null
    private var binder = Binder()
    private var bassBoost: BassBoost? = null
    private var reverbPreset: PresetReverb? = null

    lateinit var audioQualityFormat: AudioQualityFormat
    lateinit var sleepTimer: SleepTimer
    private var timerJob: TimerJob? = null

    val currentMediaItem = MutableStateFlow<MediaItem?>(null)

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    private val currentSong = currentMediaItem.flatMapLatest { mediaItem ->
        Database.songTable.findById( mediaItem?.mediaId ?: "" )
    }.stateIn(coroutineScope, SharingStarted.Lazily, null)

    var currentSongStateDownload = MutableStateFlow(Download.STATE_STOPPED)

    lateinit var connectivityObserver: AndroidConnectivityObserverLegacy
    private val isNetworkAvailable = MutableStateFlow(true)
    private val waitingForNetwork = MutableStateFlow(false)

    private var notificationManager: NotificationManager? = null

    private lateinit var notificationActionReceiver: NotificationActionReceiver

    @kotlin.OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        // When persistent queue is enabled, Android
        // will start this service before MainApplication,
        // this will cause [Settings.preferences] to
        // throw error because it isn't init yet.
        // Problem can be solved by loading it here
        Preferences.load( this )

        super.onCreate()

        NewPipe.init( NewPipeDownloader() )

        // Enable Android Auto if disabled, REQUIRE ENABLING DEV MODE IN ANDROID AUTO
        try {
            connectivityObserver.unregister()
        } catch (e: Exception) {
            // isn't registered
        }
        connectivityObserver = AndroidConnectivityObserverLegacy(this@PlayerServiceModern)
        coroutineScope.launch {
            connectivityObserver.networkStatus.collect { isAvailable ->
                isNetworkAvailable.value = isAvailable
                Timber.d("PlayerServiceModern network status: $isAvailable")
                println("PlayerServiceModern network status: $isAvailable")
                if (isAvailable && waitingForNetwork.value) {
                    waitingForNetwork.value = false
                    withContext( Dispatchers.Main ) {
                        binder.gracefulPlay()
                    }
                }
            }
        }

        when( Preferences.NOTIFICATION_TYPE.value ){
            NotificationType.Default -> {
                // DEFAULT NOTIFICATION PROVIDER
                //        setMediaNotificationProvider(
                //            DefaultMediaNotificationProvider(
                //                this,
                //                { NotificationId },
                //                NotificationChannelId,
                //                R.string.player
                //            )
                //            .apply {
                //                setSmallIcon(R.drawable.app_icon)
                //            }
                //        )

                // DEFAULT NOTIFICATION PROVIDER MODDED
                setMediaNotificationProvider(CustomMediaNotificationProvider(this)
                    .apply {
                        setSmallIcon(R.drawable.ic_launcher_monochrome)
                    }
                )
            }
            NotificationType.Advanced -> {
                // CUSTOM NOTIFICATION PROVIDER -> CUSTOM NOTIFICATION PROVIDER WITH ACTIONS AND PENDING INTENT
                // ACTUALLY NOT STABLE
                setMediaNotificationProvider(object : MediaNotification.Provider{
                    override fun createNotification(
                        mediaSession: MediaSession,
                        customLayout: ImmutableList<CommandButton>,
                        actionFactory: MediaNotification.ActionFactory,
                        onNotificationChangedCallback: MediaNotification.Provider.Callback
                    ): MediaNotification {
                        return updateCustomNotification(mediaSession)
                    }

                    override fun handleCustomCommand(session: MediaSession, action: String, extras: Bundle): Boolean { return false }
                })
            }
        }

        runCatching {
            bitmapProvider = BitmapProvider(
                bitmapSize = (512 * resources.displayMetrics.density).roundToInt(),
                colorProvider = { isSystemInDarkMode ->
                    if (isSystemInDarkMode) Color.BLACK else Color.WHITE
                }
            )
        }.onFailure {
            Timber.e("Failed init bitmap provider in PlayerService ${it.stackTraceToString()}")
        }

        val preferences = preferences
        isPersistentQueueEnabled = Preferences.ENABLE_PERSISTENT_QUEUE.value

        audioQualityFormat = Preferences.AUDIO_QUALITY.value

        val cacheSize by Preferences.SONG_CACHE_SIZE

        val cacheEvictor = when( cacheSize ) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()

            ExoPlayerDiskCacheMaxSize.Custom    -> {
                val customCacheSize = Preferences.SONG_CACHE_CUSTOM_SIZE.value * 1000 * 1000L
                LeastRecentlyUsedCacheEvictor( customCacheSize )
            }

            else                                -> LeastRecentlyUsedCacheEvictor( cacheSize.bytes )
        }

        val cacheDir = when( cacheSize ) {
            // Temporary directory deletes itself after close
            // It means songs remain on device as long as it's open
            ExoPlayerDiskCacheMaxSize.Disabled -> createTempDirectory( CACHE_DIRNAME ).toFile()

            else                               ->
                // Looks a bit ugly but what it does is
                // check location set by user and return
                // appropriate path with [CACHE_DIRNAME] appended.
                when( Preferences.EXO_CACHE_LOCATION.value ) {
                    ExoPlayerCacheLocation.System  -> cacheDir
                    ExoPlayerCacheLocation.Private -> filesDir
                }.resolve( CACHE_DIRNAME )
        }

        // Ensure this location exists
        cacheDir.mkdirs()

        cache = SimpleCache( cacheDir, cacheEvictor, StandaloneDatabaseProvider(this) )
        downloadCache = MyDownloadHelper.getDownloadCache( applicationContext )


        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(createMediaSourceFactory())
            .setRenderersFactory(createRendersFactory())
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                isHandleAudioFocusEnabled()
            )
            .setUsePlatformDiagnostics(false)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                addListener(this@PlayerServiceModern)
                sleepTimer = SleepTimer(coroutineScope, this)
                addListener(sleepTimer)
                addAnalyticsListener(PlaybackStatsListener(false, this@PlayerServiceModern))
            }

        preferences.registerOnSharedPreferenceChangeListener(this)

        // Force player to add all commands available, prior to android 13
        val forwardingPlayer =
            object : ForwardingPlayer(player) {
                override fun getAvailableCommands(): Player.Commands {
                    return super.getAvailableCommands()
                        .buildUpon()
                        .addAllCommands()
                        //.remove(COMMAND_SEEK_TO_PREVIOUS)
                        //.remove(COMMAND_SEEK_TO_NEXT)
                        .build()
                }
            }

        mediaLibrarySessionCallback.apply {
            binder = this@PlayerServiceModern.binder
            toggleLike = ::toggleLike
            toggleDownload = ::toggleDownload
            toggleRepeat = ::toggleRepeat
            toggleShuffle = ::toggleShuffle
            startRadio = ::startRadio
            callPause = binder::gracefulPause
            actionSearch = ::actionSearch
        }

        // Build the media library session
        mediaSession =
            MediaLibrarySession.Builder(this, forwardingPlayer, mediaLibrarySessionCallback)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java)
                            .putExtra("expandPlayerBottomSheet", true),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setBitmapLoader(CoilBitmapLoader(
                    this,
                    coroutineScope,
                    512 * resources.displayMetrics.density.toInt()
                ))
                .build()

        player.skipSilenceEnabled = Preferences.AUDIO_SKIP_SILENCE.value
        player.addListener(this@PlayerServiceModern)
        player.addAnalyticsListener(PlaybackStatsListener(false, this@PlayerServiceModern))

        player.repeatMode = Preferences.QUEUE_LOOP_TYPE.value.type

        binder.player.playbackParameters = PlaybackParameters(
            Preferences.AUDIO_SPEED_VALUE.value,
            Preferences.AUDIO_PITCH.value
        )
        binder.player.volume = Preferences.AUDIO_VOLUME.value
        binder.player.setGlobalVolume(binder.player.volume)

        // Keep a connected controller so that notification works
        val sessionToken = SessionToken(this, ComponentName(this, PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({ controllerFuture.get() }, MoreExecutors.directExecutor())

        audioVolumeObserver = AudioVolumeObserver(this)
        audioVolumeObserver.register(AudioManager.STREAM_MUSIC, this)

        // Download listener help to notify download change to UI
        downloadListener = object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) = run {
                if (download.request.id != currentMediaItem.value?.mediaId) return@run
                println("PlayerServiceModern onDownloadChanged current song ${currentMediaItem.value?.mediaId} state ${download.state} key ${download.request.id}")
                updateDownloadedState()
            }
        }
        MyDownloadHelper.getDownloadManager(this).addListener(downloadListener)

        notificationActionReceiver = NotificationActionReceiver(player)


        val filter = IntentFilter().apply {
            addAction(Action.play.value)
            addAction(Action.pause.value)
            addAction(Action.next.value)
            addAction(Action.previous.value)
            addAction(Action.like.value)
            addAction(Action.download.value)
            addAction(Action.playradio.value)
            addAction(Action.shuffle.value)
            addAction(Action.repeat.value)
            addAction(Action.search.value)
        }

        ContextCompat.registerReceiver(
            this,
            notificationActionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Ensure that song is updated
        currentSong.debounce(1000).collect(coroutineScope) { song ->
            println("PlayerServiceModern onCreate currentSong $song")
            updateDownloadedState()
            println("PlayerServiceModern onCreate currentSongIsDownloaded ${currentSongStateDownload.value}")

            updateDefaultNotification()
            withContext(Dispatchers.Main) {
                player.currentMediaItem?.also {
                    if( !isAtLeastAndroid6 || !Preferences.DISCORD_LOGIN.value ) return@also

                    updateDiscordPresence(
                        this@PlayerServiceModern,
                        mediaItem = it,
                        timeStart = if (player.isPlaying)
                            System.currentTimeMillis() - player.currentPosition
                        else
                            0L,
                        timeEnd = if (player.isPlaying)
                            (System.currentTimeMillis() - player.currentPosition) + player.duration
                        else
                            0L
                    )
                }

                updateWidgets()
            }
        }

        maybeRestorePlayerQueue()

        maybeResumePlaybackWhenDeviceConnected()

        maybeBassBoost()

        maybeReverb()

        /* Queue is saved in events without scheduling it (remove this in future)*/
        // Load persistent queue when start activity and save periodically in background
        if (isPersistentQueueEnabled) {
            maybeResumePlaybackOnStart()

            val scheduler = Executors.newScheduledThreadPool(1)
            scheduler.scheduleWithFixedDelay({
                println("PlayerServiceModern onCreate savePersistentQueue")
                maybeSavePlayerQueue()
            }, 0, 30, TimeUnit.SECONDS)

        }


    }

    override fun onBind(intent: Intent?) = super.onBind(intent) ?: binder

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        maybeSavePlayerQueue()
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        updateDefaultNotification()
        Preferences.QUEUE_LOOP_TYPE.value = QueueLoopType.from( repeatMode )
    }



    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        // if pause listen history is enabled, don't register statistic event
        if ( Preferences.PAUSE_HISTORY.value ) return

        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        val totalPlayTimeMs = playbackStats.totalPlayTimeMs

        if ( totalPlayTimeMs > 5000 )
            Database.asyncTransaction {
                songTable.updateTotalPlayTime( mediaItem.mediaId, totalPlayTimeMs, true )
            }


        val minTimeForEvent by Preferences.QUICK_PICKS_MIN_DURATION

        if ( totalPlayTimeMs > minTimeForEvent.asMillis ) {
            Database.asyncTransaction {
                eventTable.insertIgnore(
                    Event(
                        songId = mediaItem.mediaId,
                        timestamp = System.currentTimeMillis(),
                        playTime = totalPlayTimeMs
                    )
                )
            }

        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        isclosebackgroundPlayerEnabled = Preferences.CLOSE_BACKGROUND_JOB_IN_TASK_MANAGER.value
        if (isclosebackgroundPlayerEnabled) {
            broadCastPendingIntent<NotificationDismissReceiver>().send()
            this.stopService(this.intent<MyDownloadService>())
            this.stopService(this.intent<PlayerServiceModern>())
            onDestroy()
        }
        super.onTaskRemoved(rootIntent)
    }

    @UnstableApi
    override fun onDestroy() {
        runCatching {
            maybeSavePlayerQueue()

            preferences.unregisterOnSharedPreferenceChangeListener(this)

            stopService(intent<MyDownloadService>())
            stopService(intent<PlayerServiceModern>())

            player.removeListener(this)
            player.stop()
            player.release()

            try{
                unregisterReceiver(notificationActionReceiver)
            } catch (e: Exception){
                Timber.e("PlayerServiceModern onDestroy unregisterReceiver notificationActionReceiver ${e.stackTraceToString()}")
            }


            mediaSession.release()
            cache.release()
            //downloadCache.release()
            MyDownloadHelper.getDownloadManager(this).removeListener(downloadListener)

            loudnessEnhancer?.release()
            audioVolumeObserver.unregister()

            timerJob?.cancel()
            timerJob = null

            notificationManager?.cancel(NotificationId)
            notificationManager?.cancelAll()
            notificationManager = null

            coroutineScope.cancel()

        }.onFailure {
            Timber.e("Failed onDestroy in PlayerService ${it.stackTraceToString()}")
        }
        super.onDestroy()
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            Preferences.ENABLE_PERSISTENT_QUEUE.key ->
                isPersistentQueueEnabled = sharedPreferences.getBoolean( key, Preferences.ENABLE_PERSISTENT_QUEUE.defaultValue )

            Preferences.AUDIO_VOLUME_NORMALIZATION.key,
            Preferences.AUDIO_VOLUME_NORMALIZATION_TARGET.key -> maybeNormalizeVolume()

            Preferences.RESUME_PLAYBACK_WHEN_CONNECT_TO_AUDIO_DEVICE.key -> maybeResumePlaybackWhenDeviceConnected()

            Preferences.AUDIO_SKIP_SILENCE.key ->
                player.skipSilenceEnabled = sharedPreferences.getBoolean( key, Preferences.AUDIO_SKIP_SILENCE.defaultValue )

            Preferences.QUEUE_LOOP_TYPE.key ->
                player.repeatMode = sharedPreferences.getEnum( key, Preferences.QUEUE_LOOP_TYPE.defaultValue ).type

            Preferences.AUDIO_BASS_BOOST_LEVEL.key,
            Preferences.AUDIO_BASS_BOOSTED.key -> maybeBassBoost()

            Preferences.AUDIO_REVERB_PRESET.key -> maybeReverb()
        }
    }

    private var pausedByZeroVolume = false
    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        if ( Preferences.PAUSE_WHEN_VOLUME_SET_TO_ZERO.value ) {
            if (player.isPlaying && currentVolume < 1) {
                binder.gracefulPause()
                pausedByZeroVolume = true
            } else if (pausedByZeroVolume && currentVolume >= 1) {
                binder.gracefulPlay()
                pausedByZeroVolume = false
            }
        }
    }

    override fun onAudioVolumeDirectionChanged(direction: Int) {
        /*
        if (direction == 0) {
            binder.player.seekToPreviousMediaItem()
        } else {
            binder.player.seekToNextMediaItem()
        }

         */
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

        println("PlayerServiceModern onMediaItemTransition mediaItem $mediaItem reason $reason")

        currentMediaItem.update { mediaItem }
        maybeRecoverPlaybackError()
        maybeNormalizeVolume()

        loadFromRadio(reason)

        with(bitmapProvider) {
            var newUriForLoad = binder.player.currentMediaItem?.mediaMetadata?.artworkUri
            if(lastUri == binder.player.currentMediaItem?.mediaMetadata?.artworkUri) {
                newUriForLoad = null
            }

            load(newUriForLoad, {
                updateDefaultNotification()
                updateWidgets()
            })
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            maybeSavePlayerQueue()
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        updateDefaultNotification()
        if (shuffleModeEnabled) {
            val shuffledIndices = IntArray(player.mediaItemCount) { it }
            shuffledIndices.shuffle()
            shuffledIndices[shuffledIndices.indexOf(player.currentMediaItemIndex)] = shuffledIndices[0]
            shuffledIndices[0] = player.currentMediaItemIndex
            player.setShuffleOrder(DefaultShuffleOrder(shuffledIndices, System.currentTimeMillis()))
        }
    }

    @UnstableApi
    override fun onIsPlayingChanged(isPlaying: Boolean) = updateWidgets()

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)

        Timber.e("PlayerServiceModern onPlayerError error code ${error.errorCode} message ${error.message} cause ${error.cause?.cause}")
        println("PlayerServiceModern onPlayerError error code ${error.errorCode} message ${error.message} cause ${error.cause?.cause}")

        val playbackConnectionExeptionList = listOf(
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED, //primary error code to manage
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
        )

        // check if error is caused by internet connection
        val isConnectionError = (error.cause?.cause is PlaybackException)
                && (error.cause?.cause as PlaybackException).errorCode in playbackConnectionExeptionList

        if (!isNetworkAvailable.value || isConnectionError) {
            waitingForNetwork.value = true
            Toaster.noInternet()
            return
        }

        val playbackHttpExeptionList = listOf(
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE,
            416 // 416 Range Not Satisfiable
        )

        if (error.errorCode in playbackHttpExeptionList) {
            Timber.e("PlayerServiceModern onPlayerError recovered occurred errorCodeName ${error.errorCodeName} cause ${error.cause?.cause}")
            println("PlayerServiceModern onPlayerError recovered occurred errorCodeName ${error.errorCodeName} cause ${error.cause?.cause}")
            player.pause()
            player.prepare()
            player.play()
            return
        }

        if ( !Preferences.PLAYBACK_SKIP_ON_ERROR.value || !player.hasNextMediaItem() )
            return

        val prev = player.currentMediaItem ?: return
        //player.seekToNextMediaItem()
        player.playNext()

        showSmartMessage(
            message = getString(
                R.string.skip_media_on_error_message,
                prev.mediaMetadata.title
            )
        )

    }

//    override fun onPlaybackStateChanged(playbackState: Int) {
//        if (playbackState == STATE_IDLE) {
//            player.shuffleModeEnabled = false
//            //player.clearMediaItems()
//        }
//    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
            val isBufferingOrReady = player.playbackState == Player.STATE_BUFFERING || player.playbackState == Player.STATE_READY
            if (isBufferingOrReady && player.playWhenReady) {
                sendOpenEqualizerIntent()
            } else {
                sendCloseEqualizerIntent()
                if (!player.playWhenReady) {
                    waitingForNetwork.value = false
                }
            }
        }

//        if (events.containsAny(EVENT_TIMELINE_CHANGED, EVENT_POSITION_DISCONTINUITY)) {
//            currentMediaItem.value = player.currentMediaItem
//        }
    }


    private fun maybeRecoverPlaybackError() {
        if (player.playerError != null) {
            player.prepare()
        }
    }

    private fun loadFromRadio( reason: Int ) {
        // Don't fetch more item if:
        // - Feature is disabled
        // - When song is repeated
        // - Start new queue
        if( !Preferences.QUEUE_AUTO_APPEND.value
            || reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
            || reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
        ) return

        val positionToLast = player.mediaItemCount - player.currentMediaItemIndex
        // Make sure only add when about 10 songs to the last song in queue
        // TODO: Add slider in settings to let user change number of songs
        if( positionToLast <= 10 && !binder.isLoadingRadio )
            player.currentMediaItem?.let {
                binder.startRadio( it, true )
            }
    }

    private fun maybeBassBoost() {
        if ( !Preferences.AUDIO_BASS_BOOSTED.value ) {
            runCatching {
                bassBoost?.enabled = false
                bassBoost?.release()
            }
            bassBoost = null
            maybeNormalizeVolume()
            return
        }

        runCatching {
            if (bassBoost == null) bassBoost = BassBoost(0, player.audioSessionId)
            val bassboostLevel =
                (Preferences.AUDIO_BASS_BOOST_LEVEL.value * 1000f).toInt().toShort()
            println("PlayerServiceModern maybeBassBoost bassboostLevel $bassboostLevel")
            bassBoost?.enabled = false
            bassBoost?.setStrength(bassboostLevel)
            bassBoost?.enabled = true
        }.onFailure {
            Toaster.e( "Can't enable bass boost" )
        }
    }

    private fun maybeReverb() {
        val presetType by Preferences.AUDIO_REVERB_PRESET
        println("PlayerServiceModern maybeReverb presetType $presetType")
        if (presetType == PresetsReverb.NONE) {
            runCatching {
                reverbPreset?.enabled = false
                player.clearAuxEffectInfo()
                reverbPreset?.release()
            }
                reverbPreset = null
            return
        }

        runCatching {
            if (reverbPreset == null) reverbPreset = PresetReverb(1, player.audioSessionId)

            reverbPreset?.enabled = false
            reverbPreset?.preset = presetType.preset
            reverbPreset?.enabled = true
            reverbPreset?.id?.let { player.setAuxEffectInfo(AuxEffectInfo(it, 1f)) }
        }
    }

    @UnstableApi
    private fun maybeNormalizeVolume() {
        if ( !Preferences.AUDIO_VOLUME_NORMALIZATION.value ) {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            volumeNormalizationJob?.cancel()
            return
        }

        runCatching {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
            }
        }.onFailure {
            Timber.e("PlayerService maybeNormalizeVolume load loudnessEnhancer ${it.stackTraceToString()}")
            println("PlayerService maybeNormalizeVolume load loudnessEnhancer ${it.stackTraceToString()}")
            return
        }

        val baseGain by Preferences.AUDIO_VOLUME_NORMALIZATION_TARGET
        player.currentMediaItem?.mediaId?.let { songId ->
            volumeNormalizationJob?.cancel()
            volumeNormalizationJob = coroutineScope.launch(Dispatchers.Main) {
                fun Float?.toMb() = ((this ?: 0f) * 100).toInt()

                Database.formatTable
                        .findBySongId( songId )
                        .cancellable()
                        .collectLatest { format ->
                            val loudnessMb = format?.loudnessDb.toMb().let {
                                if (it !in -2000..2000) {
                                    Toaster.w( "Extreme loudness detected" )

                                    0
                                } else
                                    it
                            }

                            try {
                                loudnessEnhancer?.setTargetGain(baseGain.toMb() - loudnessMb)
                                loudnessEnhancer?.enabled = true
                            } catch (e: Exception) {
                                Timber.e("PlayerService maybeNormalizeVolume apply targetGain ${e.stackTraceToString()}")
                                println("PlayerService maybeNormalizeVolume apply targetGain ${e.stackTraceToString()}")
                            }
                        }
            }
        }
    }


    @SuppressLint("NewApi")
    private fun maybeResumePlaybackWhenDeviceConnected() {
        if (!isAtLeastAndroid6) return

        if ( Preferences.RESUME_PLAYBACK_WHEN_CONNECT_TO_AUDIO_DEVICE.value ) {
            if (audioManager == null) {
                audioManager = getSystemService(AUDIO_SERVICE) as AudioManager?
            }

            audioDeviceCallback = object : AudioDeviceCallback() {
                private fun canPlayMusic(audioDeviceInfo: AudioDeviceInfo): Boolean {
                    if (!audioDeviceInfo.isSink) return false

                    return audioDeviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }

                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    if (!player.isPlaying && addedDevices.any(::canPlayMusic)) {
                        player.play()
                    }
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) = Unit
            }

            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, handler)

        } else {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
            audioDeviceCallback = null
        }
    }

    private fun createRendersFactory() = object : DefaultRenderersFactory(this) {
        override fun buildAudioSink(
            context: Context,
            enableFloatOutput: Boolean,
            enableAudioTrackPlaybackParams: Boolean
        ): AudioSink {
            val minimumSilenceDuration: Long = Preferences.AUDIO_SKIP_SILENCE_LENGTH
                                                       .value
                                                       .coerceIn( 1_000L..2_000_000L )

            return DefaultAudioSink.Builder(applicationContext)
                .setEnableFloatOutput(enableFloatOutput)
                .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                .setAudioOffloadSupportProvider(
                    DefaultAudioOffloadSupportProvider(applicationContext)
                )
                .setAudioProcessorChain(
                    DefaultAudioProcessorChain(
                        arrayOf(),
                        SilenceSkippingAudioProcessor(
                            /* minimumSilenceDurationUs = */ minimumSilenceDuration,
                            /* silenceRetentionRatio = */ 0.01f,
                            /* maxSilenceToKeepDurationUs = */ minimumSilenceDuration,
                            /* minVolumeToKeepPercentageWhenMuting = */ 0,
                            /* silenceThresholdLevel = */ 256
                        ),
                        SonicAudioProcessor()
                    )
                )
                .build()
                .apply {
                    if (isAtLeastAndroid10) setOffloadMode(AudioSink.OFFLOAD_MODE_DISABLED)
                }
        }
    }

    private fun createMediaSourceFactory() = DefaultMediaSourceFactory(
        createDataSourceFactory(),
        DefaultExtractorsFactory()
    ).setLoadErrorHandlingPolicy(
        object : DefaultLoadErrorHandlingPolicy() {
            override fun isEligibleForFallback(exception: IOException) = true
        }
    )


    private fun buildCustomCommandButtons(): MutableList<CommandButton> {
        val notificationPlayerFirstIcon by Preferences.MEDIA_NOTIFICATION_FIRST_ICON
        val notificationPlayerSecondIcon by Preferences.MEDIA_NOTIFICATION_SECOND_ICON

        val commandButtonsList = mutableListOf<CommandButton>()
        val firstCommandButton = NotificationButtons.entries.let { buttons ->
            buttons
                .filter { it == notificationPlayerFirstIcon }
                .map {
                    val displayName = appContext().resources.getString( it.textId )

                    CommandButton.Builder()
                        .setDisplayName( displayName )
                        .setIconResId(
                            it.getStateIcon(
                                it,
                                currentSong.value?.likedAt,
                                currentSongStateDownload.value,
                                player.repeatMode,
                                player.shuffleModeEnabled
                            )
                        )
                        .setSessionCommand(it.sessionCommand)
                        .build()
                }
        }

        val secondCommandButton =  NotificationButtons.entries.let { buttons ->
            buttons
                .filter { it == notificationPlayerSecondIcon }
                .map {
                    val displayName = appContext().resources.getString( it.textId )

                    CommandButton.Builder()
                        .setDisplayName( displayName )
                        .setIconResId(
                            it.getStateIcon(
                                it,
                                currentSong.value?.likedAt,
                                currentSongStateDownload.value,
                                player.repeatMode,
                                player.shuffleModeEnabled
                            )
                        )
                        .setSessionCommand(it.sessionCommand)
                        .build()
                }
        }

        val otherCommandButtons = NotificationButtons.entries.let { buttons ->
            buttons
                .filterNot { it == notificationPlayerFirstIcon || it == notificationPlayerSecondIcon }
                .map {
                    val displayName = appContext().resources.getString( it.textId )

                    CommandButton.Builder()
                        .setDisplayName( displayName )
                        .setIconResId(
                            it.getStateIcon(
                                it,
                                currentSong.value?.likedAt,
                                currentSongStateDownload.value,
                                player.repeatMode,
                                player.shuffleModeEnabled
                            )
                        )
                        .setSessionCommand(it.sessionCommand)
                        .build()
                }
        }

        commandButtonsList += firstCommandButton + secondCommandButton + otherCommandButtons

        return commandButtonsList
    }

    private fun updateCustomNotification(session: MediaSession): MediaNotification {

        val playIntent = Action.play.pendingIntent
        val pauseIntent = Action.pause.pendingIntent
        val nextIntent = Action.next.pendingIntent
        val prevIntent = Action.previous.pendingIntent

        val mediaMetadata = player.mediaMetadata

        bitmapProvider.load(mediaMetadata.artworkUri) {}

        val customNotify = if (isAtLeastAndroid8) {
            NotificationCompat.Builder(this, NotificationChannelId)
        } else {
            NotificationCompat.Builder(this)
        }
            .setContentTitle(cleanPrefix(player.mediaMetadata.title.toString()))
            .setContentText(
                if (mediaMetadata.albumTitle != null && mediaMetadata.artist != "")
                    "${mediaMetadata.artist} | ${mediaMetadata.albumTitle}"
                else mediaMetadata.artist
            )
            .setSubText(
                if (mediaMetadata.albumTitle != null && mediaMetadata.artist != "")
                    "${mediaMetadata.artist} | ${mediaMetadata.albumTitle}"
                else mediaMetadata.artist
            )
            .setLargeIcon(bitmapProvider.bitmap)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(player.playerError?.let { R.drawable.alert_circle }
                ?: R.drawable.ic_launcher_monochrome)
            .setOngoing(false)
            .setContentIntent(activityPendingIntent<MainActivity>(
                flags = PendingIntent.FLAG_UPDATE_CURRENT
            ) {
                putExtra("expandPlayerBottomSheet", true)
            })
            .setDeleteIntent(broadCastPendingIntent<NotificationDismissReceiver>())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
            .addAction(R.drawable.play_skip_back, "Skip back", prevIntent)
            .addAction(
                if (player.isPlaying) R.drawable.pause else R.drawable.play,
                if (player.isPlaying) "Pause" else "Play",
                if (player.isPlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.play_skip_forward, "Skip forward", nextIntent)

        //***********************
        val notificationPlayerFirstIcon by Preferences.MEDIA_NOTIFICATION_FIRST_ICON
        val notificationPlayerSecondIcon by Preferences.MEDIA_NOTIFICATION_SECOND_ICON

        NotificationButtons.entries.let { buttons ->
            buttons
                .filter { it == notificationPlayerFirstIcon }
                .map {
                    customNotify.addAction(
                        it.getStateIcon(
                            it,
                            currentSong.value?.likedAt,
                            currentSongStateDownload.value,
                            player.repeatMode,
                            player.shuffleModeEnabled
                        ),
                        appContext().resources.getString( it.textId ),
                        it.pendingIntent
                    )
                }
        }

        NotificationButtons.entries.let { buttons ->
            buttons
                .filter { it == notificationPlayerSecondIcon }
                .map {
                    customNotify.addAction(
                        it.getStateIcon(
                            it,
                            currentSong.value?.likedAt,
                            currentSongStateDownload.value,
                            player.repeatMode,
                            player.shuffleModeEnabled
                        ),
                        appContext().resources.getString( it.textId ),
                        it.pendingIntent
                    )
                }
        }

        NotificationButtons.entries.let { buttons ->
            buttons
                .filterNot { it == notificationPlayerFirstIcon || it == notificationPlayerSecondIcon }
                .map {
                    customNotify.addAction(
                        it.getStateIcon(
                            it,
                            currentSong.value?.likedAt,
                            currentSongStateDownload.value,
                            player.repeatMode,
                            player.shuffleModeEnabled
                        ),
                        appContext().resources.getString( it.textId ),
                        it.pendingIntent
                    )
                }
        }
        //***********************

        updateWallpaper()

        return MediaNotification(NotificationId, customNotify.build())
    }

    private fun updateWallpaper() {
        val wallpaperEnabled by Preferences.ENABLE_WALLPAPER
        val wallpaperType by Preferences.WALLPAPER_TYPE
        if (isAtLeastAndroid7 && wallpaperEnabled) {
            coroutineScope.launch(Dispatchers.IO) {
                val wpManager = WallpaperManager.getInstance(this@PlayerServiceModern)
                wpManager.setBitmap(bitmapProvider.bitmap, null, true,
                    when (wallpaperType) {
                        WallpaperType.Both -> (FLAG_LOCK or FLAG_SYSTEM)
                        WallpaperType.Lockscreen -> FLAG_LOCK
                        WallpaperType.Home -> FLAG_SYSTEM
                    }
                )
            }
        }
    }

    private fun updateDefaultNotification() {
        coroutineScope.launch(Dispatchers.Main) {
            mediaSession.setCustomLayout( buildCustomCommandButtons() )
        }

    }

    fun toggleLike() {
        binder.toggleLike()
    }

    fun toggleDownload() {
        binder.toggleDownload()
    }

    fun toggleRepeat() {
        binder.toggleRepeat()
    }

    fun toggleShuffle() {
        binder.toggleShuffle()
    }

    fun startRadio() {
        player.currentMediaItem?.let( binder::startRadio )
    }

    private fun showSmartMessage( message: String ) = Toaster.i(message)

    @MainThread
    fun updateWidgets() {
        val status = Triple(
            binder.player.mediaMetadata.title.toString(),
            binder.player.mediaMetadata.artist.toString(),
            binder.player.isPlaying
        )

        val actions = Triple(
            if( status.third ) binder::gracefulPause else binder::gracefulPlay,
            binder.player::seekToPrevious,
            binder.player::seekToNext
        )

        CoroutineScope( Dispatchers.IO ).launch {
            // Save bitmap to file
            val file = File( cacheDir, "widget_thumbnail.png" )
            FileOutputStream(file).use { outStream ->
                bitmapProvider.bitmap.compress( Bitmap.CompressFormat.PNG, 50, outStream )
            }

            withContext( Dispatchers.Default ) {
                Widget.Vertical.update( applicationContext, actions, status, file )
                Widget.Horizontal.update( applicationContext, actions, status, file )
            }
        }
    }

    @UnstableApi
    private fun sendOpenEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
        )
    }


    @UnstableApi
    private fun sendCloseEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            }
        )
    }

    private fun actionSearch() {
        binder.actionSearch()
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        Timber.d("PlayerServiceModern onPositionDiscontinuity oldPosition ${oldPosition.mediaItemIndex} newPosition ${newPosition.mediaItemIndex} reason $reason")
        println("PlayerServiceModern onPositionDiscontinuity oldPosition ${oldPosition.mediaItemIndex} newPosition ${newPosition.mediaItemIndex} reason $reason")
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
    }

    private fun maybeSavePlayerQueue() {
        println("PlayerServiceModern onCreate savePersistentQueue")
        if (!isPersistentQueueEnabled) return
        println("PlayerServiceModern onCreate savePersistentQueue is enabled")

        CoroutineScope(Dispatchers.Main).launch {
            val mediaItems = player.currentTimeline.mediaItems
            val mediaItemIndex = player.currentMediaItemIndex
            val mediaItemPosition = player.currentPosition

            if (mediaItems.isEmpty()) return@launch


            mediaItems.mapIndexed { index, mediaItem ->
                QueuedMediaItem(
                    mediaItem = mediaItem,
                    position = if (index == mediaItemIndex) mediaItemPosition else null
                )
            }.let { queuedMediaItems ->
                if (queuedMediaItems.isEmpty()) return@let

                Database.asyncTransaction {
                    queueTable.deleteAll()
                    queueTable.insert( queuedMediaItems )
                }

                Timber.d("PlayerServiceModern QueuePersistentEnabled Saved queue")
            }

        }
    }

    private fun maybeResumePlaybackOnStart() {
        if( isPersistentQueueEnabled && Preferences.RESUME_PLAYBACK_ON_STARTUP.value )
            binder.gracefulPlay()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @UnstableApi
    private fun maybeRestorePlayerQueue() {
        if (!isPersistentQueueEnabled) return

        Database.asyncQuery {
            val queuedSong = runBlocking {
                queueTable.all().first()
            }

            if (queuedSong.isEmpty()) return@asyncQuery

            val index = queuedSong.indexOfFirst { it.position != null }.coerceAtLeast(0)

            runBlocking(Dispatchers.Main) {
                player.setMediaItems(
                    queuedSong.map { mediaItem ->
                        mediaItem.mediaItem.buildUpon()
                            .setUri(mediaItem.mediaItem.mediaId)
                            .setCustomCacheKey(mediaItem.mediaItem.mediaId)
                            .build().apply {
                                mediaMetadata.extras?.putBoolean("isFromPersistentQueue", true)
                            }
                    },
                    index,
                    queuedSong[index].position ?: C.TIME_UNSET
                )
                player.prepare()
            }
        }

    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @UnstableApi
    private fun maybeRestoreFromDiskPlayerQueue() {
        //if (!isPersistentQueueEnabled) return
        //Log.d("mediaItem", "QueuePersistentEnabled Restore Initial")

        runCatching {
            filesDir.resolve("persistentQueue.data").inputStream().use { fis ->
                ObjectInputStream(fis).use { oos ->
                    oos.readObject() as PersistentQueue
                }
            }
        }.onSuccess { queue ->
            //Log.d("mediaItem", "QueuePersistentEnabled Restored queue $queue")
            //Log.d("mediaItem", "QueuePersistentEnabled Restored ${queue.songMediaItems.size}")
            runBlocking(Dispatchers.Main) {
                player.setMediaItems(
                    queue.songMediaItems.map { song ->
                        song.asMediaItem.buildUpon()
                            .setUri(song.asMediaItem.mediaId)
                            .setCustomCacheKey(song.asMediaItem.mediaId)
                            .build().apply {
                                mediaMetadata.extras?.putBoolean("isFromPersistentQueue", true)
                            }
                    },
                    queue.mediaItemIndex,
                    queue.position
                )

                player.prepare()

            }

        }.onFailure {
            //it.printStackTrace()
            Timber.e(it.stackTraceToString())
        }

        //Log.d("mediaItem", "QueuePersistentEnabled Restored ${player.currentTimeline.mediaItems.size}")

    }

    private fun maybeSaveToDiskPlayerQueue() {

        //if (!isPersistentQueueEnabled) return
        //Log.d("mediaItem", "QueuePersistentEnabled Save ${player.currentTimeline.mediaItems.size}")

        val persistentQueue = PersistentQueue(
            title = "title",
            songMediaItems = player.currentTimeline.mediaItems.map {
                PersistentSong(
                    id = it.mediaId,
                    title = it.mediaMetadata.title.toString(),
                    durationText = it.mediaMetadata.extras?.getString("durationText").toString(),
                    thumbnailUrl = it.mediaMetadata.artworkUri.toString()
                )
            },
            mediaItemIndex = player.currentMediaItemIndex,
            position = player.currentPosition
        )

        runCatching {
            filesDir.resolve("persistentQueue.data").outputStream().use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(persistentQueue)
                }
            }
        }.onFailure {
            //it.printStackTrace()
            Timber.e(it.stackTraceToString())

        }.onSuccess {
            Log.d("mediaItem", "QueuePersistentEnabled Saved $persistentQueue")
        }

    }

    fun updateDownloadedState() {
        if (currentSong.value == null) return
        val mediaId = currentSong.value!!.id
        val downloads = MyDownloadHelper.downloads.value
        currentSongStateDownload.value = downloads[mediaId]?.state ?: Download.STATE_STOPPED
        /*
        if (downloads[currentSong.value?.id]?.state == Download.STATE_COMPLETED) {
            currentSongIsDownloaded.value = true
        } else {
            currentSongIsDownloaded.value = false
        }
        */
        println("PlayerServiceModern updateDownloadedState downloads count ${downloads.size} currentSongIsDownloaded ${currentSong.value?.id}")
        updateDefaultNotification()

    }

    /**
     * This method should ONLY be called when the application (sc. activity) is in the foreground!
     */
    fun restartForegroundOrStop() {
        binder.restartForegroundOrStop()
    }

    @UnstableApi
    class CustomMediaNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {
        override fun getNotificationContentTitle(metadata: MediaMetadata): CharSequence? {
            val customMetadata = MediaMetadata.Builder()
                .setTitle(cleanPrefix(metadata.title?.toString() ?: ""))
                .build()
            return super.getNotificationContentTitle(customMetadata)
        }

//        override fun getNotificationContentText(metadata: MediaMetadata): CharSequence? {
//            val customMetadata = MediaMetadata.Builder()
//                .setArtist(cleanPrefix(metadata.artist?.toString() ?: ""))
//                .build()
//            return super.getNotificationContentText(customMetadata)
//        }
    }


    class NotificationDismissReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            kotlin.runCatching {
                context.stopService(context.intent<MyDownloadService>())
            }.onFailure {
                Timber.e("Failed NotificationDismissReceiver stopService in PlayerServiceModern (MyDownloadService) ${it.stackTraceToString()}")
            }
            kotlin.runCatching {
                context.stopService(context.intent<PlayerServiceModern>())
            }.onFailure {
                Timber.e("Failed NotificationDismissReceiver stopService in PlayerServiceModern (PlayerServiceModern) ${it.stackTraceToString()}")
            }
        }
    }

    inner class NotificationActionReceiver(private val player: Player) : BroadcastReceiver() {


        @ExperimentalCoroutinesApi
        @FlowPreview
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Action.pause.value -> binder.gracefulPause()
                Action.play.value -> binder.gracefulPlay()
                Action.next.value -> player.playNext()
                Action.previous.value -> player.playPrevious()
                Action.like.value -> {
                    binder.toggleLike()
                }

                Action.download.value -> {
                    binder.toggleDownload()
                }

                Action.playradio.value -> startRadio()

                Action.shuffle.value -> {
                    binder.toggleShuffle()
                }

                Action.search.value -> {
                    binder.actionSearch()
                }

                Action.repeat.value -> {
                    binder.toggleRepeat()
                }


            }

        }

    }

    open inner class Binder : AndroidBinder() {
        val service: PlayerServiceModern
            get() = this@PlayerServiceModern

        /*
        fun setBitmapListener(listener: ((Bitmap?) -> Unit)?) {
            bitmapProvider.listener = listener
        }

        */
        val bitmap: Bitmap
            get() = bitmapProvider.bitmap


        val player: ExoPlayer
            get() = this@PlayerServiceModern.player

        val cache: Cache
            get() = this@PlayerServiceModern.cache

        val downloadCache: Cache
            get() = this@PlayerServiceModern.downloadCache

        val sleepTimerMillisLeft: StateFlow<Long?>?
            get() = timerJob?.millisLeft

        fun startSleepTimer(delayMillis: Long) {
            timerJob?.cancel()



            timerJob = coroutineScope.timer(delayMillis) {
                val notification = NotificationCompat
                    .Builder(this@PlayerServiceModern, SleepTimerNotificationChannelId)
                    .setContentTitle(getString(R.string.sleep_timer_ended))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_launcher_monochrome)
                    .build()

                notificationManager?.notify(SleepTimerNotificationId, notification)

                stopSelf()
                exitProcess(0)
            }
        }

        fun cancelSleepTimer() {
            timerJob?.cancel()
            timerJob = null
        }

        private var radioJob: Job? = null

        var isLoadingRadio by mutableStateOf(false)
            private set

        /**
         * Contains 2 major steps:
         * 1. Fetch YouTube Music for **playlistId** of this song
         * 2. Use said **playlistId** to get more songs
         *
         * **_playlistId_** isn't the playlist this song belongs to,
         * but rather the "mood", "style", or "vibe" matches this song.
         */
        fun startRadio(
            mediaItem: MediaItem,
            append: Boolean = false,
            endpoint: NavigationEndpoint.Endpoint.Watch? = null
        ) {
            this.stopRadio()

            // Play song immediately while other songs are being loaded
            if( player.currentMediaItem?.mediaId != mediaItem.mediaId )
                player.forcePlay( mediaItem )

            // Prevent UI from freezing up while data is being fetched
            radioJob = coroutineScope.launch {
                isLoadingRadio = true

                NewInnertube.radio(
                    mediaItem.mediaId,
                    CURRENT_LOCALE,
                    endpoint?.playlistId ?: "RDAMVM${mediaItem.mediaId}",
                    endpoint?.params
                ).onSuccess { relatedSongs ->
                    CoroutineScope(Dispatchers.IO ).launch {
                        relatedSongs.fastForEach {
                            Database.upsert( it )
                        }
                    }

                    // Any call to [player] must happen on Main thread
                    val currentQueue = withContext( Dispatchers.Main ) {
                        player.mediaItems.fastMap( MediaItem::mediaId )
                    }

                    // Songs with the same id as provided [Song] should be removed.
                    // The song usually lives at the the first index, but this
                    // way is safer to implement, as it can live through changes in position.
                    relatedSongs.dropWhile { it.id == mediaItem.mediaId || it.id in currentQueue }
                                .fastMap( InnertubeSong::toMediaItem )
                                .also {
                                    // Any call to [player] must happen on Main thread
                                    withContext( Dispatchers.Main ) {
                                        /*
                                            There are 2 possible outcomes when append is not enabled.
                                            User starts radio on currently playing song,
                                            or on a completely different song.

                                            When radio is activated on the same song, remain position
                                            of currently playing song, delete next songs, and append
                                            it with new songs.

                                            When new song is used for radio, replace entire queue with new songs.
                                          */
                                        val curIndex = player.currentMediaItemIndex
                                        val endIndex = player.mediaItemCount
                                        if( !append && player.mediaItemCount > 1 ) {
                                            player.moveMediaItem( curIndex, 0 )
                                            player.removeMediaItems( curIndex + 1, endIndex )
                                        }

                                        player.addMediaItems(it)
                                    }
                                }
                }.onFailure {
                    it.printStackTrace()
                    it.message?.also( Toaster::e )
                }

                isLoadingRadio = false
            }
        }

        fun startRadio(
            song: Song,
            append: Boolean = false,
            endpoint: NavigationEndpoint.Endpoint.Watch? = null
        ) = startRadio( song.asMediaItem, append, endpoint )

        fun stopRadio() {
            isLoadingRadio = false
            radioJob?.cancel()
        }

        /**
         * Pause with fade out effect
         */
        @MainThread
        fun gracefulPause() {
            val duration by Preferences.AUDIO_FADE_DURATION
            player.fadeOutEffect( duration.asMillis )
        }

        /**
         * Start playing with fade in effect
         */
        @MainThread
        fun gracefulPlay() {
            val duration by Preferences.AUDIO_FADE_DURATION
            player.fadeInEffect( duration.asMillis )
        }

        /**
         * This method should ONLY be called when the application (sc. activity) is in the foreground!
         */
        fun restartForegroundOrStop() {
            player.pause()
            stopSelf()
        }

        @kotlin.OptIn(FlowPreview::class)
        fun toggleLike() {
            Database.asyncTransaction {
                currentSong.value?.let {
                    songTable.rotateLikeState( it.id )
                }.also {
                    currentSong.debounce(1000).collect(coroutineScope) { updateDefaultNotification() }
                }
            }

            currentSong.value
                ?.let { MyDownloadHelper.autoDownloadWhenLiked(this@PlayerServiceModern, it.asMediaItem) }
        }

        fun toggleDownload() {
            println("PlayerServiceModern toggleDownload currentMediaItem ${currentMediaItem.value} currentSongIsDownloaded ${currentSongStateDownload.value}")
            manageDownload(
                context = this@PlayerServiceModern,
                mediaItem = currentMediaItem.value ?: return,
                downloadState = currentSongStateDownload.value == Download.STATE_COMPLETED
            )
        }

        fun toggleRepeat() {
            player.toggleRepeatMode()
            updateDefaultNotification()
        }

        fun toggleShuffle() {
            player.toggleShuffleMode()
            updateDefaultNotification()
        }

        fun actionSearch() {
            startActivity(Intent(applicationContext, MainActivity::class.java)
                .setAction(MainActivity.action_search)
                .setFlags(FLAG_ACTIVITY_NEW_TASK + FLAG_ACTIVITY_CLEAR_TASK))
            println("PlayerServiceModern actionSearch")
        }
    }

    @JvmInline
    value class Action(val value: String) {
        val pendingIntent: PendingIntent
            get() = PendingIntent.getBroadcast(
                appContext(),
                100,
                Intent(value).setPackage(appContext().packageName),
                PendingIntent.FLAG_UPDATE_CURRENT.or(if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0)
            )

        companion object {

            val pause = Action("it.fast4x.rimusic.pause")
            val play = Action("it.fast4x.rimusic.play")
            val next = Action("it.fast4x.rimusic.next")
            val previous = Action("it.fast4x.rimusic.previous")
            val like = Action("it.fast4x.rimusic.like")
            val download = Action("it.fast4x.rimusic.download")
            val playradio = Action("it.fast4x.rimusic.playradio")
            val shuffle = Action("it.fast4x.rimusic.shuffle")
            val search = Action("it.fast4x.rimusic.search")
            val repeat = Action("it.fast4x.rimusic.repeat")

        }
    }

    companion object {
        const val NotificationId = 1001
        const val NotificationChannelId = "default_channel_id"

        const val SleepTimerNotificationId = 1002
        const val SleepTimerNotificationChannelId = "sleep_timer_channel_id"

        val PlayerErrorsToReload = arrayOf(416, 4003)
        val PlayerErrorsToSkip = arrayOf(2000)

        const val ROOT = "root"
        const val SONG = "song"
        const val ARTIST = "artist"
        const val ALBUM = "album"
        const val PLAYLIST = "playlist"
        const val SEARCHED = "searched"

        const val CACHE_DIRNAME = "exo_cache"
    }

}