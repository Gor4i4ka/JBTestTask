package legacy

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class AllVisitor: PsiElementVisitor() {

    override fun visitElement(element: PsiElement) {
        println(element)
        super.visitElement(element)
    }
}