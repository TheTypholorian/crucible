package net.typho.crucible.intellij

import com.intellij.util.xmlb.Converter
import net.typho.crucible.ide.data.IDEProject
import net.typho.data_util.DataReadException
import net.typho.data_util.impl.JsonFormat

class CrucibleIDEDataConverter : Converter<IDEProject>() {
    override fun fromString(value: String): IDEProject? = try {
        JsonFormat().read(IDEProject.CODEC, value)
    } catch (e: DataReadException) {
        e.printStackTrace()
        null
    }

    override fun toString(value: IDEProject) = JsonFormat().write(IDEProject.CODEC, value)
}