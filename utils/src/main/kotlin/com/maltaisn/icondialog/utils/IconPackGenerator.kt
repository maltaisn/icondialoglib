/*
 * Copyright 2020 Nicolas Maltais
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

package com.maltaisn.icondialog.utils

import com.maltaisn.icondialog.utils.XmlElement.XmlTag
import java.io.File
import java.text.Normalizer
import java.util.*


abstract class IconPackGenerator(val outputDir: File, val iconSize: Int) {

    protected var iconPack = mutableMapOf<Category, MutableList<Icon>>()

    /**
     * Create `icons.xml`, `strings.xml` and `tags.xml` files for an [iconPack].
     * Files are generated in the [outputDir] directory.
     */
    fun createFiles() {
        println("Exporting XML files")
        createIconsXml()
        createStringsXml()
        createTagsXml()
    }

    private fun createIconsXml() {
        val xml = XmlTag("icons", "width" to iconSize, "height" to iconSize) {
            for ((catg, catgIcons) in iconPack) {
                // Create icon tags
                val iconTags = mutableListOf<XmlTag>()
                for (icon in catgIcons) {
                    val tags = if (icon.tags.isEmpty()) null else icon.tags.joinToString(",") { it.name }
                    val width = if (icon.width == iconSize) null else icon.width
                    val height = if (icon.height == iconSize) null else icon.height
                    iconTags += XmlTag("icon", "id" to icon.id,
                            "tags" to tags, "width" to width, "height" to height,
                            "path" to icon.pathData)
                }
                if (catg !== NO_CATEGORY) {
                    // Icons have a category, wrap them.
                    tag("category", "id" to catg.id,
                            "name" to "@string/catg_${catg.name}") {
                        children += iconTags
                    }
                } else {
                    // Icons have no category, add them directly.
                    children += iconTags
                }
            }
        }
        File(outputDir, ICONS_XML).writeText(xml.toXml())
    }

    private fun createStringsXml() {
        val categories = iconPack.keys.sortedBy { it.id }
        val xml = XmlTag("resources") {
            for (catg in categories) {
                tag("string", "name" to "catg_${catg.name}") {
                    text(catg.nameValue)
                }
            }
        }
        File(outputDir, STRINGS_XML).writeText(xml.toXml())
    }

    private fun createTagsXml() {
        // Find all tags
        val tags = linkedSetOf<Tag>()
        for (catgIcons in iconPack.values) {
            for (icon in catgIcons) {
                tags += icon.tags
            }
        }

        // Create and export XML
        val xml = XmlTag("tags") {
            for (tag in tags.sorted()) {
                tag("tag", "name" to tag.name) {
                    val values = tag.values
                    if (values.size == 1) {
                        // Single value
                        text(values.first())
                    } else if (values.size > 1) {
                        // Use aliases
                        for (value in values) {
                            tag("alias") {
                                text(value)
                            }
                        }
                    }
                }
            }
        }
        File(outputDir, TAGS_XML).writeText(xml.toXml())
    }

    data class Category(var id: Int,
                        val name: String,
                        val nameValue: String)

    data class Icon(val id: Int,
                    val tags: MutableList<Tag>,
                    val pathData: String,
                    val width: Int,
                    val height: Int)

    data class Tag(val name: String, val values: List<String>) : Comparable<Tag> {
        override fun compareTo(other: Tag) = name.compareTo(other.name)
    }

    /**
     * Normalize [this] string to make it a valid icon pack name for a category or a tag. All diacritics,
     * all unicode characters, hyphens, apostrophes are removed. Common word separators are replaced with
     * underscores. Resulting text has only lowercase latin letters, digits and underscores.
     * Leading and trailing underscores are also removed.
     */
    fun String.normalizeName(): String {
        val parts = split('-', '_', ' ', ',', ';', '&', '+', '.')
                .filter { it.isNotEmpty() }.toMutableList()
        for ((i, part) in parts.withIndex()) {
            var normalized = part.toLowerCase(Locale.ROOT).trim()
            normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD)
            parts[i] = normalized.replace("[^a-z0-9]".toRegex(), "")
        }
        return parts.joinToString("_")
    }

    /**
     * Normalize [this] string used as a tag value.
     * - Only lowercase letters and digits are kept.
     * - String is trimmed, all whitespace sequences are converted to a single space.
     * - "'s" suffix is removed.
     * - First letter is capitalized.
     */
    fun String.normalizeValue(): String {
        var normalized = this.toLowerCase(Locale.ROOT).trim()
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD)
        normalized = normalized.replace("""\s+""".toRegex(), " ")

        val sb = StringBuilder()
        for (c in normalized) {
            if (c == ' ' || c == '\'' || c == '-' || c in 'a'..'z' || c in '0'..'9') {
                sb.append(c)
            }
        }
        if (sb.isNotEmpty()) {
            sb[0] = sb[0].toUpperCase()
        }

        return sb.toString()
    }

    companion object {
        private const val ICONS_XML = "icons.xml"
        private const val STRINGS_XML = "strings.xml"
        private const val TAGS_XML = "tags.xml"

        val NO_CATEGORY = Category(-1, "", "")
    }
}
