package net.typho.crucible.intellij

import com.intellij.ui.IconManager

object CrucibleIcons {
    @JvmField
    val ICON = IconManager.getInstance().getIcon("/icon.png", javaClass.classLoader)
}