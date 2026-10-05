package net.typho.crucible.intellij.task

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory
import com.intellij.openapi.project.Project
import net.typho.crucible.intellij.CrucibleSystemManager
import net.typho.crucible.intellij.SYSTEM_ID

class CrucibleToolWindowFactory : AbstractExternalSystemToolWindowFactory(SYSTEM_ID) {
    override fun getSettings(project: Project) = project.getService(CrucibleSystemManager.SystemSettings::class.java)
}