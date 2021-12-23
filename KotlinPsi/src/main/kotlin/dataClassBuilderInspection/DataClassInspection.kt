package dataClassBuilderInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.psi.*

class DataClassInspection: LocalInspectionTool() {

    private val inspectionDescription = "HERE GOES THE DESCRIPTION BOIZ"
    private val inspectionFix = DataClassFix

/*TODO: Add GUI Swing panel

    Вопрос: добавить Swing панельку для более красочного отображения description'а плагина (в потенциале)?
    PS: это откуда-то сворованная заготовка
*/
//// PREPARATIONS FOR GUI BOIZZZZZ
//    override fun createOptionsPanel(): JComponent {
//        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
//        val checkedClasses = JTextField(CHECKED_CLASSES)
//        checkedClasses.document.addDocumentListener(object : DocumentAdapter() {
//            override fun textChanged(event: DocumentEvent) {
//                CHECKED_CLASSES = checkedClasses.text
//            }
//        })
//        panel.add(checkedClasses)
//        return panel
//    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        object: KtVisitorVoid() {

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