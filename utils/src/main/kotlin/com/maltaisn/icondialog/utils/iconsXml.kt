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
 * Create an `icons.xml` and a `strings.xml` file from a list of [icons] and [categories].
 * Files are generated in the [outputDir] directory.
 *
 * Icon pack uses [packWidth] and [packHeight] as global icon size. If any icon in list has
 * a different size, it will be defined explicitly in XML.
 */
fun createIconsXml(icons: List<Icon>, categories: List<Category>, outputDir: File,
                   packWidth: Int, packHeight: Int) {
    // Map categories and icons
    val catgMap = categories.associateBy { it.id }
    val iconsMap = icons.groupBy { it.categoryId }.toSortedMap()

    // Create icons.xml file
    val iconsXml = StringBuilder()
    iconsXml.appendln("""<icons width="$packWidth" height="$packHeight">""")
    for ((catgId, catgIcons) in iconsMap) {
        iconsXml.appendln()
        if (catgId != -1) {
            val catg = catgMap[catgId] ?: error("Icon category '$catgId' doesn't exist.")
            iconsXml.appendIndent(1)
            iconsXml.appendln("""<category id="$catgId" name="@string/catg_${catg.name}">""")
        }
        for (icon in catgIcons) {
            iconsXml.appendIndent(if (catgId == -1) 1 else 2)
            iconsXml.append("""<icon id="${icon.id}"""")
            if (icon.tags.isNotEmpty()) {
                iconsXml.append(""" tags="""")
                iconsXml.append(icon.tags.joinToString(","))
                iconsXml.append('"')
            }
            if (icon.width != packWidth) {
                iconsXml.append(""" width="${icon.width}"""")
            }
            if (icon.height != packHeight) {
                iconsXml.append(""" height="${icon.height}"""")
            }
            iconsXml.appendln(""" path="${icon.pathData}"/>""")
        }
        if (catgId != -1) {
            iconsXml.appendIndent(1)
            iconsXml.appendln("</category>")
        }
    }
    iconsXml.appendln("</icons>")

    // Create strings.xml file
    val stringsXml = StringBuilder()
    stringsXml.appendln("<resources>")
    for (catg in catgMap.values) {
        val nameValue = catg.nameValue
                .replace("&", "&amp;")
                .replace("'", "&apos;")
        stringsXml.appendIndent(1)
        stringsXml.appendln("""<string name="catg_${catg.name}">$nameValue</string>""")
    }
    stringsXml.appendln("</resources>")

    // Export files
    File(outputDir, "icons.xml").writeText(iconsXml.toString())
    File(outputDir, "strings.xml").writeText(stringsXml.toString())
}

data class Icon(val id: Int,
                val categoryId: Int,
                val tags: List<String>,
                val pathData: String,
                val width: Int,
                val height: Int)

data class Category(val id: Int,
                    val name: String,
                    val nameValue: String)
