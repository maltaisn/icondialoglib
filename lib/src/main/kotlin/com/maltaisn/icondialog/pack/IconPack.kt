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

import android.graphics.drawable.Drawable
import androidx.annotation.XmlRes
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.IconTag
import java.util.*


/**
 * An icon pack containing icons, categories and tags
 *
 * @property parent A pack can have a parent pack which will be used to resolve elements that aren't
 * found in this pack. This way, icons, categories and tags can be overriden by
 * a child pack. Parent is `null` if there's none.
 * @property locales List of locales supported by this icon pack tags. List can be empty if
 * there are no tags at all.
 * @property tagsXml XML resource containing the tags. Can be set to `0` if there are no tags.
 */
class IconPack(val parent: IconPack? = null,
               val icons: MutableMap<Int, Icon> = mutableMapOf(),
               val categories: MutableMap<Int, Category> = mutableMapOf(),
               val tags: MutableMap<String, IconTag> = mutableMapOf(),
               val locales: List<Locale> = emptyList(),
               @XmlRes val tagsXml: Int = 0) {

    /**
     * Create a new list containing all the icons from this pack and its parents,
     * but only those visible from this pack (i.e: overwritten icons not present).
     */
    val allIcons: MutableList<Icon>
        get() {
            val iconsMap = linkedMapOf<Int, Icon>()
            var currentPack: IconPack? = this
            while (currentPack != null) {
                for ((id, icon) in currentPack.icons) {
                    if (id !in iconsMap) {
                        iconsMap[id] = icon
                    }
                }
                currentPack = currentPack.parent
            }
            return iconsMap.values.toMutableList()
        }

    /**
     * Get an icon by [id] from the icon pack. If icon is not found,
     * parent packs are searched recursively. Returns `null` if not found.
     */
    fun getIcon(id: Int): Icon? = icons[id] ?: parent?.getIcon(id)

    /**
     * Get a category by [id] from the icon pack. If category is not found,
     * parent packs are searched recursively. Returns `null` if not found.
     */
    fun getCategory(id: Int): Category? = categories[id] ?: parent?.getCategory(id)

    /**
     * Get a tag by [name] from the icon pack. If tag is not found,
     * parent packs are searched recursively. Returns `null` if not found.
     */
    fun getTag(name: String): IconTag? = tags[name] ?: parent?.getTag(name)

    /**
     * Get the drawable for an icon [id]. Returns `null` if icon doesn't exist
     * or if the drawable couldn't be loaded with the [loader].
     */
    fun getIconDrawable(id: Int, loader: IconDrawableLoader): Drawable? {
        val icon = getIcon(id) ?: return null
        loader.loadDrawable(icon)
        return icon.drawable
    }

    /**
     * Load all icons in this pack with a [loader].
     * This is probably best called on another thread than the main thread.
     */
    fun loadDrawables(loader: IconDrawableLoader) {
        for (icon in allIcons) {
            loader.loadDrawable(icon)
        }
    }

    override fun toString() = "IconPack(${icons.size} icons, ${categories.size} categories, " +
            "${tags.size} tags, locales=$locales, parent=$parent)"

}
