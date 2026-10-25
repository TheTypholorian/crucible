package net.typho.crucible.intellij

import com.intellij.openapi.externalSystem.ui.ExternalSystemIconProvider
import javax.swing.Icon

class CrucibleIconProvider : ExternalSystemIconProvider {
    override val projectIcon: Icon
        get() = CrucibleIcons.ICON
    override val reloadIcon: Icon
        get() = CrucibleIcons.ICON
}