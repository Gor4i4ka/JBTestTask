package builderUsagesInspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtPsiFactory

//TODO: Fill correctly all the stuff
class BuilderWrapperFix: LocalQuickFix {

    lateinit var kotlinFactory: KtPsiFactory

    override fun getFamilyName(): String {
        return "Dunno what to write here yet"
    }

    //TODO: This proxyFix needs to be written
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        println("Applied!")
    }
}