package dataClassBuilderInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer


//TODO Make an adequate PSI modification. Not from a string.

object DataClassFix : LocalQuickFix {

    // The factory to generate kotlin code
    private lateinit var kotlinFactory: KtPsiFactory

    // Generated builder
    private lateinit var builderToAddPointer: SmartPsiElementPointer<KtClass>


    // Information about the data class
    private lateinit var dataClassPointer: SmartPsiElementPointer<KtClass>
    private lateinit var dataClassIdentifierName: String

    private lateinit var dataValueParamList: List<KtParameter>
    private lateinit var dataValueParamListString: ArrayList<String>
    private lateinit var dataValueParamListIsNullable: ArrayList<Boolean>

    override fun getFamilyName(): String {
        return "Build the builder!!"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        // The factory to generate kotlin code
        kotlinFactory = KtPsiFactory(project)

        // Fill the fields by parsing information about the data class
        parseInformation(descriptor)

        // Getting the whole builder PSI sub-graph to attach to the file where the builder is
        generateBuilder()

        // Attaching the builder
        attachTheBuilder()
    }

    // Parsing necessary information
    private fun parseInformation(descriptor: ProblemDescriptor) {

        dataClassPointer = (descriptor.psiElement.parent as KtClass).createSmartPointer()
        dataClassIdentifierName = descriptor.psiElement.text

        dataValueParamList = (descriptor.psiElement.parent as KtClass).primaryConstructorParameters
        dataValueParamListString = ArrayList()
        dataValueParamListIsNullable = ArrayList()

        for (param in dataValueParamList) {
            // TODO: remove kostil
            var userType: String = param.type().toString()
            userType = if (userType.last() == '?') {
                dataValueParamListIsNullable.add(true);
                userType
            } else {
                dataValueParamListIsNullable.add(false)
                "$userType?"
            }
            dataValueParamListString.add(userType)
        }
    }

    private fun generateBuilder() {
        val builderToAddString: String =
            StringBuilder()
                .append("class ")
                .append("${dataClassIdentifierName}Builder ")
                .append("{")
                .append(generateFields())
                .append(generateBuildFunction())
                .append("}")
                .toString()

        builderToAddPointer = kotlinFactory.createClass(builderToAddString).createSmartPointer()

    }

    private fun generateFields(): String {

        val fieldsToAddStringBuilder = StringBuilder()

        // Iterating through every field
        for (fieldIndex in dataValueParamList.indices)
            fieldsToAddStringBuilder.append(
                "var ${dataValueParamList[fieldIndex].nameAsSafeName}: " +
                        "${dataValueParamListString[fieldIndex]} = null\n"
            )

        return fieldsToAddStringBuilder.toString()
    }

    private fun generateBuildFunction(): String {

        val functionToAddStringBuilder = StringBuilder()
            .append("fun build(): $dataClassIdentifierName?")

            // Function block
            .append("{")
            .append("return ")
            .append("if ")

            // "If" Condition
            .append("(")
            .apply {
                var hasAtLeastOneCond = false
                for (fieldIndex in dataValueParamList.indices) {
                    if (!dataValueParamListIsNullable[fieldIndex]) {
                        if (hasAtLeastOneCond)
                            this.append(" || ")
                        this.append("${dataValueParamList[fieldIndex].nameAsSafeName} == null")
                        hasAtLeastOneCond = true
                    }
                }

                if (!hasAtLeastOneCond)
                    this.append("false")
            }
            .append(")")

            .append("null else ")
            .append(dataClassIdentifierName)

            // "primary constructor" call
            .append("(")
            .apply {
                var hasAtLeastOneCond = false
                for (fieldIndex in dataValueParamList.indices) {

                    if (hasAtLeastOneCond)
                        this.append(", ")

                    this.append("${dataValueParamList[fieldIndex].nameAsSafeName}")

                    if (!dataValueParamListIsNullable[fieldIndex])
                        this.append("!!")

                    hasAtLeastOneCond = true
                }
            }
            .append(")")

            .append("}")

        return functionToAddStringBuilder.toString()
    }

//    private fun generateBuilderOld(): KtClass {
//
//        // Adding necessary fields
//        for (param in dataValueParamList) {
//            builderBody?.addBefore(generateField(param), builderBody.lastChild)
//        }
//        //
//
//        // Adding "build" function
//        builderBody?.addBefore(generateBuildFunction(), builderBody.lastChild)
//        //
//
//        return builderToAdd
//    }
//
//    // Adding necessary fields: FUNCTION
//    private fun generateFieldOld(param: KtParameter): KtProperty {
//
//        // TODO (remove kostil)
//        var userType: String = param.type().toString()
//        userType = if (userType.last() == '?') {
//            userType
//        } else {
//            "$userType?"
//        }
//
//        return kotlinFactory.createProperty(param.nameAsSafeName.toString(), userType, true, "null")
//    }
//
//    private fun generateBuildFunctionOld(): KtNamedFunction {
//
//
//        val functionName = "fun build(): $DataClassIdentifierName? "
//        val functionBody =
//            "{" +
//                    " return if" +
//                    generateIfCond() +
//                    "\nnull " +
//                    "\nelse\n" +
//                    generatePrimaryConstructorCall() +
//                    "}"
//
//        return kotlinFactory.createFunction(functionName + functionBody)
//    }
//
//    private fun generateIfCondOld(): String {
//
//        var resultIfCondition = "("
//
//        var hasPrevious = false
//        for (index in dataValueParamList.indices) {
//            if (dataValueParamListIsNullable[index]) {
//                if (hasPrevious)
//                    resultIfCondition += " || "
//                resultIfCondition += "${dataValueParamList[index].nameAsSafeName} == null"
//                hasPrevious = true
//            }
//        }
//
//        // Sanity condition check
//        if (resultIfCondition.last() == '(')
//            resultIfCondition += "false"
//        resultIfCondition += ")"
//        return resultIfCondition
//    }
//
//    private fun generatePrimaryConstructorCallOld(): String {
//        var constructorCall = "$DataClassIdentifierName("
//
//        var hasPrevious = false
//        for (index in dataValueParamList.indices) {
//            if (hasPrevious)
//                constructorCall += ", "
//            constructorCall += "${dataValueParamList[index].nameAsSafeName}"
//            if (!dataValueParamListIsNullable[index])
//                constructorCall += "!!"
//            hasPrevious = true
//        }
//
//        constructorCall += ")"
//        return constructorCall
//    }

    private fun attachTheBuilder() {
        val dataClass = dataClassPointer.element
        dataClass?.containingFile?.addAfter(builderToAddPointer.element!!, dataClass)
    }
}