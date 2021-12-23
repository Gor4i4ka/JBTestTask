package dataClassBuilderInspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.psi.KtClass

/*TODO: replace with an adequate check in the whole module/package/project (Need "findUsages")
Для того, чтобы option инспекции "создать builder" не вылезал, когда он уже есть, необходимо либо
1) либо Найти все использования data class и проверить среди них String типа "${identifier.name}Builder"
2) либо пройтись по всем классам в проекте/проектах и сравнить их с таким String

как реализовать (2) НЕОПТИМАЛЬНО понятно, но наверняка можно сделать (1). По документации reference'ов на использование
определения нет, референс всегда ОТ использования к определению. Наверняка есть findDeclUsages для Kotlin, для
java нечто подобное вроде было найдено, но возможно оно реализовано через (2).

В любом случае нахождение "обратных references" тесно связано с использованием нормальных reference'ов, но
и с resolve'ингом последних возникли проблемы.

Вопрос: достаточно ли топорно(2) найти потенциальный builder для data class'а. Так же ГДЕ искать потенциальный builder:
1) package
2) модуль
3) проект
4) все зависимости
Мне кажется логичным (2) (модуль, где объявление видимо)

вопрос: аналогично, где сделать доступным замену вызова primary constructor: думаю так же в модуле где объявление видимо

//////////////////////////////////////////////////////////////////

В документации по напомисанию плагинов есть упоминание UAST и других представлений программы.
Вопрос: верно ли, что мне вряд ли стоит сейчас их исследовать и не пытаться сделать тот же resolve через другой граф?

///////////////////////////////////////////////////////////////////

В документации по написанию плагинов присутствует упоминание "кэширования" и советов заменять дорогие операции
более дешевыми.
Вопрос: стоит ли мне в это вникать или для задания неважно.

///////////////////////////////////////////////////////////////////

Вопрос: У меня при выполнении таска Gradle:runide безумное количество варнингов и ерроров.
В основе корутины и отсутствующие классы для... рефлексии? Это нормально? Стоит ли мне обращать внимание?
P.S. Я знаю, что надо добавить groupKey инспекциям

////////////////////////////////////////////////////////////////////

Вопрос: Unit тесты по типу примеров из репозитория с примерами плагинов intelliji сойдут?
https://github.com/JetBrains/intellij-sdk-code-samples

 */

class DataClassVisitor(private val holder: ProblemsHolder, private val isOnTheFly: Boolean): PsiElementVisitor() {

    private val DESCRIPTION_TEMPLATE = "HERE GOES THE DESCRIPTION BOIZ"
    private val quickFix = DataClassBuilderCreationFix()


    override fun visitElement(element: PsiElement) {


        if (element is KtClass && element.isData()) {
            val dataClassIdentifier = element.nameIdentifier
            if (!checkBuilderExists(dataClassIdentifier))
                holder.registerProblem(dataClassIdentifier!!, DESCRIPTION_TEMPLATE, quickFix)
        }

        super.visitElement(element)
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