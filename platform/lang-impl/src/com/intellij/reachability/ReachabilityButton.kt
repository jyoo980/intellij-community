// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.reachability

import com.intellij.icons.AllIcons
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

  fun activateAction() {
    this.button.addActionListener {
      Desktop.getDesktop().browse(URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
    }
  }

  protected fun optIdentifierName(): String? {
    return (this.elementUnderCursor as? PsiIdentifier)?.text
  }
}

class ForwardReachabilityButton(element: PsiElement): ReachabilityButton(element) {

  override fun setButtonText() {
    val optIdName = this.optIdentifierName()
    val text = "How was ${optIdName ?: "this argument" } created?"
    this.button.text = text
  }
}

class BackwardReachabilityButton(element: PsiElement): ReachabilityButton(element) {

  override fun setButtonText() {
    val optIdName = this.optIdentifierName()
    val text = "How is ${optIdName ?: "this variable" } modified?"
    this.button.text = text
  }
}