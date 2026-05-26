package com.remenod.oop2_coursework.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val SearchDebounceMillis = 250L

@Composable
fun DebouncedSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var localQuery by rememberSaveable { mutableStateOf(query) }

    LaunchedEffect(query) {
        if (query != localQuery) {
            localQuery = query
        }
    }

    LaunchedEffect(localQuery) {
        delay(SearchDebounceMillis)
        if (localQuery != query) {
            onQueryChange(localQuery)
        }
    }

    OutlinedTextField(
        value = localQuery,
        onValueChange = { localQuery = it },
        modifier = modifier,
        singleLine = true,
        label = { Text(label) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (localQuery.isNotBlank()) {
                IconButton(
                    onClick = {
                        localQuery = ""
                        onQueryChange("")
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        }
    )
}
