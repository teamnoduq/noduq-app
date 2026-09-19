package com.noduq.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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
import com.noduq.app.theme.NoduqMotion
import org.jetbrains.compose.resources.painterResource

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
fun Kicker(text: String, color: Color = NoduqColors.cyan) {
    Text(
        text = text.uppercase(),
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        color = color,
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
        Text(
            text,
            color = NoduqColors.night,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (hero) 18.sp else 16.sp,
        )
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
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f)),
    ) {
        Text(text, color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun GoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = NoduqColors.raised,
            contentColor = NoduqColors.ink,
            disabledContainerColor = NoduqColors.raised.copy(alpha = 0.6f),
            disabledContentColor = NoduqColors.muted,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, NoduqColors.line),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = NoduqColors.cyan,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(10.dp))
        } else {
            Image(
                imageVector = GoogleG,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            if (loading) "Abriendo Google…" else "Continuar con Google",
            color = NoduqColors.ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun OrDivider() {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(NoduqColors.line),
        )
        Text("o", color = NoduqColors.muted, fontSize = 13.sp)
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(NoduqColors.line),
        )
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
        Text(text, color = NoduqColors.cyan, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
fun QuietTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = NoduqColors.cyan),
    ) {
        Text(text, color = NoduqColors.cyan, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun BackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
    ) {
        Image(
            imageVector = Phosphor.CaretLeft,
            contentDescription = "Volver",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(NoduqColors.cyan),
        )
    }
}

@Composable
fun AuthLinkRow(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(prompt, color = NoduqColors.muted, fontSize = 14.sp)
        TextButton(
            onClick = onClick,
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = NoduqColors.cyan),
        ) {
            Text(action, color = NoduqColors.cyan, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PasswordChecklist(
    password: String,
    modifier: Modifier = Modifier,
) {
    val typing = password.isNotEmpty()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PasswordRuleRow("Longitud (6+)", typing, password.length >= 6)
        PasswordRuleRow("Mayúscula", typing, password.any { it.isUpperCase() })
        PasswordRuleRow("Número", typing, password.any { it.isDigit() })
    }
}

@Composable
fun PasswordMatchHint(
    password: String,
    confirm: String,
    modifier: Modifier = Modifier,
) {
    if (confirm.isEmpty()) return
    PasswordRuleRow(
        label = "Las contraseñas coinciden",
        active = true,
        met = password == confirm,
        modifier = modifier,
    )
}

@Composable
private fun PasswordRuleRow(
    label: String,
    active: Boolean,
    met: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = when {
        !active -> NoduqColors.muted
        met -> NoduqColors.ok
        else -> NoduqColors.danger
    }
    val icon = when {
        !active -> Phosphor.Circle
        met -> Phosphor.Check
        else -> Phosphor.X
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(color),
        )
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChipButton(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(99.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) NoduqColors.cyan else Color.Transparent,
            contentColor = if (selected) NoduqColors.night else NoduqColors.cyan,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            NoduqColors.cyan.copy(alpha = if (selected) 1f else 0.4f),
        ),
    ) {
        Text(text, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
fun NoduqField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onIme: () -> Unit = {},
    mono: Boolean = false,
    optional: Boolean = false,
    floatLabel: Boolean = true,
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            enabled = enabled,
            label = if (floatLabel) {
                { Text(if (optional) "$label (opcional)" else label) }
            } else {
                null
            },
            placeholder = if (placeholder != null || !floatLabel) {
                {
                    Text(
                        placeholder ?: label,
                        color = NoduqColors.muted,
                        fontFamily = if (mono) FontFamily.Monospace else androidx.compose.material3.LocalTextStyle.current.fontFamily,
                        fontSize = if (mono) 22.sp else 16.sp,
                        fontWeight = if (mono) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = if (mono) 2.sp else 0.sp,
                    )
                }
            } else {
                null
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
                focusedPlaceholderColor = NoduqColors.muted,
                unfocusedPlaceholderColor = NoduqColors.muted,
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

enum class RoleKind { Admin, Employee }

@Composable
fun RoleCard(
    kind: RoleKind,
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val shape = RoundedCornerShape(18.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(NoduqMotion.pressMs, easing = NoduqMotion.easeOut),
        label = "role-press",
    )
    val background by animateColorAsState(
        targetValue = if (selected) NoduqColors.night else NoduqColors.raised,
        animationSpec = tween(NoduqMotion.selectMs, easing = NoduqMotion.easeOut),
        label = "role-bg",
    )
    val stroke by animateColorAsState(
        targetValue = if (selected) NoduqColors.cyan else NoduqColors.line,
        animationSpec = tween(NoduqMotion.selectMs, easing = NoduqMotion.easeOut),
        label = "role-stroke",
    )
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(background)
            .border(1.dp, stroke, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoleGlyph(kind, selected)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                color = NoduqColors.ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            Text(
                body,
                color = NoduqColors.muted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun RoleGlyph(kind: RoleKind, selected: Boolean) {
    val wash by animateFloatAsState(
        targetValue = if (selected) 0.28f else 0.16f,
        animationSpec = tween(NoduqMotion.selectMs, easing = NoduqMotion.easeOut),
        label = "role-glyph",
    )
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(NoduqColors.cyan.copy(alpha = wash)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = when (kind) {
                RoleKind.Admin -> Phosphor.Storefront
                RoleKind.Employee -> Phosphor.IdentificationBadge
            },
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(NoduqColors.cyan),
        )
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
            .fillMaxWidth()
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
