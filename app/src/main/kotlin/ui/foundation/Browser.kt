package org.keizar.android.ui.foundation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import me.him188.ani.utils.logging.logger

object AndroidBrowserNavigator {
    private val logger = logger(this::class)

    fun openBrowser(context: Context, url: String) {
        runCatching {
            launchChromeTab(context, url)
        }.recoverCatching {
            launchIntent(url, context)
        }.onFailure {
            logger.warn("Failed to open tab", it)
        }
    }

    private fun launchChromeTab(context: Context, url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    }

    private fun launchIntent(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            setData(Uri.parse(url))
        }
        context.startActivity(browserIntent)
    }
}