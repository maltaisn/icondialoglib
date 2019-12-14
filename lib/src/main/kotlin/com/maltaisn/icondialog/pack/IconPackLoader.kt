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
import android.util.SparseArray
import androidx.annotation.WorkerThread
import androidx.annotation.XmlRes


/**
 * Class for loading icon packs from XML resources.
 * All operations are blocking and should be called asynchronously. The class is thread-safe.
 *
 * @param context Any context, needed to load the XML resources.
 */
@WorkerThread
class IconPackLoader(context: Context) {

    private val context = context.applicationContext

    /**
     * Load an icon pack from XML resources for icons and tags.
     */
    @JvmOverloads
    fun load(@XmlRes iconsXml: Int, @XmlRes tagsXml: Int, parent: IconPack? = null): IconPack {
        val pack = IconPack(parent, SparseArray(), SparseArray(), mutableMapOf(), tagsXml)
        loadIcons(pack, iconsXml)
        loadTags(pack)
        return pack
    }

    /**
     * Reload the tags of an icon [pack] and its parents.
     * This must be called whenever the application language changes.
     * A `BroadcastListener` should be attached to listen for this event.
     * This operation is blocking and should be executed asynchronously.
     */
    fun reloadTags(pack: IconPack) {
        if (pack.parent != null) {
            reloadTags(pack.parent)
        }
        loadTags(pack)
    }


    private fun loadIcons(pack: IconPack, @XmlRes iconsXml: Int) {
        // TODO
    }

    /**
     * Load tags of a [pack].
     */
    private fun loadTags(pack: IconPack) {
        // TODO
    }

    companion object {
        private val TAG = IconPackLoader::class.java.simpleName

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

        private const val XML_TAG_TAG = "tag"
        private const val XML_TAG_ALIAS = "alias"
        private const val XML_ATTR_TAG_NAME = "name"
    }

}
