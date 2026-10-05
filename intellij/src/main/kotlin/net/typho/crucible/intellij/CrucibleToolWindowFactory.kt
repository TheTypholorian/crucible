package net.typho.crucible.intellij

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory
import com.intellij.openapi.project.Project

class CrucibleToolWindowFactory : AbstractExternalSystemToolWindowFactory(SYSTEM_ID) {
    override fun getSettings(project: Project) = project.getService(CrucibleSystemManager.SystemSettings::class.java)
}