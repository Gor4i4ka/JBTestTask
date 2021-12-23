package legacy

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtVisitorVoid

class DataClassInspectorApplicabilityTest: AbstractApplicabilityBasedInspection<KtElement>(KtElement::class.java) {

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
            override fun visitKtElement(element: KtElement) {
                super.visitKtElement(element)

                //with (element as KtClass) {
                //    visitTargetElement(this.nameIdentifier as KtElement, holder, isOnTheFly)
                //}
            }
        }


    override val defaultFixText: String
        get() = "Some defaultFixText"

    override fun applyTo(element: KtElement, project: Project, editor: Editor?) {
        return
    }

    override fun inspectionText(element: KtElement): String {
        return "Some inspectionText"
    }

    // Здесь баг: текст рейндж куда-то улетает
    override fun inspectionHighlightRangeInElement(element: KtElement): TextRange? {
        return with(element as KtClass) { element.nameIdentifier?.textRange }
    }


    override fun isApplicable(element: KtElement): Boolean {

        if (element is KtClass) {
            if (!element.isData())
                return false

            val file = element.containingFile

            for (child in file.children)
                if (child is KtClass && child.name == "${element.text}Builder")
                    return false

            return true
        }
        else
            return false
    }
}