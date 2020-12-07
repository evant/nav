package me.tatarka.nav.route

import android.net.Uri
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Handles matching the given parameterized path.
 */
class RouteMatcher(path: String) {

    private val pattern: Pattern
    private val isParameterizedQuery: Boolean
    private val exactDeepLink: Boolean
    private val arguments = mutableListOf<String>()
    private val paramArgMap = mutableMapOf<String, ParamQuery>()

    init {
        val parameterizedUri = Uri.parse(path)
        isParameterizedQuery = parameterizedUri.query != null
        val uriRegex = StringBuilder("^")
        val fillInPattern = Pattern.compile("\\{(.+?)\\}")
        if (isParameterizedQuery) {
            var matcher = Pattern.compile("(\\?)").matcher(path)
            if (matcher.find()) {
                buildPathRegex(path.substring(0, matcher.start()), uriRegex, fillInPattern)
            }
            exactDeepLink = false
            for (paramName in parameterizedUri.queryParameterNames) {
                val argRegex = StringBuilder()
                val queryParam = parameterizedUri.getQueryParameter(paramName)!!
                matcher = fillInPattern.matcher(queryParam)
                var appendPos = 0
                val paramArgNames = mutableListOf<String>()
                // Build the regex for each query param
                while (matcher.find()) {
                    paramArgNames.add(matcher.group(1)!!)
                    argRegex.append(
                        Pattern.quote(
                            queryParam.substring(
                                appendPos,
                                matcher.start()
                            )
                        )
                    )
                    argRegex.append("(.+?)?")
                    appendPos = matcher.end()
                }
                if (appendPos < queryParam.length) {
                    argRegex.append(Pattern.quote(queryParam.substring(appendPos)))
                }
                // Save the regex with wildcards unquoted, and add the param to the map with its
                // name as the key
                val paramRegex = argRegex.toString().replace(".*", "\\E.*\\Q")
                paramArgMap[paramName] = ParamQuery(paramRegex, paramArgNames)
            }
        } else {
            exactDeepLink = buildPathRegex(path, uriRegex, fillInPattern)
        }
        // Since we've used Pattern.quote() above, we need to
        // specifically escape any .* instances to ensure
        // they are still treated as wildcards in our final regex
        val finalRegex = uriRegex.toString().replace(".*", "\\E.*\\Q")
        pattern = Pattern.compile(finalRegex)
    }

    private fun buildPathRegex(
        uri: String, uriRegex: StringBuilder,
        fillInPattern: Pattern
    ): Boolean {
        val matcher: Matcher = fillInPattern.matcher(uri)
        var appendPos = 0
        // Track whether this is an exact deep link
        var exactDeepLink = !uri.contains(".*")
        while (matcher.find()) {
            val argName: String = matcher.group(1)!!
            arguments.add(argName)
            // Use Pattern.quote() to treat the input string as a literal
            uriRegex.append(Pattern.quote(uri.substring(appendPos, matcher.start())))
            uriRegex.append("(.+?)")
            appendPos = matcher.end()
            exactDeepLink = false
        }
        if (appendPos < uri.length) {
            // Use Pattern.quote() to treat the input string as a literal
            uriRegex.append(Pattern.quote(uri.substring(appendPos)))
        }
        // Match either the end of string if all params are optional or match the
        // question mark and 0 or more characters after it
        // We do not use '.*' here because the finalregex would replace it with a quoted
        // version below.
        uriRegex.append("($|(\\?(.)*))")
        return exactDeepLink
    }

    /**
     * Matches the given uri.
     * @return null if it does not match, otherwise returns a mapping of extracted parameters.
     */
    fun match(deepLink: Uri): Map<String, String>? {
        val path = deepLink.path.orEmpty()
        val matcher: Matcher = pattern.matcher(path)
        if (!matcher.matches()) {
            return null
        }
        val result = mutableMapOf<String, String>()
        for (index in arguments.indices) {
            val argumentName: String = arguments[index]
            val value = Uri.decode(matcher.group(index + 1))
            result[argumentName] = value
        }
        if (isParameterizedQuery) {
            for (paramName in paramArgMap.keys) {
                var argMatcher: Matcher? = null
                val storedParam: ParamQuery = paramArgMap.getValue(paramName)
                val inputParams = deepLink.getQueryParameter(paramName)
                if (inputParams != null) {
                    // Match the input arguments with the saved regex
                    argMatcher = Pattern.compile(storedParam.paramRegex).matcher(inputParams)
                    if (!argMatcher.matches()) {
                        return null
                    }
                }
                // Params could have multiple arguments, we need to handle them all
                for (index in storedParam.arguments.indices) {
                    var value: String? = null
                    if (argMatcher != null) {
                        value = Uri.decode(argMatcher.group(index + 1))
                    }
                    val argName: String = storedParam.arguments[index]
                    if (value != null && value.replace("[{}]".toRegex(), "") != argName) {
                        result[argName] = value
                    }
                }
            }
        }
        return result
    }

    /**
     * Used to maintain query parameters and the mArguments they match with.
     */
    private class ParamQuery(val paramRegex: String, val arguments: List<String>)
}