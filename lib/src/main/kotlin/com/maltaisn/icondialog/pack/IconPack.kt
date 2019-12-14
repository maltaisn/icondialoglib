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

import android.util.SparseArray
import androidx.annotation.XmlRes
import androidx.core.util.size
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.IconTag


/**
 * An icon pack containing icons, categories and tags.
 *
 * @param parent A pack can have a parent pack which will be used to resolve elements that aren't
 * found in this pack. This way, icons, categories and tags can be overriden by
 * a child pack. Parent is `null` if there's none.
 */
class IconPack(val parent: IconPack?,
               val icons: SparseArray<Icon>,
               val categories: SparseArray<Category>,
               val tags: MutableMap<String, IconTag>,
               @XmlRes val tagsXml: Int) {

    /**
     * Get an icon from the icon pack. If icon is not found,
     * parent packs are searched recursively. Returns `null` if not found.
     */
    fun getIcon(id: Int): Icon? = icons[id] ?: parent?.getIcon(id)

    /**
     * Get a category from the icon pack. If category is not found,
     * parent packs are searched recursively. Returns `null` if not found.
     */
    fun getCategory(id: Int): Category? = categories[id] ?: parent?.getCategory(id)

    /**
     * Get a tag from the icon pack. If tag is not found,
     * parent packs are searched recursively. Returns `null` if not found.
     */
    fun getTag(name: String): IconTag? = tags[name] ?: parent?.getTag(name)


    override fun toString() = "IconPack(${icons.size} icons, " +
            "${categories.size} categories, ${tags.size} tags, parent=$parent)"

}
