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


import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Default icon searcher used by IconDialog
 * Can be subclassed to provide custom implementation of {@link #matchesSearch(String[], LabelValue)},
 * used for matching search terms with icon labels.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class IconFilter extends BaseIconFilter {

    private static final String TAG = IconFilter.class.getSimpleName();

    private @Nullable String termPattern;
    private boolean normalizeSearch;
    private boolean enabledIdSearch;

    /**
     * Create new icon filter with default settings
     */
    public IconFilter() {
        super();

        termPattern = "[;, ]";
        normalizeSearch = true;
        enabledIdSearch = false;
    }

    /**
     * Set whether to normalize search text, removing all diacritics,
     * all unicode characters, hyphens, apostrophes and more
     * By default, text is normalized
     * @param normalize whether to normalize
     * @see IconHelper#normalizeText(String)
     */
    public IconFilter setNormalizeSearch(boolean normalize) {
        this.normalizeSearch = normalize;
        return this;
    }

    /**
     * Set pattern used to split search text into multiple terms
     * By default, terms are split on pattern "[;, ]"
     * @param pattern regex pattern to use, or null to do no split
     */
    public IconFilter setTermSplitPattern(@Nullable String pattern) {
        termPattern = pattern;
        return this;
    }

    /**
     * If enabled, user can search icons by ID with "#xxx"
     * This is usually for debug purposes
     * @param enabled whether to enable or not
     */
    public IconFilter setIdSearchEnabled(boolean enabled) {
        enabledIdSearch = enabled;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Icon> getIconsForSearch(@Nullable String search) {
        if (enabledIdSearch && search != null && search.startsWith("#")) {
            try {
                int id = Integer.valueOf(search.substring(1));
                if (id >= 0) {
                    Icon icon = iconHelper.getIcon(id);
                    if (icon != null) {
                        List<Icon> matchingIcons = new ArrayList<>();
                        matchingIcons.add(icon);
                        return matchingIcons;
                    }
                }
            } catch (NumberFormatException e) {
                // Don't do ID search, invalid ID
            }
        }

        List<Icon> matchingIcons = super.getIconsForSearch(search);

        String[] searchTerms = null;
        if (search != null) {
            // Split search into terms
            if (termPattern == null) {
                searchTerms = new String[]{search};
            } else {
                searchTerms = search.split(termPattern);
            }

            if (normalizeSearch) {
                search = IconHelper.normalizeText(search);
            } else {
                search = search.toLowerCase();
            }
        }

        // Remove all icons that don't match any of the search terms
        for (int i = matchingIcons.size() - 1; i >= 0; i--) {
            boolean matches = false;
            if (search == null || search.isEmpty()) {
                matches = true;
            } else {
                Icon icon = matchingIcons.get(i);
                for (Label label : icon.labels) {
                    if (label.aliases != null) {
                        for (LabelValue alias : label.aliases) {
                            if (matchesSearch(searchTerms, alias)) {
                                matches = true;
                                break;
                            }
                        }
                    } else if (label.value != null) {
                        if (matchesSearch(searchTerms, label.value)) {
                            matches = true;
                            break;
                        }
                    }
                }
            }

            if (!matches) matchingIcons.remove(i);
        }

        return matchingIcons;
    }

    /**
     * Check if a reference text contains search or search contains reference text
     * @param searchTerms search terms, lowercase
     * @param label reference text
     * @return whether it matches search or not
     */
    protected boolean matchesSearch(String[] searchTerms, LabelValue label) {
        String text = (normalizeSearch ? label.normValue : label.value);
        for (String term : searchTerms) {
            if (term.length() == text.length() && term.equals(text) ||
                    term.length() < text.length() && text.contains(term)) {
                // Term is in search or search is in term
                return true;
            }
        }
        return false;
    }

}
