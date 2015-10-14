/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.SYNTHESIZED
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.codeInsight.DescriptorToSourceUtilsIde
import org.jetbrains.kotlin.idea.core.KotlinNameSuggester
import org.jetbrains.kotlin.idea.core.mapArgumentsToParameters
import org.jetbrains.kotlin.idea.core.quickfix.QuickFixUtil
import org.jetbrains.kotlin.idea.refactoring.changeSignature.JetChangeSignatureConfiguration
import org.jetbrains.kotlin.idea.refactoring.changeSignature.JetMethodDescriptor
import org.jetbrains.kotlin.idea.refactoring.changeSignature.modify
import org.jetbrains.kotlin.idea.refactoring.changeSignature.runChangeSignature
import org.jetbrains.kotlin.psi.JetCallElement
import org.jetbrains.kotlin.psi.JetFile
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.resolve.calls.callUtil.getCall
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.checker.JetTypeChecker

abstract class ChangeFunctionSignatureFix(
        protected val context: PsiElement,
        protected val functionDescriptor: FunctionDescriptor
) : KotlinQuickFixAction<PsiElement>(context) {

    override fun getFamilyName() = FAMILY_NAME

    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile): Boolean {
        if (!super.isAvailable(project, editor, file)) return false

        val declarations = DescriptorToSourceUtilsIde.getAllDeclarations(project, functionDescriptor)
        return declarations.all { it.isValid && QuickFixUtil.canModifyElement(it) }
    }

    protected fun getNewArgumentName(argument: ValueArgument, validator: Function1<String, Boolean>): String {
        val argumentName = argument.getArgumentName()
        val expression = argument.getArgumentExpression()

        if (argumentName != null) {
            return KotlinNameSuggester.suggestNameByName(argumentName.asName.asString(), validator)
        }
        else if (expression != null) {
            val bindingContext = expression.analyze(BodyResolveMode.PARTIAL)
            return KotlinNameSuggester.suggestNamesByExpressionAndType(expression, bindingContext, validator, "param").first()
        }
        else {
            return KotlinNameSuggester.suggestNameByName("param", validator)
        }
    }

    companion object : KotlinIntentionActionFactoryWithDelegate<JetCallElement, Data>() {
        data class Data(val callElement: JetCallElement, val descriptor: CallableDescriptor)

        override fun getElementOfInterest(diagnostic: Diagnostic): JetCallElement? {
            return diagnostic.psiElement.getNonStrictParentOfType<JetCallElement>()
        }

        override fun createQuickFixData(element: JetCallElement, diagnostic: Diagnostic): Data? {
            val descriptor = DiagnosticFactory.cast(diagnostic, Errors.TOO_MANY_ARGUMENTS, Errors.NO_VALUE_FOR_PARAMETER).a
            return Data(element, descriptor)
        }

        override fun createQuickFix(diagnostic: Diagnostic, quickFixDataFactory: () -> Data?): QuickFixWithDelegateFactory {
            return QuickFixWithDelegateFactory {
                val (callElement, descriptor) = quickFixDataFactory() ?: return@QuickFixWithDelegateFactory null
                createFix(callElement, callElement, descriptor)
            }
        }

        private fun createFix(callElement: JetCallElement, context: PsiElement, descriptor: CallableDescriptor): ChangeFunctionSignatureFix? {
            val functionDescriptor = when (descriptor) {
                is FunctionDescriptor -> descriptor as FunctionDescriptor
                else -> if (descriptor is ValueParameterDescriptor) descriptor.containingDeclaration as? FunctionDescriptor
                else null
            } ?: return null

            if (functionDescriptor.kind == SYNTHESIZED) return null

            if (descriptor is ValueParameterDescriptor) {
                return RemoveParameterFix(context, functionDescriptor, descriptor)
            }
            else {
                val parameters = functionDescriptor.valueParameters
                val arguments = callElement.valueArguments

                if (arguments.size > parameters.size) {
                    val bindingContext = callElement.analyze()
                    val call = callElement.getCall(bindingContext) ?: return null
                    val argumentToParameter = call.mapArgumentsToParameters(functionDescriptor)
                    val hasTypeMismatches = argumentToParameter.any {
                        val argument = it.key
                        val parameter = it.value
                        val argumentType = argument.getArgumentExpression()?.let { bindingContext.getType(it) }
                        argumentType == null || !JetTypeChecker.DEFAULT.isSubtypeOf(argumentType, parameter.type)
                    }
                    return AddFunctionParametersFix(callElement, functionDescriptor, hasTypeMismatches)
                }
            }

            return null
        }

        private class RemoveParameterFix(
                context: PsiElement,
                functionDescriptor: FunctionDescriptor,
                private val parameterToRemove: ValueParameterDescriptor
        ) : ChangeFunctionSignatureFix(context, functionDescriptor) {

            override fun getText() = "Remove parameter '${parameterToRemove.name.asString()}'"

            override fun invoke(project: Project, editor: Editor?, file: JetFile) {
                runRemoveParameter(parameterToRemove, context)
            }
        }

        val FAMILY_NAME = "Change signature of function/constructor"

        fun runRemoveParameter(parameterDescriptor: ValueParameterDescriptor, context: PsiElement) {
            val functionDescriptor = parameterDescriptor.containingDeclaration as FunctionDescriptor
            runChangeSignature(
                    context.project,
                    functionDescriptor,
                    object : JetChangeSignatureConfiguration {
                        override fun configure(originalDescriptor: JetMethodDescriptor): JetMethodDescriptor {
                            return originalDescriptor.modify { descriptor ->
                                val index = if (descriptor.receiver != null) parameterDescriptor.index + 1 else parameterDescriptor.index
                                descriptor.removeParameter(index)
                            }
                        }

                        override fun performSilently(affectedFunctions: Collection<PsiElement>) = false
                        override fun forcePerformForSelectedFunctionOnly() = false
                    },
                    context,
                    "Remove parameter '${parameterDescriptor.name.asString()}'")
        }

    }
}


