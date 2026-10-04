package net.typho.crucible.intellij

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.util.Function
import com.intellij.util.messages.Topic
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionProvider

@JvmField
val SYSTEM_ID = ProjectSystemId("CRUCIBLE")
@JvmField
val SETTINGS_TOPIC = Topic.create(
    "Crucible external settings",
    CrucibleSystemManager.SettingsListener::class.java
)

class CrucibleSystemManager : ExternalSystemManager<
        CrucibleSystemManager.ProjectSettings,
        CrucibleSystemManager.SettingsListener,
        CrucibleSystemManager.SystemSettings,
        CrucibleSystemManager.LocalSettings,
        CrucibleSystemManager.ExecutionSettings
        > {
    override fun getSystemId() = SYSTEM_ID

    override fun getSettingsProvider(): Function<Project, SystemSettings> {
        return { SystemSettings(SETTINGS_TOPIC, it) }
    }

    override fun getLocalSettingsProvider(): Function<Project, LocalSettings> {
        return { LocalSettings(SYSTEM_ID, it) }
    }

    override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, ExecutionSettings> {
        return { ExecutionSettings() }
    }

    override fun getProjectResolverClass() = CrucibleProjectResolver::class.java

    override fun getExternalProjectDescriptor(): FileChooserDescriptor {
        TODO("Not yet implemented")
    }

    override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
        // TODO
    }

    class ProjectSettings : ExternalProjectSettings() {
        override fun clone() = ProjectSettings()
    }

    class SettingsListener : ExternalSystemSettingsListener<ProjectSettings>

    class SystemSettings(
        topic: Topic<SettingsListener>,
        project: Project
    ) : AbstractExternalSystemSettings<SystemSettings, ProjectSettings, SettingsListener>(topic, project) {
        override fun copyExtraSettingsFrom(settings: SystemSettings) {
        }

        override fun checkSettings(
            old: ProjectSettings,
            current: ProjectSettings
        ) {
        }

        override fun subscribe(
            listener: ExternalSystemSettingsListener<ProjectSettings>,
            parentDisposable: Disposable
        ) {
        }
    }

    class LocalSettings : AbstractExternalSystemLocalSettings<LocalSettings.State> {
        class State : AbstractExternalSystemLocalSettings.State()

        constructor(externalSystemId: ProjectSystemId, project: Project, state: State) : super(externalSystemId, project, state)

        constructor(externalSystemId: ProjectSystemId, project: Project) : super(externalSystemId, project)
    }

    class ExecutionSettings : ExternalSystemExecutionSettings()
}