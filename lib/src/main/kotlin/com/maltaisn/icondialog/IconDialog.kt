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

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack


class IconDialog : DialogFragment(), IconDialogContract.View {

    private var presenter: IconDialogContract.Presenter? = null

    /** The settings used for the dialog. */
    override lateinit var settings: IconDialogSettings
        private set

    override val iconPack: IconPack
        get() = callback.iconDialogIconPack

    /**
     * The selected icon IDs in the [iconPack].
     * Must be set before showing the dialog.
     */
    override var selectedIconIds: List<Int> = emptyList()


    @SuppressLint("InflateParams")
    override fun onCreateDialog(state: Bundle?): Dialog {
        if (state != null) {
            settings = state.getParcelable("settings")!!
            selectedIconIds = state.getIntegerArrayList("selectedIconIds")!!
        }

        // Wrap recurrence picker theme to context
        val context = requireContext()
        val ta = context.obtainStyledAttributes(intArrayOf(R.attr.icdStyle))
        val style = ta.getResourceId(0, R.style.IcdStyle)
        ta.recycle()
        val contextWrapper = ContextThemeWrapper(context, style)
        val localInflater = LayoutInflater.from(contextWrapper)

        // Create the dialog
        val builder = MaterialAlertDialogBuilder(contextWrapper)
        val view = localInflater.inflate(R.layout.icd_dialog_icon, null, false)
        builder.setView(view)

        // Recurrence list
        val rcv: RecyclerView = view.findViewById(R.id.icd_rcv_icon_list)

        // Attach the presenter
        presenter = IconDialogPresenter()
        presenter?.attach(this, state)

        return builder.create()
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)

        state.putParcelable("settings", settings)
        state.putIntegerArrayList("selectedIconIds", ArrayList(selectedIconIds))

        presenter?.saveState(state)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Detach the presenter
        presenter?.detach()
        presenter = null
    }

    override fun onCancel(dialog: DialogInterface) {
        presenter?.onCancel()
    }

    override fun exit() {
        dismiss()
    }

    private val callback: Callback
        get() = (parentFragment as? Callback)
                ?: (targetFragment as? Callback)
                ?: (activity as? Callback)
                ?: error("Icon dialog must have a callback.")

    /**
     * Callback interface to be implemented by parent activity, parent fragment or
     * target fragment and used to communicate the results of the icon dialog.
     */
    interface Callback {
        /**
         * The icon pack to be displayed by the dialog.
         */
        val iconDialogIconPack: IconPack

        /**
         * Called when icons are selected and user confirms the selection.
         */
        fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>)

        /**
         * Called when user dismissed the dialog by clicking outside or by clicking
         * on the Cancel button.
         */
        fun onIconDialogCancelled() = Unit
    }

    enum class SearchVisibility {
        NEVER,
        ALWAYS,
        IF_LANGUAGE_AVAILABLE,
    }

    enum class TitleVisibility {
        NEVER,
        ALWAYS,
        IF_SEARCH_HIDDEN,
    }

    enum class HeadersVisibility {
        HIDE,
        SHOW,
        STICKY
    }

    companion object {
        /**
         * Create a new instance of the dialog with [settings].
         * More settings can be set with the returned dialog instance later.
         */
        @JvmStatic
        fun newInstance(settings: IconDialogSettings): IconDialog {
            val dialog = IconDialog()
            dialog.settings = settings
            return dialog
        }
    }

}
