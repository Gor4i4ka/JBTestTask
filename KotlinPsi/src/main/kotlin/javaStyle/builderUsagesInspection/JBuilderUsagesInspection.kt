package dslStyle.builderUsagesInspection

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf

class JBuilderUsagesInspection : AbstractApplicabilityBasedInspection<KtCallElement>(KtCallElement::class.java) {

    private lateinit var builderClassPointer: SmartPsiElementPointer<KtClass>
    private lateinit var dataClassPointer: SmartPsiElementPointer<KtClass>

    override val defaultFixText: String
        get() = "Wrap the constructor call with the builder"

    override fun applyTo(element: KtCallElement, project: Project, editor: Editor?) {
        val kotlinFactory = KtPsiFactory(project)

        val builderCallString =
            StringBuilder()
                .append("${builderClassPointer.element?.nameIdentifier?.text}()")
                .append(".apply")

                // "apply" body
                .append("{")
                .apply {
                    val dataValueParamList = dataClassPointer.element?.primaryConstructorParameters
                    val callValueArgumentList = element.valueArgumentList

                    dataValueParamList?.let {

                        var hasPrevious = false
                        for (fieldIndex in it.indices) {
                            if (hasPrevious)
                                this.append("; ")

                            //TODO: remove kostil
                            this.append(
                                "${dataValueParamList[fieldIndex].nameAsSafeName} = " +
                                        "   ${callValueArgumentList?.children?.get(fieldIndex)?.firstChild?.text}"
                            )

                            hasPrevious = true
                        }
                    }
                }
                .append("}")

                .append(".build()!!")
                .toString()

        element.replace(kotlinFactory.createExpression(builderCallString))
    }


    override fun inspectionText(element: KtCallElement): String {
        return "Wraps data class' primary constructor call with a Java-style builder if one is present in the project."
    }

    override fun isApplicable(element: KtCallElement): Boolean {

        val potentialConstructor = element.resolveToCall()?.resultingDescriptor?.findPsi()

        if (potentialConstructor is KtPrimaryConstructor) {
            val potentialDataClass = potentialConstructor.parent
            if (potentialDataClass is KtClass && potentialDataClass.isData()) {

                dataClassPointer = potentialDataClass.createSmartPointer()

                val builderClass = findBuilderClass(potentialDataClass)
                if (builderClass == null)
                    return false
                else
                    builderClassPointer = builderClass.createSmartPointer()

                // Sanity check to not report inside the builder
                if (element.isInsideOf(listOf(builderClassPointer.element!!)))
                    return false

                return true
            }
            return false
        }
        return false
    }

    private fun findBuilderClass(potentialDataClass: KtClass): KtClass? {

        val project = potentialDataClass.project
        val builderName = potentialDataClass.nameIdentifier?.text

        val potentialBuilders: Collection<KtClassOrObject> = KotlinClassShortNameIndex
            .getInstance()
            .get("${builderName}Builder", project, GlobalSearchScope.allScope(project))

        if (potentialBuilders.isEmpty())
            return null

        return potentialBuilders.first() as KtClass?
    }

}