package legacy

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtModifierList

class KtClassVisitorOld: PsiElementVisitor() {

    override fun visitElement(element: PsiElement) {
        //super.visitElement(element)
        if (element is KtClass) {
            println("${element.identifyingElement}  is data ${element.isData()}")
            val mods: KtModifierList? = element.modifierList
            println(mods)

            mods?.let {
                println("HELLO + $it")
                var iterator = it.firstChild

                while (iterator != null) {
                    println(iterator)
                    iterator = iterator.nextSibling
                }

                for (mod in it.children) {
                    println(mod)
                }

            }
        }
        super.visitElement(element)
    }
}