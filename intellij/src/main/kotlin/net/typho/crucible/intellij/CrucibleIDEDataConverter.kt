package net.typho.crucible.intellij

import com.intellij.util.xmlb.Converter
import net.typho.crucible.ide_data.IDERootData
import net.typho.data_util.SerializedData
import net.typho.data_util.error.DataReadException
import net.typho.data_util.json.JsonFormat

class CrucibleIDEDataConverter : Converter<IDERootData>() {
    override fun fromString(value: String): IDERootData? = try {
        JsonFormat.read(IDERootData.CODEC, SerializedData(value))
    } catch (e: DataReadException) {
        e.printStackTrace()
        null
    }

    override fun toString(value: IDERootData) = JsonFormat.write(IDERootData.CODEC, value).asString()
}