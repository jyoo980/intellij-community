// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.reachability

import com.intellij.slicer.SliceNode

class JavaSliceHydrationService: SliceHydrationService() {
  override fun sliceInfo(slice: SliceNode?): String {
    return slice?.value?.element?.toString() ?: "Unrecognized structure"
  }
}
