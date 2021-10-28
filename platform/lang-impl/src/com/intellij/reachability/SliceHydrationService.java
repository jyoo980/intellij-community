// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.reachability;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.slicer.SliceNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SliceHydrationService {

  protected final Logger logger = Logger.getInstance(SliceHydrationService.class);

  public Map<SliceNode, String> hydrateSlices(final Collection<SliceNode> sliceNodes) {
    final Map<SliceNode, String> hydratedSlices = new HashMap<>();
    sliceNodes.stream()
      .map(slice -> Pair.create(slice, this.sliceInfo(slice)))
      .forEach(pair -> hydratedSlices.put(pair.first, pair.second));
    return hydratedSlices;
  }

  // Technically, we don't need to generate our own description for the slice.
  // Each slice has an field of type PsiElement. The OOP way of doing this would
  // be to define a top-level "descriptive string" method and override it in
  // the subtypes that we need.
  abstract String sliceInfo(final SliceNode slice);
}
