package com.akash.androidtvremoteinput

import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo

object InputSwitcher {
    fun switchTo(context: Context, inputId: String?): Boolean {
        if (inputId.isNullOrBlank()) return false

        if (inputId == InputCatalog.HOME_ID) {
            return startSafely(context, Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        val input = InputCatalog.get(context).firstOrNull { it.id == inputId } ?: return false

        if (input.passthrough) {
            val uri = TvContract.buildChannelUriForPassthroughInput(input.id)
            if (startSafely(context, Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })) return true
        }

        val internal = Intent("android.media.tv.action.VIEW_INPUT").apply {
            putExtra(TvInputInfo.EXTRA_INPUT_ID, input.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (startSafely(context, internal)) return true

        val channels = Intent(
            Intent.ACTION_VIEW,
            TvContract.buildChannelsUriForInput(input.id)
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return startSafely(context, channels)
    }

    private fun startSafely(context: Context, intent: Intent): Boolean =
        try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
}