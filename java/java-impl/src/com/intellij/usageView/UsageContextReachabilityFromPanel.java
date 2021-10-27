// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.usageView;

import com.intellij.java.JavaBundle;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiVariable;
import com.intellij.usages.*;
import com.intellij.usages.impl.UsageContextPanelBase;
import com.intellij.usages.impl.UsageViewImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class UsageContextReachabilityFromPanel extends UsageContextPanelBase {

  private JComponent myPanel;

  public UsageContextReachabilityFromPanel(@NotNull Project project,
                                           @NotNull UsageViewPresentation presentation) {
    super(project, presentation);
  }

  @Override
  protected void updateLayoutLater(@Nullable List<? extends UsageInfo> infos) {

  }

  @Override
  public void dispose() {
    super.dispose();
    this.myPanel = null;
  }

  public static class Provider implements UsageContextPanel.Provider {
    @NotNull
    @Override
    public UsageContextPanel create(@NotNull UsageView usageView) {
      return new UsageContextReachabilityFromPanel(((UsageViewImpl)usageView).getProject(), usageView.getPresentation());
    }

    @Override
    public boolean isAvailableFor(@NotNull UsageView usageView) {
      UsageTarget[] targets = ((UsageViewImpl) usageView).getTargets();
      if (targets.length == 0) return false;
      UsageTarget target = targets[0];
      if (!(target instanceof PsiElementUsageTarget)) return false;
      PsiElement element = ((PsiElementUsageTarget) target).getElement();
      if (element == null || !element.isValid()) return false;
      if (!(element instanceof PsiVariable)) return false;
      PsiFile file = element.getContainingFile();
      return file instanceof PsiJavaFile;
    }

    @NotNull
    @Override
    public String getTabTitle() {
      return JavaBundle.message("reachability.from.here");
    }
  }
}
