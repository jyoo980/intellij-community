// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.navigation.actions

import com.intellij.codeInsight.navigation.impl.GTDUActionResult
import com.intellij.codeInsight.navigation.impl.NavigationActionResult
import com.intellij.navigation.impl.RawNavigationRequest
import com.intellij.navigation.impl.SourceNavigationRequest
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.PsiManagerImpl

object GotoJumpMemory {

  private val logger: Logger = Logger.getInstance(GotoJumpMemory::class.java)

  internal fun recordJump(GTDUActionResult: GTDUActionResult?, elementBeforeJump: PsiElement, project: Project) {
    GTDUActionResult?.let { it ->
      when (it) {
        is GTDUActionResult.GTD -> {
          val actionResult = it.navigationActionResult
          if (actionResult is NavigationActionResult.SingleTarget) {
            when (val req = actionResult.request) {
              is RawNavigationRequest -> {
                if (req.navigatable is OpenFileDescriptor) {
                  val virtualFile = req.navigatable.file
                  val offset = req.navigatable.offset
                  getElementAtOffset(virtualFile, offset, project)?.let { afterJump ->
                    logger.info(jumpRecord(elementBeforeJump, afterJump))
                  }
                }
              }
              is SourceNavigationRequest -> getElementAtOffset(req.file, req.offset, project)?.let { afterJump ->
                logger.info(jumpRecord(elementBeforeJump, afterJump))
              }
            }
          }
        }
        is GTDUActionResult.SU -> {
          val searchTarget = it.targetVariants
          logger.info("Show Usages triggered on: ${searchTarget[0]}")
        }
      }
    }
  }

  private fun getElementAtOffset(virtualFile: VirtualFile, offset: Int, project: Project): PsiElement? {
    val fileAfterJump = PsiManagerImpl.getInstance(project).findFile(virtualFile)
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

  private fun jumpRecord(prev: PsiElement, curr: PsiElement): String {
    val prevFileName = prev.containingFile.name
    val currFileName = curr.containingFile.name
    val lhs = "Jump recorded: $prevFileName::$prev"
    val rhs = "$currFileName::$curr"
    return "$lhs -> $rhs"
  }
}