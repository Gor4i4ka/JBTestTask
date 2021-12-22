package dataClassBuilderInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*

class DataClassBuilderCreationFix: LocalQuickFix {

    lateinit var kotlinFactory: KtPsiFactory

    override fun getFamilyName(): String {
        return "Build the builder!!"
    }


    private fun generateBuilder(kotlinFactory: KtPsiFactory, dataClass: KtClass,
                                identifierName: String): KtClass {

        // Builder parts
        val builderToAdd = kotlinFactory.createClass("class ${identifierName}Builder {}")
        val builderBody = builderToAdd.body
        //

        // Handy globals
        val dataValueParamList = dataClass.primaryConstructorParameters
        val dataValueParamListString: ArrayList<String> = ArrayList()
        val dataValueParamListIsNullable: ArrayList<Boolean> = ArrayList()

        /////////////////////////////////////////////
        // Adding necessary fields: FUNCTION
        fun generateField(param: KtParameter): KtProperty {

            // TODO (remove kostil)
            var userType: String = param.type().toString()
            userType = if (userType.last() == '?') {userType} else {"$userType?"}

            return kotlinFactory.createProperty(param.nameAsSafeName.toString(), userType, true, "null")
        }
        //

        // Adding necessary "build" function: FUNCTION

        //TODO (REMOVE THIS HORRENDOUS KOSTIL!!!!!!)
        fun generateBuildFunction(): KtNamedFunction {

            fun generateIfCond():String {

                var resultIfCondition = "("

                var hasPrevious = false
                for (index in dataValueParamList.indices) {
                    if (dataValueParamListIsNullable[index]) {
                        if (hasPrevious)
                            resultIfCondition += " || "
                        resultIfCondition += "${dataValueParamList[index].nameAsSafeName} == null"
                        hasPrevious = true
                    }
                }

                // Sanity condition check
                if (resultIfCondition.last() == '(')
                    resultIfCondition += "false"
                resultIfCondition += ")"
                return resultIfCondition
            }


            fun generatePrimaryConstructorCall(): String {
                var constructorCall = "$identifierName("

                var hasPrevious = false
                for (index in dataValueParamList.indices) {
                    if (hasPrevious)
                        constructorCall += ", "
                    constructorCall += "${dataValueParamList[index].nameAsSafeName}"
                    if (!dataValueParamListIsNullable[index])
                        constructorCall += "!!"
                    hasPrevious = true
                }

                constructorCall += ")"
                return constructorCall
            }


            val functionName = "fun build(): $identifierName? "
            val functionBody =
                "{" +
                        " return if" +
                        generateIfCond() +
                        "\nnull " +
                        "\nelse\n" +
                        generatePrimaryConstructorCall() +
                "}"

            return kotlinFactory.createFunction(functionName + functionBody)
        }

        //
        /////////////////////////////////////////////

        // Parsing necessary information
        for (param in dataValueParamList) {
            // TODO: remove kostil
            var userType: String = param.type().toString()
            userType = if (userType.last() == '?') {dataValueParamListIsNullable.add(true);
                                                    userType}
                       else {dataValueParamListIsNullable.add(false)
                            "$userType?"}
            dataValueParamListString.add(userType)
        }
        //

        // Adding necessary fields
        for (param in dataValueParamList) {
            builderBody?.addBefore(generateField(param), builderBody.lastChild)
        }
        //

        // Adding "build" function
        builderBody?.addBefore(generateBuildFunction(), builderBody.lastChild)
        //

        return builderToAdd
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        val identifier = descriptor.psiElement
        val clazz = identifier.parent as KtClass
        val file = clazz.parent

        // The factory to generate kotlin code
        kotlinFactory = KtPsiFactory(project)

        val builderToAdd: KtClass = generateBuilder(kotlinFactory, clazz, identifier.text)

        file.add(builderToAdd)
    }
}