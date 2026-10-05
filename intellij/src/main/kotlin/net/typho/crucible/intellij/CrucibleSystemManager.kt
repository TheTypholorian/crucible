package net.typho.crucible.intellij

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.ExternalSystemUiAware
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
import icons.ExternalSystemIcons
import net.typho.crucible.intellij.task.CrucibleSystemTaskManager
import java.nio.file.Paths
import kotlin.io.path.name

@JvmField
val SYSTEM_ID = ProjectSystemId("CRUCIBLE", "Crucible")
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
        >, ExternalSystemUiAware {
    override fun getSystemId() = SYSTEM_ID

    override fun getSettingsProvider(): Function<Project, SystemSettings> {
        return { it.getService(SystemSettings::class.java) }
    }

    override fun getLocalSettingsProvider(): Function<Project, LocalSettings> {
        return { it.getService(LocalSettings::class.java) }
    }

    override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, ExecutionSettings> {
        return { ExecutionSettings() }
    }

    override fun getProjectResolverClass() = CrucibleProjectResolver::class.java

    override fun getTaskManagerClass() = CrucibleSystemTaskManager::class.java

    override fun getExternalProjectDescriptor() = FileChooserDescriptor(
        true,
        true,
        false,
        false,
        false,
        false
    )

    override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
    }

    override fun getProjectRepresentationName(
        targetProjectPath: String,
        rootProjectPath: String?
    ) = Paths.get(targetProjectPath).parent.name

    override fun getExternalProjectConfigDescriptor() = null

    override fun getProjectIcon() = CrucibleIcons.ICON // TODO

    override fun getTaskIcon() = ExternalSystemIcons.Task

    class ProjectSettings : ExternalProjectSettings() {
        override fun clone() = ProjectSettings().also {
            copyTo(it)
        }
    }

    interface SettingsListener : ExternalSystemSettingsListener<ProjectSettings>

    @Service(Service.Level.PROJECT)
    @State(name = "SystemSettings", storages = [Storage("crucible.xml")])
    class SystemSettings(project: Project) : AbstractExternalSystemSettings<SystemSettings, ProjectSettings, SettingsListener>(SETTINGS_TOPIC, project), PersistentStateComponent<SystemSettings.State> {
        class State : AbstractExternalSystemSettings.State<ProjectSettings> {
            @JvmField
            val linkedExternalProjectsSettings = mutableSetOf<ProjectSettings>()

            override fun getLinkedExternalProjectsSettings() = linkedExternalProjectsSettings

            override fun setLinkedExternalProjectsSettings(settings: Set<ProjectSettings>) {
                linkedExternalProjectsSettings.addAll(settings)
            }
        }

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
            doSubscribe(DelegatingCrucibleSettingsListenerAdapter(listener), parentDisposable)
        }

        override fun getState(): State {
            return State().also {
                fillState(it)
            }
        }

        override fun loadState(state: State) {
            super.loadState(state)
        }
    }

    @Service(Service.Level.PROJECT)
    @State(name = "LocalSettings", storages = [Storage(StoragePathMacros.CACHE_FILE)])
    class LocalSettings : AbstractExternalSystemLocalSettings<LocalSettings.State>, PersistentStateComponent<LocalSettings.State> {
        class State : AbstractExternalSystemLocalSettings.State()

        constructor(project: Project) : super(SYSTEM_ID, project, State())

        override fun loadState(state: State) {
            super.loadState(state)
        }
    }

    class ExecutionSettings : ExternalSystemExecutionSettings()
}