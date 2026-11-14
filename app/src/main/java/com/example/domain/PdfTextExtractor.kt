package com.example.domain

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.Inflater

/**
 * Lightweight pure-Kotlin PDF text extractor supporting FlateDecode streams,
 * ToUnicode CMaps, UTF-16BE / WinAnsi text strings, and page boundaries (\f).
 */
object PdfTextExtractor {

    fun extractText(pdfBytes: ByteArray): String {
        val raw = String(pdfBytes, StandardCharsets.ISO_8859_1)
        val streams = extractStreams(pdfBytes, raw)
        if (streams.isEmpty()) return ""

        val cMap = mutableMapOf<Int, String>()
        val contentStreams = mutableListOf<String>()

        for (decoded in streams) {
            if (decoded.contains("beginbfchar") || decoded.contains("beginbfrange")) {
                parseCMap(decoded, cMap)
            } else if (decoded.contains("BT") && decoded.contains("ET")) {
                contentStreams += decoded
            }
        }

        if (contentStreams.isEmpty()) return ""

        val out = StringBuilder()
        contentStreams.forEachIndexed { idx, stream ->
            if (idx > 0) out.append('\u000c').append('\n')
            extractTextOperators(stream, cMap, out)
        }
        return out.toString()
    }

    private fun extractStreams(bytes: ByteArray, raw: String): List<String> {
        val results = mutableListOf<String>()
        var searchFrom = 0
        while (true) {
            val streamIdx = raw.indexOf("stream", searchFrom)
            if (streamIdx < 0) break

            val dictStart = raw.lastIndexOf("<<", streamIdx).coerceAtLeast(0)
            val dict = raw.substring(dictStart, streamIdx)
            val isFlate = dict.contains("/FlateDecode")

            var dataStart = streamIdx + 6
            if (dataStart < bytes.size && bytes[dataStart] == '\r'.code.toByte()) dataStart++
            if (dataStart < bytes.size && bytes[dataStart] == '\n'.code.toByte()) dataStart++

            val endStreamIdx = raw.indexOf("endstream", dataStart)
            if (endStreamIdx < 0) break

            var dataEnd = endStreamIdx
            while (dataEnd > dataStart && (bytes[dataEnd - 1] == '\n'.code.toByte() || bytes[dataEnd - 1] == '\r'.code.toByte())) {
                dataEnd--
            }

            if (dataEnd > dataStart) {
                val slice = bytes.copyOfRange(dataStart, dataEnd)
                val decoded = if (isFlate) inflate(slice) else String(slice, StandardCharsets.ISO_8859_1)
                if (decoded != null) results += decoded
            }
            searchFrom = endStreamIdx + 9
        }
        return results
    }

    private fun inflate(compressed: ByteArray): String? = try {
        val inflater = Inflater()
        inflater.setInput(compressed)
        val out = ByteArrayOutputStream(compressed.size * 2)
        val buf = ByteArray(4096)
        while (!inflater.finished()) {
            val count = inflater.inflate(buf)
            if (count == 0) {
                if (inflater.needsInput() || inflater.needsDictionary()) break
            } else {
                out.write(buf, 0, count)
            }
        }
        inflater.end()
        String(out.toByteArray(), StandardCharsets.ISO_8859_1)
    } catch (_: Exception) {
        null
    }

    private fun parseCMap(cmapText: String, map: MutableMap<Int, String>) {
        val bfCharRegex = Regex("""<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>""")
        val charSection = Regex("""beginbfchar\s*(.*?)\s*endbfchar""", RegexOption.DOT_MATCHES_ALL)
        for (sec in charSection.findAll(cmapText)) {
            for (m in bfCharRegex.findAll(sec.groupValues[1])) {
                val code = m.groupValues[1].toIntOrNull(16) ?: continue
                val unicode = decodeHexUtf16(m.groupValues[2])
                map[code] = unicode
            }
        }

        val bfRangeRegex = Regex("""<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>""")
        val rangeSection = Regex("""beginbfrange\s*(.*?)\s*endbfrange""", RegexOption.DOT_MATCHES_ALL)
        for (sec in rangeSection.findAll(cmapText)) {
            for (m in bfRangeRegex.findAll(sec.groupValues[1])) {
                val start = m.groupValues[1].toIntOrNull(16) ?: continue
                val end = m.groupValues[2].toIntOrNull(16) ?: continue
                var target = m.groupValues[3].toIntOrNull(16) ?: continue
                if (end - start <= 512) {
                    for (code in start..end) {
                        map[code] = String(Character.toChars(target))
                        target++
                    }
                }
            }
        }
    }

    private fun decodeHexUtf16(hex: String): String {
        val clean = if (hex.length % 2 != 0) "0$hex" else hex
        val bytes = ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return if (bytes.size >= 2) {
            String(bytes, StandardCharsets.UTF_16BE)
        } else {
            String(bytes, StandardCharsets.ISO_8859_1)
        }
    }

    private fun extractTextOperators(stream: String, cMap: Map<Int, String>, out: StringBuilder) {
        val btRegex = Regex("""\bBT\b(.*?)\bET\b""", RegexOption.DOT_MATCHES_ALL)
        for (block in btRegex.findAll(stream)) {
            val content = block.groupValues[1]
            var i = 0
            var lineWritten = false
            while (i < content.length) {
                when (content[i]) {
                    '(' -> {
                        val (lit, nextIdx) = readLiteralString(content, i)
                        val op = readTrailingOperator(content, nextIdx)
                        if (op == "Tj" || op == "'" || op == "\"") {
                            if (op == "'" || op == "\"") out.append('\n')
                            val text = decodePdfBytes(lit, cMap)
                            if (text.isNotEmpty()) {
                                out.append(text)
                                lineWritten = true
                            }
                        }
                        i = nextIdx
                    }
                    '<' -> {
                        if (i + 1 < content.length && content[i + 1] != '<') {
                            val end = content.indexOf('>', i + 1)
                            if (end > i) {
                                val hex = content.substring(i + 1, end).replace(Regex("\\s+"), "")
                                val op = readTrailingOperator(content, end + 1)
                                if (op == "Tj" || op == "'" || op == "\"") {
                                    if (op == "'" || op == "\"") out.append('\n')
                                    out.append(decodeHexString(hex, cMap))
                                    lineWritten = true
                                }
                                i = end + 1
                                continue
                            }
                        }
                        i++
                    }
                    '[' -> {
                        val end = findArrayEnd(content, i)
                        val op = readTrailingOperator(content, end + 1)
                        if (op == "TJ") {
                            val arrayBody = content.substring(i + 1, end)
                            parseTjArray(arrayBody, cMap, out)
                            lineWritten = true
                        }
                        i = end + 1
                    }
                    'T' -> {
                        if (i + 1 < content.length && (content[i + 1] == 'd' || content[i + 1] == 'D' || content[i + 1] == '*')) {
                            if (lineWritten) {
                                out.append('\n')
                                lineWritten = false
                            }
                            i += 2
                        } else i++
                    }
                    else -> i++
                }
            }
            if (lineWritten) out.append('\n')
        }
    }

    private fun parseTjArray(body: String, cMap: Map<Int, String>, out: StringBuilder) {
        var i = 0
        while (i < body.length) {
            when {
                body[i] == '(' -> {
                    val (lit, next) = readLiteralString(body, i)
                    out.append(decodePdfBytes(lit, cMap))
                    i = next
                }
                body[i] == '<' -> {
                    val end = body.indexOf('>', i + 1)
                    if (end > i) {
                        val hex = body.substring(i + 1, end).replace(Regex("\\s+"), "")
                        out.append(decodeHexString(hex, cMap))
                        i = end + 1
                    } else i++
                }
                body[i] == '-' || body[i].isDigit() -> {
                    var j = i + 1
                    while (j < body.length && (body[j].isDigit() || body[j] == '.')) j++
                    val num = body.substring(i, j).toDoubleOrNull() ?: 0.0
                    if (num < -180.0) out.append(' ')
                    i = j
                }
                else -> i++
            }
        }
    }

    private fun readTrailingOperator(s: String, start: Int): String {
        var idx = start
        while (idx < s.length && s[idx].isWhitespace()) idx++
        val end = idx
        var j = end
        while (j < s.length && !s[j].isWhitespace() && s[j] != '(' && s[j] != '<' && s[j] != '[') j++
        return s.substring(end, j)
    }

    private fun findArrayEnd(s: String, start: Int): Int {
        var i = start + 1
        var inStr = false
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && inStr) { i += 2; continue }
            if (c == '(') inStr = true
            else if (c == ')') inStr = false
            else if (c == ']' && !inStr) return i
            i++
        }
        return s.length - 1
    }

    private fun readLiteralString(s: String, start: Int): Pair<ByteArray, Int> {
        val out = ByteArrayOutputStream()
        var i = start + 1
        var depth = 1
        while (i < s.length && depth > 0) {
            val c = s[i]
            when (c) {
                '\\' -> {
                    i++
                    if (i >= s.length) break
                    when (val esc = s[i]) {
                        'n' -> out.write('\n'.code)
                        'r' -> out.write('\r'.code)
                        't' -> out.write('\t'.code)
                        'b' -> out.write('\b'.code)
                        'f' -> out.write(0x0c)
                        '(', ')', '\\' -> out.write(esc.code)
                        '\r', '\n' -> {}
                        in '0'..'7' -> {
                            var oct = esc.toString()
                            var k = 1
                            while (k < 3 && i + 1 < s.length && s[i + 1] in '0'..'7') {
                                i++
                                oct += s[i]
                                k++
                            }
                            out.write(oct.toInt(8))
                        }
                        else -> out.write(esc.code)
                    }
                }
                '(' -> { depth++; out.write(c.code) }
                ')' -> { depth--; if (depth > 0) out.write(c.code) }
                else -> out.write(c.code)
            }
            i++
        }
        return out.toByteArray() to i
    }

    private fun decodePdfBytes(bytes: ByteArray, cMap: Map<Int, String>): String {
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16BE)
        }
        if (cMap.isNotEmpty()) {
            val sb = StringBuilder()
            var i = 0
            while (i < bytes.size) {
                val b1 = bytes[i].toInt() and 0xFF
                val twoByte = if (i + 1 < bytes.size) ((b1 shl 8) or (bytes[i + 1].toInt() and 0xFF)) else -1
                if (twoByte >= 0 && cMap.containsKey(twoByte)) {
                    sb.append(cMap[twoByte])
                    i += 2
                } else if (cMap.containsKey(b1)) {
                    sb.append(cMap[b1])
                    i++
                } else {
                    sb.append(b1.toChar())
                    i++
                }
            }
            return sb.toString()
        }
        return String(bytes, StandardCharsets.UTF_8)
    }

    private fun decodeHexString(hex: String, cMap: Map<Int, String>): String {
        val clean = if (hex.length % 2 != 0) "${hex}0" else hex
        val bytes = ByteArray(clean.length / 2) { idx ->
            clean.substring(idx * 2, idx * 2 + 2).toInt(16).toByte()
        }
        return decodePdfBytes(bytes, cMap)
    }
}
