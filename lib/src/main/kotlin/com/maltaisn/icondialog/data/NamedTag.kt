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


/**
 * An icon tag with a localized value and any number of aliases.
 * Aliases can be synonyms or related terms with similar meaning.
 */
data class NamedTag(override val name: String,
                    val value: Value?,
                    val aliases: List<Value>) : IconTag {

    /**
     * A label value, storing both the raw value and the normalized value, used for searching.
     */
    data class Value(val value: String, val normValue: String)

}
