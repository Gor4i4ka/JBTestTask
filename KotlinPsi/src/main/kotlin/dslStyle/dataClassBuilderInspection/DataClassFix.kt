package dslStyle.dataClassBuilderInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class DataClassFix : LocalQuickFix {

    // The factory to generate kotlin code
    private lateinit var kotlinFactory: KtPsiFactory

    // Generated builder
    private lateinit var builderToAddPointer: SmartPsiElementPointer<KtClass>

    // Generated build function
    private lateinit var buildFunctionPointer: SmartPsiElementPointer<KtFunction>

    // Information about the data class
    private lateinit var dataClassPointer: SmartPsiElementPointer<KtClass>
    private lateinit var dataClassIdentifierName: String

    // Information about child data classes without builders
    private lateinit var unimplementedBuilderSet: HashSet<String>

    // Information about every parameter in a compact way
    data class ParameterInfoUnit(
        val parameter: KtParameter, val parameterName: String,
        val type: KotlinType, val typeName: String,
        val isNullable: Boolean, val isPrimitive: Boolean,
        val isCollection: Boolean, val collectionShortName: String?,
        val collectionParameterTypeName: String?, val isDataClass: Boolean,
        val collectionParameterIsDataClass: Boolean, val childDataClass: KtClass?,
    )

    private lateinit var parameterInfo: List<ParameterInfoUnit>

    // Information about collections handled by the inspection
    //TODO: replace with something adequate
    private val collectionsHandled = setOf<String>("List")

    override fun getFamilyName(): String {
        return "Generate a Java-style builder"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

        applyFixToPsiElement(project, descriptor.psiElement.parent as KtClass)

    }

    private fun applyFixToPsiElement(project: Project, clazz: KtClass) {

        // The factory to generate kotlin code
        kotlinFactory = KtPsiFactory(project)

        // Fill the fields by parsing information about the data class
        parseInformation(clazz, project)

        // Apply fix to used data classes
        applyFixToChildren()

        // Getting the whole builder PSI sub-graph to attach to the file where the builder is
        generateBuilder()

        // Attaching building function
        generateBuildFunction()

        // Attaching the builder
        attachElements()
    }

    // Parsing necessary information
    private fun parseInformation(clazz: KtClass, project: Project) {

        dataClassPointer = clazz.createSmartPointer()
        dataClassIdentifierName = clazz.nameAsSafeName.toString()

        parameterInfo = ArrayList()
        unimplementedBuilderSet = HashSet()

        val dataValueParamList = clazz.primaryConstructorParameters

        for (param in dataValueParamList) {
            val parameter: KtParameter = param
            val parameterName: String = param.nameAsSafeName.toString()
            val type: KotlinType = param.type()!!
            val typeName: String = type.toString()
            val isNullable: Boolean = typeName.endsWith("?")
            val isPrimitive: Boolean = type.isPrimitiveNumberType()
            //TODO: remove kostil with isCollection
            val collectionParameterTypeName = Regex("<.*>").find(typeName)?.value?.drop(1)?.dropLast(1)
            val collectionShortName = Regex(".*<").find(typeName)?.value?.dropLast(1)
            val isCollection: Boolean = collectionShortName in collectionsHandled

            var isDataClass: Boolean = false
            var collectionParameterIsDataClass: Boolean = false
            var childDataClass: KtClass? = null

            var fieldClass: KtClass? = null
            var collectionParameterClass: KtClass? = null

            if (!isPrimitive && !isCollection) {
                fieldClass = KotlinClassShortNameIndex
                    .getInstance()
                    .get(typeName, project, GlobalSearchScope.allScope(project))
                    .firstOrNull() as KtClass?
            }

            if (fieldClass != null && fieldClass.isData()) {
                isDataClass = true
                collectionParameterIsDataClass = false
                childDataClass = fieldClass
            }

            if (!isPrimitive && isCollection && collectionParameterTypeName != null)
                collectionParameterClass = KotlinClassShortNameIndex
                    .getInstance()
                    .get(collectionParameterTypeName, project, GlobalSearchScope.allScope(project))
                    .firstOrNull() as KtClass?

            if (collectionParameterClass != null && collectionParameterClass.isData()) {
                isDataClass = false
                collectionParameterIsDataClass = true
                childDataClass = collectionParameterClass
            }

            if (childDataClass != null)
                if (KotlinFunctionShortNameIndex
                        .getInstance()
                        .get("build${childDataClass.nameAsSafeName}", project, GlobalSearchScope.allScope(project))
                        .isEmpty()
                )
                    unimplementedBuilderSet.add(childDataClass.nameAsSafeName.toString())

            (parameterInfo as ArrayList<ParameterInfoUnit>).add(
                ParameterInfoUnit(
                    parameter = parameter, parameterName = parameterName, type = type,
                    typeName = typeName, isNullable = isNullable, isPrimitive = isPrimitive,
                    isCollection = isCollection, collectionParameterTypeName = collectionParameterTypeName,
                    isDataClass = isDataClass, collectionParameterIsDataClass = collectionParameterIsDataClass,
                    childDataClass = childDataClass, collectionShortName = collectionShortName
                )
            )

        }
    }

    private fun applyFixToChildren() {
        for (parameter in parameterInfo)
            if ((parameter.isDataClass || parameter.collectionParameterIsDataClass) &&
                parameter.typeName in unimplementedBuilderSet) {
                    DataClassFix().applyFixToPsiElement(parameter.childDataClass!!.project, parameter.childDataClass)
                    unimplementedBuilderSet.remove(parameter.typeName)
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

        val fieldBuildFunctionsToAttach = StringBuilder()
        val fieldsToAddStringBuilder = StringBuilder()

            // Iterating through every field
            .apply {
                for (parameter in parameterInfo) {

                    // Primitive parameter field generation
                    if (parameter.isPrimitive) {

                        if (parameter.isNullable)
                            append("var ${parameter.parameterName}: ${parameter.typeName} = null\n")
                        else
                            append("var ${parameter.parameterName} by Delegates.notNull<${parameter.typeName}>()\n")
                    }

                    // Collection parameter field generation
                    else if (parameter.isCollection) {
                        append(
                            "private val ${parameter.parameterName} = " +
                                    "mutable${parameter.collectionShortName}Of" +
                                    "<${parameter.collectionParameterTypeName}>()\n"
                        )

                        if (parameter.collectionParameterIsDataClass)
                            fieldBuildFunctionsToAttach.append(generateBuildForField(parameter))
                    }

                    // Every other field generation
                    else {

                        if (parameter.isNullable)
                            append("var ${parameter.parameterName}: ${parameter.typeName} = null\n")
                        else
                            append("lateinit var ${parameter.parameterName}: ${parameter.typeName}\n")
                    }
                }
            }
            .append("\n")

        return fieldsToAddStringBuilder.append(fieldBuildFunctionsToAttach).toString()
    }

    private fun generateBuildForField(parameter: ParameterInfoUnit): String {

        val functionToAddStringBuilder = StringBuilder()
            .append(
                "fun ${parameter.parameterName}Element" +
                        "(init: ${parameter.collectionParameterTypeName}Builder.() -> Unit) {\n" +
                        "${parameter.parameterName}.add(build${parameter.collectionParameterTypeName}(init))}\n"
            )

        return functionToAddStringBuilder.toString()
    }

    private fun generateBuildMethod(): String {

        val functionToAddStringBuilder = StringBuilder()
            .append("fun build(): $dataClassIdentifierName = \n")
            .append(dataClassIdentifierName)
            .append("(")

            //Insert every field
            .apply {
                var hasPrevious = false
                for (parameter in parameterInfo) {
                    if (hasPrevious)
                        append(", ")

                    append(parameter.parameterName)
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