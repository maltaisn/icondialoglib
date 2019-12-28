/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.icondialog.filter

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.NamedTag
import com.maltaisn.icondialog.normalize
import com.maltaisn.icondialog.pack.IconPack
import java.util.*
import kotlin.math.min


/**
 * Default icon searcher used by the [IconDialogSettings].
 */
open class DefaultIconFilter : IconFilter {

    /**
     * Regex used to split the query into multiple search terms.
     * Can also be null to not split the query.
     */
    var termSplitPattern: Regex? = """[;,\s]""".toRegex()

    /**
     * Whether to normalize search query or not, using [String.normalize].
     */
    var queryNormalized = true

    /**
     * Whether to allow search by ID with a query starting with `#`.
     */
    var idSearchEnabled = false

    /**
     * Get a list of all matching icons for a search [query].
     * Base implementation only returns the complete list of icons in the pack,
     * sorted by ID. Subclasses take care of actual searching and must always ensure
     * that the returned list is sorted by ID.
     */
    override fun queryIcons(pack: IconPack, query: String?): MutableList<Icon> {
        // If query starts with #, try to get icon by ID.
        if (idSearchEnabled && query != null && query.startsWith("#")) {
            val id = query.substring(1).toIntOrNull()
            if (id != null) {
                val icon = pack.getIcon(id)
                if (icon != null) {
                    return mutableListOf(icon)
                }
            }
        }

        val icons = pack.allIcons

        if (query == null || query.isBlank()) {
            // No search query, return all icons.
            return icons
        }

        // Split query into terms.
        val terms = if (termSplitPattern == null) {
            listOf(query)
        } else {
            query.split(termSplitPattern!!)
        }.mapNotNull {
            val term = if (queryNormalized) {
                it.normalize()
            } else {
                it.toLowerCase(Locale.ROOT)
            }
            if (term.isBlank()) null else term
        }

        // Remove all icons that don't match any of the search terms.
        for (i in icons.indices.reversed()) {
            var matches = false
            val icon = icons[i]
            for (tagName in icon.tags) {
                val tag = pack.getTag(tagName) as? NamedTag ?: continue
                if (tag.values.any { matchesSearch(it, terms) }) {
                    matches = true
                    break
                }
            }
            if (!matches) {
                icons.removeAt(i)
            }
        }

        return icons
    }


    /**
     * Check if a [tag] value matches any of the search [terms].
     */
    protected open fun matchesSearch(tag: NamedTag.Value, terms: List<String>): Boolean {
        val text: String = if (queryNormalized) tag.normValue else tag.value
        return terms.any { it in text }
    }


    /**
     * Compare two icons.
     * By default, icon are first sorted by tag names, alphabetically. If icons have the same first
     * tags, the one with less tags is put first. If the icons have the same number of tags,
     * they are sorted by ascending ID.
     */
    override fun compare(icon1: Icon, icon2: Icon): Int {
        // Compare tags
        val len1: Int = icon1.tags.size
        val len2: Int = icon2.tags.size
        for (i in 0 until min(len1, len2)) {
            val tag1 = icon1.tags.getOrNull(i)
            val tag2 = icon2.tags.getOrNull(i)
            if (tag1 == null || tag2 == null) continue
            val result = tag1.compareTo(tag2)
            if (result != 0) {
                return result
            }
        }

        return if (len1 != len2) {
            // Compare number of tags
            len1.compareTo(len2)
        } else {
            // Compare ID
            icon1.id.compareTo(icon2.id)
        }
    }


    // Parcelable stuff
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val bundle = Bundle()
        bundle.putString("termSplitPattern", termSplitPattern?.pattern)
        bundle.putBoolean("queryNormalized", queryNormalized)
        bundle.putBoolean("idSearchEnabled", idSearchEnabled)
        parcel.writeBundle(bundle)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DefaultIconFilter> {
            override fun createFromParcel(parcel: Parcel) = DefaultIconFilter().apply {
                val bundle = parcel.readBundle(DefaultIconFilter::class.java.classLoader)!!
                termSplitPattern = bundle.getString("termSplitPattern")?.toRegex()
                queryNormalized = bundle.getBoolean("queryNormalized")
                idSearchEnabled = bundle.getBoolean("idSearchEnabled")

            }

            override fun newArray(size: Int) = arrayOfNulls<DefaultIconFilter>(size)
        }
    }

}
