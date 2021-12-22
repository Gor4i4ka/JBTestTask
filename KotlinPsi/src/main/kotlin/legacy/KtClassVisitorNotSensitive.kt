package legacy

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import dataClassBuilderInspection.DataClassBuilderCreationFix
import org.jetbrains.kotlin.psi.KtClass

class KtClassVisitorNotSensitive(private val holder: ProblemsHolder, private val isOnTheFly: Boolean): PsiElementVisitor() {

    private val DESCRIPTION_TEMPLATE = "DESCRIPTION BOIZ"
    private val quickFix = DataClassBuilderCreationFix()


    override fun visitElement(element: PsiElement) {

        if (element is KtClass && element.isData()) {
            val dataClassIdentifier = element.nameIdentifier
            holder.registerProblem(dataClassIdentifier!!, DESCRIPTION_TEMPLATE, quickFix)
        }

        super.visitElement(element)
    }
}