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

import androidx.test.platform.app.InstrumentationRegistry
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.NamedTag
import com.maltaisn.icondialog.test.R
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull


@RunWith(MockitoJUnitRunner::class)
internal class IconPackLoaderTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private val packLoader = IconPackLoader(context).apply {
        drawableLoader = mock()
        whenever(drawableLoader.loadDrawable(any())).thenReturn(mock())
    }

    /*
    // This test only works if `context = context.applicationContext` line is removed in loaders.
    @Test
    fun reloadStrings() {
        val contextFr = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocale(Locale.FRENCH)
        })
        val packLoaderFr = IconPackLoader(contextFr)
        val pack = packLoader.load(R.xml.icons_catg_name_ref, R.xml.tags_valid)
        packLoaderFr.reloadStrings(pack)

        assertEquals(Category(0, "catg-fr", R.string.category), pack.categories[0])
        assertEquals("tag1-fr", (pack.tags["tag1"] as NamedTag).value?.value)
        assertEquals("tag2-fr", (pack.tags["tag2"] as NamedTag).value?.value)
        assertEquals("tag3-fr", (pack.tags["tag3"] as NamedTag).value?.value)
    }
     */

    // ICON LOADING

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongRootTag_shouldFail() {
        packLoader.load(R.xml.icons_wrong_root_element, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongElement_shouldFail() {
        packLoader.load(R.xml.icons_wrong_element, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongIconId_shouldFail() {
        packLoader.load(R.xml.icons_wrong_icon_id, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongCategoryId_shouldFail() {
        packLoader.load(R.xml.icons_wrong_catg_id, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_noIconPathData_shouldFail() {
        packLoader.load(R.xml.icons_no_icon_path_data, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_noCategoryName_shouldFail() {
        packLoader.load(R.xml.icons_no_catg_name, R.xml.tags_empty)
    }

    @Test
    fun loadIcons_noCategoryName_overriden() {
        val parent = packLoader.load(R.xml.icons_valid, R.xml.tags_empty)
        val child = packLoader.load(R.xml.icons_no_catg_name, R.xml.tags_empty, parent = parent)
        assertEquals("catg", child.categories[0]?.name)
    }

    @Test
    fun loadIcons_categoryNameRef() {
        val pack = packLoader.load(R.xml.icons_catg_name_ref, R.xml.tags_empty)
        assertEquals("catg", pack.categories[0]?.name)
        assertEquals(R.string.category, pack.categories[0]?.nameRes)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_iconChildElement_shouldFail() {
        packLoader.load(R.xml.icons_icon_child_element, R.xml.tags_empty)
    }

    @Test
    fun loadIcons_noIconCategory() {
        val pack = packLoader.load(R.xml.icons_no_icon_category, R.xml.tags_empty)
        assertEquals(-1, pack.icons[0]?.categoryId)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_nestedCategory_shouldFail() {
        packLoader.load(R.xml.icons_nested_category, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_duplicateIcon_shouldFail() {
        packLoader.load(R.xml.icons_duplicate_icon, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_duplicateCategory_shouldFail() {
        packLoader.load(R.xml.icons_duplicate_catg, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongIconIdNegative_shouldFail() {
        packLoader.load(R.xml.icons_wrong_icon_id_negative, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongCategoryIdNegative_shouldFail() {
        packLoader.load(R.xml.icons_wrong_catg_id_negative, R.xml.tags_empty)
    }

    @Test(expected = IconPackParseException::class)
    fun loadIcons_wrongSize_shouldFail() {
        packLoader.load(R.xml.icons_wrong_icon_size, R.xml.tags_empty)
    }

    @Test
    fun loadIcons_valid() {
        val pack = packLoader.load(R.xml.icons_valid, R.xml.tags_empty)
        assertEquals(Icon(0, -1, listOf("tag1", "tag2"), ".", 32, 32), pack.icons[0])
        assertEquals(Icon(1, 0, listOf("_group", "tag1", "tag3"), "..", 32, 32), pack.icons[1])
        assertEquals(Icon(2, 0, listOf("_group", "tag1", "tag3"), "...", 32, 32), pack.icons[2])
        assertEquals(Category(0, "catg", 0), pack.categories[0])
        assertEquals(3, pack.icons.size)
        assertEquals(1, pack.categories.size)
    }

    @Test
    fun loadIcons_valid_flat() {
        val pack = packLoader.load(R.xml.icons_valid_flat, R.xml.tags_empty)
        assertEquals(Icon(0, 0, emptyList(), "...", 24, 24), pack.icons[0])
        assertEquals(Icon(1, 0, emptyList(), "...", 32, 32), pack.icons[1])
        assertEquals(Category(0, "catg", 0), pack.categories[0])
    }

    @Test
    fun loadIcons_override() {
        val parent = packLoader.load(R.xml.icons_override_parent, 0)
        val child = packLoader.load(R.xml.icons_override_child, 0, parent = parent)
        assertEquals(Icon(0, 1, listOf("tag2"), "child", 24, 24), child.icons[0])
        assertEquals(Category(0, "child", 0), child.categories[0])
    }


    // TAGS LOADING

    @Test(expected = IconPackParseException::class)
    fun loadTags_bothValueAndAlias_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_both_value_alias)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_duplicateTag_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_duplicate_tag)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_groupingTag_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_grouping_tag)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_nestedAlias_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_nested_alias)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_nestedTag_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_nested_tag)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_noTagName_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_no_tag_name)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_wrongElement_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_wrong_element)
    }

    @Test(expected = IconPackParseException::class)
    fun loadTags_wrongRootElement_shouldFail() {
        packLoader.load(R.xml.icons_empty, R.xml.tags_wrong_root_element)
    }

    @Test
    fun loadTags_singleQuoteEscape() {
        val pack = packLoader.load(R.xml.icons_empty, R.xml.tags_single_quote_escape)
        assertEquals("Don't do that!", (pack.tags["tag"] as NamedTag).value?.value)
    }

    @Test
    fun loadTags_valid() {
        val pack = packLoader.load(R.xml.icons_empty, R.xml.tags_valid)
        assertEquals(NamedTag.Value("Tag 1", "tag1"), (pack.tags["tag1"] as NamedTag).value)
        assertEquals(NamedTag.Value("ȚàĞ ²", "tag2"), (pack.tags["tag2"] as NamedTag).value)

        val tag3 = pack.tags["tag3"] as NamedTag
        assertNull(tag3.value)
        assertEquals(listOf(NamedTag.Value("Tag 3", "tag3"),
                NamedTag.Value("Tàg three", "tagthree")), tag3.aliases)

        assertEquals(3, pack.tags.size)
    }

    @Test
    fun loadTags_override() {
        val parent = packLoader.load(R.xml.icons_empty, R.xml.tags_override_parent)
        val child = packLoader.load(R.xml.icons_empty, R.xml.tags_override_child, parent = parent)
        assertEquals("Child", (child.tags["tag"] as NamedTag).value?.value)
    }

}
