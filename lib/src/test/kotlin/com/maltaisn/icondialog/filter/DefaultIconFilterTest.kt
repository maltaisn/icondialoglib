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

import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.NamedTag
import com.maltaisn.icondialog.normalize
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.testCatg
import com.maltaisn.icondialog.testIcon
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class DefaultIconFilterTest {

    private val filter = DefaultIconFilter()

    private val parentPack = IconPack().apply {
        icons[0] = testIcon(0, 0)
        icons[6] = testIcon(6, 0)

        categories[0] = testCatg(0)
    }

    private val pack = IconPack(parentPack).apply {
        icons[0] = testIcon(0, 0, listOf("abc", "def"))
        icons[1] = testIcon(1, 2, listOf("bcd", "efg"))
        icons[2] = testIcon(2, 2, listOf("cde", "fgh"))
        icons[3] = testIcon(3, 1, listOf("bàd", "dád"))
        icons[4] = testIcon(4, 2, listOf("cde", "fgh"))
        icons[5] = testIcon(5, 2, listOf("cde", "fgh", "xyz"))

        categories[1] = testCatg(1)
        categories[2] = testCatg(2)

        // Add all tags objects referenced above
        tags["xyz"] = NamedTag("xyz", listOf(NamedTag.Value("xyz", "xyz"),
                NamedTag.Value("zyx", "zyx")))
        for (icon in icons.values) {
            for (tag in icon.tags) {
                if (tag !in tags) {
                    tags[tag] = NamedTag(tag, listOf(NamedTag.Value(tag, tag.normalize())))
                }
            }
        }
    }

    @Test
    fun `should return all icons when no query`() {
        val icons = filter.queryIcons(pack)
        assertEquals(pack.allIcons, icons)
    }

    @Test
    fun `should return all icons when blank query`() {
        val icons = filter.queryIcons(pack, "    ")
        assertEquals(pack.allIcons, icons)
    }

    @Test
    fun `should not return icon by id (disabled)`() {
        val icons = filter.queryIcons(pack, "#1")
        assertTrue(icons.isEmpty())
    }

    @Test
    fun `should not return icon by id (bad id)`() {
        filter.idSearchEnabled = true
        val icons = filter.queryIcons(pack, "#id")
        assertTrue(icons.isEmpty())
    }

    @Test
    fun `should return icon by id`() {
        filter.idSearchEnabled = true
        val icons = filter.queryIcons(pack, "#1")
        assertIconsAre(icons, 1)
    }

    @Test
    fun `should sort icons correctly`() {
        val icons = filter.queryIcons(pack).sortedWith(filter)
        assertIconsAre(icons, 6, 0, 1, 3, 2, 4, 5)
    }

    @Test
    fun `should get icons for query 1`() {
        val icons = filter.queryIcons(pack, "a")
        assertIconsAre(icons, 0, 3)
    }

    @Test
    fun `should get icons for query 2`() {
        val icons = filter.queryIcons(pack, "c")
        assertIconsAre(icons, 0, 1, 2, 4, 5)
    }

    @Test
    fun `should get icons for query 3`() {
        val icons = filter.queryIcons(pack, "gh")
        assertIconsAre(icons, 2, 4, 5)
    }

    @Test
    fun `should get icons for query multiple terms`() {
        val icons = filter.queryIcons(pack, "ef,fg")
        assertIconsAre(icons, 0, 1, 2, 4, 5)
    }

    @Test
    fun `should get icons for query untrimmed`() {
        val icons = filter.queryIcons(pack, "  á   ")
        assertIconsAre(icons, 0, 3)
    }

    @Test
    fun `should get icons for query untrimmed multiple terms`() {
        val icons = filter.queryIcons(pack, ",  A , ;  ")
        assertIconsAre(icons, 0, 3)
    }

    @Test
    fun `should get icons for query in aliases`() {
        val icons = filter.queryIcons(pack, "yx")
        assertIconsAre(icons, 5)
    }

    @Test
    fun `should get icons for query not normalized`() {
        filter.queryNormalized = false
        val icons = filter.queryIcons(pack, "À")
        assertIconsAre(icons, 3)
    }

    @Test
    fun `should get icons for query multiple terms no split`() {
        filter.termSplitPattern = null
        val icons = filter.queryIcons(pack, "a,b")
        assertIconsAre(icons, 0)
    }

    @Test
    fun `should get icons for query multiple terms custom split`() {
        filter.termSplitPattern = """[:]""".toRegex()
        val icons = filter.queryIcons(pack, "a:b")
        assertIconsAre(icons, 0, 1, 3)
    }

    private fun assertIconsAre(icons: List<Icon>, vararg ids: Int) {
        assertEquals(ids.map { pack.getIcon(it) }, icons)
    }

}
