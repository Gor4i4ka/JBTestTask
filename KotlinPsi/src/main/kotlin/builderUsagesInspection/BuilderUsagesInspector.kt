package builderUsagesInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder

class BuilderUsagesInspector: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PotentialBuilderUsageVisitor {
        return PotentialBuilderUsageVisitor(holder, isOnTheFly)
    }
}