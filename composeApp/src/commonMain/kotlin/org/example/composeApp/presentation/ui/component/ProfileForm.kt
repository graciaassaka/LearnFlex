package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import org.example.composeApp.presentation.ui.layout.EnumScrollablePickerLayout
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileForm(
    isLoading: Boolean,
    photoUrl: String,
    username: String,
    usernameError: String?,
    goal: String,
    level: Level,
    isLevelDropdownVisible: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    onHandleError: (Throwable) -> Unit,
    onUsernameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onToggleLevelDropdownVisibility: () -> Unit,
    onSelectLevel: (Level) -> Unit,
    onSelectField: (Field) -> Unit,
    onSubmit: () -> Unit,
    submitText: String,
    modifier: Modifier = Modifier,
    currentImageUrl: String? = null
) {
    var goalCharCount by remember { mutableIntStateOf(0) }
    val maxGoalLen = 80

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
        ImageUpload(
            enabled = !isLoading,
            onImageSelected = onImageSelected,
            onImageDeleted = onImageDeleted,
            handleError = onHandleError,
            modifier = Modifier.testTag(TestTags.PERSONAL_INFO_IMAGE_UPLOAD.tag),
            isUploaded = photoUrl.isNotBlank(),
            currentImageUrl = currentImageUrl
        )
        TextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag),
            enabled = !isLoading,
            label = { Text(stringResource(Res.string.username_label)) },
            leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
            supportingText = { Text(usernameError ?: "") },
            isError = usernameError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        TextField(
            value = goal,
            onValueChange = {
                if (it.length < maxGoalLen) onGoalChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag),
            enabled = !isLoading,
            label = { Text(stringResource(Res.string.goals_label)) },
            leadingIcon = { Icon(Icons.Default.SportsFootball, null) },
            supportingText = {
                Text(
                    text = "$goalCharCount/$maxGoalLen",
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_GOAL_CHAR_COUNTER.tag)
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = false
        )
        EnumDropdown<Level>(
            label = stringResource(Res.string.level_label),
            selected = level,
            isDropDownVisible = isLevelDropdownVisible,
            onDropDownVisibilityChanged = onToggleLevelDropdownVisibility,
            onSelected = onSelectLevel,
            enabled = !isLoading,
            modifier = Modifier.testTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN.tag)
        )
        EnumScrollablePickerLayout<Field>(
            label = stringResource(Res.string.field_label),
            onChange = onSelectField,
            enabled = !isLoading,
            modifier = Modifier.testTag(TestTags.PERSONAL_INFO_FIELD_PICKER.tag)
        )
        Button(
            onClick = onSubmit,
            enabled = !isLoading && usernameError.isNullOrBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                .testTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag),
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
            content = { Text(submitText) }
        )
        Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
    }
}