package localTests

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf

class BuilderUsagesInspectionOld :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private lateinit var kotlinFactory: KtPsiFactory
    private var debugFlag: Boolean? = null
    private val handledCollectionsOf = setOf<String>("listOf")
    private val handledCollections = setOf<String>("List")

    override val defaultFixText: String
        get() = "Wrap the constructor call with the DSL-style builder"

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {
        debugFlag = true
        kotlinFactory = KtPsiFactory(project)
        val applyResult = applyTo(null, element.parent as KtCallElement, project)
        if (applyResult != null)
            element.parent.replace(applyResult)
    }

    private fun applyTo(parentCall: KtCallElement?, call: KtCallElement, project: Project): KtCallElement? {

        val wrapperString: String? =
            if (isDataClassCall(call))
                processDataClassCall(call, project)
            else if (isCollectionOfCall(call))
                processCollectionClassCall(parentCall, call, project)
            else null

        // Sanity check (unreachable)
        if (wrapperString == null)
            return null

        val wrapperExpression = kotlinFactory.createExpression(wrapperString) as KtCallElement
        //call.replace(wrapperExpression)

        //println(wrapperExpression.text)

        for (potentialChild in getPotentialChildren(wrapperExpression)) {
            if (isApplicable(potentialChild)) {
                val applyResult = applyTo(call, potentialChild, project)
                if (applyResult != null)
                    potentialChild.replace(applyResult)
            }

        }
        return wrapperExpression
    }

    private fun getPotentialChildren(call: KtCallElement): List<KtCallElement> {


        //TODO: REMOVE THE KOSTIL
        var lambdaBlock: KtBlockExpression? = null

        fun extractBlockFromCall(call: KtElement) {
            for (child in call.children) {
                if (child is KtBlockExpression)
                    lambdaBlock = child

                extractBlockFromCall(child as KtElement)
            }
        }

        extractBlockFromCall(call)

        val childrenList = ArrayList<KtCallElement>()

        // With sanity check

        if (lambdaBlock != null) {
            for (potentialChild in lambdaBlock!!.children()) {

                // Simple binaryOps
                if (potentialChild.psi !is KtElement)
                    continue

                val potentialChildPsi = potentialChild.psi as KtElement

                // Simple binaryOps
                if (potentialChildPsi is KtBinaryExpression) {
                    val right = potentialChildPsi.right
                    if (right is KtCallElement)
                        childrenList.add(right as KtCallElement)
                }

                // TODO: add lambda visits
                if (potentialChildPsi is KtCallElement) {
                    childrenList.addAll(getPotentialChildren(potentialChildPsi))
                }
            }
        }

        return childrenList
    }

    private fun resolveConstructor(call: KtCallElement): KtPrimaryConstructor? {
        return (KotlinClassShortNameIndex
            .getInstance()
            .get("${call.firstChild?.text}", call.project, GlobalSearchScope.allScope(call.project))
            .firstOrNull() as KtClass?)?.primaryConstructor
    }

    private fun isDataClassCall(call: KtCallElement): Boolean {

        // TODO: ResolveToCall doesn't work for children!!!
        //PsiTestUtil.checkFileStructure(call.containingFile)
        //val potentialConstructor = call.resolveToCall()?.resultingDescriptor?.findPsi()

        if (debugFlag != null)
            println("${call.firstChild?.text}")

        val potentialConstructor = resolveConstructor(call)

        if (potentialConstructor is KtPrimaryConstructor) {
            val clazz = potentialConstructor.parent as KtClass
            if (clazz.isData()) {
                val potentialBuildingPair = findBuildAndBuilderByClassName(
                    clazz.nameAsSafeName.toString(),
                    clazz.project
                )
                if (potentialBuildingPair != null) {

                    // Sanity check
                    if (potentialBuildingPair.second != null &&
                        call.isInsideOf(listOf(potentialBuildingPair.second!!))
                    )
                        return false

                    return true
                }
            }
        }
        return false
    }

    private fun processDataClassCall(call: KtCallElement, project: Project): String {
        val resolvedConstructor = resolveConstructor(call)
        val rr = resolvedConstructor?.typeParameters
        val resolvedConstructorName = resolvedConstructor?.nameAsSafeName.toString()
        val resolvedConstructorParametersName: ArrayList<String> = ArrayList()

        if (resolvedConstructor != null) {
            for (parameter in resolvedConstructor.valueParameters)
                resolvedConstructorParametersName.add(parameter.nameAsSafeName.toString())
        }

        val resolvedBuildFunction = findBuildAndBuilderByClassName(resolvedConstructorName, project)?.first

        val wrapperString = StringBuilder()
            .append("${resolvedBuildFunction?.nameAsSafeName} {\n")
//            .apply {
//                for (argumentIndex in call.valueArguments.indices) {
//                    if (!isHandledCollection(resolvedConstructor?.valueParameters?.getOrNull(argumentIndex)?.
//                        typeWithSmartCast()?.nameIfStandardType.toString()))
//                        append("${resolvedConstructorParametersName[argumentIndex]} = ")
//                    append("${call.valueArguments[argumentIndex].getArgumentExpression()?.text}\n")
//                }
//
//            }
            .append("}")
            .toString()

        return wrapperString
    }

    private fun isHandledCollection(typeName: String?): Boolean {
        if (typeName == null)
            return false
        val collectionShortName: String? = Regex(".*<").find(typeName)?.value?.dropLast(1)
        if (collectionShortName != null && collectionShortName in handledCollections)
            return true
        return false
    }

    private fun isCollectionOfCall(call: KtCallElement): Boolean {
        val potentialCollectionOfName = call.firstChild?.text
        if (potentialCollectionOfName in handledCollectionsOf)
            return true
        return false
    }

    private fun processCollectionClassCall(parentCall: KtCallElement?, call: KtCallElement, project: Project): String {

        //Sanity check
        if (parentCall == null)
            return ""

        val resolvedConstructor = resolveConstructor(parentCall)
        val resolvedConstructorName = resolvedConstructor?.nameAsSafeName.toString()

        //Searching for Builder
        val resolvedBuilder = findBuildAndBuilderByClassName(resolvedConstructorName, project)?.second

        return "buildCollection {}"

    }

    private fun isApplicable(call: KtCallElement): Boolean {
        if (isDataClassCall(call) || isCollectionOfCall(call))
            return true
        return false
    }

    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Wraps data class' primary constructor call with a DSL-style builder if one is present in the project."
    }

    override fun isApplicable(element: KtNameReferenceExpression): Boolean {

        val parent = element.parent
        if (parent is KtCallElement && element.text !in handledCollectionsOf)
            return isApplicable(parent)
        return false
    }

    private fun findBuildAndBuilderByClassName(
        dataClassName: String?,
        project: Project
    ): Pair<KtNamedFunction, KtClass?>? {

        if (dataClassName == null)
            return null

        val potentialBuilder: KtClass? =
            KotlinClassShortNameIndex
                .getInstance()
                .get("${dataClassName}Builder", project, GlobalSearchScope.allScope(project))
                .firstOrNull() as KtClass?

        val potentialBuildFunction: KtNamedFunction =
            KotlinFunctionShortNameIndex
                .getInstance()
                .get("build${dataClassName}", project, GlobalSearchScope.allScope(project))
                .firstOrNull() ?: return null

        return Pair(potentialBuildFunction, potentialBuilder)

    }


}