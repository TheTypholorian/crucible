package net.typho.crucible.intellij.task

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.project.Project
import net.typho.crucible.intellij.SYSTEM_ID

class CrucibleRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : ExternalSystemRunConfiguration(SYSTEM_ID, project, factory, name)