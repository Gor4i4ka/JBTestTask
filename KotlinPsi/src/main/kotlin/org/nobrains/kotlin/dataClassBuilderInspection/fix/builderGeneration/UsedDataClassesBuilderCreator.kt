package org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration

import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import org.nobrains.kotlin.dataClassBuilderInspection.utils.*

object UsedDataClassesBuilderCreator {

    private val collectionsHandled = listOf("List", "Set", "Stack")

    fun createDslBuilders(usedDataClasses: List<DataClassInfo>) {
        for (infoUnit in usedDataClasses) {
            val kotlinFactory = KtPsiFactory(infoUnit.clazz.project)
            if (!infoUnit.hasBuilderClass)
                attachSingleBuilder(infoUnit.clazz, kotlinFactory)
            if (!infoUnit.hasBuildFunction)
                attachSingleBuildFunction(infoUnit.clazz, kotlinFactory)
        }
    }

    private fun attachSingleBuilder(clazz: KtClass, kotlinFactory: KtPsiFactory) {

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
                    val collectionShortName = parameterType?.extractCollectionNameOrNull()
                    val collectionParameterTypeName = parameterType?.extractCollectionArgumentNameOrNull()
                    val isCollection = collectionShortName in collectionsHandled

                    // Generate if this parameter is primitive
                    if (parameterType?.isPrimitiveNumberType() == true) {
                        if (parameterType.isMarkedNullable)
                            append("var ${parameterName}: $parameterType = null")
                        else
                            append("var $parameterName by Delegates.notNull<${parameterType}>()")
                    }
                    // Generate if this parameter is collection
                    else if (isCollection) {
                        append(
                            "var $parameterName = " +
                                    "mutable${collectionShortName}Of" +
                                    "<${collectionParameterTypeName}>()\n"
                        )

                        val potentialDataClass =
                            collectionParameterTypeName?.let { resolveIndexClassOrNull(it, parameter.project) }
                        if (potentialDataClass != null && potentialDataClass.isData())
                            append(generateBuildForSingleField(parameter))
                    }
                    // Generate if this parameter is something else
                    else {
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

        val collectionParameterTypeName = parameter.type()?.extractCollectionArgumentNameOrNull()

        return StringBuilder()
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

    private fun attachSingleBuildFunction(clazz: KtClass, kotlinFactory: KtPsiFactory) {

        val buildFunctionToAddString: String =
            StringBuilder()
                .append("fun build${clazz.nameAsSafeName}")
                .append("(init: ${clazz.nameAsSafeName}Builder.() -> Unit): ${clazz.nameAsSafeName} =\n")
                .append("${clazz.nameAsSafeName}Builder().apply(init).build()")
                .toString()

        val buildFunctionToAdd = kotlinFactory.createFunction(buildFunctionToAddString)
        clazz.containingFile.addAfter(buildFunctionToAdd, clazz)
    }

}