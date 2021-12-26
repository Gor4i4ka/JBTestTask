package dslStyle.builderUsagesInspection

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.cfg.pseudocode.and
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.j2k.getContainingClass
import org.jetbrains.kotlin.j2k.getContainingMethod
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf

class BuilderUsagesInspection :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private lateinit var builderClassPointer: SmartPsiElementPointer<KtClass>
    private lateinit var dataClassPointer: SmartPsiElementPointer<KtClass>

    override val defaultFixText: String
        get() = "Wrap the constructor call with the DSL-style builder"

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {
    }


    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Wraps data class' primary constructor call with a DSL-style builder if one is present in the project."
    }

    override fun isApplicable(element: KtNameReferenceExpression): Boolean {

        val parent = element.parent

        if (parent is KtCallElement) {
            val potentialConstructor = element.resolveToCall()?.resultingDescriptor?.findPsi()
            if (potentialConstructor is KtPrimaryConstructor) {
                val clazz = potentialConstructor.parent as KtClass
                if (clazz.isData()) {
                    val potentialBuildingPair = findBuildAndBuilderByClassName(
                        clazz.nameAsSafeName.toString(),
                        clazz.project
                    )
                    if (potentialBuildingPair != null) {

                        // Sanity check
                        if (potentialBuildingPair.second != null &&
                            element.isInsideOf(listOf(potentialBuildingPair.second!!))
                        )
                            return false

                        return true
                    }
                }
            }
        }

        return false
    }

    private fun findBuildAndBuilderByClassName(
        dataClassName: String,
        project: Project
    ): Pair<KtNamedFunction, KtClass?>? {
        val potentialBuilder: KtClass? =
            KotlinClassShortNameIndex
                .getInstance()
                .get("${dataClassName}Builder", project, GlobalSearchScope.allScope(project))
                .firstOrNull() as KtClass?

        val potentialBuildFunction: KtNamedFunction =
            KotlinFunctionShortNameIndex
                .getInstance()
                .get("build${dataClassName}", project, GlobalSearchScope.allScope(project))
                .firstOrNull() ?: return null

        return Pair(potentialBuildFunction, potentialBuilder)

    }


}