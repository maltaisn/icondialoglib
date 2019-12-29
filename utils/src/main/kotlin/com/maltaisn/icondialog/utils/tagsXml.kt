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

import java.io.File


/**
 * Create an `tas.xml` file from a list of [tags].
 * File is generated in the [outputDir] directory.
 */
fun createTagsXml(tags: List<Tag>, outputDir: File) {
    // Create content
    val tagsXml = StringBuilder()
    tagsXml.appendln("<tags>")
    for (tag in tags) {
        // Escape invalid XML characters in tag values
        val values = tag.values.map {
            it.replace("&", "&amp;")
                    .replace("'", "`")
        }

        tagsXml.appendIndent(1)
        tagsXml.append("""<tag name="${tag.name}">""")

        if (values.size == 1) {
            // Use single value
            tagsXml.appendln("${values.first()}</tag>")

        } else if (values.size > 1) {
            // Use aliases
            tagsXml.appendln()
            for (value in values) {
                tagsXml.appendIndent(2)
                tagsXml.appendln("<alias>$value</alias>")
            }
            tagsXml.appendIndent(1)
            tagsXml.appendln("</tag>")
        }
    }
    tagsXml.appendln("</tags>")

    // Export file
    File(outputDir, "tags.xml").writeText(tagsXml.toString())
}

data class Tag(val name: String, val values: List<String>) : Comparable<Tag> {
    override fun compareTo(other: Tag) = name.compareTo(other.name)
}
