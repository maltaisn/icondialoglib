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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Thin wrapper around ImageView to set an icon from the library in XML by ID
 * However, no preview is shown, because it's impossible: getting the icon drawable requires
 * reflection, which cannot be done by the layout editor
 */
public class IconView extends AppCompatImageView {

    public IconView(Context context) {
        this(context, null, 0);
    }

    public IconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null && !isInEditMode()) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IconView);

            int iconId = ta.getInt(R.styleable.IconView_icdIcon, -1);
            if (iconId != -1) setIcon(iconId);

            ta.recycle();
        }
    }

    /**
     * Set icon to show
     * @param icon icon object
     */
    public void setIcon(@NonNull Icon icon) {
        setImageDrawable(icon.getDrawable(getContext()));
    }

    /**
     * Set icon to show
     * @param iconId icon ID. An invalid ID will show nothing.
     */
    public void setIcon(int iconId) {
        Icon icon = IconHelper.getInstance(getContext()).getIcon(iconId);
        Drawable iconDrawable = null;
        if (icon != null) iconDrawable = icon.getDrawable(getContext());
        setImageDrawable(iconDrawable);
    }

}
