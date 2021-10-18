// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.actions.searcheverywhere;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.NavigationItemListCellRenderer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.Processor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StackOverflowContributor implements SearchEverywhereContributor {

  @Override
  public @NotNull String getSearchProviderId() {
    return getClass().getSimpleName();
  }

  @Nls
  @Override
  public @NotNull String getGroupName() {
    return IdeBundle.message("searcheverywhere.stackoverflow.tab.name");
  }

  @Override
  public int getSortWeight() {
    return 0;
  }

  @Override
  public boolean showInFindResults() {
    return false;
  }

  @Override
  public void fetchElements(@NotNull String pattern,
                            @NotNull ProgressIndicator progressIndicator,
                            @NotNull Processor consumer) {


  }

  @Override
  public boolean processSelectedItem(@NotNull Object selected, int modifiers, @NotNull String searchText) {
    return false;
  }

  @Override
  public @NotNull ListCellRenderer getElementsRenderer() {
    return new NavigationItemListCellRenderer();
  }

  @Override
  public @Nullable Object getDataForItem(@NotNull Object element,
                                         @NotNull String dataId) {
    return null;
  }

  @Override
  public boolean isShownInSeparateTab() {
    return true;
  }
}
