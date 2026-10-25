package net.typho.crucible.intellij

import com.intellij.util.xmlb.Converter
import net.typho.crucible.ide.data.CrucibleIDEData
import net.typho.data_util.DataReadException
import net.typho.data_util.impl.JsonFormat

class CrucibleIDEDataConverter : Converter<CrucibleIDEData>() {
    override fun fromString(value: String): CrucibleIDEData? = try {
        JsonFormat().read(CrucibleIDEData.CODEC, value)
    } catch (e: DataReadException) {
        e.printStackTrace()
        null
    }

    override fun toString(value: CrucibleIDEData) = JsonFormat().write(CrucibleIDEData.CODEC, value)
}