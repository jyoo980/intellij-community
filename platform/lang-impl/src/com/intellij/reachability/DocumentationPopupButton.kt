// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.reachability

import com.intellij.icons.AllIcons
import com.intellij.lang.documentation.ide.impl.resizePopup
import com.intellij.lang.documentation.ide.ui.DocumentationPopupUI
import com.intellij.openapi.editor.PopupBridge
import com.intellij.ui.popup.AbstractPopup
import javax.swing.JButton
import javax.swing.SwingConstants

class DocumentationPopupButton {

  val button: JButton = JButton(AllIcons.Toolwindows.Documentation).also{
    it.horizontalAlignment = SwingConstants.LEFT
    it.isBorderPainted = false
    it.isContentAreaFilled = false
  }

  fun setText(text: String) {
    this.button.text = text
  }

  internal fun activateAction(bridge: PopupBridge, ui: DocumentationPopupUI) {
    this.button.addActionListener {
      bridge.performWhenAvailable { popup: AbstractPopup ->
        ui.showDocumentationPane()
        ui.setPopup(popup)
        ui.updatePopup { resizePopup(popup) }
      }
    }
  }
}