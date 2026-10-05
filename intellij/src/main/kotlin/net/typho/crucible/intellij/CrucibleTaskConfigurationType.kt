package net.typho.crucible.intellij

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType
import com.intellij.openapi.project.Project

class CrucibleTaskConfigurationType : AbstractExternalSystemTaskConfigurationType(SYSTEM_ID) {
    override fun getConfigurationFactoryId() = "Crucible"

    override fun doCreateConfiguration(
        externalSystemId: ProjectSystemId,
        project: Project,
        factory: ConfigurationFactory,
        name: String
    ) = CrucibleRunConfiguration(project, factory, name)
}