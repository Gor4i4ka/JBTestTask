package dslStyleOld

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType

class BuilderGenerationSubInspectionOld(private val call: KtCallElement, val project: Project) {

    data class DataClassInfo(val clazz: KtClass, val hasBuildFunction: Boolean, val hasBuilderClass: Boolean)

    private val allUsedDataClassesSet = HashSet<DataClassInfo>()
    private val kotlinFactory = KtPsiFactory(project)
    private val collectionsHandled = listOf("List", "Set", "Stack")

    fun launchBuilderGeneration() {
        analyzeChildren(call)
        generateBuilders()
    }

    private fun analyzeChildren(element: KtElement) {

        // It is useful to analyze only KtNameReferenceExpression
        if (element is KtCallElement)
            analyzeElement(element)
        for (child in element.children)
            analyzeChildren(child as KtElement)
    }

    private fun analyzeElement(element: KtCallElement) {

        val callName = element.firstChild?.text
        val correspondingDataClass: KtClass? = callName?.let { resolveClass(it) }

        if (correspondingDataClass != null && correspondingDataClass.isData()) {
            val hasPotentialBuilder =
                !KotlinClassShortNameIndex
                    .getInstance()
                    .get("${callName}Builder", project, GlobalSearchScope.allScope(project))
                    .isEmpty()
            val hasPotentialBuild =
                !KotlinFunctionShortNameIndex
                    .getInstance()
                    .get("build${callName}", project, GlobalSearchScope.allScope(project))
                    .isEmpty()

            val dataClassInfo = DataClassInfo(
                clazz = correspondingDataClass,
                hasBuildFunction = hasPotentialBuild,
                hasBuilderClass = hasPotentialBuilder
            )

            allUsedDataClassesSet.add(dataClassInfo)
        }
    }

    private fun generateBuilders() {
        for (infoUnit in allUsedDataClassesSet) {
            if (!infoUnit.hasBuilderClass)
                generateSingleBuilder(infoUnit.clazz)
            if (!infoUnit.hasBuildFunction)
                generateClassBuildFunction(infoUnit.clazz)
            //allUsedDataClassesSet.remove(infoUnit)
        }
    }

    private fun generateSingleBuilder(clazz: KtClass) {

        val builderToAddString: String =
            StringBuilder()
                .append("class ")
                .append("${clazz.nameAsSafeName}Builder ")
                .append("{\n")
                .append(generateFields(clazz))
                .append(generateBuildMethod(clazz))
                .append("}")
                .toString()

        val builderToAdd = kotlinFactory.createClass(builderToAddString)
        clazz.containingFile.addAfter(builderToAdd, clazz)
    }

    private fun generateFields(clazz: KtClass): String {
        val fieldsToAppendString: String =
            StringBuilder()
                .apply {
                    for (parameter in clazz.primaryConstructorParameters)
                        append(generateSingleField(parameter))
                }
                .append("\n")
                .toString()

        return fieldsToAppendString
    }

    private fun generateSingleField(parameter: KtParameter): String {
        val fieldToAppendString: String =
            StringBuilder()
                .apply {

                    val parameterName = parameter.nameAsSafeName.toString()
                    val parameterType = parameter.type()
                    val collectionParameterTypeName =
                        Regex("<.*>").find(parameterType.toString())?.value?.drop(1)?.dropLast(1)
                    val collectionShortName = Regex(".*<").find(parameterType.toString())?.value?.dropLast(1)
                    val isCollection = collectionShortName in collectionsHandled

                    if (parameterType?.isPrimitiveNumberType() == true) {
                        if (parameterType.isMarkedNullable)
                            append("var ${parameterName}: $parameterType = null")
                        else
                            append("var $parameterName by Delegates.notNull<${parameterType}>()")
                    } else if (isCollection) {
                        append(
                            "var $parameterName = " +
                                    "mutable${collectionShortName}Of" +
                                    "<${collectionParameterTypeName}>()\n"
                        )

                        val potentialDataClass = collectionParameterTypeName?.let { resolveClass(it) }
                        if (potentialDataClass != null && potentialDataClass.isData())
                            append(generateBuildForSingleField(parameter))
                    } else {
                        if (parameterType?.isMarkedNullable == true)
                            append("var ${parameterName}: $parameterType = null")
                        else
                            append("lateinit var ${parameterName}: $parameterType")
                    }
                }
                .append("\n")
                .toString()

        return fieldToAppendString
    }

    private fun generateBuildForSingleField(parameter: KtParameter): String {

        val collectionParameterTypeName = Regex("<.*>").find(parameter.type().toString())?.value?.drop(1)?.dropLast(1)

        return StringBuilder()
//            .append(
//                "fun ${parameter.nameAsSafeName}Element" +
//                        "(init: ${collectionParameterTypeName}Builder.() -> Unit) {\n" +
//                        "${parameter.nameAsSafeName}.add(build${collectionParameterTypeName}(init))}\n"
//            )
            .append(
                "fun ${parameter.nameAsSafeName}Element" +
                        "(element: ${collectionParameterTypeName}) {\n" +
                        "${parameter.nameAsSafeName}.add(element)\n}"
            )
            .toString()
    }

    private fun generateBuildMethod(clazz: KtClass): String {
        val fieldBuildMethodToAddString: String =
            StringBuilder()
                .append("fun build(): ${clazz.nameAsSafeName} = \n")
                .append("${clazz.nameAsSafeName}")
                .append("(")
                .apply {
                    var hasPrevious = false
                    for (parameter in clazz.primaryConstructorParameters) {

                        if (hasPrevious)
                            append(", ")

                        append(parameter.nameAsSafeName)
                        hasPrevious = true
                    }
                }
                .append(")")
                .toString()

        return fieldBuildMethodToAddString
    }

    private fun generateClassBuildFunction(clazz: KtClass) {

        val buildFunctionToAddString: String =
            StringBuilder()
                .append("fun build${clazz.nameAsSafeName}")
                .append("(init: ${clazz.nameAsSafeName}Builder.() -> Unit): ${clazz.nameAsSafeName} =\n")
                .append("${clazz.nameAsSafeName}Builder().apply(init).build()")
                .toString()

        val buildFunctionToAdd = kotlinFactory.createFunction(buildFunctionToAddString)
        clazz.containingFile.addAfter(buildFunctionToAdd, clazz)
    }


    private fun resolveClass(className: String): KtClass? {
        return KotlinClassShortNameIndex
            .getInstance()
            .get(className, project, GlobalSearchScope.allScope(project))
            .firstOrNull() as KtClass?
    }
}