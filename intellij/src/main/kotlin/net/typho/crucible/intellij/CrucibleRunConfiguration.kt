package net.typho.crucible.intellij

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.project.Project

class CrucibleRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : ExternalSystemRunConfiguration(SYSTEM_ID, project, factory, name)