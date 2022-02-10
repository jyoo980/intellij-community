// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.reachability

import com.intellij.codeInsight.hint.actions.QuickPreviewAction
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.CaretSpecificDataContext
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import java.awt.Desktop
import java.net.URI
import javax.swing.JButton
import javax.swing.SwingConstants

sealed class ReachabilityButton(element: PsiElement) {

  protected val elementUnderCursor = element
  val button: JButton = JButton(AllIcons.General.ReachabilityQuestion).also {
    it.horizontalAlignment = SwingConstants.LEFT
    it.isBorderPainted = false
    it.isContentAreaFilled = false
  }

  abstract fun setButtonText()

  open fun activateAction(editor: Editor) {
    this.button.addActionListener {
      Desktop.getDesktop().browse(URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
    }
  }

  protected fun optIdentifierName(): String? {
    return (this.elementUnderCursor as? PsiIdentifier)?.text?.let {
      "<span style=\"font-family:JetBrains Mono;\">$it</font></span>"
    }
  }
}

class ForwardReachabilityButton(element: PsiElement): ReachabilityButton(element) {

  override fun setButtonText() {
    val optIdName = this.optIdentifierName()
    this.button.text = "<html>How was ${optIdName ?: "this argument"} created?</html>"
  }

  override fun activateAction(editor: Editor) {
    val dataContext = CaretSpecificDataContext(DataManager.getInstance().getDataContext(editor.contentComponent), editor.caretModel.currentCaret)
    this.button.addActionListener {
      QuickPreviewAction().performForContext(dataContext, false)
    }
  }
}

class BackwardReachabilityButton(element: PsiElement): ReachabilityButton(element) {

  override fun setButtonText() {
    val optIdName = this.optIdentifierName()
    val text = "<html>How is ${optIdName ?: "this variable"} modified?</html>"
    this.button.text = text
  }
}