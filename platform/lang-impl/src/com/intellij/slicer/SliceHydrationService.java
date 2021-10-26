// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.slicer;

import com.intellij.openapi.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// TODO: Extract top-level interface. Do NOT hard-code to Java.
public class SliceHydrationService {

  // https://stackoverflow.com/questions/68633/regex-that-will-match-a-java-method-declaration
  private static final String METHOD_DECL =
    "(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\],\\s]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])";
  private static final String RETURN_STMT = "return .*";
  private static final String CONDITIONAL_STMT = "if(\\s)*(.*)(\\s)*";
  // TODO: Need a way to handle the JetBrains comment that seemingly happens at the start of each trace.
  private static final String COMMENT_STR = "\\/\\/.*";

  public Map<SliceNode, String> hydrateSlices(final Collection<SliceNode> sliceNodes) {
    final Map<SliceNode, String> hydratedSlices = new HashMap<>();
    sliceNodes.stream()
      .map(slice -> Pair.create(slice, this.sliceInfo(slice)))
      .forEach(pair -> hydratedSlices.put(pair.first, pair.second));
    return hydratedSlices;
  }

  private String sliceInfo(final SliceNode slice) {
    final String sliceText = slice.getNodeText();
    if (Pattern.matches(METHOD_DECL, sliceText)) {
      return String.format("Method declaration: %s", sliceText);
    } else if (Pattern.matches(RETURN_STMT, sliceText)) {
      return String.format("Return statement: %s", sliceText);
    } else if (Pattern.matches(CONDITIONAL_STMT, sliceText)) {
      return String.format("Conditional statement: %s", sliceText);
    } else if (Pattern.matches(COMMENT_STR, sliceText)) {
      return String.format("Comment: %s", sliceText);
    } else return String.format("Currently unrecognized structure: %s", sliceText);
  }
}
