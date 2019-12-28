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

import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon


internal fun testIcon(id: Int, catgId: Int, tags: List<String> = emptyList()) =
        Icon(id, catgId, tags, "", 0, 0)

internal fun testCatg(id: Int) =
        Category(id, "Category $id", 0)
