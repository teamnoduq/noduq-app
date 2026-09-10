package com.noduq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.resources.Res
import com.noduq.app.resources.logo_nq_cian_noche
import com.noduq.app.theme.NoduqColors
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image

private val FieldShape = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(18.dp)

@Composable
fun BrandMark(compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(Res.drawable.logo_nq_cian_noche),
            contentDescription = "NODUQ",
            modifier = Modifier.size(if (compact) 28.dp else 40.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "NODUQ",
            color = NoduqColors.cyan,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.4.sp,
            fontSize = if (compact) 13.sp else 15.sp,
        )
    }
}

@Composable
fun Kicker(text: String) {
    Text(
        text = text.uppercase(),
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        color = NoduqColors.cyan,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    hero: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (hero) 64.dp else 56.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = NoduqColors.cyan,
            contentColor = NoduqColors.night,
            disabledContainerColor = NoduqColors.cyan.copy(alpha = 0.35f),
            disabledContentColor = NoduqColors.night.copy(alpha = 0.7f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = NoduqColors.night,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = if (hero) 18.sp else 16.sp)
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
) {
    val color = if (danger) NoduqColors.danger else NoduqColors.ink
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = color,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.28f)),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun QuietButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = NoduqColors.cyan),
    ) {
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NoduqField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onIme: () -> Unit = {},
    mono: Boolean = false,
    optional: Boolean = false,
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            enabled = enabled,
            label = {
                Text(if (optional) "$label (opcional)" else label)
            },
            isError = error != null,
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (password) KeyboardType.Password else keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(onAny = { onIme() }),
            singleLine = true,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                fontFamily = if (mono) FontFamily.Monospace else androidx.compose.material3.LocalTextStyle.current.fontFamily,
                fontSize = if (mono) 22.sp else 16.sp,
                fontWeight = if (mono) FontWeight.SemiBold else FontWeight.Normal,
                letterSpacing = if (mono) 2.sp else 0.sp,
                color = NoduqColors.ink,
            ),
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NoduqColors.ink,
                unfocusedTextColor = NoduqColors.ink,
                disabledTextColor = NoduqColors.muted,
                focusedBorderColor = NoduqColors.cyan,
                unfocusedBorderColor = NoduqColors.line,
                errorBorderColor = NoduqColors.danger,
                focusedContainerColor = NoduqColors.inset,
                unfocusedContainerColor = NoduqColors.inset,
                disabledContainerColor = NoduqColors.inset,
                errorContainerColor = NoduqColors.inset,
                cursorColor = NoduqColors.cyan,
                focusedLabelColor = NoduqColors.cyan,
                unfocusedLabelColor = NoduqColors.muted,
                errorLabelColor = NoduqColors.danger,
            ),
        )
        if (error != null) {
            Text(error, color = NoduqColors.danger, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        } else if (hint != null) {
            Text(hint, color = NoduqColors.muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
fun Banner(text: String, tone: String = "error") {
    val color = if (tone == "ok") NoduqColors.ok else NoduqColors.danger
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(text, color = color, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (highlighted) NoduqColors.cyan else NoduqColors.raised)
            .then(
                if (highlighted) Modifier
                else Modifier.border(1.dp, NoduqColors.line, shape),
            )
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier,
            )
            .padding(22.dp),
        content = content,
    )
}

@Composable
fun CodeBlock(code: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(vertical = 20.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            letterSpacing = 2.sp,
            color = NoduqColors.cyan,
        )
    }
}

@Composable
fun LiveDot() {
    Box(
        Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(NoduqColors.cyan),
    )
}

@Composable
fun ScreenColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}
