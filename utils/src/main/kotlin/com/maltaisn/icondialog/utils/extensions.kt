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

package com.maltaisn.icondialog.utils

import java.text.Normalizer
import java.util.*


/**
 * Normalize [this] string to make it a valid icon pack name for a category or a tag. All diacritics,
 * all unicode characters, hyphens, apostrophes are removed. Common word separators are replaced with
 * underscores. Resulting text has only lowercase latin letters, digits and underscores.
 */
fun String.normalizeName(): String {
    val parts = split('-', '_', ' ', ',', ';', '&', '+', '.').toMutableList()
    for ((i, part) in parts.withIndex()) {
        var normalized = part.toLowerCase(Locale.ROOT).trim()
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD)
        parts[i] = normalized.replace("""[^A-Z0-9]""", "")
    }
    return parts.joinToString("_")
}


/**
 * Append indent up to a [level] to [this] StringBuilder.
 */
fun StringBuilder.appendIndent(level: Int = 1) {
    if (level >= 0) {
        repeat(level) {
            append("    ")
        }
    }
}
