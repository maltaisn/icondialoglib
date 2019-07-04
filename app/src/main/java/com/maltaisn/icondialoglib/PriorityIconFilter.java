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


import androidx.annotation.NonNull;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconFilter;

public class PriorityIconFilter extends IconFilter {

    private static final int CUSTOM_CATEGORY_ID = 100;

    @Override
    public int compare(@NonNull Icon icon1, @NonNull Icon icon2) {
        int categoryId1 = icon1.getCategory().getId();
        int categoryId2 = icon2.getCategory().getId();
        if (categoryId1 == CUSTOM_CATEGORY_ID && categoryId2 != CUSTOM_CATEGORY_ID) {
            return -1;
        } else if (categoryId1 != CUSTOM_CATEGORY_ID && categoryId2 == CUSTOM_CATEGORY_ID) {
            return 1;
        } else {
            return super.compare(icon1, icon2);
        }
    }
}
