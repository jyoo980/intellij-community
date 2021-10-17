// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.util;

import com.intellij.util.text.DateFormatUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SearchQueryCollector {

  private static SearchQueryCollector instance = null;
  private final Map<Long, String> queryMap = new HashMap<>();

  private SearchQueryCollector() {

  }

  public static SearchQueryCollector getInstance() {
    if (instance == null) {
      instance = new SearchQueryCollector();
    }
    return instance;
  }

  public void put(String query) {
    long currentTimeMs = Instant.now().toEpochMilli();
    this.queryMap.put(currentTimeMs, query);
  }

  public String getQueryResultString() {
    if (!this.queryMap.isEmpty()) {
      StringBuilder queriesWithTime = new StringBuilder();
      this.queryMap.forEach((timeInMs, query) -> {
        if (!query.isBlank()) {
          String fmtQuery = String.format("Date: %s, Query: %s\n", DateFormatUtil.formatDateTime(timeInMs), query);
          queriesWithTime.append(fmtQuery);
        }
      });
      return queriesWithTime.toString();
    }
    return "No Queries Executed";
  }
}
