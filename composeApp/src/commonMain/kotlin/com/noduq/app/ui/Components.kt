package com.noduq.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.PermissionState
import com.noduq.app.copLabel
import com.noduq.app.motionEnabled
import com.noduq.app.resources.Res
import com.noduq.app.resources.logo_nq_cian_noche
import com.noduq.app.theme.NoduqColors
import com.noduq.app.theme.NoduqMotion
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
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
    icon: ImageVector? = null,
    iconTint: Color = NoduqColors.night,
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
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
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
    color: Color = if (danger) NoduqColors.danger else NoduqColors.ink,
) {
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
    label: String = "Continuar con Google",
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
            if (loading) "Abriendo Google…" else label,
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
        Text(text, color = NoduqColors.cyan, fontWeight = FontWeight.Medium, fontSize = 16.sp)
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
            containerColor = if (selected) NoduqColors.cyan else NoduqColors.inset,
            contentColor = if (selected) NoduqColors.night else Color.White,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            NoduqColors.cyan.copy(alpha = if (selected) 1f else 0.4f),
        ),
    ) {
        Text(
            text,
            color = if (selected) NoduqColors.night else Color.White,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun SlidingFilterChips(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val motion = motionEnabled()
    val spots = remember { mutableStateMapOf<String, Pair<Float, Float>>() }
    val pillX = remember { Animatable(0f) }
    val pillW = remember { Animatable(0f) }
    val target = spots[selected]
    LaunchedEffect(selected, target?.first, target?.second, motion) {
        val left = target?.first ?: return@LaunchedEffect
        val width = target.second
        if (!motion || pillW.value == 0f) {
            pillX.snapTo(left)
            pillW.snapTo(width)
        } else {
            launch { pillX.animateTo(left, tween(NoduqMotion.selectMs, easing = NoduqMotion.easeOut)) }
            launch { pillW.animateTo(width, tween(NoduqMotion.selectMs, easing = NoduqMotion.easeOut)) }
        }
    }
    Box(modifier) {
        if (pillW.value > 0f) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(pillX.value.roundToInt(), 0) }
                    .width(with(density) { pillW.value.toDp() })
                    .height(40.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(NoduqColors.cyan),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (id, label) ->
                FilterChipLabel(
                    label = label,
                    selected = id == selected,
                    onClick = { onSelect(id) },
                    modifier = Modifier.onGloballyPositioned { coords ->
                        val bounds = coords.boundsInParent()
                        val prev = spots[id]
                        if (
                            prev == null ||
                            kotlin.math.abs(prev.first - bounds.left) > 0.5f ||
                            kotlin.math.abs(prev.second - bounds.width) > 0.5f
                        ) {
                            spots[id] = bounds.left to bounds.width
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterChipLabel(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (selected) NoduqColors.night else Color.White,
        animationSpec = tween(NoduqMotion.selectMs, easing = NoduqMotion.easeOut),
        label = "chip-ink",
    )
    val shape = RoundedCornerShape(99.dp)
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, NoduqColors.cyan.copy(alpha = 0.4f), shape),
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = color,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
        )
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

@Composable
fun FeedbackToast(
    text: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf<String?>(null) }
    val motion = motionEnabled()
    LaunchedEffect(text) {
        if (text == null) {
            visible = false
            return@LaunchedEffect
        }
        shown = text
        visible = true
        kotlinx.coroutines.delay(2600)
        visible = false
        kotlinx.coroutines.delay(if (motion) 220L else 0L)
        onDismiss()
    }
    AnimatedVisibility(
        visible = visible && shown != null,
        modifier = modifier,
        enter = if (motion) {
            fadeIn(tween(200, easing = NoduqMotion.easeOut)) +
                slideInVertically(tween(200, easing = NoduqMotion.easeOut)) { it / 2 }
        } else {
            fadeIn(tween(0))
        },
        exit = if (motion) {
            fadeOut(tween(180, easing = NoduqMotion.easeOut)) +
                slideOutVertically(tween(180, easing = NoduqMotion.easeOut)) { it / 2 }
        } else {
            fadeOut(tween(0))
        },
    ) {
        val message = shown ?: return@AnimatedVisibility
        Row(
            Modifier
                .padding(horizontal = 22.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(NoduqColors.raised)
                .border(1.dp, NoduqColors.ok.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                NoduqIcons.Check,
                contentDescription = null,
                tint = NoduqColors.ok,
                modifier = Modifier.size(18.dp),
            )
            Text(
                message,
                color = NoduqColors.ink,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
        }
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
fun CodeBlock(
    code: String,
    onCopy: (() -> Unit)? = null,
    copied: Boolean = false,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                letterSpacing = 2.sp,
                color = NoduqColors.cyan,
            )
            if (onCopy != null) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onCopy),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (copied) NoduqIcons.Check else NoduqIcons.Copy,
                        contentDescription = if (copied) "Copiado" else "Copiar código",
                        tint = if (copied) NoduqColors.ok else NoduqColors.cyan,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun CountingCop(
    amount: Double,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
) {
    val motion = motionEnabled()
    val value = remember { Animatable(amount.toFloat()) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(amount, motion) {
        if (!motion) {
            value.snapTo(amount.toFloat())
            fade.snapTo(1f)
        } else {
            fade.snapTo(0.55f)
            launch { fade.animateTo(1f, tween(150, easing = NoduqMotion.easeOut)) }
            value.animateTo(amount.toFloat(), tween(420, easing = NoduqMotion.easeOut))
        }
    }
    Text(
        copLabel(value.value.toDouble()),
        modifier = modifier.graphicsLayer { alpha = fade.value },
        color = NoduqColors.cyan,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        letterSpacing = (-0.8).sp,
    )
}

@Composable
fun LiveDot(color: Color = NoduqColors.cyan) {
    val pulse = rememberInfiniteTransition(label = "live")
    val alpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "live-alpha",
    )
    val scale by pulse.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "live-scale",
    )
    Box(Modifier.size(14.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(14.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha * 0.35f
                }
                .clip(CircleShape)
                .background(color),
        )
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha)),
        )
    }
}

@Composable
fun PlanBenefitRow(title: String, body: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(NoduqColors.cyan.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Phosphor.Check,
                contentDescription = null,
                tint = NoduqColors.cyan,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = NoduqColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(body, color = NoduqColors.muted, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun PermissionGrantCard(
    icon: ImageVector,
    title: String,
    body: String,
    state: PermissionState,
    onAsk: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val granted = state == PermissionState.Granted || state == PermissionState.NotNeeded
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NoduqColors.cyan.copy(alpha = if (granted) 0.28f else 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = NoduqColors.cyan, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = NoduqColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(body, color = NoduqColors.muted, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        when (state) {
            PermissionState.Denied -> PrimaryButton("Permitir", onClick = onAsk)
            PermissionState.Blocked -> GhostButton("Abrir ajustes", onClick = onOpenSettings)
            PermissionState.Granted, PermissionState.NotNeeded -> {
                Row(
                    Modifier.heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Phosphor.Check,
                        contentDescription = null,
                        tint = NoduqColors.ok,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "Concedido",
                        color = NoduqColors.ok,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyBadge(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Phosphor.ShieldCheck,
            contentDescription = null,
            tint = NoduqColors.cyan,
            modifier = Modifier.size(22.dp).padding(top = 2.dp),
        )
        Text(text, color = NoduqColors.muted, fontSize = 13.sp, lineHeight = 19.sp)
    }
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
