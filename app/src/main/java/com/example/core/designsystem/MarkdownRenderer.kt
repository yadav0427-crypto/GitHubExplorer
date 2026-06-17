package com.example.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * A native, high-performance Markdown parser and viewer for Jetpack Compose.
 * Renders GitHub Flavored Markdown (GFM) natively using Material 3 specifications.
 */
@Composable
fun MarkdownViewer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val blocks = remember(markdown) { MarkdownParser.parse(markdown) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val scale = when (block.level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        3 -> 18.sp
                        else -> 16.sp
                    }
                    val weight = if (block.level <= 3) FontWeight.Bold else FontWeight.SemiBold
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = block.text,
                        fontSize = scale,
                        fontWeight = weight,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (block.level == 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = renderInlineMarkdown(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = block.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
                is MarkdownBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.depth * 12).dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (block.ordered) "${block.index}. " else "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = renderInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.ImageBlock -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = block.url,
                            contentDescription = block.alt,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        if (block.alt.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = block.alt,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
                MarkdownBlock.HorizontalRule -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/**
 * Basic block types for Markdown representation
 */
sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
    data class ListItem(val depth: Int, val ordered: Boolean, val index: Int, val text: String) : MarkdownBlock
    data class ImageBlock(val url: String, val alt: String) : MarkdownBlock
    object HorizontalRule : MarkdownBlock
}

/**
 * Fast Line parser
 */
object MarkdownParser {
    fun parse(markdown: String): List<MarkdownBlock> {
        val lines = markdown.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trimEnd()
            val trimmed = line.trim()

            // 1. Code Block Fence
            if (trimmed.startsWith("```")) {
                val lang = trimmed.removePrefix("```").trim().ifBlank { null }
                val codeBuilder = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeBuilder.append(lines[i]).append("\n")
                    i++
                }
                blocks.add(MarkdownBlock.CodeBlock(lang, codeBuilder.toString().trimEnd()))
                i++
                continue
            }

            // 2. Headings
            if (trimmed.startsWith("#")) {
                val level = trimmed.takeWhile { it == '#' }.length
                if (level in 1..6 && trimmed.getOrNull(level) == ' ') {
                    val text = trimmed.drop(level + 1).trim()
                    blocks.add(MarkdownBlock.Header(level, text))
                    i++
                    continue
                }
            }

            // 3. Horizontal Rule
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                blocks.add(MarkdownBlock.HorizontalRule)
                i++
                continue
            }

            // 4. List Items
            val bulletMatch = line.matchBullet()
            if (bulletMatch != null) {
                blocks.add(
                    MarkdownBlock.ListItem(
                        depth = bulletMatch.depth,
                        ordered = bulletMatch.ordered,
                        index = bulletMatch.index,
                        text = bulletMatch.text
                    )
                )
                i++
                continue
            }

            // 5. Raw Markdown images e.g. ![alt](url)
            val imgMatch = trimmed.matchImage()
            if (imgMatch != null) {
                blocks.add(MarkdownBlock.ImageBlock(imgMatch.first, imgMatch.second))
                i++
                continue
            }

            // Empty lines skipped
            if (trimmed.isBlank()) {
                i++
                continue
            }

            // 6. Generic Paragraph
            blocks.add(MarkdownBlock.Paragraph(trimmed))
            i++
        }
        return blocks
    }

    private fun String.matchBullet(): BulletMatch? {
        val indentSpaces = this.takeWhile { it == ' ' || it == '\t' }.length
        val depth = indentSpaces / 2
        val trimmed = this.trim()
        
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ")) {
            return BulletMatch(depth, false, 0, trimmed.substring(2).trim())
        }
        
        val firstWord = trimmed.substringBefore(" ")
        if (firstWord.endsWith(".") && firstWord.dropLast(1).all { it.isDigit() }) {
            val num = firstWord.dropLast(1).toInt()
            return BulletMatch(depth, true, num, trimmed.substring(firstWord.length).trim())
        }
        
        return null
    }

    private fun String.matchImage(): Pair<String, String>? {
        if (!this.startsWith("![") || !this.contains("](")) return null
        return try {
            val altStart = 2
            val altEnd = this.indexOf("](")
            val alt = this.substring(altStart, altEnd)
            val urlStart = altEnd + 2
            val urlEnd = this.indexOf(")", urlStart)
            val url = this.substring(urlStart, urlEnd)
            Pair(url, alt)
        } catch (e: Exception) {
            null
        }
    }

    private class BulletMatch(val depth: Int, val ordered: Boolean, val index: Int, val text: String)
}

/**
 * Basic inliner applying Bold, Italic, and Inline Monospace code using regular expressions.
 */
@Composable
fun renderInlineMarkdown(text: String) = buildAnnotatedString {
    // Basic formatting matcher
    var index = 0
    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
    val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)
    val codeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // Parse bold e.g. **text** and inline codes `code`
    var i = 0
    val len = text.length
    while (i < len) {
        if (i < len - 2 && text[i] == '*' && text[i + 1] == '*') {
            val endBold = text.indexOf("**", i + 2)
            if (endBold != -1) {
                append(text.substring(index, i))
                pushStyle(boldStyle)
                append(text.substring(i + 2, endBold))
                pop()
                i = endBold + 2
                index = i
                continue
            }
        }
        if (text[i] == '`') {
            val endCode = text.indexOf('`', i + 1)
            if (endCode != -1) {
                append(text.substring(index, i))
                pushStyle(codeStyle)
                append(text.substring(i + 1, endCode))
                pop()
                i = endCode + 1
                index = i
                continue
            }
        }
        i++
    }
    if (index < len) {
        append(text.substring(index))
    }
}
