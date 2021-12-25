package legacy

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dslStyle.dataClassBuilderInspection.DataClassFix
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtVisitorVoid

class DataClassVisitor(private val holder: ProblemsHolder, private val isOnTheFly: Boolean): KtVisitorVoid() {

    private val inspectionDescription = "HERE GOES THE DESCRIPTION BOIZ"
    private val inspectionFix = DataClassFix


    override fun visitClass(clazz: KtClass) {


        if (clazz.isData()) {
            val dataClassIdentifier = clazz.nameIdentifier
            if (!checkBuilderExists(dataClassIdentifier))
                holder.registerProblem(dataClassIdentifier!!, inspectionDescription, inspectionFix)
        }

        super.visitElement(clazz)
    }

    // TODO: replace with an adequate check in the whole module/package/project (Need "findUsages")
    private fun checkBuilderExists(element: PsiElement?): Boolean {
        val file = element?.containingFile

        if (file != null)
            for (child in file.children)
                if (child is KtClass && child.name == "${element.text}Builder")
                    return true

        return false
    }

//    private fun checkBuilderExists(dataClassIdentifier: PsiElement?): Boolean {
//
//        val builderName = "${dataClassIdentifier?.text}Builder"
//        val references = dataClassIdentifier?.references
//
//        println(builderName)
//        if (references != null) {
//            println("REFS BOIZ: $references ${references.size}")
//            for (ref in references) {
//                println("DA REF BOI: $ref")
//                println("RESOLVE: ${ref.resolve()}")
//
//                val resolvedElement = ref.resolve()
//                if(resolvedElement is KtClass && resolvedElement.nameIdentifier?.text == builderName)
//                    return false
//            }
//        }
//        return true
//    }

}