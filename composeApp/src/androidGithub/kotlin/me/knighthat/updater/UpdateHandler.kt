package me.knighthat.updater

import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import app.kreate.android.BuildConfig
import app.kreate.android.Preferences
import it.fast4x.rimusic.enums.CheckUpdateState
import java.io.File

@Composable
fun UpdateHandler() {
    DownloadAndInstallDialog.Render()
    NewUpdatePrompt.Render()

    val check4UpdateState by Preferences.CHECK_UPDATE
    LaunchedEffect( check4UpdateState ) {
        if( check4UpdateState != CheckUpdateState.DISABLED )
            Updater.checkForUpdate()
    }

    val context = LocalContext.current
    LaunchedEffect( Unit ) {
        val apkFile = File(
            context.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ),
            "Kreate-${BuildConfig.BUILD_TYPE}.apk"
        )
        if( apkFile.exists() && !BuildConfig.DEBUG )
            apkFile.delete()
    }
}