package app.kreate.android.themed.common.component.settings.data

import androidx.compose.runtime.getValue
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import app.kreate.android.Preferences

@UnstableApi
class ExoCacheIndicator(
    private val preference: Preferences.Long,
    private val cache: Cache
): CacheUsageIndicator() {

    override fun updateProgress() {
        val maxSize by preference
        super.progress = if( maxSize == 0L )
            0f
        else
            cache.cacheSpace.toFloat() / maxSize
    }

    override fun onConfirm() {
        cache.keys.forEach( cache::removeResource )
        updateProgress()
        hideDialog()
    }
}