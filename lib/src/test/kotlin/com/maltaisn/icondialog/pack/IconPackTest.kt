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

import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.GroupingTag
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.IconTag
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


internal class IconPackTest {

    private val pack: IconPack

    init {
        val icons1 = mutableMapOf<Int, Icon>()
        val categories1 = mutableMapOf<Int, Category>()
        val tags1 = mutableMapOf<String, IconTag>()
        icons1[0] = Icon(0, 0, emptyList(), "")
        categories1[0] = Category(0, "", 0)
        tags1["_0"] = GroupingTag("_0")

        val icons2 = mutableMapOf<Int, Icon>()
        val categories2 = mutableMapOf<Int, Category>()
        val tags2 = mutableMapOf<String, IconTag>()
        icons2[1] = Icon(1, 1, emptyList(), "")
        categories2[1] = Category(1, "", 0)
        tags2["_1"] = GroupingTag("_1")

        val parent = IconPack(null, icons1, categories1, tags1, emptyList(), 0)
        pack = IconPack(parent, icons2, categories2, tags2, emptyList(), 0)
    }

    @Test
    fun shouldGetIconShallow() {
        val icon = pack.getIcon(1)!!
        assertEquals(1, icon.id)
    }

    @Test
    fun shouldGetIconDeep() {
        val icon = pack.getIcon(0)!!
        assertEquals(0, icon.id)
    }

    @Test
    fun shouldNotGetIcon() {
        val icon = pack.getIcon(10)
        assertNull(icon)
    }

    @Test
    fun shouldGetCategoryShallow() {
        val catg = pack.getCategory(1)!!
        assertEquals(1, catg.id)
    }

    @Test
    fun shouldGetCategoryDeep() {
        val catg = pack.getCategory(0)!!
        assertEquals(0, catg.id)
    }

    @Test
    fun shouldNotGetCategory() {
        val catg = pack.getCategory(10)
        assertNull(catg)
    }

    @Test
    fun shouldGetTagShallow() {
        val tag = pack.getTag("_1")!!
        assertEquals("_1", tag.name)
    }

    @Test
    fun shouldGetTagDeep() {
        val tag = pack.getTag("_0")!!
        assertEquals("_0", tag.name)
    }

    @Test
    fun shouldNotGetTag() {
        val tag = pack.getTag("_10")
        assertNull(tag)
    }

}
