package com.example.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

data class StringProvider(
    val lang: String,
    private val map: Map<String, String>,
    private val fallback: Map<String, String>
) {
    operator fun get(key: String): String = map[key] ?: fallback[key] ?: key

    fun format(key: String, vararg args: Pair<String, Any>): String {
        var out = get(key)
        for ((k, v) in args) {
            out = out.replace("{$k}", v.toString())
        }
        return out
    }
}

val LocalStrings = staticCompositionLocalOf {
    StringProvider(lang = "az", map = StringsAz, fallback = StringsAz)
}

fun stringsFor(lang: String): StringProvider =
    if (lang == "en") StringProvider("en", StringsEn, StringsAz)
    else StringProvider("az", StringsAz, StringsAz)

@Composable
@ReadOnlyComposable
fun s(key: String): String = LocalStrings.current[key]

@Composable
@ReadOnlyComposable
fun s(key: String, vararg args: Pair<String, Any>): String =
    LocalStrings.current.format(key, *args)
