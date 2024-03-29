/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package su.kore.dto.tool

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.javapoet.*
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import javax.lang.model.element.Modifier

fun main(args: Array<String>) {
    val configPath = if (args.size == 1) {
        args[0];
    } else {
        "config.json"
    }

    val config = jacksonObjectMapper().readValue<Config>(File(configPath))
    Config.set(config)

    Files.createDirectories(Paths.get(config.targetDirectory))

    val cl = URLClassLoader(config.jarLocations.map { URL("jar:${File(it).toURI().toURL()}!/") }.toTypedArray())
    RefUtil.init(cl, config.entityPackages)
    config.classNames.map { Class.forName(it, false, cl) }.forEach { processType(it) }
}

fun processType(type: Type) {
    if (type is Class<*> && !type.isEnum && Config.get().entityPackages.any { type.`package`?.name?.startsWith(it) == true }) {
        val classData = ClassData.fromClass(type)
        writeSpec(classData, type.dtoTypeName() as ClassName)
        classData.properties.forEach { processType(it.type) }
        if (type.isInterface) {
            RefUtil.get().getSubTypesOf(type).forEach { processType(it) }
        }
    } else if (type is ParameterizedType) {
        type.actualTypeArguments.forEach { processType(it) }
    }
}

fun writeSpec(classData: ClassData, className: ClassName) {
    val classBuilder = if (classData.clazz.isInterface) {
        TypeSpec.interfaceBuilder(className.simpleName()).apply {
            addModifiers(Modifier.PUBLIC)
            classData.clazz.interfaces.forEach {
                addSuperinterface(it.dtoTypeName())
            }
            addMethod(fromInterfaceMethod(classData))
            addFields(classData.properties.map { field(it) }.toList())
            addMethods(
                    classData.properties.flatMap {
                        listOf(
                                getter(it, false)
                        )
                    }.toList()
            )
        }
    } else {
        TypeSpec.classBuilder(className.simpleName()).apply {
            addModifiers(Modifier.PUBLIC)
            classData.clazz.interfaces.forEach {
                addSuperinterface(it.dtoTypeName())
            }
            if (classData.clazz.superclass != Any().javaClass) {
                superclass(classData.clazz.superclass.dtoTypeName())
            }
            addMethod(fromMethod(classData))
            addFields(classData.properties.map { field(it) }.toList())
            addMethods(
                    classData.properties.flatMap {
                        listOf(
                                getter(it, true),
                                setter(it)
                        )
                    }.toList()
            )
        }
    }

    val javaFile = JavaFile.builder(className.packageName(), classBuilder.build()).build()
    javaFile.writeTo(Paths.get(Config.get().targetDirectory))
}

fun fromInterfaceMethod(classData: ClassData): MethodSpec {
    return MethodSpec.methodBuilder("from").apply {
        addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        addParameter(classData.clazz, "source")
        returns(classData.clazz.dtoTypeName())
        beginControlFlow("if (source == null)")
        addStatement("return null")
        endControlFlow()
        RefUtil.get().getSubTypesOf(classData.clazz).forEach {
            beginControlFlow("if (source instanceof \$T)", ClassName.get(it))
            addStatement("return \$T.from((\$T)source)", it.dtoTypeName(), ClassName.get(it))
            endControlFlow()
        }
        addStatement("throw new \$T()", ClassName.get(UnsupportedOperationException::class.java))
    }.build()
}

fun fromMethod(classData: ClassData): MethodSpec {
    return MethodSpec.methodBuilder("from").apply {
        addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        addParameter(classData.clazz, "source")
        returns(classData.clazz.dtoTypeName())
        beginControlFlow("if (source == null)")
        addStatement("return null")
        endControlFlow()
        addStatement("\$T dto = new \$T()", classData.clazz.dtoTypeName(), classData.clazz.dtoTypeName())
        classData.properties.forEach {
            if (it.type.dtoTypeName() == TypeName.get(it.type)) {
                addStatement("dto.set${it.name.capitalize()}(source.get${it.name.capitalize()}())")
            } else {
                addStatement("dto.set${it.name.capitalize()}(\$T.from(source.get${it.name.capitalize()}()))", it.type.dtoTypeName())
            }
        }
        addStatement("return dto")
    }.build()
}

fun getter(propertyData: PropertyData, implement: Boolean): MethodSpec {
    return MethodSpec.methodBuilder("get${propertyData.name.capitalize()}").apply {
        addModifiers(Modifier.PUBLIC)
        returns(propertyData.type.dtoTypeName())
        if (implement) {
            addStatement("return ${propertyData.name}")
        }
    }.build()
}

fun setter(propertyData: PropertyData): MethodSpec {
    return MethodSpec.methodBuilder("set${propertyData.name.capitalize()}")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(propertyData.type.dtoTypeName(), propertyData.name)
            .addStatement("this.${propertyData.name} = ${propertyData.name}")
            .build()
}

fun field(propertyData: PropertyData): FieldSpec {
    return FieldSpec.builder(propertyData.type.dtoTypeName(), propertyData.name, Modifier.PRIVATE).build()
}

fun Type.dtoTypeName(): TypeName {
    if (this is Class<*> && !this.isEnum) {
        val oldValue = Config.get().entityPackages.firstOrNull() { this.`package`?.name?.startsWith(it) == true }
        if (oldValue != null) {
            return ClassName.get(this.`package`.name.replace(oldValue, Config.get().targetPackage), "${this.simpleName}Dto");
        }
    }
    return TypeName.get(this)
}
