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
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import com.maltaisn.icondialog.IconDialog.*
import com.maltaisn.icondialog.IconDialogSettings.Builder
import com.maltaisn.icondialog.IconDialogSettings.Companion.NO_MAX_SELECTION


/**
 * A class for configuring the behavior and appearance of the icon dialog.
 * The class is immutable, use the [Builder] to create it.
 */
data class IconDialogSettings(
        /** Icon filter used to search and sort icons. Default is [DefaultIconFilter]. */
        val iconFilter: IconFilter,

        /** Visibility of the search bar. Default is [SearchVisibility.IF_LANGUAGE_AVAILABLE]. */
        val searchVisibility: SearchVisibility,

        /** Visibility of the category headers. Default is [HeadersVisibility.STICKY]. */
        val headersVisibility: HeadersVisibility,

        /** Visibility of the dialog title. Default is [TitleVisibility.IF_SEARCH_HIDDEN]. */
        val titleVisibility: TitleVisibility,

        /** The string resource ID for the dialog title. Default is `R.string.icd_title` */
        @StringRes val dialogTitle: Int,

        /** The maximum number of icons that can be selected. Default is `1`.
         * Can be set to [NO_MAX_SELECTION] to allow any number of icons to be selected.
         * Has no effect is [showSelectBtn] is `false`. */
        val maxSelection: Int,

        /** Whether to show a toast indicating that max selection is already reached when
         * user selects an icon. If not shown, the first selected icon will be unselected.
         * Default is `false`. */
        val showMaxSelectionMessage: Boolean,

        /** Whether to show the Select button. If not shown, a click on any icon will select it.
         * [maxSelection] must be `1` if this option is set to `false`. Default is `true`. */
        val showSelectBtn: Boolean,

        /** Whether to show a button to clear icon selection. Default is `false`. */
        val showClearBtn: Boolean
) : Parcelable {

    class Builder {
        var iconFilter = DefaultIconFilter()
        var searchVisibility = SearchVisibility.IF_LANGUAGE_AVAILABLE
        var headersVisibility = HeadersVisibility.STICKY
        var titleVisibility = TitleVisibility.IF_SEARCH_HIDDEN
        @StringRes var dialogTitle = R.string.icd_title
        var maxSelection = 1
        var showMaxSelectionMessage = false
        var showSelectBtn = true
        var showClearBtn = false

        fun build() = IconDialogSettings(iconFilter, searchVisibility,
                headersVisibility, titleVisibility, dialogTitle, maxSelection,
                showMaxSelectionMessage, showSelectBtn, showClearBtn)
    }

    // Parcelable stuff
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val bundle = Bundle()
        bundle.putParcelable("iconFilter", iconFilter)
        bundle.putSerializable("searchVisibility", searchVisibility)
        bundle.putSerializable("headersVisibility", headersVisibility)
        bundle.putSerializable("titleVisibility", titleVisibility)
        bundle.putInt("dialogTitle", dialogTitle)
        bundle.putInt("maxSelection", maxSelection)
        bundle.putBoolean("showMaxSelectionMessage", showMaxSelectionMessage)
        bundle.putBoolean("showSelectBtn", showSelectBtn)
        bundle.putBoolean("showClearBtn", showClearBtn)
        parcel.writeBundle(bundle)
    }

    override fun describeContents() = 0

    companion object {
        const val NO_MAX_SELECTION = -1

        @JvmField
        val CREATOR = object : Parcelable.Creator<IconDialogSettings> {
            override fun createFromParcel(parcel: Parcel) = IconDialogSettings {
                val bundle = parcel.readBundle(IconDialogSettings::class.java.classLoader)!!
                iconFilter = bundle.getParcelable("iconFilter")!!
                searchVisibility = bundle.getSerializable("searchVisibility") as SearchVisibility
                headersVisibility = bundle.getSerializable("headersVisibility") as HeadersVisibility
                titleVisibility = bundle.getSerializable("titleVisibility") as TitleVisibility
                dialogTitle = bundle.getInt("dialogTitle")
                maxSelection = bundle.getInt("maxSelection")
                showMaxSelectionMessage = bundle.getBoolean("showMaxSelectionMessage")
                showSelectBtn = bundle.getBoolean("showSelectBtn")
                showClearBtn = bundle.getBoolean("showClearBtn")
            }

            override fun newArray(size: Int) = arrayOfNulls<IconDialogSettings>(size)
        }

        /**
         * Utility function to create settings using constructor-like syntax.
         */
        inline operator fun invoke(init: Builder.() -> Unit = {}) = Builder().apply(init).build()
    }

}
