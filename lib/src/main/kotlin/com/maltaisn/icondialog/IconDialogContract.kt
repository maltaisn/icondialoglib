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
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import java.util.*


internal interface IconDialogContract {

    interface View {
        val settings: IconDialogSettings
        val iconPack: IconPack
        val selectedIconIds: List<Int>
        val locale: Locale

        fun exit()
        fun hideKeyboard()

        fun setCancelResult()
        fun setSelectionResult(selected: List<Icon>)

        fun setTitleVisible(visible: Boolean)
        fun setSearchBarVisible(visible: Boolean)
        fun setClearSearchBtnVisible(visible: Boolean)
        fun setClearBtnVisible(visible: Boolean)
        fun setProgressBarVisible(visible: Boolean)
        fun setNoResultLabelVisible(visible: Boolean)
        fun setFooterVisible(visible: Boolean)
        fun removeLayoutPadding()

        fun setSelectBtnEnabled(enabled: Boolean)
        fun scrollToItemPosition(pos: Int)
        fun notifyIconItemChanged(pos: Int)
        fun notifyAllIconsChanged()

        fun showMaxSelectionMessage()
    }

    interface Presenter {
        fun attach(view: View, state: Bundle?)
        fun detach()
        fun saveState(state: Bundle)

        fun onSearchQueryEntered(query: String)
        fun onSearchActionEvent(query: String)
        fun onSearchClearBtnClicked()

        val itemCount: Int
        fun getItemId(pos: Int): Long
        fun getItemType(pos: Int): Int
        fun getItemSpanCount(pos: Int, max: Int): Int
        fun onBindIconItemView(pos: Int, itemView: IconItemView)
        fun onBindHeaderItemView(pos: Int, itemView: HeaderItemView)
        fun onIconItemClicked(pos: Int)

        fun onSelectBtnClicked()
        fun onCancelBtnClicked()
        fun onClearBtnClicked()

        fun onDialogCancelled()
    }

    interface IconItemView {
        fun bindView(icon: Icon, selected: Boolean)
    }

    interface HeaderItemView {
        fun bindView(category: Category)
    }

}
