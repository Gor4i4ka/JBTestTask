package dslStyle.dataClassBuilderInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType


//TODO Make an adequate PSI modification. Not from a string.

object DataClassFix : LocalQuickFix {

    // The factory to generate kotlin code
    private lateinit var kotlinFactory: KtPsiFactory

    // Generated builder
    private lateinit var builderToAddPointer: SmartPsiElementPointer<KtClass>

    // Generated build function
    private lateinit var buildFunctionPointer: SmartPsiElementPointer<KtFunction>

    // Information about the data class
    private lateinit var dataClassPointer: SmartPsiElementPointer<KtClass>
    private lateinit var dataClassIdentifierName: String

    private lateinit var dataValueParamList: List<KtParameter>
    private lateinit var dataValueParamListName: ArrayList<String>
    private lateinit var dataValueParamListIsNullable: ArrayList<Boolean>

    override fun getFamilyName(): String {
        return "Generate a Java-style builder"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        // The factory to generate kotlin code
        kotlinFactory = KtPsiFactory(project)

        // Fill the fields by parsing information about the data class
        parseInformation(descriptor)

        // Getting the whole builder PSI sub-graph to attach to the file where the builder is
        generateBuilder()

        // Attaching building function
        generateBuildFunction()

        // Attaching the builder
        attachElements()
    }

    // Parsing necessary information
    private fun parseInformation(descriptor: ProblemDescriptor) {

        dataClassPointer = (descriptor.psiElement.parent as KtClass).createSmartPointer()
        dataClassIdentifierName = descriptor.psiElement.text

        dataValueParamList = (descriptor.psiElement.parent as KtClass).primaryConstructorParameters
        dataValueParamListName = ArrayList()
        dataValueParamListIsNullable = ArrayList()

        for (param in dataValueParamList) {
            val userType: String = param.type().toString()
            dataValueParamListIsNullable.add(userType.last() == '?')
            dataValueParamListName.add(userType)
        }

    }

    private fun generateBuilder() {
        val builderToAddString: String =
            StringBuilder()
                .append("class ")
                .append("${dataClassIdentifierName}Builder ")
                .append("{")
                .append(generateFields())
                .append(generateBuildMethod())
                .append("}")
                .toString()

        builderToAddPointer = kotlinFactory.createClass(builderToAddString).createSmartPointer()

    }

    //TODO: refactor to beautify
    private fun generateFields(): String {

        val fieldsToAddStringBuilder = StringBuilder()

            // Iterating through every field
            .apply {
                for (fieldIndex in dataValueParamList.indices) {

                    val paramType = dataValueParamList[fieldIndex].type()!!

                    val isOfPrimitiveType = dataValueParamList[fieldIndex].type()?.isPrimitiveNumberType()!!
                    if (!dataValueParamListIsNullable[fieldIndex] && !isOfPrimitiveType)
                        append("lateinit ")

                    append("var ")
                    append("${dataValueParamList[fieldIndex].nameAsSafeName}")

                    if (isOfPrimitiveType) {
                        append(" by Delegates.notNull<${dataValueParamListName[fieldIndex]}>()")
                    } else {
                        append(": ${dataValueParamListName[fieldIndex]} ")
                        if (dataValueParamListIsNullable[fieldIndex])
                            append("= null")
                    }

                    append("\n")
                }
            }
            .append("\n")

        return fieldsToAddStringBuilder.toString()
    }

    private fun generateBuildMethod(): String {

        val functionToAddStringBuilder = StringBuilder()
            .append("fun build(): $dataClassIdentifierName = \n")
            .append(dataClassIdentifierName)
            .append("(")

            //Insert every field
            .apply {
                var hasPrevious = false
                for (param in dataValueParamList) {
                    if (hasPrevious)
                        append(", ")

                    append(param.nameAsSafeName)
                    hasPrevious = true
                }
            }

            .append(")")
        return functionToAddStringBuilder.toString()
    }

    private fun generateBuildFunction() {
        val buildFunctionToAddString: String =
            StringBuilder()
                .append("fun build${dataClassIdentifierName}")
                .append("(init: ${dataClassIdentifierName}Builder.() -> Unit): $dataClassIdentifierName =\n")
                .append("${dataClassIdentifierName}Builder().apply(init).build()")
                .toString()

        buildFunctionPointer = kotlinFactory.createFunction(buildFunctionToAddString).createSmartPointer()
    }

    private fun attachElements() {
        val dataClass = dataClassPointer.element
        dataClass?.containingFile?.addAfter(builderToAddPointer.element!!, dataClass)
        dataClass?.containingFile?.addAfter(buildFunctionPointer.element!!, dataClass)
    }
}