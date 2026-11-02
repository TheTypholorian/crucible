package net.typho.crucible.intellij

import com.intellij.util.xmlb.Converter
import net.typho.crucible.ide.data.CrucibleRootData
import net.typho.data_util.SerializedData
import net.typho.data_util.error.DataReadException
import net.typho.data_util.json.JsonFormat

class CrucibleIDEDataConverter : Converter<CrucibleRootData>() {
    override fun fromString(value: String): CrucibleRootData? = try {
        JsonFormat.read(CrucibleRootData.CODEC, SerializedData(value))
    } catch (e: DataReadException) {
        e.printStackTrace()
        null
    }

    override fun toString(value: CrucibleRootData) = JsonFormat.write(CrucibleRootData.CODEC, value).asString()
}