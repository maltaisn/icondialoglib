/*
 * Copyright (c) 2018 Nicolas Maltais
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.maltaisn.icondialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue", "SameParameterValue"})
public class IconDialog extends DialogFragment {

    private static final String TAG = IconDialog.class.getSimpleName();

    public static final int VISIBILITY_ALWAYS = 0;
    public static final int VISIBILITY_NEVER = 1;
    public static final int VISIBILITY_IF_LANG_AVAILABLE = 2;
    public static final int VISIBILITY_IF_NO_SEARCH = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {VISIBILITY_ALWAYS, VISIBILITY_NEVER, VISIBILITY_IF_LANG_AVAILABLE})
    public @interface SearchVisibility {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {VISIBILITY_ALWAYS, VISIBILITY_NEVER, VISIBILITY_IF_NO_SEARCH})
    public @interface TitleVisibility {}

    public static final int MAX_SELECTION_NONE = -1;

    // Delay after the last search character is typed when search is made
    private static final int SEARCH_DELAY = 250;

    private Context context;
    private IconHelper iconHelper;

    private IconLayoutManager iconListLayout;
    private Button selectBtn;
    private Button clearBtn;

    private int[] maxDialogDimensions;
    private int iconSize;
    private int[] iconColors;

    private @SearchVisibility int searchVisibility;
    private @Nullable Locale searchLanguage;
    private boolean showHeaders;
    private boolean stickyHeaders;
    private boolean showSelectBtn;
    private int maxSelection;
    private boolean maxSelShowMessage;
    private @Nullable String maxSelMessage;
    private boolean showClearBtn;
    private @TitleVisibility int dialogTitleVisibility;
    private @Nullable String dialogTitle;
    private BaseIconFilter iconFilter;
    private boolean loadIconDrawables;

    private List<Item> listItems;
    private List<Item> selectedItems;
    private @Nullable int[] selectedIconsId;
    private String searchText;

    private boolean searchIgnoreDelay;

    /**
     * Create a new icon dialog with default settings
     */
    public IconDialog() {
        searchVisibility = VISIBILITY_IF_LANG_AVAILABLE;
        searchLanguage = null;

        showHeaders = true;
        stickyHeaders = true;

        dialogTitleVisibility = VISIBILITY_IF_NO_SEARCH;
        dialogTitle = null;

        showSelectBtn = true;
        maxSelection = 1;
        maxSelShowMessage = false;
        showClearBtn = false;
        selectedItems = new ArrayList<>();
        selectedIconsId = null;

        iconFilter = new IconFilter();

        loadIconDrawables = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        iconHelper = IconHelper.getInstance(getActivity());

        iconFilter.iconHelper = iconHelper;

        // Get style attributes value
        TypedArray ta = context.obtainStyledAttributes(R.styleable.IconDialog);
        maxDialogDimensions = new int[]{
                ta.getDimensionPixelSize(R.styleable.IconDialog_icdMaxWidth, -1),
                ta.getDimensionPixelSize(R.styleable.IconDialog_icdMaxHeight, -1),
        };

        iconSize = ta.getDimensionPixelSize(R.styleable.IconDialog_icdIconSize, -1);
        iconColors = new int[]{
                ta.getColor(R.styleable.IconDialog_icdIconColor, 0),
                ta.getColor(R.styleable.IconDialog_icdSelectedIconColor, 0),
        };

        maxSelMessage = ta.getString(R.styleable.IconDialog_icdMaxSelectionMessage);

        if (loadIconDrawables) {
            iconHelper.loadIconDrawables();
        }

        ta.recycle();
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle state) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.icd_dialog_icon, null);

        TextView titleTxv = view.findViewById(R.id.txv_title);
        final EditText searchEdt = view.findViewById(R.id.edt_search);
        final ImageView searchImv = view.findViewById(R.id.imv_search);
        final ImageView cancelSearchImv = view.findViewById(R.id.imv_cancel_search);
        final RecyclerView iconListRcv = view.findViewById(R.id.rcv_icon_list);
        final TextView noResultTxv = view.findViewById(R.id.txv_no_result);
        final Button cancelBtn = view.findViewById(R.id.btn_cancel);
        selectBtn = view.findViewById(R.id.btn_select);
        clearBtn = view.findViewById(R.id.btn_clear);

        // Show search bar if necessary
        final boolean searchShown = isSearchAvailable();
        if (searchShown) {
            searchEdt.setVisibility(View.VISIBLE);
            searchImv.setVisibility(View.VISIBLE);
        } else {
            searchEdt.setVisibility(View.GONE);
            searchImv.setVisibility(View.GONE);
        }

        // Show title if necessary
        boolean titleShown = dialogTitleVisibility == VISIBILITY_ALWAYS ||
                dialogTitleVisibility == VISIBILITY_IF_NO_SEARCH && !searchShown;
        if (titleShown) {
            if (dialogTitle != null) {
                titleTxv.setText(dialogTitle);
            }
        } else {
            titleTxv.setVisibility(View.GONE);
            if (!searchShown) {
                view.findViewById(R.id.div_header).setVisibility(View.GONE);
            }
        }

        if (!searchShown && !titleShown) {
            // Remove ConstraintLayout padding to have no space when scrolling the list
            view.setPadding(0, 0, 0, 0);
        }

        // Set up icon recycler view layout and adapter
        final IconAdapter adapter = new IconAdapter();
        iconListRcv.setAdapter(adapter);

        iconListLayout = new IconLayoutManager(context, iconSize);
        iconListLayout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < listItems.size() && adapter.getItemViewType(position) == Item.TYPE_HEADER) {
                    return iconListLayout.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        iconListRcv.setLayoutManager(iconListLayout);

        if (stickyHeaders) {
            iconListRcv.addItemDecoration(new StickyHeaderDecoration(iconListRcv, adapter));
        }

        // Set up search bar
        final Handler searchHandler = new Handler();
        final Runnable searchRunnable = new Runnable() {
            @Override
            public void run() {
                listItems = getListItems(searchEdt.getText().toString());
                adapter.notifyDataSetChanged();

                noResultTxv.setVisibility(listItems.size() > 0 ? View.GONE : View.VISIBLE);
            }
        };
        searchEdt.setText(searchText);
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable text) {
                if (text.length() > 0) {
                    cancelSearchImv.setVisibility(View.VISIBLE);
                } else if (text.length() == 0) {
                    cancelSearchImv.setVisibility(View.GONE);
                }

                searchHandler.removeCallbacks(searchRunnable);
                if (searchIgnoreDelay) {
                    searchHandler.post(searchRunnable);
                    searchIgnoreDelay = false;
                } else {
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
                }
                searchText = text.toString();
            }
        });
        searchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Hide keyboard
                    searchEdt.clearFocus();
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    //noinspection ConstantConditions
                    imm.hideSoftInputFromWindow(searchEdt.getWindowToken(), 0);

                    // Do search
                    searchHandler.removeCallbacks(searchRunnable);
                    searchHandler.post(searchRunnable);

                    return true;
                }
                return false;
            }
        });

        // Set cancel search icon to remove search if clicked
        cancelSearchImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIgnoreDelay = true;
                searchEdt.setText(null);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSelectCallback();
                dismiss();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unselect all selected icons
                int[] pos = getItemsPosition(selectedItems.toArray(new Item[selectedItems.size()]));
                for (int p : pos) {
                    listItems.get(p).isSelected = false;
                    adapter.notifyItemChanged(p);
                }
                selectedItems.clear();

                clearBtn.setVisibility(View.GONE);
                selectBtn.setEnabled(false);
            }
        });

        // Hide button bar if no preview is shown
        if (!showSelectBtn) {
            cancelBtn.setVisibility(View.GONE);
            selectBtn.setVisibility(View.GONE);
            view.findViewById(R.id.div_footer).setVisibility(View.GONE);
        }

        if (state == null) {
            listItems = getListItems(null);
            if (selectedItems.size() > 0) {
                int firstSelectedPos = getItemsPosition(selectedItems.get(0))[0];
                iconListLayout.scrollToPositionWithOffset(firstSelectedPos, iconSize);
                // Arbitrary offset just so list doesn't scroll right under sticky header
            } else {
                selectBtn.setEnabled(false);
            }

        } else {
            if (searchText != null && !searchText.isEmpty()) {
                cancelSearchImv.setVisibility(View.VISIBLE);
            }

            iconListLayout.onRestoreInstanceState(state.getParcelable("listLayoutState"));
        }

        boolean showClear = showClearBtn && selectedItems.size() > 0;
        clearBtn.setVisibility(showClear ? View.VISIBLE : View.GONE);

        // Set up dialog
        final Dialog dialog = new Dialog(context);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Get maximum dialog dimensions
                Rect fgPadding = new Rect();
                dialog.getWindow().getDecorView().getBackground().getPadding(fgPadding);
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int height = metrics.heightPixels - fgPadding.top - fgPadding.bottom;
                int width = metrics.widthPixels - fgPadding.top - fgPadding.bottom;

                // Set dialog's dimensions
                if (width > maxDialogDimensions[0]) width = maxDialogDimensions[0];
                if (height > maxDialogDimensions[1]) height = maxDialogDimensions[1];
                dialog.getWindow().setLayout(width, height);

                // Set dialog's content
                view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                dialog.setContentView(view);
            }
        });

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        state.putParcelable("listLayoutState", iconListLayout.onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Wrap icon dialog's theme to context
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.icdStyle});
        int style = ta.getResourceId(0, R.style.IcdStyle);
        ta.recycle();
        this.context = new ContextThemeWrapper(context, style);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        context = null;
        iconHelper.stopLoadingDrawables();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        searchText = null;
        selectedItems = new ArrayList<>();
    }

    private void callSelectCallback() {
        try {
            Icon[] icons = new Icon[selectedItems.size()];
            for (int i = 0; i < icons.length; i++) {
                icons[i] = selectedItems.get(i).icon;
            }

            if (getTargetFragment() != null) {
                // Caller was a fragment
                ((Callback) getTargetFragment()).onIconDialogIconsSelected(icons);
            } else {
                // Caller was an activity
                //noinspection ConstantConditions
                ((Callback) getActivity()).onIconDialogIconsSelected(icons);
            }

        } catch (ClassCastException e) {
            // Callback interface is not implemented by caller
        }
    }

    /**
     * Get the list of icons matching search with category headers
     * @param search null to get whole list, or text to search among icon labels to filter icons
     * @return the list of items
     */
    private List<Item> getListItems(@Nullable String search) {
        // Get list of matching icons
        List<Icon> matchingIcons = iconFilter.getIconsForSearch(search);

        // Set past or initial selection to new items
        // while creating the item list
        List<Item> items = new ArrayList<>(matchingIcons.size());
        if (selectedIconsId != null && selectedIconsId.length > 0) {
            // Set initial selection
            if (maxSelection != MAX_SELECTION_NONE && selectedIconsId.length > maxSelection) {
                // Truncate too big initial selection
                selectedIconsId = Arrays.copyOf(selectedIconsId, maxSelection);
            }

            int selectedIndex = 0;
            for (Icon icon : matchingIcons) {
                Item item = new Item(icon);
                items.add(item);
                if (selectedIndex < selectedIconsId.length && icon.id >= selectedIconsId[selectedIndex]) {
                    if (icon.id == selectedIconsId[selectedIndex]) {
                        item.isSelected = true;
                        selectedItems.add(item);
                    }
                    selectedIndex++;
                }
            }
            selectedIconsId = null;

        } else {
            // Set past selection in new item list
            int selectedIndex = 0;
            Collections.sort(selectedItems, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    return Integer.compare(i1.icon.id, i2.icon.id);
                }
            });
            List<Item> newSel = new ArrayList<>(selectedItems);
            for (Icon icon : matchingIcons) {
                Item item = new Item(icon);
                items.add(item);
                for (int i = 0; i < selectedItems.size(); i++) {
                    Icon selIcon = selectedItems.get(i).icon;
                    if (selIcon.id >= icon.id) {
                        if (selIcon.id == icon.id) {
                            item.isSelected = true;
                            newSel.set(i, item);
                        }
                        break;
                    }
                }
            }
            selectedItems = newSel;
        }

        // Sort the icons
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return iconFilter.compare(i1.icon, i2.icon);
            }
        });

        // Add headers to list
        if (showHeaders && items.size() > 0) {
            int i = 1;
            Category firstCatg = items.get(0).icon.category;
            items.add(0, new Item(firstCatg));
            while (i < items.size() - 1) {
                Category c1 = items.get(i).icon.category;
                Category c2 = items.get(i + 1).icon.category;
                if (c1.id != c2.id) {
                    i++;
                    items.add(i, new Item(c2));
                }
                i++;
            }
        }

        return items;
    }

    /**
     * Get the position of items in the adapter
     * @param items array of icon items (varargs)
     * @return the array of positions
     */
    private int[] getItemsPosition(Item... items) {
        int[] pos = new int[items.length];
        int itemsLeft = items.length;
        for (int i = 0; i < listItems.size(); i++) {
            Item item = listItems.get(i);
            if (item.type == Item.TYPE_ICON) {
                for (int j = 0; j < items.length; j++) {
                    if (item.icon == items[j].icon) {
                        pos[j] = i;
                        itemsLeft--;
                        break;
                    }
                }
            }
            if (itemsLeft == 0) break;
        }
        return pos;
    }

    /**
     * Depending on the setting set at {@link #setSearchEnabled(int, Locale)}, checks
     * whether search will be enabled or not
     * @return true if search is enabled
     */
    public boolean isSearchAvailable() {
        if (searchVisibility == VISIBILITY_ALWAYS) {
            if (searchLanguage == null) {
                searchLanguage = Locale.ENGLISH;
            }
            return true;

        } else if (searchVisibility == VISIBILITY_IF_LANG_AVAILABLE) {
            if (searchLanguage == null) {
                // Get device's language
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    searchLanguage = context.getResources().getConfiguration().getLocales().get(0);
                } else {
                    //noinspection deprecation
                    searchLanguage = context.getResources().getConfiguration().locale;
                }
            }

            // Check if search language is among the languages the library is translated in
            String searchLang = searchLanguage.getLanguage();
            for (String lang : BuildConfig.ICD_LANG) {  // See module gradle file for this
                if (searchLang.equalsIgnoreCase(lang)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Set whether search is enabled or not
     * By default, search is only enabled if device's language is available
     * When search is not enabled, a title will be shown on the dialog instead of the search bar
     * @param visibility {@link #VISIBILITY_ALWAYS} to always enable search.
     *               {@link #VISIBILITY_NEVER} to always disable search.
     *               {@link #VISIBILITY_IF_LANG_AVAILABLE} to enable search only if the language is available
     * @param lang If search is always shown, the default language to use if device's is not available
     *                  Null can be set to use English as the default language
     *             If search is always disabled, set to null
     *             If search is enabled only if the language is available, what is that language.
     *                  Null can be set to use device's language
     * @return the dialog
     */
    public IconDialog setSearchEnabled(@SearchVisibility int visibility, @Nullable Locale lang) {
        searchVisibility = visibility;
        searchLanguage = lang;
        return this;
    }

    /**
     * Set the title of the dialog and when it is shown
     * @param visibility {@link #VISIBILITY_ALWAYS} to always show the title
     *               {@link #VISIBILITY_NEVER} to never show the title
     *               {@link #VISIBILITY_IF_NO_SEARCH} to only show the title if search is not available
     * @param title title of the dialog, use "" to not show one or null to show default one.
     * @return the dialog
     */
    public IconDialog setTitle(@TitleVisibility int visibility, @Nullable String title) {
        dialogTitleVisibility = visibility;
        dialogTitle = title;
        return this;
    }

    /**
     * Set list header options
     * By default, headers are shown and are sticky
     * @param show whether to show the headers or not
     * @param sticky if headers are shown, whether they appear on top of the list when scrolling down
     * @return the dialog
     */
    public IconDialog setShowHeaders(boolean show, boolean sticky) {
        showHeaders = show;
        stickyHeaders = show && sticky;
        return this;
    }

    /**
     * Set whether the select button and the other dialog dialog buttons are shown
     * If not, dialog will be dismissed immediately after an icon is clicked.
     * By default, selection is shown. It is always shown if multiple selection is allowed
     * @param show whether select button is shown or not
     * @return the dialog
     */
    public IconDialog setShowSelectButton(boolean show) {
        showSelectBtn = maxSelection > 1 || show;
        return this;
    }

    /**
     * Set initial selected icons
     * @param iconIds varargs of icons id, null or empty array for no initial selection
     * @return the dialog
     */
    public IconDialog setSelectedIcons(@Nullable int... iconIds) {
        selectedIconsId = iconIds;
        if (iconIds != null && iconIds.length > 1) {
            Arrays.sort(iconIds);
        }
        return this;
    }


    /**
     * Set initial selected icons
     * @param icons varargs of icons, null or empty array for no initial selection
     * @return the dialog
     */
    public IconDialog setSelectedIcons(@Nullable Icon... icons) {
        if (icons != null) {
            int[] ids = new int[icons.length];
            for (int i = 0; i < icons.length; i++) {
                if (icons[i] == null) {
                    throw new IllegalArgumentException("Selected icon at index " + i + " is null");
                }
                ids[i] = icons[i].id;
            }
            return setSelectedIcons(ids);
        } else {
            selectedIconsId = null;
            return this;
        }
    }

    /**
     * Set maximum number of icons that can be selected
     * @param max maximum number
     * @param showMessage If true, a message will be shown when maximum selection is reached
     *                    User will need to deselect icons to select others
     *                    If false, no message will be shown and first selected icon will
     *                    be deselect to allow new selection
     * @return the dialog
     */
    public IconDialog setMaxSelection(int max, boolean showMessage) {
        if (max != MAX_SELECTION_NONE && max <= 0) {
            throw new IllegalArgumentException("Max selection must be MAX_SELECTION_NONE or strictly positive.");
        }

        maxSelection = max;
        maxSelShowMessage = showMessage;

        // If selecting more than one icon or showing max selection message, dialog buttons must be shown
        if (max > 1 || showMessage) showSelectBtn = true;

        return this;
    }

    /**
     * Set whether to show the neutral clear button to unselect all icons
     * By default, this button is not shown.
     * @param show whether to show it or not
     * @return the dialog
     */
    public IconDialog setShowClearButton(boolean show) {
        showClearBtn = show;
        return this;
    }

    /**
     * Set the icon filter class for searching and sorting icons.
     * By default, icons will match search if one of their label has part of the search text
     * and will be sorted by category, then by labels and then by ID
     * in its value. Multiple search terms can be separated with either " ", "," or ";"
     * A custom icon searcher can be set by subclassing {@link IconFilter}
     * @param filter icon filter
     * @return the dialog
     */
    public IconDialog setIconFilter(BaseIconFilter filter) {
        iconFilter = filter;
        if (iconHelper != null) {
            iconFilter.iconHelper = iconHelper;
        }
        return this;
    }

    /**
     * Get the IconFilter used to search and sort icons.
     * You can set additionnal settings with it
     * @return the icon searcher
     * @see IconFilter#setDisabledCategories(int...)
     * @see IconFilter#setDisabledIcons(int...)
     */
    public BaseIconFilter getIconFilter() {
        return iconFilter;
    }


    /**
     * Set whether all icon drawables will be preloaded when dialog is shown to allow a smoother
     * scrolling in the icon list. By default, drawables are preloaded.
     * @param load whether to load them or not
     * @return the dialog
     * @see IconHelper#loadIconDrawables()
     * @see IconHelper#freeIconDrawables()
     */
    public IconDialog setLoadIconDrawables(boolean load) {
        loadIconDrawables = load;
        return this;
    }


    static class Item {

        static final int TYPE_ICON = 0;
        static final int TYPE_HEADER = 1;

        final int type;
        final Icon icon;
        final Category category;
        boolean isSelected;

        Item(Icon icon) {
            this.icon = icon;
            this.category = icon.category;
            type = TYPE_ICON;
        }

        Item(Category category) {
            this.icon = null;
            this.category = category;
            type = TYPE_HEADER;
        }

        int getId() {
            if (type == TYPE_ICON) {
                //noinspection ConstantConditions
                return icon.id;
            } else {
                return -(category.id + 1);
            }
        }

    }

    private class IconAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements StickyHeaderDecoration.StickyHeaderImpl {

        private RecyclerView parent;

        @SuppressWarnings("ConstantConditions")
        @SuppressLint("InflateParams")
        IconAdapter() {
            setHasStableIds(true);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            parent = recyclerView;
        }

        class IconViewHolder extends RecyclerView.ViewHolder {

            private ImageView iconImv;

            IconViewHolder(View view) {
                super(view);
                iconImv = (ImageView) view;
            }

            void bindViewHolder(final Item item) {
                iconImv.setImageDrawable(item.icon.getDrawable(context));
                iconImv.setOnClickListener(new View.OnClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onClick(View v) {
                        if (item.icon.noDrawable) {
                            return;  // Can't select unavailable icon
                        }

                        // Icon clicked, select it
                        if (showSelectBtn) {
                            int itemPos = getAdapterPosition();

                            if (item.isSelected) {
                                item.isSelected = false;
                                selectedItems.remove(item);
                            } else {
                                if (selectedItems.size() == maxSelection) {
                                    if (maxSelShowMessage) {
                                        // Show message and don't select icon
                                        Toast.makeText(context, maxSelMessage, Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        // Remove first selected icon, select new one
                                        Item first = selectedItems.remove(0);
                                        int pos = getItemsPosition(first)[0];
                                        first.isSelected = false;
                                        notifyItemChanged(pos);

                                        item.isSelected = true;
                                        selectedItems.add(item);
                                    }
                                } else {
                                    item.isSelected = true;
                                    selectedItems.add(item);
                                }
                            }
                            notifyItemChanged(itemPos);

                            // Update dialog buttons
                            selectBtn.setEnabled(selectedItems.size() > 0);
                            if (showClearBtn) {
                                clearBtn.setVisibility(selectedItems.size() > 0 ? View.VISIBLE : View.GONE);
                            }

                        } else {
                            if (selectedItems.size() > 0) {
                                Item oldItem = selectedItems.remove(0);
                                oldItem.isSelected = false;
                            }
                            item.isSelected = true;
                            selectedItems.add(item);

                            // Send result to caller
                            callSelectCallback();
                            dismiss();
                        }
                    }
                });


                iconImv.setAlpha(item.icon.noDrawable ? 0.3f : 1.0f);
                if (item.isSelected) {
                    // Icon is selected
                    iconImv.setColorFilter(iconColors[1], PorterDuff.Mode.SRC_IN);
                } else {
                    iconImv.setColorFilter(iconColors[0], PorterDuff.Mode.SRC_IN);
                }
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            private TextView headerTxv;

            HeaderViewHolder(View view) {
                // Used with both "icd_item_header" and "icd_item_sticky_header" layouts
                super(view);
                headerTxv = view.findViewById(R.id.header_txv);
            }

            void bindViewHolder(final Item item) {
                headerTxv.setText(item.category.nameResId);
            }
        }

        @Override
        public @NonNull RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == Item.TYPE_ICON) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.icd_item_icon, parent, false);
                return new IconViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.icd_item_header, parent, false);
                return new HeaderViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            final Item item = listItems.get(position);
            if (item.type == Item.TYPE_ICON) {
                ((IconViewHolder) holder).bindViewHolder(item);
            } else {
                ((HeaderViewHolder) holder).bindViewHolder(item);
            }
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        @Override
        public long getItemId(int position) {
            return listItems.get(position).getId();
        }

        @Override
        public int getItemViewType(int position) {
            return listItems.get(position).type;
        }


        // Sticky header methods

        @Override
        public boolean isHeader(int position) {
            return listItems.get(position).type == Item.TYPE_HEADER;
        }

        @Override
        public int getHeaderPositionForItem(int position) {
            do {
                if (isHeader(position)) return position;
                position--;
            } while (position >= 0);
            return -1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder() {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.icd_item_sticky_header, parent, false);
            return new HeaderViewHolder(v);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            onBindViewHolder(viewHolder, position);
        }
    }

    public interface Callback {
        void onIconDialogIconsSelected(Icon[] icons);
    }

}
