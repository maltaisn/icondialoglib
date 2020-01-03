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

package com.maltaisn.icondialog.pack

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.util.Xml
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.maltaisn.icondialog.data.Icon
import org.xmlpull.v1.XmlPullParser
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Class use to load icon drawables.
 * @param context Any context, needed to get resources.
 */
open class IconDrawableLoader(context: Context) {

    private val context = context.applicationContext

    /**
     * Create the vector drawable for an [icon] and set it.
     */
    @SuppressLint("DiscouragedPrivateApi,PrivateApi")
    open fun loadDrawable(icon: Icon): Drawable? {
        if (icon.drawable != null) {
            // Icon drawable is already loaded.
            return icon.drawable
        }

        val drawable: Drawable?
        val binXml = createDrawableBinaryXml(icon.pathData, icon.width, icon.height)
        try {
            // Get the binary XML parser (XmlBlock.Parser) and use it to create the drawable
            // This should be equivalent to AssetManager#getXml()
            val xmlBlock = Class.forName("android.content.res.XmlBlock")
            val xmlBlockConstr = xmlBlock.getConstructor(ByteArray::class.java)
            val xmlParserNew = xmlBlock.getDeclaredMethod("newParser")
            xmlBlockConstr.isAccessible = true
            xmlParserNew.isAccessible = true
            val parser = xmlParserNew.invoke(xmlBlockConstr.newInstance(binXml as Any)) as XmlPullParser

            if (Build.VERSION.SDK_INT >= 24) {
                drawable = Drawable.createFromXml(context.resources, parser)
            } else {
                // Before API 24, vector drawables aren't rendered correctly without compat lib
                val attrs = Xml.asAttributeSet(parser)
                var type = parser.next()
                while (type != XmlPullParser.START_TAG) {
                    type = parser.next()
                }
                drawable = VectorDrawableCompat.createFromXmlInner(context.resources, parser, attrs, null)
            }

        } catch (e: Exception) {
            // Could not load icon.
            Log.e(TAG, "Could not create vector drawable for icon ID ${icon.id}.", e)
            return null
        }

        icon.drawable = drawable
        return icon.drawable
    }

    companion object {
        private val TAG = IconDrawableLoader::class.java.simpleName

        private val BIN_XML_START = ubyteArrayOf(
                0x03u, 0x00u, 0x08u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x01u, 0x00u, 0x1Cu, 0x00u,
                0x00u, 0x00u, 0x00u, 0x00u, 0x0Au, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u,
                0x00u, 0x01u, 0x00u, 0x00u, 0x44u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u,
                0x00u, 0x00u, 0x00u, 0x00u, 0x09u, 0x00u, 0x00u, 0x00u, 0x11u, 0x00u, 0x00u, 0x00u,
                0x21u, 0x00u, 0x00u, 0x00u, 0x32u, 0x00u, 0x00u, 0x00u, 0x3Eu, 0x00u, 0x00u, 0x00u,
                0x49u, 0x00u, 0x00u, 0x00u, 0x76u, 0x00u, 0x00u, 0x00u, 0x7Du, 0x00u, 0x00u, 0x00u,
                0x86u, 0x00u, 0x00u, 0x00u, 0x06u, 0x06u, 0x68u, 0x65u, 0x69u, 0x67u, 0x68u, 0x74u,
                0x00u, 0x05u, 0x05u, 0x77u, 0x69u, 0x64u, 0x74u, 0x68u, 0x00u, 0x0Du, 0x0Du, 0x76u,
                0x69u, 0x65u, 0x77u, 0x70u, 0x6Fu, 0x72u, 0x74u, 0x57u, 0x69u, 0x64u, 0x74u, 0x68u,
                0x00u, 0x0Eu, 0x0Eu, 0x76u, 0x69u, 0x65u, 0x77u, 0x70u, 0x6Fu, 0x72u, 0x74u, 0x48u,
                0x65u, 0x69u, 0x67u, 0x68u, 0x74u, 0x00u, 0x09u, 0x09u, 0x66u, 0x69u, 0x6Cu, 0x6Cu,
                0x43u, 0x6Fu, 0x6Cu, 0x6Fu, 0x72u, 0x00u, 0x08u, 0x08u, 0x70u, 0x61u, 0x74u, 0x68u,
                0x44u, 0x61u, 0x74u, 0x61u, 0x00u, 0x2Au, 0x2Au, 0x68u, 0x74u, 0x74u, 0x70u, 0x3Au,
                0x2Fu, 0x2Fu, 0x73u, 0x63u, 0x68u, 0x65u, 0x6Du, 0x61u, 0x73u, 0x2Eu, 0x61u, 0x6Eu,
                0x64u, 0x72u, 0x6Fu, 0x69u, 0x64u, 0x2Eu, 0x63u, 0x6Fu, 0x6Du, 0x2Fu, 0x61u, 0x70u,
                0x6Bu, 0x2Fu, 0x72u, 0x65u, 0x73u, 0x2Fu, 0x61u, 0x6Eu, 0x64u, 0x72u, 0x6Fu, 0x69u,
                0x64u, 0x00u, 0x04u, 0x04u, 0x70u, 0x61u, 0x74u, 0x68u, 0x00u, 0x06u, 0x06u, 0x76u,
                0x65u, 0x63u, 0x74u, 0x6Fu, 0x72u, 0x00u)

        private val BIN_XML_END = ubyteArrayOf(
                0x80u, 0x01u, 0x08u, 0x00u, 0x20u, 0x00u, 0x00u, 0x00u, 0x55u, 0x01u, 0x01u, 0x01u,
                0x59u, 0x01u, 0x01u, 0x01u, 0x02u, 0x04u, 0x01u, 0x01u, 0x03u, 0x04u, 0x01u, 0x01u,
                0x04u, 0x04u, 0x01u, 0x01u, 0x05u, 0x04u, 0x01u, 0x01u, 0x02u, 0x01u, 0x10u, 0x00u,
                0x74u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu,
                0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x08u, 0x00u, 0x00u, 0x00u, 0x14u, 0x00u, 0x14u, 0x00u,
                0x04u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x06u, 0x00u, 0x00u, 0x00u,
                0x00u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x08u, 0x00u, 0x00u, 0x05u,
                0x01u, 0x18u, 0x00u, 0x00u, 0x06u, 0x00u, 0x00u, 0x00u, 0x01u, 0x00u, 0x00u, 0x00u,
                0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x08u, 0x00u, 0x00u, 0x05u, 0x01u, 0x18u, 0x00u, 0x00u,
                0x06u, 0x00u, 0x00u, 0x00u, 0x02u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu,
                0x08u, 0x00u, 0x00u, 0x04u, 0x00u, 0x00u, 0x00u, 0x00u, 0x06u, 0x00u, 0x00u, 0x00u,
                0x03u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x08u, 0x00u, 0x00u, 0x04u,
                0x00u, 0x00u, 0x00u, 0x00u, 0x02u, 0x01u, 0x10u, 0x00u, 0x4Cu, 0x00u, 0x00u, 0x00u,
                0x00u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu,
                0x07u, 0x00u, 0x00u, 0x00u, 0x14u, 0x00u, 0x14u, 0x00u, 0x02u, 0x00u, 0x00u, 0x00u,
                0x00u, 0x00u, 0x00u, 0x00u, 0x06u, 0x00u, 0x00u, 0x00u, 0x04u, 0x00u, 0x00u, 0x00u,
                0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x08u, 0x00u, 0x00u, 0x1Du, 0x00u, 0x00u, 0x00u, 0xFFu,
                0x06u, 0x00u, 0x00u, 0x00u, 0x05u, 0x00u, 0x00u, 0x00u, 0x09u, 0x00u, 0x00u, 0x00u,
                0x08u, 0x00u, 0x00u, 0x03u, 0x09u, 0x00u, 0x00u, 0x00u, 0x03u, 0x01u, 0x10u, 0x00u,
                0x18u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu,
                0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x07u, 0x00u, 0x00u, 0x00u, 0x03u, 0x01u, 0x10u, 0x00u,
                0x18u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0xFFu, 0xFFu, 0xFFu, 0xFFu,
                0xFFu, 0xFFu, 0xFFu, 0xFFu, 0x08u, 0x00u, 0x00u, 0x00u)

        /**
         * Create a vector drawable binary XML from [pathData] so that it can be parsed and created.
         * Path data should fit in a viewport of a [width] and a [height].
         *
         * See [https://justanapplication.wordpress.com/category/android/android-binary-xml/] and
         * [https://stackoverflow.com/a/49920860/5288316] for more documentation.
         */
        private fun createDrawableBinaryXml(pathData: String, width: Int, height: Int): ByteArray {
            val pathBytes = pathData.toByteArray()
            val pathSpLength = pathBytes.size + if (pathBytes.size > 127) 5 else 3
            var spPaddingLength = (BIN_XML_START.size + pathSpLength) % 4
            if (spPaddingLength != 0) spPaddingLength = 4 - spPaddingLength
            val totalLength = BIN_XML_START.size + pathSpLength + spPaddingLength + BIN_XML_END.size

            val bb = ByteBuffer.allocate(totalLength)
            bb.order(ByteOrder.LITTLE_ENDIAN)

            // Write XML chunk header and string pool
            for (b in BIN_XML_START) {
                bb.put(b.toByte())
            }

            // Write XML size and string pool size
            bb.position(4)
            bb.putInt(totalLength)
            bb.position(12)
            bb.putInt(BIN_XML_START.size - 8 + pathSpLength + spPaddingLength)

            bb.position(BIN_XML_START.size)

            // Write path data
            if (pathBytes.size > 127) {
                val high = (pathBytes.size and 0xFF00 or 0x8000 ushr 8).toByte()
                val low = (pathBytes.size and 0xFF).toByte()
                bb.put(high)
                bb.put(low)
                bb.put(high)
                bb.put(low)
            } else {
                val len = pathBytes.size.toByte()
                bb.put(len)
                bb.put(len)
            }
            bb.put(pathBytes)
            bb.put(0.toByte())

            // Padding to align on 32-bit
            if (spPaddingLength > 0) {
                bb.put(ByteArray(spPaddingLength))
            }

            // Write XML tag and attributes data
            val index = bb.position()
            for (b in BIN_XML_END) {
                bb.put(b.toByte())
            }

            // Write viewport size attributes
            bb.putInt(index + 124, width.toFloat().toRawBits())  // android:viewportWidth="..."
            bb.putInt(index + 144, height.toFloat().toRawBits())  // android:viewportHeight="..."

            return bb.array()
        }
    }

}
