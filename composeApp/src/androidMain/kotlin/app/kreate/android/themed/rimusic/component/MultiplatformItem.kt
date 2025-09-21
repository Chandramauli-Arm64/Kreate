package app.kreate.android.themed.rimusic.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.kreate.android.R
import app.kreate.android.enums.PlatformIndicatorType

interface MultiplatformItem {

    val platformIndicatorType: PlatformIndicatorType

    @Composable
    fun BoxScope.PlatformIndicator() =
        when( platformIndicatorType ) {
            PlatformIndicatorType.DISABLED -> { /* Does nothing */ }
            PlatformIndicatorType.ICON -> {
                Image(
                    painter = painterResource( R.drawable.ytmusic ),
                    colorFilter = ColorFilter.tint(
                        Color.Red
                             .copy( 0.75f )
                             .compositeOver( Color.White )
                    ),
                    contentDescription = "YouTube\'s logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size( 40.dp )
                                       .padding( all = 5.dp )
                                       .align( Alignment.TopStart )
                )
            }
        }
}