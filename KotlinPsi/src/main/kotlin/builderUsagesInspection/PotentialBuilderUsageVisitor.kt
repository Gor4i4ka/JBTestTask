package builderUsagesInspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.refactoring.typeCook.deductive.resolver.BindingFactory
import dataClassBuilderInspection.DataClassBuilderCreationFix
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.references.KtInvokeFunctionReference
import org.jetbrains.kotlin.idea.references.KtInvokeFunctionReferenceDescriptorsImpl
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.idea.references.resolveToDescriptors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class PotentialBuilderUsageVisitor(private val holder: ProblemsHolder, private val isOnTheFly: Boolean):
    PsiElementVisitor() {

    private val DESCRIPTION_TEMPLATE = "Gotcha the builder, boyz"
    private val quickFix = BuilderWrapperFix()


    /*TODO: Understand what the hell is going on
    Возникла проблема с resolve'ингом reference'ов в Kotlin: все в null.
    В файлике, который кидался с Заданием было пояснение как resolve'нуть reference'ы.

    val call: KtCallElement =
        val resolvedTo: DeclarationDescriptor? =
        call.mainReference.resolveToDescriptors().firstOrNull()

    Я попытался так сделать и:
    1) Во первых resolveToDescriptors требует BindingContext, я попытался достать его с помощью analyze метода к самому элементу,
    потыкал разные параметры к analyze но зарезолвить так и не смог. Видимо он не для этого.
    2) Java-шные reference'ы резолвятся очень хорошо по типу element.mainReference.resolve(), с Kotlin не работает
    3) Потыкал разные классы, в которых было навание Search, Find, названия уже не помню, но не помогло. Думал найду
    какой-то полезный дескриптор.
    4) потыкал разные StackOverflow на тему написания плагинов (есть реп с примерами плагинов, но там самая база и для java,
    того же JavaPsiFacade походу для Kotlin нет), люди сталкивались с таким же постоянным null что и я.

    *) Есть мега-неоптимальный вариант искать Visitor'ом в каждом VirtualFile необходимый data class и его primary constructor: в package legacy
    я где-то эксперементировал с получением информации о всем проекте и запуске Visitor на нем. Но в таком случае это
    ОЧЕНЬ НЕОПТИМАЛЬНО и так делать очевидно не следует.

    Вопрос:Как на 2021 год нормально resolve'ить Kotlin референсы?Не получается(

     */
    override fun visitElement(element: PsiElement) {

        if (element is KtCallElement) {

            println("#########SPLIT##########")
            println(element.text)

            val mainRef = element.mainReference
            println("MainRef $mainRef")

            val resolve = mainRef?.resolve()
            println("Resolve $resolve")

            val analyze = element.analyze(BodyResolveMode.FULL)
            println("analyze $analyze")

            val resolveToDesc = mainRef?.resolveToDescriptors(analyze)
            println("resolveDesc $resolveToDesc")

            val resolvedTo = resolveToDesc?.firstOrNull()
            println("resolvedTo $resolvedTo")

        }
    }

}