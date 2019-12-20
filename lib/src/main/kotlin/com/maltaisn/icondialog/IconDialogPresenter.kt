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
import com.maltaisn.icondialog.IconDialogContract.View
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack


internal class IconDialogPresenter : IconDialogContract.Presenter {

    private var view: View? = null

    private val settings: IconDialogSettings
        get() = view!!.settings

    private val iconPack: IconPack
        get() = view!!.iconPack

    private val selectedIcons = mutableListOf<Icon>()


    override fun attach(view: View, state: Bundle?) {
        check(this.view == null) { "Presenter already attached." }
        this.view = view

        selectedIcons.clear()

        if (state == null) {
            // Init state
            selectedIcons += view.selectedIconIds.map {
                iconPack.getIcon(it) ?: error("Selected icon ID ${it} not found in icon pack.")
            }
        } else {
            // Restore state
            selectedIcons += state.getIntegerArrayList("selectedIconIds")!!
                    .map { iconPack.getIcon(it)!! }
        }

        // Initialize view state
        view.apply {
            // TODO
        }
    }

    override fun detach() {
        view = null

        selectedIcons.clear()
    }

    override fun saveState(state: Bundle) {
        state.putIntegerArrayList("selectedIconIds", selectedIcons.mapTo(ArrayList()) { it.id })
    }

    override fun onSelectBtnClicked() {
        view?.setSelectionResult(selectedIcons)
        view?.exit()
    }

    override fun onCancelBtnClicked() {
        onDialogCancelled()
    }

    override fun onClearBtnClicked() {
        selectedIcons.clear()
        // TODO update view
    }

    override fun onDialogCancelled() {
        view?.setCancelResult()
        view?.exit()
    }

    override fun onSearchQueryEntered(query: String) {
        TODO("not implemented")
    }

    override fun onSearchActionEvent() {
        TODO("not implemented")
    }

    override fun onSearchClearBtnClicked() {
        TODO("not implemented")
    }

    override val itemCount: Int
        get() = TODO("not implemented")

    override fun getItemId(pos: Int): Long {
        TODO("not implemented")
    }

    override fun getItemType(pos: Int): Int {
        TODO("not implemented")
    }

    override fun onBindItemView(pos: Int) {
        TODO("not implemented")
    }

    override fun onIconItemClicked(pos: Int) {
        TODO("not implemented")
    }

    companion object {
        internal const val ITEM_TYPE_ICON = 0
        internal const val ITEM_TYPE_HEADER = 1
    }

}
