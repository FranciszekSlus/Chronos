package com.tasker.chronos.utils

import com.google.gson.*
import com.tasker.chronos.data.models.TaskReminder
import com.tasker.chronos.data.models.TaskReminderUnit
import java.lang.reflect.Type

class TaskReminderAdapter : JsonSerializer<TaskReminder>, JsonDeserializer<TaskReminder> {

    override fun serialize(
        src: TaskReminder?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null) return JsonNull.INSTANCE

        return JsonObject().apply {
            addProperty("value", src.value)
            addProperty("unit", src.unit.name)
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TaskReminder {
        if (json == null || json.isJsonNull) {
            return TaskReminder(30, TaskReminderUnit.MINUTES)
        }

        val jsonObject = json.asJsonObject
        val value = jsonObject.get("value")?.asInt ?: 30
        val unitName = jsonObject.get("unit")?.asString ?: "MINUTES"
        val unit = try {
            TaskReminderUnit.valueOf(unitName)
        } catch (e: Exception) {
            TaskReminderUnit.MINUTES
        }

        return TaskReminder(value, unit)
    }
}