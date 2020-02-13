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

package com.maltaisn.icondialog.pack

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.annotation.XmlRes
import com.maltaisn.icondialog.data.*
import com.maltaisn.icondialog.normalize
import org.xmlpull.v1.XmlPullParser
import java.util.*


/**
 * Class for loading icon packs from XML resources.
 * @param context Any context, needed to load the XML resources.
 */
class IconPackLoader(context: Context) {

    private val context = context.applicationContext

    var drawableLoader = IconDrawableLoader(context)
        internal set

    /**
     * Load an icon pack from XML resources for icons and tags.
     *
     * @param iconsXml XML resource containing the icons
     * @param tagsXml XML resource containing the tags, can be `0` if there aren't tags.
     * @param locales List of locales supported by the icon pack, can be empty if there are no tags.
     * @param parent Parent pack for inheriting data, can be `null` for none.
     *
     * @throws IconPackParseException Thrown when icons or tags XML is invalid.
     */
    fun load(@XmlRes iconsXml: Int, @XmlRes tagsXml: Int = 0,
             locales: List<Locale> = emptyList(), parent: IconPack? = null): IconPack {
        val pack = IconPack(parent = parent, locales = locales, tagsXml = tagsXml)
        loadIcons(pack, iconsXml)
        loadTags(pack)
        return pack
    }

    /**
     * Reload the tag values of an icon [pack] and its parents, as
     * well as category names. This must be called whenever the application language changes.
     * A `BroadcastListener` should be attached to listen for this event.
     *
     * Note that since [Category] and [NamedTag] are immutable, this will change all instances.
     *
     * @throws IconPackParseException Thrown when tags XML is invalid.
     */
    fun reloadStrings(pack: IconPack) {
        if (pack.parent != null) {
            reloadStrings(pack.parent)
        }

        // Clear and load tags
        pack.tags.clear()
        loadTags(pack)

        // Reload category names
        for ((id, category) in pack.categories) {
            if (category.nameRes != 0) {
                pack.categories[id] = category.copy(name = context.getString(category.nameRes))
            }
        }
    }


    private fun loadIcons(pack: IconPack, @XmlRes iconsXml: Int) {
        val newIcons = mutableMapOf<Int, Icon>()
        val newCategories = mutableMapOf<Int, Category>()
        var categoryId: Int = -1

        var documentStarted = false
        var iconStarted = false

        var packWidth = 0
        var packHeight = 0

        val parser = context.resources.getXml(iconsXml)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val element = parser.name
            if (eventType == XmlPullParser.START_TAG) {
                if (element == XML_TAG_ICONS) {
                    documentStarted = true
                    packWidth = parser.getPositiveInt(XML_ATTR_ICON_WIDTH) { "Invalid global icon width '$it'." } ?: 24
                    packHeight = parser.getPositiveInt(XML_ATTR_ICON_HEIGHT) { "Invalid global icon height '$it'." } ?: 24

                } else {
                    if (!documentStarted) parseError("Invalid root element <$element>.")
                    if (iconStarted) parseError("Icon element cannot have body.")

                    when (element) {
                        XML_TAG_CATEGORY -> {
                            if (categoryId != -1) parseError("Nested category element is not allowed.")
                            val category = parseCategory(parser, pack)
                            categoryId = category.id
                            if (categoryId in newCategories) {
                                parseError("Duplicate category ID '$categoryId' in same file.")
                            }
                            newCategories[categoryId] = category
                        }
                        XML_TAG_ICON -> {
                            val icon = parseIcon(parser, pack, categoryId, packWidth, packHeight)
                            if (icon.id in newIcons) {
                                parseError("Duplicate icon ID '${icon.id}' in same file.")
                            }
                            newIcons[icon.id] = icon
                            iconStarted = true
                        }
                        else -> parseError("Unknown element <$element>.")
                    }
                }

            } else if (eventType == XmlPullParser.END_TAG) {
                if (element == XML_TAG_CATEGORY) {
                    categoryId = -1
                } else if (element == XML_TAG_ICON) {
                    iconStarted = false
                }
            }
            eventType = parser.next()
        }

        // Add new elements
        pack.icons += newIcons
        pack.categories += newCategories
    }

    private fun parseCategory(parser: XmlResourceParser, pack: IconPack): Category {
        val id = parser.getPositiveInt(XML_ATTR_CATG_ID) { "Invalid category ID '$it'." }
                ?: parseError("Category must have an ID.")

        val nameRes: Int
        val name: String
        val nameStr = parser.getAttributeValue(null, XML_ATTR_CATG_NAME)
        if (nameStr != null) {
            if (nameStr.startsWith('@')) {
                nameRes = if (nameStr.startsWith("@string/")) {
                    // There's an AAPT bug where the string reference isn't changed to an ID
                    // in XML resources. Resolve string resource from name.
                    // See https://github.com/maltaisn/icondialoglib/issues/13.
                    context.resources.getIdentifier(
                            nameStr.substring(8), "string", context.packageName)
                } else {
                    nameStr.substring(1).toIntOrNull() ?: 0
                }
                name = context.getString(nameRes)
            } else {
                // No string resource, hardcoded string name.
                nameRes = 0
                name = nameStr
            }
        } else {
            // Check if name can be inherited from overriden category.
            val overriden = pack.getCategory(id)
            if (overriden != null) {
                nameRes = overriden.nameRes
                name = overriden.name
            } else {
                parseError("Missing name for category ID $id.")
            }
        }

        return Category(id, name, nameRes)
    }

    private fun parseIcon(parser: XmlResourceParser, pack: IconPack,
                          categoryId: Int, packWidth: Int, packHeight: Int): Icon {
        val id = parser.getPositiveInt(XML_ATTR_ICON_ID) { "Invalid icon ID '$it'." }
                ?: parseError("Icon must have an ID.")
        val overriden = pack.getIcon(id)

        val catgId = parser.getPositiveInt(XML_ATTR_ICON_CATG) { "Invalid icon category ID '$it'." }
                ?: overriden?.categoryId ?: categoryId

        val tagsStr = parser.getAttributeValue(null, XML_ATTR_ICON_TAGS)
        val tags: List<String>
        if (tagsStr != null) {
            tags = tagsStr.split(',')

            // Add any grouping tags to the pack.
            for (tag in tags) {
                if (tag.startsWith('_')) {
                    pack.tags[tag] = GroupingTag(tag)
                }
            }
        } else {
            // Check if tags can be inherited from overriden icon.
            tags = overriden?.tags ?: emptyList()
        }

        val pathStr = parser.getAttributeValue(null, XML_ATTR_ICON_PATH)
        val pathData = pathStr ?: overriden?.pathData ?: ""
        val drawableResId = parser.getAttributeValue(null, XML_ATTR_ICON_DRAWABLE_RES_ID)
                ?.let { parseIconDrawableId(it) } ?: overriden?.drawableResId

        if (pathData.isBlank() && drawableResId == null)
            parseError("Icon ID $id has no path data and no valid drawableResId")

        val width = parser.getPositiveInt(XML_ATTR_ICON_WIDTH) { "Invalid icon width '$it'." } ?: packWidth
        val height = parser.getPositiveInt(XML_ATTR_ICON_HEIGHT) { "Invalid icon height '$it'." } ?: packHeight

        return Icon(id, catgId, tags, pathData, width, height, drawableResId)
    }

    private fun parseIconDrawableId(drawableIdString: String): Int? {
        return if (drawableIdString.startsWith('@')) {
            if (drawableIdString.startsWith("@drawable/")) {
                context.resources.getIdentifier(
                        drawableIdString.substring(10), "drawable", context.packageName)
            } else {
                drawableIdString.substring(1).toIntOrNull()
            }
        } else {
            null
        }
    }

    /**
     * Load tags of a [pack].
     */
    private fun loadTags(pack: IconPack) {
        if (pack.tagsXml == 0) {
            // This icon pack has no tags.
            return
        }

        val newTags = mutableMapOf<String, IconTag>()

        var tagName: String? = null
        val tagValues = mutableListOf<NamedTag.Value>()
        var singleValueTag = false

        val parser = context.resources.getXml(pack.tagsXml)
        var documentStarted = false
        var aliasStarted = false
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val element = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> if (element == XML_TAG_TAGS) {
                    documentStarted = true
                } else {
                    if (!documentStarted) {
                        parseError("Invalid root element <$element>.")
                    }
                    if (aliasStarted) {
                        parseError("Alias cannot have nested elements.")
                    }

                    if (element == XML_TAG_TAG) {
                        if (tagName != null) {
                            parseError("Nested tag element is not allowed.")
                        }

                        tagName = parser.getAttributeValue(null, XML_ATTR_TAG_NAME)
                                ?: parseError("Tag element has no name attribute.")
                        if (tagName.startsWith('_')) {
                            parseError("Grouping tag '$tagName' not allowed in labels XML.")
                        } else if (tagName in newTags) {
                            parseError("Duplicate tag '$tagName' in same file.")
                        }
                        singleValueTag = false

                    } else if (element == XML_TAG_ALIAS) {
                        if (tagName == null) {
                            parseError("Alias element must be in tag element body.")
                        }
                        if (singleValueTag) {
                            parseError("Tag cannot have both a value and aliases.")
                        }
                        aliasStarted = true

                    } else {
                        parseError("Unknown element <$element>.")
                    }
                }
                XmlPullParser.TEXT -> {
                    if (tagName != null) {
                        // Tag or alias value.
                        // Replace backtick used to imitate single quote since they cannot be escaped due to AAPT bug...
                        val text = parser.text.replace('`', '\'')
                        val value = NamedTag.Value(text, text.normalize())
                        when {
                            aliasStarted -> {
                                tagValues += value
                            }
                            tagValues.isEmpty() -> {
                                tagValues += value
                                singleValueTag = true
                            }
                            else -> {
                                parseError("Tag cannot have both a value and aliases.")
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (element == XML_TAG_TAG && tagName != null) {
                    // Add new tag
                    newTags[tagName] = NamedTag(tagName, tagValues.toList())
                    tagName = null
                    tagValues.clear()
                    singleValueTag = false

                } else if (element == XML_TAG_ALIAS) {
                    aliasStarted = false
                }
            }
            eventType = parser.next()
        }

        // Add new tags
        pack.tags += newTags
    }

    private inline fun XmlResourceParser.getPositiveInt(attr: String,
                                                        error: (String) -> String): Int? {
        val str = getAttributeValue(null, attr) ?: return null
        val value = str.toIntOrNull()
        if (value == null || value < 0) {
            parseError(error(str))
        }
        return value
    }


    companion object {
        // XML elements and attributes
        private const val XML_TAG_ICONS = "icons"
        private const val XML_TAG_TAGS = "tags"

        private const val XML_TAG_CATEGORY = "category"
        private const val XML_ATTR_CATG_ID = "id"
        private const val XML_ATTR_CATG_NAME = "name"

        private const val XML_TAG_ICON = "icon"
        private const val XML_ATTR_ICON_ID = "id"
        private const val XML_ATTR_ICON_CATG = "category"
        private const val XML_ATTR_ICON_TAGS = "tags"
        private const val XML_ATTR_ICON_PATH = "path"
        private const val XML_ATTR_ICON_WIDTH = "width"
        private const val XML_ATTR_ICON_HEIGHT = "height"
        private const val XML_ATTR_ICON_DRAWABLE_RES_ID = "drawableResId"

        private const val XML_TAG_TAG = "tag"
        private const val XML_TAG_ALIAS = "alias"
        private const val XML_ATTR_TAG_NAME = "name"
    }

}

class IconPackParseException(message: String) : Exception(message)

private fun parseError(message: String): Nothing = throw IconPackParseException(message)
