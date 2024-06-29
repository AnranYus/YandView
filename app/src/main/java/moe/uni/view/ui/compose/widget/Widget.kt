package moe.uni.view.ui.compose.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SearchBar(modifier: Modifier = Modifier,searchTarget: String = "", searchEvent: (String) -> Unit,menuButtonAction: () -> Unit) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(56.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            modifier = Modifier
                .padding(15.dp)
                .height(30.dp)
                .width(30.dp)
                .clickable {
                    menuButtonAction.invoke()
                }
        )

        var input: String by remember { mutableStateOf(searchTarget) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val forceManager = LocalFocusManager.current
        BasicTextField(
            value = input,
            onValueChange = {
                input = it
            },
            singleLine = true,
            textStyle = TextStyle(fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                forceManager.clearFocus()
                searchEvent.invoke(input)
            },
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .fillMaxWidth(),
        )

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier
                .padding(15.dp)
                .height(30.dp)
                .width(30.dp)
        )

    }
}