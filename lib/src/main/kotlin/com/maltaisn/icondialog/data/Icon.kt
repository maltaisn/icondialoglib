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
import com.maltaisn.icondialog.pack.IconDrawableLoader


/**
 * An icon with an unique [id], a [categoryId], a list of [tags] names and SVG [pathData].
 * [categoryId] can be `-1` if the icon has no category.
 * Icon also have a [width] and a [height] in pixels.
 */
data class Icon(val id: Int,
                val categoryId: Int,
                val tags: List<String>,
                val pathData: String,
                val width: Int,
                val height: Int,
                val drawableResId: Int? = null
) {

    /**
     * The icon drawable.
     * Can be `null` if drawable isn't loaded or couldn't be loaded.
     * Use [IconDrawableLoader] class to load it.
     * 
     * Note that each get call creates a new drawable that shares its constant state with all 
     * drawables created for this icon. You should also call `drawable.mutate()` before tinting it.
     */
    var drawable: Drawable? = null
        internal set
        get() = field?.constantState?.newDrawable()

}
