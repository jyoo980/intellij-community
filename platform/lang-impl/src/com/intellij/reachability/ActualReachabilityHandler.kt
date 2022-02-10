// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.reachability

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

object ActualReachabilityHandler {

  fun constructButtonFor(element: PsiElement): ReachabilityButton? {
    val optButton = if (this.isNonLiteralMethodArgReference(element)) {
      ForwardReachabilityButton(element)
    } else if (this.isLocalVariableReference(element)) {
      BackwardReachabilityButton(element)
    } else null
    optButton?.setButtonText()
    return optButton
  }

  private fun isNonLiteralMethodArgReference(element: PsiElement): Boolean {
    val optParentMethodCallExpr = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression::class.java)
    // Need to check if one of the element's parents is a method list. Otherwise, we get items like a.foo() being erroneously reported.
    val optParentExprList = PsiTreeUtil.getParentOfType(element, PsiExpressionList::class.java)
    return PsiTreeUtil.instanceOf(element, PsiIdentifier::class.java)  && optParentMethodCallExpr != null && optParentExprList != null
  }

  private fun isLocalVariableReference(element: PsiElement): Boolean {
    val isParentLocalVar = element.parent?.let { PsiTreeUtil.instanceOf(it, PsiLocalVariable::class.java) } ?: false
    return isParentLocalVar && PsiTreeUtil.instanceOf(element, PsiIdentifier::class.java)
  }
}