// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.navigation.actions

import com.intellij.codeInsight.navigation.impl.GTDUActionResult
import com.intellij.codeInsight.navigation.impl.NavigationActionResult
import com.intellij.navigation.impl.RawNavigationRequest
import com.intellij.navigation.impl.SourceNavigationRequest
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.refactoring.actions.BaseRefactoringAction

object GotoJumpMemory {

  private val logger: Logger = Logger.getInstance(GotoJumpMemory::class.java)

  internal fun recordJump(GTDUActionResult: GTDUActionResult?, elementBeforeJump: PsiElement, project: Project, editor: Editor) {
    GTDUActionResult?.let { it ->
      if (it is GTDUActionResult.GTD) {
        val actionResult = it.navigationActionResult
        if (actionResult is NavigationActionResult.SingleTarget) {
          when (val req = actionResult.request) {
            is RawNavigationRequest -> {
              if (req.navigatable is OpenFileDescriptor) {
                val elementAfterJump = extractElement(req.navigatable, project, editor)
                logger.info(
                  "Jump recorded: ${elementBeforeJump.containingFile.name}::${elementBeforeJump} -> ${elementAfterJump.containingFile.name}::${elementAfterJump}"
                )
              }
            }
            is SourceNavigationRequest ->
              error("Not yet implemented")
          }
        }
      }
    }
  }

  // FIXME: 2022-01-04 I have a suspicion that the offset isn't correct here... need to find a way to see where the cursor ends up.
  private fun extractElement(openFileDescriptor: OpenFileDescriptor, project: Project, editor: Editor): PsiElement {
    val virtualFile = openFileDescriptor.file
    val fileAfterJump = PsiManagerImpl.getInstance(project).findFile(virtualFile)
    // FIXME: 2022-01-04 The editor cursor is likely stale (offset stored from the old file). 
    return BaseRefactoringAction.getElementAtCaret(editor, fileAfterJump)
  }
}