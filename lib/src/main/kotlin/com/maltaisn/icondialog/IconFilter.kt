/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.icondialog

import android.os.Parcelable
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack


/**
 * Class used to filter the icons for search and sort them afterwards.
 * Icon filter must be parcelable to be put in bundle.
 */
interface IconFilter : Comparator<Icon>, Parcelable {

    /**
     * Get a list of all matching icons for a search [query], in no specific order.
     */
    fun queryIcons(pack: IconPack, query: String? = null): MutableList<Icon>

    /**
     * Compare [icon1] and [icon2], two icons which are of the same category.
     */
    override fun compare(icon1: Icon, icon2: Icon): Int

}
