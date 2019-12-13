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


import android.util.SparseArray;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Base class used to search icons by label
 * Can be subclassed to customize the search algorithm
 * By default, dialog uses {@link IconFilter} subclass
 * This class does no search, it only filters enabled categories and disabled icons
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseIconFilter implements Comparator<Icon> {

    private static final String TAG = BaseIconFilter.class.getSimpleName();

    protected IconHelper iconHelper;

    private @Nullable BitSet disabledCategories;
    private @Nullable BitSet disabledIcons;


    public BaseIconFilter() {
        iconHelper = null;

        disabledCategories = null;
        disabledIcons = null;
    }

    /**
     * Get a list of all matching icons for a search text
     * @param search text to search in labels, use null to get all icons
     * @return the list of matching icons.
     *         Must be sorted by icon ID! You can just remove items that don't match
     *         from the list returned by BaseIconFilter to keep the sorted order.
     */
    @CallSuper
    @NonNull
    public List<Icon> getIconsForSearch(@Nullable String search) {
        if (iconHelper == null) {
            throw new IllegalStateException("Icon helper was not set for icon filter.");
        }

        // Get all enabled icons
        SparseArray<Icon> allIcons = iconHelper.getIcons();
        List<Icon> enabledIcons = new ArrayList<>();
        for (int i = 0; i < allIcons.size(); i++) {
            Icon icon = allIcons.valueAt(i);
            if ((disabledCategories == null || !disabledCategories.get(icon.category.id)) &&
                    (disabledIcons == null || !disabledIcons.get(icon.id))) {
                // Icon's category is shown and icon is enabled
                enabledIcons.add(icon);
            }
        }

        return enabledIcons;
    }

    /**
     * Compare two icons. All icons of a same categories MUST be grouped together if headers of
     * the icon list are shown, or undefined behavior will happen.
     * By default, icons are sorted by category, then by labels, then by ID
     * @param icon1 first icon
     * @param icon2 second icon
     * @return -1 to show icon1 before icon2, otherwise 1
     */
    @Override
    public int compare(@NonNull Icon icon1, @NonNull Icon icon2) {
        int result = compareIntegers(icon1.category.id, icon2.category.id);
        if (result == 0) {
            int len1 = icon1.labels.length;
            int len2 = icon2.labels.length;
            for (int i = 0; i < Math.min(len1, len2); i++) {
                Label label1 = icon1.labels[i];
                Label label2 = icon2.labels[i];
                if (label1 == null || label2 == null) continue;
                result = label1.compareTo(label2);
                if (result != 0) {
                    break;
                }
            }
            if (result == 0) {
                if (len1 != len2) {
                    return compareIntegers(len1, len2);
                } else {
                    return compareIntegers(icon1.id, icon2.id);
                }
            }
        }
        return result;
    }

    static int compareIntegers(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * Set which categories will not be shown in the list
     * By default, no categories are disabled
     * @param categories array or varargs of category IDs to disable
     * @return the filter
     */
    public BaseIconFilter setDisabledCategories(@Nullable int... categories) {
        if (categories != null) {
            // Convert array of categories to a bit set for faster access
            disabledCategories = new BitSet();
            for (int id : categories) {
                disabledCategories.set(id);
            }
        }
        return this;
    }

    /**
     * Set which icons will not be shown in the list
     * By default, no icons are disabled
     * @param icons array or varargs of icon IDs to disable
     * @return the filter
     */
    public BaseIconFilter setDisabledIcons(@Nullable int... icons) {
        if (icons != null) {
            // Convert array of icons to a bit set for faster access
            disabledIcons = new BitSet();
            for (int id : icons) {
                disabledIcons.set(id);
            }
        }
        return this;
    }

}
