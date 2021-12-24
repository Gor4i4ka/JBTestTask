package builderUsagesInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder

class BuilderUsagesInspectorOld: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PotentialBuilderUsageVisitorOld {
        return PotentialBuilderUsageVisitorOld(holder, isOnTheFly)
    }
}