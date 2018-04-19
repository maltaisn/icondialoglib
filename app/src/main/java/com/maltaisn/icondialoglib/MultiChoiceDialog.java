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


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class MultiChoiceDialog extends DialogFragment {

    private String[] choiceNames;
    private int selectedChoices;
    private String dialogTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        boolean[] selected = new boolean[choiceNames.length];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = (selectedChoices & (1 << i)) == (1 << i);
        }

        builder.setMultiChoiceItems(choiceNames, selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                if (isChecked) {
                    selectedChoices |= 1 << position;
                } else {
                    selectedChoices &= ~(1 << position);
                }
            }
        });
        builder.setTitle(dialogTitle);
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //noinspection ConstantConditions
                    ((Callback) getActivity()).onChoicesSelected(selectedChoices);
                } catch (ClassCastException e) {
                    // Not implemented by caller
                }
            }
        });
        return builder.create();
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

    public MultiChoiceDialog setChoices(@NonNull String[] names, int selected) {
        choiceNames = names;
        selectedChoices = selected;
        return this;
    }

    public MultiChoiceDialog setTitle(@Nullable String title) {
        dialogTitle = title;
        return this;
    }

    public interface Callback {
        void onChoicesSelected(int selected);
    }

}
