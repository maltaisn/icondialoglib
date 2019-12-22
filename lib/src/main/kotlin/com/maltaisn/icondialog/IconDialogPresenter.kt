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

import android.os.Bundle
import com.maltaisn.icondialog.IconDialog.*
import com.maltaisn.icondialog.IconDialogContract.View
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack


internal class IconDialogPresenter : IconDialogContract.Presenter {

    private var view: View? = null

    private val settings: IconDialogSettings
        get() = view!!.settings

    private val iconPack: IconPack
        get() = view!!.iconPack

    private val listItems = mutableListOf<Item>()
    private val selectedIconIds = linkedSetOf<Int>()
    private var searchQuery = ""


    override fun attach(view: View, state: Bundle?) {
        check(this.view == null) { "Presenter already attached." }
        this.view = view

        listItems.clear()
        selectedIconIds.clear()
        searchQuery = ""

        if (state == null) {
            // Check if initial selection is valid.
            val selection = view.selectedIconIds.toMutableList()
            for (id in selection) {
                if (iconPack.getIcon(id) == null) {
                    error("Selected icon ID $id not found in icon pack.")
                }
            }
            if (selection.size > settings.maxSelection
                    && settings.maxSelection != IconDialogSettings.NO_MAX_SELECTION) {
                // Initial selection too big, truncate it.
                selection.subList(settings.maxSelection, selection.size).clear()
            }
            selectedIconIds += selection

            // Scroll to first selected item.
            if (selectedIconIds.isNotEmpty()) {
                view.scrollToItemPosition(getPosByIconId(selectedIconIds.first()))
            }

        } else {
            // Restore state
            selectedIconIds += state.getIntegerArrayList("selectedIconIds")!!
            searchQuery = state.getString("searchQuery")!!
        }

        // Initialize view state
        view.apply {
            setSelectBtnEnabled(selectedIconIds.isNotEmpty())
            setFooterVisible(settings.showSelectBtn)
            setClearBtnVisible(settings.showClearBtn && selectedIconIds.isNotEmpty())
            setNoResultLabelVisible(false)
            setProgressBarVisible(false)
            setClearSearchBtnVisible(searchQuery.isNotEmpty())

            val searchVisible = settings.searchVisibility == SearchVisibility.ALWAYS
                    || settings.searchVisibility == SearchVisibility.IF_LANGUAGE_AVAILABLE
                    && view.locale.language in iconPack.locales.map { it.language }
            val titleVisible = settings.titleVisibility == TitleVisibility.ALWAYS
                    || settings.titleVisibility == TitleVisibility.IF_SEARCH_HIDDEN && !searchVisible
            setSearchBarVisible(searchVisible)
            setTitleVisible(titleVisible)
            if (!searchVisible && !titleVisible) {
                removeLayoutPadding()
            }
        }

        updateList()
    }

    override fun detach() {
        view = null
    }

    override fun saveState(state: Bundle) {
        state.putIntegerArrayList("selectedIconIds", ArrayList(selectedIconIds))
        state.putString("searchQuery", searchQuery)
    }

    override fun onSelectBtnClicked() {
        confirmSelection()
    }

    override fun onCancelBtnClicked() {
        onDialogCancelled()
    }

    override fun onClearBtnClicked() {
        for (id in selectedIconIds) {
            val pos = getPosByIconId(id)
            if (pos != -1) {
                view?.notifyIconItemChanged(pos)
            }
        }
        selectedIconIds.clear()

        view?.setClearBtnVisible(false)
        view?.setSelectBtnEnabled(false)
    }

    override fun onDialogCancelled() {
        view?.setCancelResult()
        view?.exit()
    }

    override fun onSearchQueryEntered(query: String) {
        val trimQuery = query.trim()
        if (trimQuery != searchQuery) {
            searchQuery = trimQuery
            updateList()
        }
        view?.setClearSearchBtnVisible(query.isNotEmpty())
    }

    override fun onSearchActionEvent(query: String) {
        view?.hideKeyboard()
        onSearchActionEvent(query)
    }

    override fun onSearchClearBtnClicked() {
        onSearchQueryEntered("")
    }

    override val itemCount: Int
        get() = listItems.size

    override fun getItemId(pos: Int) = listItems[pos].id

    override fun getItemType(pos: Int) = if (listItems[pos] is IconItem) {
        ITEM_TYPE_ICON
    } else {
        ITEM_TYPE_HEADER
    }

    override fun getItemSpanCount(pos: Int, max: Int) =
            if (listItems[pos] is HeaderItem) max else 1

    override fun onBindIconItemView(pos: Int, itemView: IconDialogContract.IconItemView) {
        val item = listItems[pos] as IconItem
        itemView.bindView(item.icon, item.selected)
    }

    override fun onBindHeaderItemView(pos: Int, itemView: IconDialogContract.HeaderItemView) {
        val item = listItems[pos] as HeaderItem
        itemView.bindView(item.category)
    }

    override fun onIconItemClicked(pos: Int) {
        view?.hideKeyboard()

        val item = listItems[pos] as IconItem
        if (item.icon.drawable == null) {
            // Icon drawable is unavailable, can't select it.
            return
        }

        if (settings.showSelectBtn) {
            if (item.selected) {
                // Unselect icon
                item.selected = false
                selectedIconIds -= item.icon.id

            } else {
                if (selectedIconIds.size == settings.maxSelection
                        && settings.maxSelection != IconDialogSettings.NO_MAX_SELECTION) {
                    // Max selection reached
                    if (settings.showMaxSelectionMessage) {
                        // Show message to user.
                        view?.showMaxSelectionMessage()
                        return
                    } else {
                        // Unselect first selected icon.
                        val firstId = selectedIconIds.first()
                        selectedIconIds.remove(firstId)
                        val firstPos = getPosByIconId(firstId)
                        if (firstPos != -1) {
                            val firstItem = listItems[firstPos] as IconItem
                            firstItem.selected = false
                            view?.notifyIconItemChanged(firstPos)
                        }

                        // Select new icon
                        item.selected = true
                        selectedIconIds += item.icon.id
                    }
                } else {
                    // Select new icon
                    item.selected = true
                    selectedIconIds += item.icon.id
                }
            }

            // Update dialog buttons
            val hasSelection = selectedIconIds.isNotEmpty()
            view?.setSelectBtnEnabled(hasSelection)
            view?.setClearBtnVisible(settings.showClearBtn && hasSelection)

        } else {
            // No select button so confirm selection directly.
            if (selectedIconIds.isNotEmpty()) {
                // Unselect other selected icon.
                val lastId = selectedIconIds.first()
                selectedIconIds -= lastId
                val lastPos = getPosByIconId(lastId)
                if (lastPos != -1) {
                    val lastItem = listItems[lastPos] as IconItem
                    lastItem.selected = false
                    view?.notifyIconItemChanged(lastPos)
                }
            }

            // Select new icon
            item.selected = true
            selectedIconIds += item.icon.id

            confirmSelection()
        }

        view?.notifyIconItemChanged(pos)
    }

    private fun confirmSelection() {
        view?.setSelectionResult(selectedIconIds.map { iconPack.getIcon(it)!! })
        view?.exit()
    }

    /**
     * Get the position of an icon with [id] in the list.
     * Returns `-1` if icon is not currently shown or doesn't exist.
     */
    private fun getPosByIconId(id: Int) =
            listItems.indexOfFirst { it is IconItem && it.icon.id == id }

    /**
     * Update icon list to current search query, inserting category header items too.
     */
    private fun updateList() {
        // Get icons matching search
        val icons = settings.iconFilter.queryIcons(iconPack, searchQuery)

        // Sort icons by category, then use icon filter for secondary sorting rules.
        icons.sortWith(Comparator { icon1, icon2 ->
            val result = icon1.categoryId.compareTo(icon2.categoryId)
            if (result != 0) {
                result
            } else {
                settings.iconFilter.compare(icon1, icon2)
            }
        })

        // Create icon items
        listItems.clear()
        listItems += icons.map { IconItem(it, it.id in selectedIconIds) }

        // Insert category headers
        if (settings.headersVisibility != HeadersVisibility.HIDE && listItems.isNotEmpty()) {
            var i = 0
            while (i < listItems.size) {
                val prevId = (listItems.getOrNull(i - 1) as IconItem?)?.icon?.categoryId
                val currId = (listItems[i] as IconItem).icon.categoryId
                if (currId != prevId) {
                    listItems.add(i, HeaderItem(iconPack.getCategory(currId)!!))
                    i++
                }
                i++
            }
        }

        view?.notifyAllIconsChanged()
        view?.setNoResultLabelVisible(listItems.isEmpty())
    }

    private interface Item {
        val id: Long
    }

    private class IconItem(val icon: Icon, var selected: Boolean) : Item {
        override val id: Long
            get() = icon.id.toLong()
    }

    private class HeaderItem(val category: Category) : Item {
        override val id: Long
            get() = category.id.inv().toLong()
    }

    companion object {
        internal const val ITEM_TYPE_ICON = 0
        internal const val ITEM_TYPE_HEADER = 1
    }

}
