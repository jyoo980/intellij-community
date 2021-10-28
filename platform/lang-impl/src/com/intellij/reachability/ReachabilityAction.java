// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.reachability;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.slicer.SliceHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URI;

public class ReachabilityAction extends CodeInsightAction {

  private final Logger logger = Logger.getInstance(ReachabilityAction.class);

  @NotNull
  @Override
  protected CodeInsightActionHandler getHandler() {
    logger.info("REACHABILITY ACTION TRIGGERED");
    // Boolean param to SliceHandler#create is `false`, since the parameter
    // represents a field called `dataFlowToThis`. Since we're only concerned
    // (at the moment) with forward reachability questions, we pass in `false`.
    SliceHandler forwardSliceHandler = SliceHandler.create(false);
    return new ReachabilityHandler(forwardSliceHandler);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
  }

}
