package dslStyle.dataClassBuilderInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.psi.*

class DataClassInspection : LocalInspectionTool() {

    private val inspectionDescription = "Generates a DSL-style builder class to construct data class instances' step by step."
    private val inspectionFix = DataClassFix()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        object : KtVisitorVoid() {

            override fun visitClass(clazz: KtClass) {

                if (clazz.isData()) {
                    val dataClassIdentifier = clazz.nameIdentifier
                    if (checkBuilderNotExists(dataClassIdentifier))
                        holder.registerProblem(dataClassIdentifier!!, inspectionDescription, inspectionFix)
                }

                super.visitElement(clazz)
            }

            private fun checkBuilderNotExists(element: PsiElement?): Boolean {
                val project = element?.project

                val potentialBuilders = KotlinClassShortNameIndex
                    .getInstance()
                    .get("${element?.text}Builder", project!!, GlobalSearchScope.allScope(project))

                if (potentialBuilders.isEmpty())
                    return true

                return false
            }
        }

}