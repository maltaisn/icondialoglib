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

import java.text.Normalizer
import java.util.*


/**
 * Normalize [this] string, removing all diacritics, all unicode characters, hyphens,
 * apostrophes and more. Resulting text has only lowercase latin letters and digits.
 */
internal fun String.normalize(): String {
    var normalized = this.toLowerCase(Locale.ROOT).trim()
    normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKD)
    val sb = StringBuilder()
    for (c in normalized) {
        if (c in 'a'..'z' || c in 'а'..'я' || c in '0'..'9') {
            sb.append(c)
        }
    }
    return sb.toString()
}
