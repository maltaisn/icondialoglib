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

import com.maltaisn.icondialog.data.GroupingTag
import com.maltaisn.icondialog.testCatg
import com.maltaisn.icondialog.testIcon
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


internal class IconPackTest {

    private val pack: IconPack

    init {
        val parent = IconPack().apply {
            icons[0] = testIcon(0, 0)
            icons[1] = testIcon(0, 0)
            categories[0] = testCatg(0)
            tags["_0"] = GroupingTag("_0")
        }
        pack = IconPack(parent).apply {
            icons[1] = testIcon(1, 1)
            categories[1] = testCatg(1)
            tags["_1"] = GroupingTag("_1")
        }
    }

    @Test
    fun `should get icon shallow`() {
        val icon = pack.getIcon(1)!!
        assertEquals(1, icon.id)
    }

    @Test
    fun `should get icon deep`() {
        val icon = pack.getIcon(0)!!
        assertEquals(0, icon.id)
    }

    @Test
    fun `should not get icon`() {
        val icon = pack.getIcon(10)
        assertNull(icon)
    }

    @Test
    fun `should get category shallow`() {
        val catg = pack.getCategory(1)!!
        assertEquals(1, catg.id)
    }

    @Test
    fun `should get category deep`() {
        val catg = pack.getCategory(0)!!
        assertEquals(0, catg.id)
    }

    @Test
    fun `should not get category`() {
        val catg = pack.getCategory(10)
        assertNull(catg)
    }

    @Test
    fun `should get tag shallow`() {
        val tag = pack.getTag("_1")!!
        assertEquals("_1", tag.name)
    }

    @Test
    fun `should get tag deep`() {
        val tag = pack.getTag("_0")!!
        assertEquals("_0", tag.name)
    }

    @Test
    fun `should not get tag`() {
        val tag = pack.getTag("_10")
        assertNull(tag)
    }

    @Test
    fun `should get all icons`() {
        assertEquals(listOf(pack.getIcon(1), pack.getIcon(0)), pack.allIcons)
    }

}
