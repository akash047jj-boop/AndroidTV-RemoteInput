package com.akash.androidtvremoteinput

import android.content.Context
import android.media.tv.TvInputManager

data class TvInputItem(val id: String, val label: String, val passthrough: Boolean, val type: Int)

object InputCatalog {
    const val HOME_ID = "__HOME__"

    fun get(context: Context): List<TvInputItem> {
        val manager = context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
            ?: return emptyList()
        return manager.tvInputList.map { info ->
            TvInputItem(
                info.id,
                info.loadLabel(context)?.toString()?.ifBlank { info.id } ?: info.id,
                info.isPassthroughInput,
                info.type
            )
        }.sortedBy { it.label.lowercase() }
    }
}