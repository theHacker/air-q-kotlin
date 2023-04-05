package biz.thehacker.airq

/**
 * Formats the config map from air-Q nicely for output.
 *
 * Currently, we only print intended for console.
 * Later, we might add options to support different options (plain/ANSIcolor/HTML/YAML/...)
 */
class AirQConfigFormatter {

    fun format(config: Map<String, Any>): String {
        return formatMap(config)
    }

    private fun formatMap(map: Map<String, Any>, depth: Int = 0): String {
        val entries = map
            .takeIf { it.isNotEmpty() }
            ?.map { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                when (value) {
                    is Map<*, *> -> "$key:\n" + formatMap(value as Map<String, Any>, depth + 1)
                    is List<*> -> "$key:\n" + formatList(value as List<Any>, depth + 1)
                    else -> "$key: $value"
                }
            }
            ?: listOf("– empty map –")

        return entries.joinToString("\n", "", "") {
            indent(depth) + it
        }
    }

    private fun formatList(list: List<Any?>, depth: Int = 0): String {
        if (list.isEmpty()) {
            return indent(depth) + "– empty list –"
        }

        return list.joinToString("\n", "", "") {
            indent(depth) + "- " + it
        }
    }

    private fun indent(depth: Int) = "  ".repeat(depth)
}
