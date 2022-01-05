// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.navigation.actions

import com.intellij.codeInsight.navigation.impl.GTDUActionResult
import com.intellij.codeInsight.navigation.impl.NavigationActionResult
import com.intellij.navigation.impl.RawNavigationRequest
import com.intellij.navigation.impl.SourceNavigationRequest
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.PsiManagerImpl

object GotoJumpMemory {

  private val logger: Logger = Logger.getInstance(GotoJumpMemory::class.java)

  internal fun recordJump(GTDUActionResult: GTDUActionResult?, elementBeforeJump: PsiElement, project: Project) {
    GTDUActionResult?.let { it ->
      if (it is GTDUActionResult.GTD) {
        val actionResult = it.navigationActionResult
        if (actionResult is NavigationActionResult.SingleTarget) {
          when (val req = actionResult.request) {
            is RawNavigationRequest -> {
              if (req.navigatable is OpenFileDescriptor) {
                getElementAtOffset(req.navigatable, project)?.let { it ->
                  logger.info("Jump recorded: ${elementBeforeJump.containingFile.name}::${elementBeforeJump} -> ${it.containingFile.name}::${it}")
                }
              }
            }
            is SourceNavigationRequest ->
              error("Not yet implemented")
          }
        }
      }
    }
  }

  private fun getElementAtOffset(fileDescriptor: OpenFileDescriptor, project: Project): PsiElement? {
    val offset = fileDescriptor.offset
    val fileAfterJump = PsiManagerImpl.getInstance(project).findFile(fileDescriptor.file)
    return fileAfterJump?.let { it ->
      var element = it.findElementAt(offset)
      if (element == null && offset == it.textLength) {
        element = it.findElementAt(offset - 1)
      }
      if (element is PsiWhiteSpace) {
        element = it.findElementAt(element.textRange.startOffset -1)
      }
      return element
    }
  }
}