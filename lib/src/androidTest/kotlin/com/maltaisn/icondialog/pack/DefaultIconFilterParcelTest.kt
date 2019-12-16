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

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maltaisn.icondialog.DefaultIconFilter
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4::class)
internal class DefaultIconFilterParcelTest {

    @Test
    fun parcelTest() {
        val filter1 = DefaultIconFilter()
        filter1.termSplitPattern = null
        filter1.queryNormalized = false
        filter1.idSearchEnabled = true

        // Write
        val parcel = Parcel.obtain()
        filter1.writeToParcel(parcel, filter1.describeContents())
        parcel.setDataPosition(0)

        // Read
        val filter2 = DefaultIconFilter.CREATOR.createFromParcel(parcel)
        assertEquals(filter1.termSplitPattern, filter2.termSplitPattern)
        assertEquals(filter1.queryNormalized, filter2.queryNormalized)
        assertEquals(filter1.idSearchEnabled, filter2.idSearchEnabled)
    }

}
