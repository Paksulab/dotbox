package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dotbox.app.DotBoxApplication
import com.dotbox.app.data.local.NoteDao
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

    val isEditing = editingNoteId != null

    fun startEditing(note: NoteEntity) {
        editingNoteId = note.id
        editTitle = note.title
        editContent = note.content
    }

    fun saveAndClose() {
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
            // Delete empty notes
            scope.launch { noteDao.deleteNote(id) }
        }
        editingNoteId = null
        editTitle = ""
        editContent = ""
    }

    fun createNew() {
        scope.launch {
            val newId = noteDao.insertNote(NoteEntity())
            editingNoteId = newId
            editTitle = ""
            editContent = ""
        }
    }

    ToolScreenScaffold(
        title = if (isEditing) "Edit Note" else "Notes",
        onBack = {
            if (isEditing) saveAndClose() else onBack()
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AnimatedVisibility(visible = !isEditing, enter = fadeIn(), exit = fadeOut()) {
                // Notes list
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No notes yet. Tap + to create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                // Note editor
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = { Text("Start writing...") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

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
