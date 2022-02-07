// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.reachability;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.slicer.SliceHandler;
import org.jetbrains.annotations.NotNull;

public class ReachabilityAction extends CodeInsightAction {

  @NotNull
  @Override
  protected CodeInsightActionHandler getHandler() {
    // Boolean param to SliceHandler#create is `false`, since the parameter
    // represents a field called `dataFlowToThis`. Since we're only concerned
    // (at the moment) with forward reachability questions, we pass in `false`.
    SliceHandler forwardSliceHandler = SliceHandler.create(false);
    SliceCollector collector = new SliceCollector();
    SliceHydrationService sliceHydrationService = new JavaSliceHydrationService();
    return new ReachabilityHandler(forwardSliceHandler, collector, sliceHydrationService);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
  }
}
