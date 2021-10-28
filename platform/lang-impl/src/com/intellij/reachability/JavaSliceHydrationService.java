// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.reachability;

import com.intellij.slicer.SliceNode;

public class JavaSliceHydrationService extends SliceHydrationService {

  @Override
  public String sliceInfo(SliceNode slice) {
    try {
      return slice.getValue().getElement().toString();
    } catch (Exception e) {
      logger.warn(String.format("Slice: %s triggered NPE", slice.toString()));
      return "Unrecognized structure";
    }
  }
}
