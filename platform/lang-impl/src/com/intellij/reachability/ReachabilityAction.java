// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.reachability;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ReachabilityAction extends CodeInsightAction {

  @Override
  protected CodeInsightActionHandler getHandler() {
    return new ReachabilityHandler();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
  }

}
