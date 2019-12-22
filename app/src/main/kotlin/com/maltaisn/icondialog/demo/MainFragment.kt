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

package com.maltaisn.icondialog.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.NamedTag
import com.maltaisn.icondialog.filter.DefaultIconFilter
import com.maltaisn.icondialog.pack.IconPack


class MainFragment : Fragment(), IconDialog.Callback {

    private lateinit var iconPack: IconPack
    private var selectedIcons = emptyList<Icon>()

    private lateinit var iconDialog: IconDialog
    private lateinit var iconsAdapter: IconsAdapter


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, state: Bundle?): View? {
        val app = requireActivity().application as DemoApp
        iconPack = app.iconPacks[0]

        val iconFilter = DefaultIconFilter()
        iconFilter.idSearchEnabled = true

        iconDialog = childFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
                ?: IconDialog.newInstance(IconDialogSettings())

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val iconPackDropdown: AutoCompleteTextView = view.findViewById(R.id.dropdown_icon_pack)
        setupDropdown(iconPackDropdown, R.array.icon_packs) { iconPack = app.iconPacks[it] }

        val titleVisbDropdown: AutoCompleteTextView = view.findViewById(R.id.dropdown_title_visibility)
        setupDropdown(titleVisbDropdown, R.array.title_visibility)

        val searchVisbDropdown: AutoCompleteTextView = view.findViewById(R.id.dropdown_search_visibility)
        setupDropdown(searchVisbDropdown, R.array.search_visibility)

        val headersVisbDropdown: AutoCompleteTextView = view.findViewById(R.id.dropdown_headers_visibility)
        setupDropdown(headersVisbDropdown, R.array.headers_visibility)

        val maxSelCheck: CheckBox = view.findViewById(R.id.chk_max_selection)
        val maxSelInput: EditText = view.findViewById(R.id.input_max_selection)
        maxSelInput.setText("1")
        maxSelInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) maxSelInput.clearFocus()
            false
        }
        maxSelCheck.setOnCheckedChangeListener { _, isChecked ->
            maxSelInput.isEnabled = isChecked
        }

        val showMaxSelMessCheck: CheckBox = view.findViewById(R.id.chk_show_max_sel_message)
        val showClearBtnCheck: CheckBox = view.findViewById(R.id.chk_show_clear_btn)

        val showSelectBtnCheck: CheckBox = view.findViewById(R.id.chk_show_select_btn)
        showSelectBtnCheck.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                maxSelCheck.isChecked = true
                maxSelInput.setText("1")
            }
            maxSelCheck.isEnabled = isChecked
            maxSelInput.isEnabled = isChecked
        }

        val darkThemeCheck: CheckBox = view.findViewById(R.id.chk_dark_theme)
        darkThemeCheck.setOnCheckedChangeListener { _, isChecked ->
            // Enable or disable dark theme without restarting the app.
            AppCompatDelegate.setDefaultNightMode(if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            })
        }

        val iconsRcv: RecyclerView = view.findViewById(R.id.rcv_icon_list)
        iconsAdapter = IconsAdapter()
        iconsRcv.adapter = iconsAdapter
        iconsRcv.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            // Create settings and show icon dialog.
            iconDialog.settings = IconDialogSettings {
                this.iconFilter = iconFilter
                titleVisibility = when (titleVisbDropdown.listSelection) {
                    0 -> IconDialog.TitleVisibility.NEVER
                    1 -> IconDialog.TitleVisibility.ALWAYS
                    else -> IconDialog.TitleVisibility.IF_SEARCH_HIDDEN
                }
                searchVisibility = when (searchVisbDropdown.listSelection) {
                    0 -> IconDialog.SearchVisibility.NEVER
                    1 -> IconDialog.SearchVisibility.ALWAYS
                    else -> IconDialog.SearchVisibility.IF_LANGUAGE_AVAILABLE
                }
                headersVisibility = when (searchVisbDropdown.listSelection) {
                    0 -> IconDialog.HeadersVisibility.SHOW
                    1 -> IconDialog.HeadersVisibility.HIDE
                    else -> IconDialog.HeadersVisibility.STICKY
                }
                maxSelection = if (maxSelCheck.isChecked) {
                    maxSelInput.text.toString().toIntOrNull() ?: 1
                } else {
                    IconDialogSettings.NO_MAX_SELECTION
                }
                showMaxSelectionMessage = showMaxSelMessCheck.isChecked
                showSelectBtn = showSelectBtnCheck.isChecked
                showClearBtn = showClearBtnCheck.isChecked
            }
            iconDialog.show(childFragmentManager, ICON_DIALOG_TAG)
        }

        if (state != null) {
            // Restore state
            selectedIcons = state.getIntegerArrayList("selectedIconIds")!!
                    .map { iconPack.getIcon(it)!! }
        }

        return view
    }

    private inline fun setupDropdown(dropdown: AutoCompleteTextView,
                                     @ArrayRes items: Int,
                                     crossinline onItemSelected: (pos: Int) -> Unit = {}) {
        val context = requireContext()
        val adapter = DropdownAdapter(context, context.resources.getStringArray(items).toList())
        dropdown.setAdapter(adapter)
        dropdown.setOnItemClickListener { _, _, pos, _ ->
            dropdown.requestLayout()
            onItemSelected(pos)
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        state.putIntegerArrayList("selectedIconIds", selectedIcons.mapTo(ArrayList()) { it.id })
    }

    // Called by icon dialog to get the icon pack.
    override val iconDialogIconPack: IconPack
        get() = iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        // Called by icon dialog when icons were selected.
        selectedIcons = icons
        iconsAdapter.notifyDataSetChanged()
    }

    /**
     * Custom AutoCompleteTextView adapter to disable filtering since we want it to act like a spinner.
     */
    private class DropdownAdapter(context: Context, items: List<String> = mutableListOf()) :
            ArrayAdapter<String>(context, R.layout.item_dropdown, items) {

        override fun getFilter() = object : Filter() {
            override fun performFiltering(constraint: CharSequence?) = null
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
        }
    }

    private inner class IconsAdapter : RecyclerView.Adapter<IconsAdapter.IconViewHolder>() {

        init {
            setHasStableIds(true)
        }

        private inner class IconViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val iconView: ImageView = view.findViewById(R.id.imv_icon)
            private val iconIdTxv: TextView = view.findViewById(R.id.txv_icon_id)
            private val catgTxv: TextView = view.findViewById(R.id.txv_icon_catg)

            fun bind(icon: Icon) {
                iconView.setImageDrawable(icon.drawable)
                iconIdTxv.text = getString(R.string.icon_id_fmt, icon.id)
                catgTxv.text = iconPack.getCategory(icon.categoryId)?.name

                val tags = mutableListOf<String>()
                for (tagName in icon.tags) {
                    val tag = iconPack.getTag(tagName) as? NamedTag ?: continue
                    tags += if (tag.value != null) {
                        tag.value!!.value
                    } else {
                        "{${tag.aliases.joinToString { it.value }}}"
                    }
                }
                if (tags.isNotEmpty()) {
                    val text = tags.joinToString()
                    itemView.setOnClickListener {
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun getItemCount() = selectedIcons.size

        override fun getItemId(pos: Int) = selectedIcons[pos].id.toLong()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                IconViewHolder(layoutInflater.inflate(R.layout.item_icon, parent, false))

        override fun onBindViewHolder(holder: IconViewHolder, pos: Int) =
                holder.bind(selectedIcons[pos])

    }

    companion object {
        private const val ICON_DIALOG_TAG = "icon-dialog"
    }

}
