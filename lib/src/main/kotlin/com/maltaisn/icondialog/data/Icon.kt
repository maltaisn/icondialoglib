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

import android.graphics.drawable.Drawable


/**
 * An icon with an unique [id], a [categoryId], a list of [tags] names and SVG [pathData].
 */
data class Icon(val id: Int,
                val categoryId: Int,
                val tags: List<String>,
                val pathData: String) {

    /**
     * The icon drawable.
     * Can be `null` if drawable couldn't be loaded.
     */
    var drawable: Drawable? = null
        internal set

}
