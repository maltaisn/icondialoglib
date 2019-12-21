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

package com.maltaisn.icondialog.data

import android.content.Context
import androidx.annotation.StringRes


/**
 * A category for icons to be grouped in. Each category has an unique [id],
 * a [name], and an optional [nameRes] string resource ID associated with it.
 * [nameRes] is set to 0 when there's resource ID.
 */
data class Category(val id: Int,
                    val name: String,
                    @StringRes val nameRes: Int) {

    /**
     * Get the name of this category from [context] resources,
     * or use [name] if it isn't a resource.
     */
    fun resolveName(context: Context) = if (nameRes == 0) {
        name
    } else {
        context.getString(nameRes)
    }

}
