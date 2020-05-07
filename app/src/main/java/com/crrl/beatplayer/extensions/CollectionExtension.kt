/*
 * Copyright (c) 2020. Carlos René Ramos López. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crrl.beatplayer.extensions

import com.crrl.beatplayer.models.MediaItem

fun <T> List<T>?.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (this == null) {
        return emptyList()
    }
    return toMutableList().apply {
        val deleted = removeAt(fromIndex)
        add(toIndex, deleted)
    }
}

fun <E> MutableList<E>.setAll(list: List<E>) {
    clear()
    addAll(list)
}

fun <T> MutableList<T>.delete(item: T) {
    item ?: throw NullPointerException("The index can't be null.")

    setAll(filterNot { it == item }.optimizeReadOnlyList())
}

internal fun <T> List<T>.optimizeReadOnlyList() = when (size) {
    0 -> emptyList()
    1 -> listOf(this[0])
    else -> this
}

fun <T : MediaItem> Collection<T>.deepEquals(
    list2: Collection<T>
) = if (size != list2.size) false else zip(list2).all { (elt1, elt2) -> elt1.compare(elt2) }