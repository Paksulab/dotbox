package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.DotBoxApplication
import com.dotbox.app.data.local.NoteEntity
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val noteDao = remember {
        (context.applicationContext as DotBoxApplication).database.noteDao()
    }
    val scope = rememberCoroutineScope()
    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())

    var editingNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editTitle by rememberSaveable { mutableStateOf("") }
    var editContent by rememberSaveable { mutableStateOf("") }
    var isPreviewMode by rememberSaveable { mutableStateOf(false) }
    var hasUnsavedChanges by rememberSaveable { mutableStateOf(false) }

    val isEditing = editingNoteId != null

    fun startEditing(note: NoteEntity) {
        editingNoteId = note.id
        editTitle = note.title
        editContent = note.content
        isPreviewMode = false
        hasUnsavedChanges = false
    }

    fun saveNote() {
        val id = editingNoteId ?: return
        if (editTitle.isNotBlank() || editContent.isNotBlank()) {
            scope.launch {
                noteDao.updateNote(
                    NoteEntity(
                        id = id,
                        title = editTitle,
                        content = editContent,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
        } else {
            scope.launch { noteDao.deleteNote(id) }
        }
        hasUnsavedChanges = false
    }

    fun saveAndClose() {
        saveNote()
        editingNoteId = null
        editTitle = ""
        editContent = ""
        isPreviewMode = false
        hasUnsavedChanges = false
    }

    fun createNew() {
        scope.launch {
            val newId = noteDao.insertNote(NoteEntity())
            editingNoteId = newId
            editTitle = ""
            editContent = ""
            isPreviewMode = false
            hasUnsavedChanges = false
        }
    }

    ToolScreenScaffold(
        title = when {
            isEditing && isPreviewMode -> "Preview"
            isEditing -> "Edit Note"
            else -> "Notes"
        },
        onBack = {
            if (isEditing) saveAndClose() else onBack()
        },
        actions = {
            if (isEditing) {
                // Edit / Preview toggle
                IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                    Icon(
                        imageVector = if (isPreviewMode) Icons.Outlined.Edit else Icons.Outlined.Visibility,
                        contentDescription = if (isPreviewMode) "Edit" else "Preview",
                    )
                }
                // Save button
                IconButton(
                    onClick = { saveNote() },
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = if (hasUnsavedChanges) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AnimatedVisibility(visible = !isEditing, enter = fadeIn(), exit = fadeOut()) {
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No notes yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to create one. Supports Markdown!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(notes, key = { it.id }) { note ->
                            NoteListItem(
                                note = note,
                                onClick = { startEditing(note) },
                                onDelete = { scope.launch { noteDao.deleteNote(note.id) } },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }

            AnimatedVisibility(visible = isEditing, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    // Title field (always visible)
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            hasUnsavedChanges = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Title") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isPreviewMode) {
                        // Markdown preview
                        SelectionContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            MarkdownContent(
                                markdown = editContent,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        // Markdown toolbar
                        MarkdownToolbar(
                            onInsert = { prefix, suffix ->
                                editContent = editContent + prefix + suffix
                                hasUnsavedChanges = true
                            },
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Content editor
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = {
                                editContent = it
                                hasUnsavedChanges = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            placeholder = { Text("Write in Markdown...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // FAB
            if (!isEditing) {
                FloatingActionButton(
                    onClick = { createNew() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Note")
                }
            }
        }
    }
}

@Composable
private fun MarkdownToolbar(
    onInsert: (prefix: String, suffix: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val chips = listOf(
            "H1" to ("# " to ""),
            "H2" to ("## " to ""),
            "H3" to ("### " to ""),
            "B" to ("**" to "**"),
            "I" to ("*" to "*"),
            "Code" to ("`" to "`"),
            "```" to ("```\n" to "\n```"),
            "•" to ("- " to ""),
            ">" to ("> " to ""),
            "[ ]" to ("- [ ] " to ""),
        )
        chips.forEach { (label, pair) ->
            FilterChip(
                selected = false,
                onClick = { onInsert(pair.first, pair.second) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    if (markdown.isBlank()) {
        Text(
            text = "Nothing to preview",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val lines = markdown.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Code block
            if (line.trimStart().startsWith("```")) {
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                i++ // skip closing ```
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(12.dp),
                ) {
                    Text(
                        text = codeLines.joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFB0BEC5),
                        ),
                    )
                }
                continue
            }

            // Headers
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(line.removePrefix("### ")),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(line.removePrefix("## ")),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        text = parseInlineMarkdown(line.removePrefix("# ")),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Horizontal rule
                line.matches(Regex("^-{3,}$")) || line.matches(Regex("^\\*{3,}$")) -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                // Blockquote
                line.startsWith("> ") -> {
                    Row {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.tertiary),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(line.removePrefix("> ")),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Checklist
                line.startsWith("- [x] ") || line.startsWith("- [X] ") -> {
                    Text(
                        text = buildAnnotatedString {
                            append("☑ ")
                            append(parseInlineMarkdown(line.removePrefix("- [x] ").removePrefix("- [X] ")))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                line.startsWith("- [ ] ") -> {
                    Text(
                        text = buildAnnotatedString {
                            append("☐ ")
                            append(parseInlineMarkdown(line.removePrefix("- [ ] ")))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Bullet list
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Text(
                        text = buildAnnotatedString {
                            append("  •  ")
                            append(parseInlineMarkdown(line.removePrefix("- ").removePrefix("* ")))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Numbered list
                line.matches(Regex("^\\d+\\. .*")) -> {
                    val num = line.substringBefore(". ")
                    val content = line.substringAfter(". ")
                    Text(
                        text = buildAnnotatedString {
                            append("  $num.  ")
                            append(parseInlineMarkdown(content))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Empty line = spacer
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Normal paragraph
                else -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            i++
        }
    }
}

@Composable
private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    var i = 0
    val defaultColor = MaterialTheme.colorScheme.onSurface
    val codeColor = MaterialTheme.colorScheme.tertiary

    while (i < text.length) {
        when {
            // Bold + Italic ***text***
            text.startsWith("***", i) -> {
                val end = text.indexOf("***", i + 3)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 3, end))
                    }
                    i = end + 3
                } else {
                    append(text[i])
                    i++
                }
            }
            // Bold **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // Italic *text*
            text.startsWith("*", i) && !text.startsWith("**", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Inline code `code`
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = codeColor,
                            fontSize = 13.sp,
                        ),
                    ) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Strikethrough ~~text~~
            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}

@Composable
private fun NoteListItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormat.format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
