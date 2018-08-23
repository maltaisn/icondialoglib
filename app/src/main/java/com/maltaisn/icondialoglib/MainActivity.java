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

package com.maltaisn.icondialoglib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;
import com.maltaisn.icondialog.IconFilter;
import com.maltaisn.icondialog.IconHelper;
import com.maltaisn.icondialog.IconView;
import com.maltaisn.icondialog.Label;
import com.maltaisn.icondialog.LabelValue;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements
        IconDialog.Callback, SingleChoiceDialog.Callback, MultiChoiceDialog.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BroadcastReceiver localeReceiver;

    private int[] selectedIconIds;

    private Icon[] selectedIcons;
    private IconAdapter iconAdapter;

    private TextView selNoneTxv;

    private EditText searchVisbEdt;
    private EditText titleVisbEdt;
    private EditText disabledCatgEdt;

    private int searchVisb;
    private int titleVisb;
    private int disabledCatg;

    private String[] searchVisbNames;
    private String[] titleVisbNames;
    private String[] catgNames;
    private MessageFormat disabledCatgFmt;

    private boolean selectingVisbForSearch;

    private boolean extraIconsLoaded;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        final IconDialog iconDialog = new IconDialog();

        final SingleChoiceDialog visbDialog = new SingleChoiceDialog();
        final MultiChoiceDialog enabledCatgDialog = new MultiChoiceDialog();

        Button openBtn = findViewById(R.id.btn_open);
        final Button addExtraBtn = findViewById(R.id.btn_add_extra);
        final TextView iconCountTxv = findViewById(R.id.txv_icon_count);
        final RecyclerView selListRcv = findViewById(R.id.rcv_selected_icons);
        selNoneTxv = findViewById(R.id.txv_none_selected);
        final CheckBox maxSelCkb = findViewById(R.id.ckb_max_sel);
        final EditText maxSelEdt = findViewById(R.id.edt_max_sel);
        final CheckBox maxSelMessageCkb = findViewById(R.id.ckb_max_sel_message);
        final CheckBox showSelCkb = findViewById(R.id.ckb_show_selection);
        final CheckBox showHeadersCkb = findViewById(R.id.ckb_show_headers);
        final CheckBox stickyHeadersCkb = findViewById(R.id.ckb_sticky_headers);
        final CheckBox showUnselBtnCkb = findViewById(R.id.ckb_show_clear_btn);
        searchVisbEdt = findViewById(R.id.edt_search_visb);
        titleVisbEdt = findViewById(R.id.edt_title_visb);
        disabledCatgEdt = findViewById(R.id.edt_disabled_catg);

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int maxSel;
                if (maxSelCkb.isChecked()) {
                    try {
                        maxSel = Integer.valueOf(maxSelEdt.getText().toString());
                    } catch (Exception e) {
                        maxSel = 1;
                    }
                } else {
                    maxSel = IconDialog.MAX_SELECTION_NONE;
                }

                iconDialog.setSelectedIcons(selectedIconIds)
                        .setMaxSelection(maxSel, maxSelMessageCkb.isChecked())
                        .setShowSelectButton(showSelCkb.isChecked())
                        .setShowClearButton(showUnselBtnCkb.isChecked())
                        .setShowHeaders(showHeadersCkb.isChecked(), stickyHeadersCkb.isChecked())
                        .setTitle(titleVisb, null)
                        .setSearchEnabled(searchVisb, null);

                int[] disabled = new int[Integer.bitCount(disabledCatg)];
                int pos = 0;
                for (int i = 0; i < 32; i++) {
                    if ((disabledCatg & (1 << i)) == (1 << i)) {
                        disabled[pos] = i;
                        pos++;
                    }
                }

                //iconDialog.setIconFilter(new PriorityIconFilter());

                IconFilter filter = (IconFilter) iconDialog.getIconFilter();
                filter.setDisabledCategories(disabled);
                filter.setIdSearchEnabled(true);

                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });

        showHeadersCkb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stickyHeadersCkb.setEnabled(isChecked);
            }
        });

        maxSelCkb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxSelEdt.setEnabled(isChecked);
                maxSelMessageCkb.setEnabled(isChecked);
            }
        });

        searchVisbEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectingVisbForSearch = true;
                visbDialog.setChoices(searchVisbNames, searchVisb);
                visbDialog.setTitle(getString(R.string.dialog_search_visb));
                visbDialog.show(getSupportFragmentManager(), "visb_dialog");
            }
        });
        titleVisbEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectingVisbForSearch = false;
                visbDialog.setChoices(titleVisbNames, titleVisb);
                visbDialog.setTitle(getString(R.string.dialog_title_visb));
                visbDialog.show(getSupportFragmentManager(), "visb_dialog");
            }
        });
        disabledCatgEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enabledCatgDialog.setChoices(catgNames, disabledCatg);
                enabledCatgDialog.setTitle(getString(R.string.dialog_disabled_catg));
                enabledCatgDialog.show(getSupportFragmentManager(), "disabled_catg_dialog");
            }
        });

        searchVisbNames = getResources().getStringArray(R.array.option_search_visibility);
        titleVisbNames = getResources().getStringArray(R.array.option_title_visibility);
        catgNames = getResources().getStringArray(R.array.option_categories);
        disabledCatgFmt = new MessageFormat(getString(R.string.option_disabled_catg_fmt));

        if (state == null) {
            showSelCkb.setChecked(true);
            showHeadersCkb.setChecked(true);
            stickyHeadersCkb.setChecked(true);
            showUnselBtnCkb.setChecked(false);

            maxSelCkb.setChecked(true);
            maxSelMessageCkb.setChecked(false);
            maxSelEdt.setText("1");

            setSearchVisb(IconDialog.VISIBILITY_IF_LANG_AVAILABLE);
            setTitleVisb(IconDialog.VISIBILITY_IF_NO_SEARCH);
            setDisabledCategories(0);

            selectedIconIds = new int[0];

        } else {
            setSearchVisb(state.getInt("searchVisb"));
            setTitleVisb(state.getInt("titleVisb"));
            setDisabledCategories(state.getInt("disabledCatg"));

            extraIconsLoaded = state.getBoolean("extraIconsLoaded");
            selectedIconIds = state.getIntArray("selectedIconIds");
        }

        // Load icons and labels
        final IconHelper iconHelper = IconHelper.getInstance(this);
        if (extraIconsLoaded) {
            iconHelper.addExtraIcons(R.xml.icons, R.xml.labels);
        }

        iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
            @Override
            public void onDataLoaded() {
                selectedIcons = new Icon[selectedIconIds.length];
                for (int i = 0; i < selectedIcons.length; i++) {
                    selectedIcons[i] = iconHelper.getIcon(selectedIconIds[i]);
                }

                iconAdapter = new IconAdapter();
                selListRcv.setAdapter(iconAdapter);
                selListRcv.setLayoutManager(new LinearLayoutManager(
                        MainActivity.this, LinearLayoutManager.HORIZONTAL, false));

                iconCountTxv.setText(MessageFormat.format(getString(R.string.icon_count_fmt), iconHelper.getIconCount()));

                addExtraBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        extraIconsLoaded = true;
                        addExtraBtn.setEnabled(false);

                        // Load extra icons
                        iconHelper.addExtraIcons(R.xml.icons, R.xml.labels);
                        iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                            @Override
                            public void onDataLoaded() {
                                iconCountTxv.setText(MessageFormat.format(getString(R.string.icon_count_fmt), iconHelper.getIconCount()));
                            }
                        });
                    }
                });
                addExtraBtn.setEnabled(!extraIconsLoaded);

                selectIcons();
            }
        });

        // Register listener for locale change to reload labels
        localeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                iconHelper.reloadLabels();
            }
        };
        registerReceiver(localeReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(localeReceiver);
        localeReceiver = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        state.putInt("searchVisb", searchVisb);
        state.putInt("titleVisb", titleVisb);
        state.putInt("disabledCatg", disabledCatg);
        state.putIntArray("selectedIconIds", selectedIconIds);
        state.putBoolean("extraIconsLoaded", extraIconsLoaded);
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        // Sort selected icons by ID
        Arrays.sort(icons, new Comparator<Icon>() {
            @Override
            public int compare(Icon i1, Icon i2) {
                return Integer.compare(i1.getId(), i2.getId());
            }
        });

        selectedIcons = icons;
        selectedIconIds = new int[icons.length];
        for (int i = 0; i < icons.length; i++) {
            selectedIconIds[i] = icons[i].getId();
        }

        selectIcons();
    }

    @Override
    public void onChoiceSelected(int visb) {
        if (selectingVisbForSearch) {
            setSearchVisb(visb);
        } else {
            setTitleVisb(visb);
        }
    }

    @Override
    public void onChoicesSelected(int selected) {
        setDisabledCategories(selected);
    }

    private void selectIcons() {
        if (selectedIconIds.length == 0) {
            selNoneTxv.setVisibility(View.VISIBLE);
        } else {
            selNoneTxv.setVisibility(View.GONE);
            iconAdapter.notifyDataSetChanged();
        }
    }

    private void setSearchVisb(int visb) {
        if (visb != searchVisb) {
            searchVisb = visb;
            searchVisbEdt.setText(searchVisbNames[visb]);
        }
    }

    private void setTitleVisb(int visb) {
        if (visb != titleVisb) {
            titleVisb = visb;
            titleVisbEdt.setText(titleVisbNames[visb]);
        }
    }

    private void setDisabledCategories(int disabled) {
        disabledCatg = disabled;
        disabledCatgEdt.setText(disabledCatgFmt.format(
                new Object[]{Integer.bitCount(disabledCatg)}));
    }


    private class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

        private MessageFormat idFmt;
        private MessageFormat infoFmt;
        private Toast infoToast;

        IconAdapter() {
            setHasStableIds(true);

            idFmt = new MessageFormat(getString(R.string.selected_icon_id_fmt));
            infoFmt = new MessageFormat(getString(R.string.icon_info_fmt));
        }

        class IconViewHolder extends RecyclerView.ViewHolder {

            private IconView iconImv;
            private TextView idTxv;

            IconViewHolder(View view) {
                super(view);
                iconImv = view.findViewById(R.id.imv_icon);
                idTxv = view.findViewById(R.id.txv_id);
            }

            void bindViewHolder(final Icon icon) {
                iconImv.setIcon(icon);
                iconImv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // Print detailed info about the icon
                        StringBuilder labelsSb = new StringBuilder();
                        for (Label label : icon.getLabels()) {
                            LabelValue[] aliases = label.getAliases();
                            if (aliases != null) {
                                labelsSb.append('{');
                                for (LabelValue alias : aliases) {
                                    labelsSb.append(alias);
                                    labelsSb.append(", ");
                                }
                                labelsSb.delete(labelsSb.length() - 2, labelsSb.length());
                                labelsSb.append("}, ");
                            } else if (label.getValue() != null) {
                                labelsSb.append(label.getValue());
                                labelsSb.append(", ");
                            }
                        }
                        labelsSb.delete(labelsSb.length() - 2, labelsSb.length());

                        // ID: 1000
                        // Category: Transport
                        // Labels: Car, Fuel, Vehicle
                        if (infoToast != null) infoToast.cancel();
                        infoToast = Toast.makeText(MainActivity.this,
                                infoFmt.format(new Object[]{icon.getId(),
                                icon.getCategory().getName(MainActivity.this),
                                labelsSb.toString()}), Toast.LENGTH_LONG);
                        infoToast.show();
                    }
                });

                idTxv.setText(idFmt.format(new Object[]{icon.getId()}));
            }
        }

        @Override
        public @NonNull IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new IconViewHolder(getLayoutInflater().inflate(R.layout.item_icon, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final IconViewHolder holder, final int position) {
            holder.bindViewHolder(selectedIcons[position]);
        }

        @Override
        public int getItemCount() {
            return selectedIcons.length;
        }

        @Override
        public long getItemId(int position) {
            return selectedIcons[position].getId();
        }

    }

}
