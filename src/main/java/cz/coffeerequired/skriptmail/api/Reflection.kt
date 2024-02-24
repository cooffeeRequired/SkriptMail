package cz.coffeerequired.skriptmail.api

import java.lang.reflect.Field


fun getField(entry: Any, field: String): Any? {
    val fields: Array<Field> = entry::class.java.declaredFields
    for (f in fields) {
        f.isAccessible = true
        if (f.name.equals(field)) {
            val objInstance = f.get(entry)
            return objInstance
        }
    }
    return null
}

fun isCalledFromExactMethod(methodName: String): Boolean {
    val stackStrace = Thread.currentThread().stackTrace
    return stackStrace.any { it.methodName == methodName }
}

annotation class WillUsed