package su.kore.dto.tool

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Type

data class ClassData(
        val clazz: Class<*>,
        val properties: List<PropertyData>
) {
    companion object {
        fun fromClass(clazz: Class<*>): ClassData {
            val properties = clazz.declaredMethods.filter { it.name.startsWith("get") && it.parameterTypes.isEmpty() && Modifier.isPublic(it.modifiers)}.map { PropertyData.fromGetter(it) }.toList()
            return ClassData(clazz, properties);
        }
    }

    fun getPackageName(): String {
        return clazz.`package`.name
    }

    fun getClassName(): String {
        return clazz.simpleName
    }
}

data class PropertyData(
        val name: String,
        val type: Type
) {
    companion object {
        fun fromGetter(method: Method): PropertyData {
            return PropertyData(method.name.substring(3).decapitalize(), method.genericReturnType)
        }
    }
}