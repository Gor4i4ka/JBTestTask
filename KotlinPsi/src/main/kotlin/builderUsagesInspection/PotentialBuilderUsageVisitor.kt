package builderUsagesInspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.refactoring.typeCook.deductive.resolver.BindingFactory
import dataClassBuilderInspection.DataClassBuilderCreationFix
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.references.KtInvokeFunctionReference
import org.jetbrains.kotlin.idea.references.KtInvokeFunctionReferenceDescriptorsImpl
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.idea.references.resolveToDescriptors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class PotentialBuilderUsageVisitor(private val holder: ProblemsHolder, private val isOnTheFly: Boolean):
    PsiElementVisitor() {

    private val DESCRIPTION_TEMPLATE = "Gotcha the builder, boyz"
    private val quickFix = BuilderWrapperFix()


    //TODO: Understand what the hell is going on
    override fun visitElement(element: PsiElement) {

        if (element is KtCallElement) {

            println("#########SPLIT##########")
            println(element.text)

            val mainRef = element.mainReference
            println("MainRef $mainRef")

            val resolve = mainRef?.resolve()
            println("Resolve $resolve")

            val analyze = element.analyze(BodyResolveMode.FULL)
            println("analyze $analyze")

            val resolveToDesc = mainRef?.resolveToDescriptors(analyze)
            println("resolveDesc $resolveToDesc")

            val resolvedTo = resolveToDesc?.firstOrNull()
            println("resolvedTo $resolvedTo")

        }
    }

}