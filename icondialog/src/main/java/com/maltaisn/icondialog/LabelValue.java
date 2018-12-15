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


import androidx.annotation.NonNull;

/**
 * Defines a label value made of a normal string and a normalized string
 * Labels are all prenormalized for performance to avoid doing it multiple times for every search
 */
public class LabelValue {

    final String value;
    final String normValue;

    LabelValue(@NonNull String value, @NonNull String normValue) {
        this.value = value;
        this.normValue = normValue;
    }

    public String getValue() {
        return value;
    }

    public String getNormalizedValue() {
        return normValue;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
