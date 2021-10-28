// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.reachability;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.slicer.*;
import com.intellij.tools.StoredSettingsBean;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ReachabilityHandler implements CodeInsightActionHandler {

  private final SliceHandler handler;
  private final SliceCollector mySliceCollector = new SliceCollector();
  private final SliceHydrationService mySliceHydrationService = new JavaSliceHydrationService();
  private final Logger logger = Logger.getInstance(ReachabilityHandler.class);

  public ReachabilityHandler(SliceHandler handler) {
    this.handler = handler;
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull Editor editor,
                     @NotNull PsiFile file) {
    final PsiElement exprAtCaret = this.handler.getExpressionAtCaret(editor, file);
    if (exprAtCaret != null) {
      SliceAnalysisParams params = this.handler.askForParams(
        exprAtCaret,
        new StoredSettingsBean(),
        "" // TODO: investigate if this param needs to have a non-test value.
      );
      SliceRootNode rootNode = new SliceRootNode(
        project,
        new DuplicateMap(),
        LanguageSlicing.getProvider(exprAtCaret).createRootUsage(exprAtCaret, params)
      );
      Collection<SliceNode> fullSlice = this.mySliceCollector.getSlicesStartingFrom(rootNode);
      this.mySliceHydrationService.hydrateSlices(fullSlice).forEach((k, v) -> logger.info(String.format("SLICE: %s", v)));
    } else {
      logger.warn("Failed to get relevant expression under cursor");
    }
  }
}
