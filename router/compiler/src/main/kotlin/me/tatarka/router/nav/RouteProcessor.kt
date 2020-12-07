package me.tatarka.router.nav

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

private val ROUTE = "me.tatarka.nav.router.Route"
private val ROUTE_MATCHER = ClassName("me.tatarka.nav.router", "RouterMatcher")
private val ROUTE_MATCHER_LIST = LIST.parameterizedBy(ROUTE_MATCHER)
private val KCLASS = ClassName("kotlin.reflect", "KClass")
private val URI = ClassName("android.net", "Uri")

class RouteProcessor : SymbolProcessor {

    private lateinit var codeGenerator: CodeGenerator
    private lateinit var logger: KSPLogger

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
    }

    override fun process(resolver: Resolver) {
        val routesMap = mutableMapOf<KSDeclaration, MutableList<Route>>()

        for (routeClass in resolver.getSymbolsWithAnnotation(ROUTE)) {
            require(routeClass is KSClassDeclaration)
            val parent = routeClass.findSealedParent()
            if (parent == null) {
                logger.error("Route must be a sealed class variant", routeClass)
                continue
            }
            routesMap.getOrPut(parent) { mutableListOf() }
                .add(
                    Route(
                        routeClass,
                        routeClass.findAnnotations(ROUTE)
                            .map { it.arguments.first().value as String })
                )
        }

        for ((parent, routeClasses) in routesMap) {
            val routes = generateRoutes(parent, routeClasses)
            val parseRoute = generateParseRoute(resolver, parent, routes, routeClasses)


            val file = FileSpec.builder(
                parent.packageName.asString(),
                "${parent.simpleName.asString()}Routes"
            )
                .addProperty(routes)
                .addFunction(parseRoute)
                .build()
            file.writeTo(codeGenerator)
        }
    }

    private fun generateRoutes(
        parent: KSDeclaration,
        routeClasses: List<Route>
    ): PropertySpec {
        return PropertySpec.builder("${parent.simpleName.asString()}Routes", ROUTE_MATCHER_LIST)
            .addModifiers(KModifier.PRIVATE)
            .initializer(CodeBlock.builder()
                .apply {
                    addStatement("listOf(")
                    val paths = routeClasses.flatMap { it.paths }
                    for (path in paths) {
                        addStatement("%T(%S),", ROUTE_MATCHER, path)
                    }
                    addStatement(")")
                }
                .build())
            .build()
    }

    private fun generateParseRoute(
        resolver: Resolver,
        parent: KSDeclaration,
        routes: PropertySpec,
        routeClasses: List<Route>
    ): FunSpec {
        val parentClassName = parent.toClassName()
        return FunSpec.builder("parseRoute")
            .receiver(KCLASS.parameterizedBy(parentClassName))
            .addParameter("deepLink", URI)
            .returns(parentClassName.copy(nullable = true))
            .addCode(CodeBlock.Builder().apply {
                addStatement(
                    "var results: %T",
                    MAP.parameterizedBy(STRING, STRING).copy(nullable = true)
                )
                var i = 0
                for (route in routeClasses) {
                    for (path in route.paths) {
                        addStatement("results = %N[%L].match(deepLink)", routes, i)
                        beginControlFlow("if (results != null)")
                        if (route.declaration.classKind == ClassKind.OBJECT) {
                            addStatement("return %T", route.declaration.toClassName())
                        } else {
                            addStatement("return %T(", route.declaration.toClassName())
                            for (param in route.declaration.primaryConstructor!!.parameters) {
                                val name = param.name!!.asString()
                                val paramType = param.type!!.resolve()
                                addStatement(
                                    "%L = results${parseValue(resolver, paramType)}",
                                    name,
                                    name
                                )
                            }
                            addStatement(")")
                        }
                        endControlFlow()
                        i += 1
                    }
                }

                add("return null")
            }.build())
            .build()
    }

    private fun parseValue(resolver: Resolver, paramType: KSType): String {
        val nullable = paramType.isMarkedNullable
        val b = resolver.builtIns
        val parse = when (paramType) {
            b.shortType -> ".toShort()"
            b.intType -> ".toInt()"
            b.longType -> ".toLong()"
            b.floatType -> ".toFloat()"
            b.doubleType -> ".toDouble()"
            b.booleanType -> ".toBoolean()"
            b.charType -> ".toChar()"
            else -> ""
        }
        return if (nullable) {
            "[%S]" + if (parse.isNotEmpty()) "?$parse" else ""
        } else {
            ".getValue(%S)$parse"
        }
    }

    override fun finish() {
    }
}


private class Route(
    val declaration: KSClassDeclaration,
    val paths: List<String>
)

private fun KSClassDeclaration.findSealedParent(): KSDeclaration? {
    for (superType in superTypes) {
        val declaration = superType.resolve().declaration
        if (Modifier.SEALED in declaration.modifiers) {
            return declaration
        }
    }
    return null
}

private fun KSDeclaration.toClassName(): ClassName {
    val name = qualifiedName!!
    val packageName = packageName.asString()
    val shortName = name.asString().removePrefix("$packageName.")
    return ClassName(if (packageName == "<root>") "" else packageName, shortName.split('.'))
}

private fun KSAnnotated.findAnnotations(name: String): List<KSAnnotation> = annotations.filter {
    it.annotationType.resolve().declaration.qualifiedName!!.asString() == name
}

private fun FileSpec.writeTo(codeGenerator: CodeGenerator) {
    codeGenerator.createNewFile(packageName, name).bufferedWriter().use {
        writeTo(it)
    }
}
