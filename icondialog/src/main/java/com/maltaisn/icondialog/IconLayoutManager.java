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


import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class IconLayoutManager extends GridLayoutManager {

    private static final String TAG = IconLayoutManager.class.getSimpleName();

    private int iconSize;

    IconLayoutManager(Context context, int iconSize) {
        super(context, -1, GridLayoutManager.VERTICAL, false);
        this.iconSize = iconSize;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int width = getWidth();
        int height = getHeight();
        if (getSpanCount() == -1 && iconSize > 0 && width > 0 && height > 0) {
            // Adjust span count on available space and icon size
            int layoutWidth = width - getPaddingRight() - getPaddingLeft();
            setSpanCount(Math.max(1, layoutWidth / iconSize));
        }
        super.onLayoutChildren(recycler, state);
    }

}
