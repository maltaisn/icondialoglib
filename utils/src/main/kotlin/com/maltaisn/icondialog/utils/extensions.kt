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
 * Leading and trailing underscores are also removed.
 */
fun String.normalizeName(): String {
    val parts = split('-', '_', ' ', ',', ';', '&', '+', '.')
            .filter { it.isNotEmpty() }.toMutableList()
    for ((i, part) in parts.withIndex()) {
        var normalized = part.toLowerCase(Locale.ROOT).trim()
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD)
        parts[i] = normalized.replace("[^a-z0-9]".toRegex(), "")
    }
    return parts.joinToString("_")
}

/**
 * Normalize [this] string used as a tag value.
 * - Only lowercase letters and digits are kept.
 * - String is trimmed, all whitespace sequences are converted to a single space.
 * - "'s" suffix is removed.
 * - First letter is capitalized.
 */
fun String.normalizeValue(): String {
    var normalized = this.toLowerCase(Locale.ROOT).trim()
    normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD)
    normalized = normalized.replace("""\s+""".toRegex(), " ")

    val sb = StringBuilder()
    for (c in normalized) {
        if (c == ' ' || c in 'a'..'z' || c in '0'..'9') {
            sb.append(c)
        }
    }
    if (sb.isNotEmpty()) {
        sb[0] = sb[0].toUpperCase()
    }

    return sb.toString()
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
