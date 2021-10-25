// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.slicer;

import java.util.*;

public class SliceCollector {

  private static final int DEFAULT_MAX_CALL_DEPTH = 10 ;

  public Collection<SliceNode> getSlicesStartingFrom(final SliceNode root) {
    return getSlicesStartingFrom(root, 0);
  }

  private Collection<SliceNode> getSlicesStartingFrom(final SliceNode root, final int callsSoFar) {
    final List<SliceNode> acc = new ArrayList<>(Collections.singletonList(root));
    if (callsSoFar < DEFAULT_MAX_CALL_DEPTH) {
      if (!root.getChildren().isEmpty()) {
        root.getChildren().stream()
          .filter(child -> !acc.contains(child))
          .map(child -> this.getSlicesStartingFrom(child, callsSoFar + 1))
          .forEach(slices -> acc.addAll(slices));
      }
    }
    return acc;
  }

  public Map<SliceNode, String> sliceDescriptionMap(final Collection<SliceNode> slices) {
    final Map<SliceNode, String> sliceDescriptions = new HashMap<>();
    slices.forEach(slice -> {
      String desc = slice.getValue().getElement().getClass().getSimpleName();
      sliceDescriptions.put(slice, desc);
    });
    return sliceDescriptions;
  }
}
