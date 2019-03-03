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
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;

@SuppressWarnings({"WeakerAccess", "unused"})
public class IconHelper {

    private static final String TAG = IconHelper.class.getSimpleName();

    private static final String XML_TAG_LIST = "list";
    private static final String XML_TAG_LABEL = "label";
    private static final String XML_TAG_ICON = "icon";
    private static final String XML_TAG_CATEGORY = "category";
    private static final String XML_TAG_ALIAS = "alias";
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_ID = "id";
    private static final String XML_ATTR_LABELS = "labels";
    private static final String XML_ATTR_PATH = "path";
    private static final String XML_ATTR_CATEGORY = "category";

    public static final int CATEGORY_PEOPLE = 0;
    public static final int CATEGORY_HOME = 1;
    public static final int CATEGORY_TECHNOLOGY = 2;
    public static final int CATEGORY_FINANCE = 3;
    public static final int CATEGORY_LEISURE = 4;
    public static final int CATEGORY_TRANSPORT = 5;
    public static final int CATEGORY_FOOD = 6;
    public static final int CATEGORY_BUILDINGS = 7;
    public static final int CATEGORY_ARTS = 8;
    public static final int CATEGORY_SPORTS = 9;
    public static final int CATEGORY_AUDIO = 10;
    public static final int CATEGORY_PHOTO = 11;
    public static final int CATEGORY_NATURE = 12;
    public static final int CATEGORY_SCIENCE = 13;
    public static final int CATEGORY_TIME = 14;
    public static final int CATEGORY_TOOLS = 15;
    public static final int CATEGORY_COMPUTER = 16;
    public static final int CATEGORY_ARROWS = 17;
    public static final int CATEGORY_SYMBOLS = 18;

    // Warning here is ignored see: http://stackoverflow.com/a/40235834/5288316
    @SuppressLint("StaticFieldLeak")
    private static IconHelper INSTANCE;
    private final Context context;

    private SparseArray<Icon> icons;
    private List<Label> labels;
    private List<Label> groupLabels;
    private SparseArray<Category> categories;

    private @XmlRes
    int extraIconsXml;
    private @XmlRes
    int extraLabelsXml;
    private boolean extraIconsSet;
    private boolean extraIconsLoadPending;

    private boolean dataLoaded;
    private TaskExecutor dataLoader;
    private TaskExecutor drawablesLoader;
    private List<LoadCallback> loadCallbacks;

    private IconHelper(Context context) {
        this.context = context.getApplicationContext();

        extraIconsSet = false;

        dataLoaded = false;

        dataLoader = new TaskExecutor();
        dataLoader.execute(new Runnable() {
            @Override
            public void run() {
                loadLabels(R.xml.icd_labels, false);
                loadIcons(R.xml.icd_icons, false);

                if (extraIconsLoadPending) {
                    // A call to load extra icons was made during this time
                    extraIconsLoadPending = false;
                    loadExtraIcons();
                }
                dataLoaded = true;
            }
        }, new TaskExecutor.Callback() {
            @Override
            public void onDone() {
                callLoadCallbacks();
            }
        });

        drawablesLoader = new TaskExecutor();
    }

    /**
     * Get the instance of IconHelper
     *
     * @param context any context
     * @return the instance
     */
    public static synchronized IconHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new IconHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    /**
     * @return true if data is loaded, false if not loaded, i.e currently loading icons or labels
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Get an icon
     *
     * @param id id of the icon
     * @return the icon, null if it doesn't exist or if data isn't loaded
     */
    @Nullable
    public Icon getIcon(int id) {
        if (!dataLoaded) return null;
        return icons.get(id);
    }

    SparseArray<Icon> getIcons() {
        return icons;
    }

    /**
     * Get a label by name
     *
     * @param name name of the label
     * @return the label, null if it doesn't exist or if data isn't loaded
     */
    @Nullable
    public Label getLabel(String name) {
        if (!dataLoaded) return null;

        name = name.toLowerCase();
        if (name.startsWith("_")) {
            name = name.substring(1);
            int index = Collections.binarySearch(groupLabels, name);
            if (index >= 0) {
                return groupLabels.get(index);
            }
        } else {
            int index = Collections.binarySearch(labels, name);
            if (index >= 0) {
                return labels.get(index);
            }
        }
        return null;
    }

    /**
     * Get a category
     *
     * @param id id of the category
     * @return the category or null if it doesn't exist or if data isn't loaded
     */
    @Nullable
    public Category getCategory(int id) {
        if (!dataLoaded) return null;
        return categories.get(id);
    }

    /**
     * @return the number of loaded icons
     * Returns 0 if data is not loaded
     */
    public int getIconCount() {
        if (!dataLoaded) return 0;
        return icons.size();
    }

    /**
     * Add extra icons for the dialog.
     * This can only be called once, subsequent calls will have no effect
     * Both XML resources must be valid, no error checking is done
     *
     * @param iconXml  xml file containing the icons
     * @param labelXml xml file containing the labels used by the icons
     */
    public synchronized void addExtraIcons(@XmlRes int iconXml, @XmlRes int labelXml) {
        if (extraIconsSet) return;

        extraIconsXml = iconXml;
        extraLabelsXml = labelXml;
        extraIconsSet = true;

        if (dataLoader.isRunning()) {
            extraIconsLoadPending = true;

        } else {
            dataLoaded = false;
            dataLoader.execute(new Runnable() {
                @Override
                public void run() {
                    loadExtraIcons();
                }
            }, new TaskExecutor.Callback() {
                @Override
                public void onDone() {
                    dataLoaded = true;
                    callLoadCallbacks();
                }
            });
        }
    }

    private void loadExtraIcons() {
        loadLabels(extraLabelsXml, true);
        loadIcons(extraIconsXml, true);
    }

    /**
     * Load label names from XML file
     * This must be called by a language BroadcastListener and manually
     * if you change your app's language without restarting.
     */
    public void reloadLabels() {
        if (dataLoader.isRunning()) {
            // Labels are currently being loaded
            return;
        }

        dataLoaded = false;
        dataLoader.execute(new Runnable() {
            @Override
            public void run() {
                loadLabels(R.xml.icd_labels, false);
                if (extraIconsSet) {
                    loadLabels(extraLabelsXml, true);
                }

                // Replace old labels references to new ones
                for (int i = 0; i < icons.size(); i++) {
                    Icon icon = icons.valueAt(i);
                    for (int j = 0; j < icon.labels.length; j++) {
                        Label label = icon.labels[j];
                        if (!label.isGroupLabel()) {
                            int index = Collections.binarySearch(labels, label.name);
                            icon.labels[j] = (index >= 0 ? labels.get(index) : null);
                        }
                    }
                }
            }
        }, new TaskExecutor.Callback() {
            @Override
            public void onDone() {
                dataLoaded = true;
                callLoadCallbacks();
            }
        });
    }

    /**
     * Load icons and categories from XML file
     */
    private synchronized void loadIcons(@XmlRes int xmlFile, boolean append) {
        if (icons == null || !append) {
            icons = new SparseArray<>();
            categories = new SparseArray<>();
            groupLabels = new ArrayList<>();
        }

        ArrayList<GroupLabelRef> groupLabelRefs = new ArrayList<>();

        XmlPullParser parser = context.getResources().getXml(xmlFile);
        try {
            Category category = null;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if (tagName.equalsIgnoreCase(XML_TAG_CATEGORY)) {
                        // <category id="##" name="@string/name">
                        String idStr = parser.getAttributeValue(null, XML_ATTR_ID);
                        String nameStr = parser.getAttributeValue(null, XML_ATTR_NAME);
                        int id = Integer.valueOf(idStr);
                        int resId = 0;
                        if (nameStr != null) {
                            if (nameStr.startsWith("@string/")) {
                                // There's an ADB bug where the string reference isn't changed to an ID.
                                // This detects and prevents it.
                                resId = context.getResources().getIdentifier(
                                        nameStr.substring(8), "string", context.getPackageName());
                            } else {
                                resId = Integer.valueOf(nameStr.substring(1));
                            }
                        }

                        category = categories.get(id);
                        if (category == null) {
                            // Only add category if not already added
                            category = new Category(id, resId);
                            categories.put(id, category);
                        } else {
                            // Overwrite name
                            if (resId != 0) {
                                category.nameResId = resId;
                            }
                        }

                        String catgStr = parser.getAttributeValue(null, XML_ATTR_CATEGORY);

                    } else if (tagName.equalsIgnoreCase(XML_TAG_ICON)) {
                        // <icon id="0" labels="label1,label2,label3"/>
                        int id = Integer.valueOf(parser.getAttributeValue(null, XML_ATTR_ID));
                        String allLabelsStr = parser.getAttributeValue(null, XML_ATTR_LABELS);
                        String pathStr = parser.getAttributeValue(null, XML_ATTR_PATH);
                        String catgStr = parser.getAttributeValue(null, XML_ATTR_CATEGORY);

                        byte[] pathData = null;
                        if (pathStr != null) {
                            pathData = pathStr.getBytes();
                        } else {
                            // Missing path attribute, check if can inherit
                            Icon parent = icons.get(id);
                            if (parent != null) {
                                pathData = parent.pathData;
                            }  // else icon is missing attribute, error
                        }
                        assert pathData != null;

                        Category iconCatg;
                        if (catgStr != null && category == null) {
                            iconCatg = categories.get(Integer.valueOf(catgStr));
                        } else {
                            assert category != null;
                            iconCatg = category;
                        }

                        Icon icon = new Icon(id, iconCatg, null, pathData);

                        // Find the ID for each label
                        if (allLabelsStr != null) {
                            String[] labelsStr = allLabelsStr.split(",");
                            icon.labels = new Label[labelsStr.length];
                            for (int i = 0; i < labelsStr.length; i++) {
                                String labelName = labelsStr[i];
                                if (labelName.charAt(0) == '_') {
                                    labelName = labelName.substring(1);  // Remove the "_"
                                    groupLabelRefs.add(new GroupLabelRef(icon, i, labelName));
                                } else {
                                    // Note: it is possible that a translation is missing a label. In that
                                    // case, set no ID which will result in label being ignored
                                    int index = Collections.binarySearch(labels, labelName);
                                    icon.labels[i] = (index >= 0 ? labels.get(index) : null);
                                }
                            }
                        } else {
                            // Missing labels attribute, check if can inherit
                            Icon parent = icons.get(id);
                            if (parent != null) {
                                icon.labels = parent.labels;
                            }  // else icon is missing attribute, error
                        }

                        icons.append(id, icon);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (tagName.equalsIgnoreCase(XML_TAG_CATEGORY)) {
                        category = null;
                    }
                }

                eventType = parser.next();
            }

            // Add group labels
            // First make a sorted list of all different group labels
            // Then set the index in that list for each reference
            for (GroupLabelRef ref : groupLabelRefs) {
                int index = Collections.binarySearch(groupLabels, ref.label);
                if (index < 0) {
                    index = -(index + 1);
                    groupLabels.add(index, new Label(ref.label, null, null));
                }
            }
            for (GroupLabelRef ref : groupLabelRefs) {
                int index = Collections.binarySearch(groupLabels, ref.label);
                ref.icon.labels[ref.labelIndex] = groupLabels.get(index);
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Could not parse icons and categories from XML.", e);
        }
    }

    private class GroupLabelRef {

        final Icon icon;
        final int labelIndex;
        final String label;

        GroupLabelRef(Icon icon, int labelIndex, String label) {
            this.icon = icon;
            this.labelIndex = labelIndex;
            this.label = label;
        }

    }

    /**
     * Load labels from XML
     *
     * @param xmlFile xml file to load from
     * @param append  if true, new labels will be appended to old ones
     */
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private void loadLabels(@XmlRes int xmlFile, boolean append) {
        if (labels == null || !append) {
            labels = new ArrayList<>();
        }

        List<LabelRef> labelRefs = new ArrayList<>();

        // Parse labels from XML
        XmlPullParser parser = context.getResources().getXml(xmlFile);
        try {
            String name = null;
            Label newLabel = null;
            Label overwritten = null;
            boolean hasAliases = false;
            int newIndex = -1;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equalsIgnoreCase(XML_TAG_LABEL)) {
                        name = parser.getAttributeValue(null, XML_ATTR_NAME);
                        newIndex = Collections.binarySearch(labels, name);
                        overwritten = (newIndex >= 0 ? labels.get(newIndex) : null);

                    } else if (tagName.equalsIgnoreCase(XML_TAG_ALIAS)) {
                        hasAliases = true;
                        if (newLabel == null) {
                            if (overwritten != null) {
                                overwritten.overwrite(null, new ArrayList<LabelValue>());
                                newLabel = overwritten;
                            } else {
                                newLabel = new Label(name, null, new ArrayList<LabelValue>());
                            }
                        }
                    }

                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (text.charAt(0) == '@') {
                        labelRefs.add(new LabelRef(name, newLabel, text));

                    } else {
                        // Replace character used to imitate apostrophe because apostrophe can't be
                        // used in res/xml due to a bug
                        text = text.replace('`', '\'');

                        LabelValue value = new LabelValue(text, normalizeText(text));
                        if (hasAliases) {
                            newLabel.aliases.add(value);
                        } else {
                            if (overwritten != null) {
                                overwritten.overwrite(value, null);
                                newLabel = overwritten;
                            } else {
                                newLabel = new Label(name, value, null);
                            }
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equalsIgnoreCase(XML_TAG_LABEL)) {
                        if (newLabel != null && overwritten == null) {
                            // Add new label if not overwriting another and not a reference
                            labels.add(-(newIndex + 1), newLabel);
                        }
                        name = null;
                        newLabel = null;
                        overwritten = null;
                        hasAliases = false;
                        newIndex = -1;
                    }
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Could not parse labels from XML.", e);
        }

        // Add values that were a reference to another label's value
        for (LabelRef ref : labelRefs) {
            int index = Collections.binarySearch(labels, ref.ref);

            Label refLabel = labels.get(index);
            if (refLabel.overwritten != null && ref.refDefault) {
                // References from extra label point to default overwritten label
                refLabel = refLabel.overwritten;
            }

            // Get referenced value, can either be a label, an alias or a list of aliases
            Object value;
            if (refLabel.aliases == null) {
                value = refLabel.value;
            } else {
                if (ref.refAlias == LabelRef.REF_ALIAS_NONE) {
                    value = refLabel.aliases;
                } else {
                    value = refLabel.aliases.get(ref.refAlias);
                }
            }

            if (ref.parent == null) {
                // Parent has no aliases and is not yet created
                Label label;
                if (value instanceof LabelValue) {
                    label = new Label(ref.name, (LabelValue) value, null);
                } else {
                    List<LabelValue> aliases = new ArrayList<>((List<LabelValue>) value);
                    label = new Label(ref.name, null, aliases);
                }

                int insertPoint = Collections.binarySearch(labels, ref.name);
                if (insertPoint < 0) {
                    labels.add(-(insertPoint + 1), label);
                } else {
                    Label overwritten = labels.get(insertPoint);
                    if (value instanceof LabelValue) {
                        overwritten.overwrite((LabelValue) value, null);
                    } else {
                        List<LabelValue> aliases = new ArrayList<>((List<LabelValue>) value);
                        overwritten.overwrite(null, aliases);
                    }
                }

            } else {
                // Parent has aliases
                if (value instanceof LabelValue) {
                    ref.parent.aliases.add((LabelValue) value);
                } else {
                    ref.parent.aliases.addAll((List<LabelValue>) value);
                }
            }
        }
    }

    private static class LabelRef {

        static final int REF_ALIAS_NONE = -1;

        final String name;
        final Label parent;
        final String ref;
        final int refAlias;
        final boolean refDefault;

        /**
         * Create new reference to a label
         *
         * @param name    name of the label to be created. If null, parent mustn't be null
         * @param parent  label to add alias to. If null, name musn't be null
         * @param refText reference raw text
         */
        LabelRef(@Nullable String name, @Nullable Label parent, @NonNull String refText) {
            this.name = name;
            this.parent = parent;

            int startPos = refText.indexOf('/');
            String ref = refText.substring(startPos + 1);
            int sepPos = ref.indexOf('$');

            if (sepPos != -1) {
                refAlias = Integer.valueOf(ref.substring(sepPos + 1));
                ref = ref.substring(0, sepPos);
            } else {
                refAlias = REF_ALIAS_NONE;
            }

            refDefault = (startPos == 10);  // true to reference the default value, not the overwritten one
            this.ref = ref;
        }

    }

    /**
     * Normalize given text, removing all diacritics, all
     * unicode characters, hyphens, apostrophes and more
     *
     * @param text text
     * @return normalized text
     */
    static String normalizeText(String text) {
        // NOTE: Might have to change this method if more translations are made
        // For example, right now it would remove all chinese and arabic characters
        text = text.toLowerCase().trim();
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Start loading all icons drawable asynchronously
     * This loading takes less than 15 sec and takes up to 50-100MB of RAM (check that by yourself)
     * This is useful to prevent lag when scrolling the icon dialog's list
     */
    void loadIconDrawables() {
        if (!dataLoaded) {
            throw new IllegalStateException("Cannot load drawables before icon data is loaded.");
        }

        drawablesLoader.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < icons.size(); i++) {
                    if (icons.valueAt(i).drawable == null) {
                        icons.valueAt(i).getDrawable(context);
                    }
                }
            }
        }, null);
    }

    void stopLoadingDrawables() {
        drawablesLoader.interrupt();
    }

    /**
     * Set to null references to all of the icons drawable so that they can be garbage collected
     */
    public void freeIconDrawables() {
        drawablesLoader.interrupt();
        for (int i = 0; i < icons.size(); i++) {
            icons.valueAt(i).drawable = null;
        }
    }

    public void addLoadCallback(@NonNull LoadCallback cb) {
        if (dataLoaded) {
            // Data is already loaded: call callback without adding it
            cb.onDataLoaded(this);
            return;
        }

        if (loadCallbacks == null) loadCallbacks = new ArrayList<>();
        loadCallbacks.add(cb);
    }

    public void removeLoadCallback(@NonNull LoadCallback cb) {
        loadCallbacks.remove(cb);
        if (loadCallbacks.size() == 0) loadCallbacks = null;
    }

    private void callLoadCallbacks() {
        if (loadCallbacks != null) {
            for (LoadCallback cb : loadCallbacks) {
                cb.onDataLoaded(this);
            }
            loadCallbacks.clear();
        }
    }

    public interface LoadCallback {
        /**
         * Called when icon data is done loading.
         * All calls to get icons, labels and categories will return null before this is called.
         */
        void onDataLoaded(IconHelper helper);
    }

}
