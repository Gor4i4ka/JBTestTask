package legacy

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder

class DataClassInspectorOld: LocalInspectionTool() {

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

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): DataClassVisitor {
        return DataClassVisitor(holder, isOnTheFly)
    }
}