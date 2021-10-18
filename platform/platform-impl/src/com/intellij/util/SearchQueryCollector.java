// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SearchQueryCollector {

  private static SearchQueryCollector instance;
  private final List<String> queries = new LinkedList<>();

  private SearchQueryCollector() {

  }

  public static SearchQueryCollector getInstance() {
    if (instance == null) {
      instance = new SearchQueryCollector();
    }
    return instance;
  }

  public void add(String query) {
    this.queries.add(query);
  }

  public Optional<String> getLatestQuery() {
    return this.queries.stream()
      .filter(Predicate.not(String::isBlank))
      .reduce((first, second) -> second);
  }
}
