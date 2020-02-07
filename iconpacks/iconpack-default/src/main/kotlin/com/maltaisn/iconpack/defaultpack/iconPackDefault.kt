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

@file:JvmName("IconPackDefault")

package com.maltaisn.iconpack.defaultpack

import com.maltaisn.icondialog.pack.IconPackLoader
import java.util.*


/**
 * Create the default icon pack with a icon pack [loader].
 */
fun createDefaultIconPack(loader: IconPackLoader) =
        loader.load(R.xml.iconpack_default_icons, R.xml.iconpack_default_tags,
                listOf(Locale("en"),
                        Locale("fr"),
                        Locale("es"),
                        Locale("de"),
                        Locale("pt")))
