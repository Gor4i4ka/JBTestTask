package legacy

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.nj2k.postProcessing.type
//import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.blockExpressionsOrSingle

class ProxyFixOld: LocalQuickFix {

    lateinit var kotlinFactory: KtPsiFactory

    override fun getFamilyName(): String {
        //println("FAMILY NAME IS SOSAMBA")
        return "SUS"
    }

    //fun func() {}

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
        fun generateBuildFunction(): KtNamedFunction {
            val resultFunction = kotlinFactory.createFunction("fun build() {} ")

            // Setting type
            resultFunction.typeReference = kotlinFactory.createType("$identifierName?")

            // Generating body statement
            //val returnStatement = kotlinFactory.createExpression("return")
            val returnStatement = kotlinFactory.createExpression("return")

            resultFunction.bodyBlockExpression?.psi?.add(returnStatement)

            println("FUNCTION")

            for (child in resultFunction.children)
                println(child)

            println("BLOCK BEGIN")

            for (child in resultFunction.bodyBlockExpression?.children!!)
                println(child)

            println("BLOCK END")

            return resultFunction
        }
        //
        /////////////////////////////////////////////

        // Parsing necessary information
        for (param in dataValueParamList) {
            // TODO: remove kostil
            var userType: String = param.type().toString()
            userType = if (userType.last() == '?') {dataValueParamListIsNullable.add(true);
                                                    userType}
                       else {dataValueParamListIsNullable.add(true)
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

        println("WE'RE HERE")

        val identifier = descriptor.psiElement
        val clazz = identifier.parent as KtClass
        val file = clazz.parent

        if (file !is KtFile) {
            println("IMPOSTOR CHECK")
            return
        }

        // The factory to generate kotlin code
        kotlinFactory = KtPsiFactory(project)

        //val funcToAdd: KtNamedFunction = kotlinFactory.createFunction("fun ${identifier.text}() {}")
        val builderToAdd: KtClass = generateBuilder(kotlinFactory, clazz, identifier.text)

        println("HERE WE GO")
        file.add(builderToAdd)
    }
}