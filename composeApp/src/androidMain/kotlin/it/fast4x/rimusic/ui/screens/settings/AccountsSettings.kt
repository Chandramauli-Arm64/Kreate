package it.fast4x.rimusic.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import io.ktor.http.Url
import it.fast4x.compose.persist.persistList
import it.fast4x.piped.Piped
import it.fast4x.piped.models.Instance
import it.fast4x.rimusic.R
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.extensions.discord.DiscordLoginAndGetToken
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.*
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("BatteryLife")
@ExperimentalAnimationApi
@Composable
fun AccountsSettings() {
    val context = LocalContext.current
    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var restartActivity by rememberPreference(restartActivityKey, false)

    Column(
        modifier = Modifier
            .background(colorPalette().background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (NavigationBarPosition.Right.isCurrent())
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
            title = stringResource(R.string.tab_accounts),
            iconId = R.drawable.person,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        // rememberEncryptedPreference only works correct with API 24 and up

        //TODO MANAGE LOGIN
        /****** YOUTUBE LOGIN ******/
/*
        var isYouTubeLoginEnabled by rememberPreference(enableYouTubeLoginKey, false)
        var loginYouTube by remember { mutableStateOf(false) }
        var visitorData by rememberEncryptedPreference(key = ytVisitorDataKey, defaultValue = "")
        var cookie by rememberEncryptedPreference(key = ytCookieKey, defaultValue = "")
        var accountName by rememberEncryptedPreference(key = ytAccountNameKey, defaultValue = "")
        var accountEmail by rememberEncryptedPreference(key = ytAccountEmailKey, defaultValue = "")
        var accountChannelHandle by rememberEncryptedPreference(
            key = ytAccountChannelHandleKey,
            defaultValue = ""
        )
        val isLoggedIn = remember(cookie) {
            "SAPISID" in parseCookieString(cookie)
        }
        //if (!isLoggedIn) isYouTubeLoginEnabled = false // disable if not logged in



        SettingsGroupSpacer()
        SettingsEntryGroupText(title = "YOUTUBE MUSIC")

        SwitchSettingEntry(
            title = "Enable YouTube Music Login",
            text = "",
            isChecked = isYouTubeLoginEnabled,
            onCheckedChange = {
                isYouTubeLoginEnabled = it
                if (!it) {
                    visitorData = ""
                    cookie = ""
                    accountName = ""
                    accountChannelHandle = ""
                    accountEmail = ""
                }
            }
        )

        AnimatedVisibility(visible = isYouTubeLoginEnabled) {
            Column(
                modifier = Modifier.padding(start = 25.dp)
            ) {
                if (isAtLeastAndroid7) {

                    Column {
                        ButtonBarSettingEntry(
                            isEnabled = true,
                            title = if (isLoggedIn) "Disconnect" else "Connect",
                            text = if (isLoggedIn) "$accountName ${accountChannelHandle}" else "",
                            icon = R.drawable.logo_youtube,
                            iconColor = colorPalette().text,
                            onClick = {
                                if (isLoggedIn) {
                                    cookie = ""
                                    accountName = ""
                                    accountChannelHandle = ""
                                    accountEmail = ""
                                    visitorData = ""
                                    loginYouTube = false
                                } else
                                    loginYouTube = true
                            }
                        )
                        /*
                        ImportantSettingsDescription(
                            text = "You need to log in to listen the songs online"
                        )
                         */
                        SettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))

                        CustomModalBottomSheet(
                            showSheet = loginYouTube,
                            onDismissRequest = {
                                SmartMessage(
                                    "Restart RiMusic, please",
                                    type = PopupType.Info,
                                    context = context
                                )
                                loginYouTube = false
                                restartActivity = !restartActivity
                            },
                            containerColor = colorPalette().background0,
                            contentColor = colorPalette().background0,
                            modifier = Modifier.fillMaxWidth(),
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            dragHandle = {
                                Surface(
                                    modifier = Modifier.padding(vertical = 0.dp),
                                    color = colorPalette().background0,
                                    shape = thumbnailShape()
                                ) {}
                            },
                            shape = thumbnailRoundness.shape()
                        ) {
                            YouTubeLogin(
                                onLogin = { success ->
                                    if (success) {
                                        loginYouTube = false
                                        SmartMessage(
                                            "Login successful, restart RiMusic",
                                            type = PopupType.Info,
                                            context = context
                                        )
                                        restartActivity = !restartActivity
                                    }
                                }
                            )
                        }
                    }

                }
            }
        }
*/
        /****** YOUTUBE LOGIN ******/

    /****** PIPED ******/

    // rememberEncryptedPreference only works correct with API 24 and up
    if (isAtLeastAndroid7) {
        var isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
        var isPipedCustomEnabled by rememberPreference(isPipedCustomEnabledKey, false)
        var pipedUsername by rememberEncryptedPreference(pipedUsernameKey, "")
        var pipedPassword by rememberEncryptedPreference(pipedPasswordKey, "")
        var pipedInstanceName by rememberEncryptedPreference(pipedInstanceNameKey, "")
        var pipedApiBaseUrl by rememberEncryptedPreference(pipedApiBaseUrlKey, "")
        var pipedApiToken by rememberEncryptedPreference(pipedApiTokenKey, "")

        var loadInstances by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var instances by persistList<Instance>(tag = "otherSettings/pipedInstances")
        var noInstances by remember { mutableStateOf(false) }
        var executeLogin by remember { mutableStateOf(false) }
        var showInstances by remember { mutableStateOf(false) }
        var session by remember {
            mutableStateOf<Result<it.fast4x.piped.models.Session>?>(
                null
            )
        }
        println("OtherSettings bookmark second")

        val menuState = LocalMenuState.current
        val coroutineScope = rememberCoroutineScope()

        if (isLoading)
            DefaultDialog(
                onDismiss = {
                    isLoading = false
                }
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

        if (loadInstances) {
            LaunchedEffect(Unit) {
                isLoading = true
                Piped.getInstances()?.getOrNull()?.let {
                    instances = it
                    //println("mediaItem Instances $it")
                } ?: run { noInstances = true }
                isLoading = false
                showInstances = true
            }
        }
        if (noInstances) {
            SmartMessage("No instances found", type = PopupType.Info, context = context)
        }

        if (executeLogin) {
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    isLoading = true
                    session = Piped.login(
                        apiBaseUrl = Url(pipedApiBaseUrl), //instances[instanceSelected!!].apiBaseUrl,
                        username = pipedUsername,
                        password = pipedPassword
                    )?.onFailure {
                        Timber.e("Failed piped login ${it.stackTraceToString()}")
                        isLoading = false
                        SmartMessage(
                            "Piped login failed",
                            type = PopupType.Error,
                            context = context
                        )
                        loadInstances = false
                        session = null
                        executeLogin = false
                    }
                    if (session?.isSuccess == false)
                        return@launch

                    SmartMessage(
                        "Piped login successful",
                        type = PopupType.Success,
                        context = context
                    )
                    Timber.i("Piped login successful")

                    session.let {
                        it?.getOrNull()?.token?.let { it1 ->
                            pipedApiToken = it1
                            pipedApiBaseUrl = it.getOrNull()!!.apiBaseUrl.toString()
                        }
                    }

                    isLoading = false
                    loadInstances = false
                    executeLogin = false
                }
            }
        }

        if (showInstances && instances.isNotEmpty()) {
            menuState.display {
                Menu {
                    MenuEntry(
                        icon = R.drawable.chevron_back,
                        text = stringResource(R.string.cancel),
                        onClick = {
                            loadInstances = false
                            showInstances = false
                            menuState.hide()
                        }
                    )
                    instances.forEach {
                        MenuEntry(
                            icon = R.drawable.server,
                            text = it.name,
                            secondaryText = "${it.locationsFormatted} Users: ${it.userCount}",
                            onClick = {
                                menuState.hide()
                                pipedApiBaseUrl = it.apiBaseUrl.toString()
                                pipedInstanceName = it.name
                                /*
                                instances.indexOf(it).let { index ->
                                    //instances[index].apiBaseUrl
                                    instanceSelected = index
                                    //println("mediaItem Instance ${instances[index].apiBaseUrl}")
                                }
                                 */
                                loadInstances = false
                                showInstances = false
                            }
                        )
                    }
                    MenuEntry(
                        icon = R.drawable.chevron_back,
                        text = stringResource(R.string.cancel),
                        onClick = {
                            loadInstances = false
                            showInstances = false
                            menuState.hide()
                        }
                    )
                }
            }
        }


        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.piped_account))
        SwitchSettingEntry(
            title = stringResource(R.string.enable_piped_syncronization),
            text = "",
            isChecked = isPipedEnabled,
            onCheckedChange = { isPipedEnabled = it }
        )

        AnimatedVisibility(visible = isPipedEnabled) {
            Column(
                modifier = Modifier.padding(start = 25.dp)
            ) {
                SwitchSettingEntry(
                    title = stringResource(R.string.piped_custom_instance),
                    text = "",
                    isChecked = isPipedCustomEnabled,
                    onCheckedChange = { isPipedCustomEnabled = it }
                )
                AnimatedVisibility(visible = isPipedCustomEnabled) {
                    Column {
                        TextDialogSettingEntry(
                            title = stringResource(R.string.piped_custom_instance),
                            text = pipedApiBaseUrl,
                            currentText = pipedApiBaseUrl,
                            onTextSave = {
                                pipedApiBaseUrl = it
                            }
                        )
                    }
                }
                AnimatedVisibility(visible = !isPipedCustomEnabled) {
                    Column {
                        ButtonBarSettingEntry(
                            //isEnabled = pipedApiToken.isEmpty(),
                            title = stringResource(R.string.piped_change_instance),
                            text = pipedInstanceName,
                            icon = R.drawable.open,
                            onClick = {
                                loadInstances = true
                            }
                        )
                    }
                }

                TextDialogSettingEntry(
                    //isEnabled = pipedApiToken.isEmpty(),
                    title = stringResource(R.string.piped_username),
                    text = pipedUsername,
                    currentText = pipedUsername,
                    onTextSave = { pipedUsername = it }
                )
                TextDialogSettingEntry(
                    //isEnabled = pipedApiToken.isEmpty(),
                    title = stringResource(R.string.piped_password),
                    text = if (pipedPassword.isNotEmpty()) "********" else "",
                    currentText = pipedPassword,
                    onTextSave = { pipedPassword = it },
                    modifier = Modifier
                        .semantics {
                            password()
                        }
                )

                ButtonBarSettingEntry(
                    isEnabled = pipedPassword.isNotEmpty() && pipedUsername.isNotEmpty() && pipedApiBaseUrl.isNotEmpty(),
                    title = if (pipedApiToken.isNotEmpty()) stringResource(R.string.piped_disconnect) else stringResource(
                        R.string.piped_connect
                    ),
                    text = if (pipedApiToken.isNotEmpty()) stringResource(R.string.piped_connected_to_s).format(
                        pipedInstanceName
                    ) else "",
                    icon = R.drawable.piped_logo,
                    iconColor = colorPalette().red,
                    onClick = {
                        if (pipedApiToken.isNotEmpty()) {
                            pipedApiToken = ""
                            executeLogin = false
                        } else executeLogin = true
                    }
                )

            }
        }
    }

    /****** PIPED ******/

    /****** DISCORD ******/

    // rememberEncryptedPreference only works correct with API 24 and up
    if (isAtLeastAndroid7) {
        var isDiscordPresenceEnabled by rememberPreference(isDiscordPresenceEnabledKey, false)
        var loginDiscord by remember { mutableStateOf(false) }
        var discordPersonalAccessToken by rememberEncryptedPreference(
            key = discordPersonalAccessTokenKey,
            defaultValue = ""
        )
        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.social_discord))
        SwitchSettingEntry(
            isEnabled = isAtLeastAndroid81,
            title = stringResource(R.string.discord_enable_rich_presence),
            text = "",
            isChecked = isDiscordPresenceEnabled,
            onCheckedChange = { isDiscordPresenceEnabled = it }
        )

        AnimatedVisibility(visible = isDiscordPresenceEnabled) {
            Column {
                ButtonBarSettingEntry(
                    isEnabled = true,
                    title = if (discordPersonalAccessToken.isNotEmpty()) stringResource(R.string.discord_disconnect) else stringResource(
                        R.string.discord_connect
                    ),
                    text = if (discordPersonalAccessToken.isNotEmpty()) stringResource(R.string.discord_connected_to_discord_account) else "",
                    icon = R.drawable.logo_discord,
                    iconColor = colorPalette().text,
                    onClick = {
                        if (discordPersonalAccessToken.isNotEmpty())
                            discordPersonalAccessToken = ""
                        else
                            loginDiscord = true
                    }
                )

                CustomModalBottomSheet(
                    showSheet = loginDiscord,
                    onDismissRequest = {
                        loginDiscord = false
                    },
                    containerColor = colorPalette().background0,
                    contentColor = colorPalette().background0,
                    modifier = Modifier.fillMaxWidth(),
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    dragHandle = {
                        Surface(
                            modifier = Modifier.padding(vertical = 0.dp),
                            color = colorPalette().background0,
                            shape = thumbnailShape()
                        ) {}
                    },
                    shape = thumbnailRoundness.shape
                ) {
                    DiscordLoginAndGetToken(
                        rememberNavController(),
                        onGetToken = { token ->
                            loginDiscord = false
                            discordPersonalAccessToken = token
                            SmartMessage(token, type = PopupType.Info, context = context)
                        }
                    )
                }
            }
        }
    }

    /****** DISCORD ******/



    }
}


