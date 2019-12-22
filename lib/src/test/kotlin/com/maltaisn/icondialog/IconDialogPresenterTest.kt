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

package com.maltaisn.icondialog

import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.NamedTag
import com.maltaisn.icondialog.pack.IconPack
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals


internal class IconDialogPresenterTest {

    private val presenter = IconDialogPresenter()
    private val settings = IconDialogSettings()

    private val iconPack = IconPack(locales = listOf(Locale.ENGLISH)).apply {
        icons[0] = Icon(0, 0, listOf("a", "b"), "")
        icons[1] = Icon(1, 0, listOf("b", "c"), "")
        icons[2] = Icon(2, 1, listOf("e", "f"), "")
        icons[3] = Icon(3, 2, listOf("a", "f"), "")

        categories[0] = Category(0, "catg-0", 0)
        categories[1] = Category(1, "catg-1", 0)
        categories[2] = Category(2, "catg-2", 0)

        for (icon in icons.values) {
            for (tag in icon.tags) {
                if (tag !in tags) {
                    tags[tag] = NamedTag(tag, NamedTag.Value(tag, tag.normalize()), emptyList())
                }
            }
            icon.drawable = mock()
        }
        icons[3]?.drawable = null
    }

    private val view: IconDialogContract.View = mock {
        on { settings } doReturn settings
        on { iconPack } doReturn iconPack
        on { locale } doReturn Locale.ENGLISH
    }

    // List items with no query is:
    // [0] Category 0 header
    // [1] Icon 0
    // [2] Icon 1
    // [3] Category 1 header
    // [4] Icon 2
    // [5] Category 2 header
    // [6] Icon 3


    @Test(expected = IllegalStateException::class)
    fun `attach wrong icon id error`() {
        whenever(view.selectedIconIds).doReturn(listOf(0, 1, 100))
        presenter.attach(view, null)
    }

    @Test
    fun `attach too many selected icons`() {
        whenever(view.settings).doReturn(settings.copy(maxSelection = 2))
        whenever(view.selectedIconIds).doReturn(listOf(0, 1, 2, 3))
        presenter.attach(view, null)
        verifySelection(0, 1)
    }

    @Test
    fun `attach search and title visibility never`() {
        whenever(view.settings).doReturn(settings.copy(
                titleVisibility = IconDialog.TitleVisibility.NEVER,
                searchVisibility = IconDialog.SearchVisibility.NEVER))
        presenter.attach(view, null)
        verify(view).setTitleVisible(false)
        verify(view).setSearchBarVisible(false)
        verify(view).removeLayoutPadding()
    }

    @Test
    fun `attach search and title visibility always`() {
        whenever(view.settings).doReturn(settings.copy(
                titleVisibility = IconDialog.TitleVisibility.ALWAYS,
                searchVisibility = IconDialog.SearchVisibility.ALWAYS))
        presenter.attach(view, null)
        verify(view).setTitleVisible(true)
        verify(view).setSearchBarVisible(true)
    }

    @Test
    fun `attach search and title visibility only search`() {
        whenever(view.settings).doReturn(settings.copy(
                titleVisibility = IconDialog.TitleVisibility.IF_SEARCH_HIDDEN,
                searchVisibility = IconDialog.SearchVisibility.IF_LANGUAGE_AVAILABLE))
        presenter.attach(view, null)
        verify(view).setTitleVisible(false)
        verify(view).setSearchBarVisible(true)
    }

    @Test
    fun `attach search and title visibility only title`() {
        whenever(view.locale).doReturn(Locale.FRENCH)
        whenever(view.settings).doReturn(settings.copy(
                titleVisibility = IconDialog.TitleVisibility.IF_SEARCH_HIDDEN,
                searchVisibility = IconDialog.SearchVisibility.IF_LANGUAGE_AVAILABLE))
        presenter.attach(view, null)
        verify(view).setTitleVisible(true)
        verify(view).setSearchBarVisible(false)
    }

    @Test
    fun `icon list no query category headers`() {
        presenter.attach(view, null)
        assertEquals(IconDialogPresenter.ITEM_TYPE_HEADER, presenter.getItemType(0))
        assertEquals(IconDialogPresenter.ITEM_TYPE_HEADER, presenter.getItemType(3))
        assertEquals(IconDialogPresenter.ITEM_TYPE_HEADER, presenter.getItemType(5))

        val headerItem: IconDialogContract.HeaderItemView = mock()
        presenter.onBindHeaderItemView(3, headerItem)
        verify(headerItem).bindView(iconPack.getCategory(1)!!)
    }

    @Test
    fun `icon list blank query`() {
        presenter.attach(view, null)
        clearInvocations(view)
        presenter.onSearchQueryEntered("    ")
        verify(view, never()).notifyAllIconsChanged()
        verify(view).setClearSearchBtnVisible(true)
        assertEquals(7, presenter.itemCount)
    }

    @Test
    fun `icon list with query`() {
        presenter.attach(view, null)
        clearInvocations(view)
        presenter.onSearchQueryEntered("a")
        verify(view).notifyAllIconsChanged()
        verify(view).setClearSearchBtnVisible(true)
        assertEquals(4, presenter.itemCount)
    }

    @Test
    fun `clear selection`() {
        whenever(view.selectedIconIds).doReturn(listOf(0))
        presenter.attach(view, null)
        clearInvocations(view)
        presenter.onClearBtnClicked()
        verify(view).notifyIconItemChanged(1)
        verify(view).setClearBtnVisible(false)
        verifySelection()
    }

    @Test
    fun `clear selection hidden by query`() {
        whenever(view.selectedIconIds).doReturn(listOf(0))
        presenter.attach(view, null)
        presenter.onSearchQueryEntered("e")
        presenter.onClearBtnClicked()
        verify(view, never()).notifyIconItemChanged(any())
        verifySelection()
    }

    @Test
    fun `icon click single selection max selection reached`() {
        whenever(view.selectedIconIds).doReturn(listOf(0))
        presenter.attach(view, null)
        presenter.onIconItemClicked(2)
        verify(view, times(2)).notifyIconItemChanged(any())
        verifySelection(1)
    }

    @Test
    fun `icon click single selection unselect`() {
        whenever(view.selectedIconIds).doReturn(listOf(0))
        presenter.attach(view, null)
        clearInvocations(view)
        presenter.onIconItemClicked(1)
        verify(view).notifyIconItemChanged(1)
        verify(view).setSelectBtnEnabled(false)
        verify(view).setClearBtnVisible(false)
        verifySelection()
    }

    @Test
    fun `icon click single selection with message`() {
        whenever(view.selectedIconIds).doReturn(listOf(0))
        whenever(view.settings).doReturn(settings.copy(showMaxSelectionMessage = true))
        presenter.attach(view, null)
        clearInvocations(view)
        presenter.onIconItemClicked(2)
        verify(view, never()).notifyIconItemChanged(any())
        verify(view).showMaxSelectionMessage()
        verifySelection(0)
    }

    @Test
    fun `icon click single selection no confirm`() {
        whenever(view.settings).doReturn(settings.copy(showSelectBtn = false))
        presenter.attach(view, null)
        presenter.onIconItemClicked(1)
        verify(view).notifyIconItemChanged(1)
        verify(view).setSelectionResult(listOf(iconPack.getIcon(0)!!))
    }

    @Test
    fun `icon click single selection no confirm with previous selection`() {
        whenever(view.selectedIconIds).doReturn(listOf(1))
        whenever(view.settings).doReturn(settings.copy(showSelectBtn = false))
        presenter.attach(view, null)
        presenter.onIconItemClicked(1)
        verify(view, times(2)).notifyIconItemChanged(any())
        verify(view).setSelectionResult(listOf(iconPack.getIcon(0)!!))
    }

    @Test
    fun `icon click icon without drawable`() {
        presenter.attach(view, null)
        presenter.onIconItemClicked(6)
        verify(view, never()).notifyIconItemChanged(any())
        verifySelection()
    }

    /** Verify that the presenter has icon [ids] selected. */
    private fun verifySelection(vararg ids: Int) {
        clearInvocations(view)
        presenter.onSelectBtnClicked()
        verify(view).setSelectionResult(ids.map { iconPack.getIcon(it)!! })
    }

}
