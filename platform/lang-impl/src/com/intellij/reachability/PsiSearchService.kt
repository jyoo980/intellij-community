// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.reachability

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLambdaExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.slicer.SliceNode

object PsiSearchService {

  fun matchToMethodAndParent(nodes: Collection<SliceNode>): Map<SliceNode, List<PsiElement?>> {
    val funs: List<(n: SliceNode) -> PsiElement?> = listOf(
      { n -> this.findNearestStructWithType(n, PsiMethod::class.java) }, // Get enclosing method
      { n -> this.findNearestStructWithType(n, PsiClass::class.java) }   // Get enclosing class
    )
    return nodes.associateBy({ it }, { funs.map { f -> f(it) }})
  }

  fun prettyPrint(optMethod: PsiElement?, optClazz: PsiElement?): String {
    val methodName = optMethod?.let { (it as PsiMethod).name } ?: "prettyPrint unsupported"
    val clazzName = optClazz?.let { (it as PsiClass).name } ?: "prettyPrint unsupported"
    return "$clazzName#$methodName"
  }

  private fun <T: PsiElement> findNearestStructWithType(node: SliceNode, targetType: Class<T>): PsiElement? {
    return node.value?.let {
      PsiTreeUtil.getParentOfType(
        it.element,
        targetType,
        true,
        PsiClass::class.java,
        PsiLambdaExpression::class.java
      )
    }
  }
}