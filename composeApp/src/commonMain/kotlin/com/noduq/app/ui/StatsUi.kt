package com.noduq.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noduq.app.AppViewModel
import com.noduq.app.StatsDto
import com.noduq.app.StatsPointDto
import com.noduq.app.calendarYear
import com.noduq.app.copLabel
import com.noduq.app.groupedInt
import com.noduq.app.monthWindows
import com.noduq.app.planActive
import com.noduq.app.theme.NoduqColors
import com.noduq.app.yearWindows
import kotlin.math.roundToInt

@Composable
fun StatsScreen(vm: AppViewModel) {
    if (vm.workspace?.planActive() != true) {
        Box(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            PlanPrompt(
                smsReady = !vm.needsSmsSetup(),
                onActivate = { vm.buyPlan() },
            )
        }
        return
    }
    StatsContent(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(vm: AppViewModel) {
    val planOn = true
    var opened by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        vm.openStats()
        opened = true
    }
    LaunchedEffect(opened, planOn) {
        if (!opened || !planOn) return@LaunchedEffect
        vm.loadPaymentHistory()
    }
    LaunchedEffect(opened, planOn, vm.statsGrain, vm.statsYear, vm.statsMonth) {
        if (!opened || !planOn) return@LaunchedEffect
        vm.loadStats()
    }
    var monthSheet by rememberSaveable { mutableStateOf(false) }
    var yearSheet by rememberSaveable { mutableStateOf(false) }
    val monthName = monthWindows(vm.statsYear).asReversed().getOrNull(vm.statsMonth - 1)?.name ?: "Mes"
    val byDay = vm.statsGrain == "month"
    val report = vm.stats

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Estadísticas",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 32.sp,
        )
        Spacer(Modifier.height(16.dp))
        SlidingFilterChips(
            options = listOf("month" to "Mes", "year" to "Año"),
            selected = vm.statsGrain,
            onSelect = vm::chooseStatsGrain,
            scrollable = true,
            trailing = listOf(
                TrailingFilterChip("$monthName ▾", vm.statsGrain == "month") { monthSheet = true },
                TrailingFilterChip("${vm.statsYear} ▾", vm.statsGrain == "year") { yearSheet = true },
            ),
        )
        Spacer(Modifier.height(16.dp))
        vm.statsError?.let {
            Banner(it)
            Spacer(Modifier.height(12.dp))
        }
        if (vm.statsLoading && report == null) {
            Text("Cargando estadísticas…", color = NoduqColors.muted, fontSize = 14.sp)
        }
        if (report != null) {
            if (vm.statsTipVisible) {
                StatsTipBanner(onDismiss = vm::dismissStatsTip)
                Spacer(Modifier.height(14.dp))
            }
            var picked by remember(report.points) { mutableIntStateOf(selectedIndex(report)) }
            val touched = report.points.getOrNull(picked)
            StatsCard(
                title = statsTitle(report, monthName),
                caption = touched?.let { "${it.detail} · ${groupedInt(it.count)} pagos" }
                    ?: "Toca la gráfica para ver un ${if (report.grain == "day") "día" else "mes"}.",
            ) {
                TrendChart(
                    report.points,
                    bars = false,
                    selected = picked,
                    onSelect = { picked = it },
                    valueOf = { it.count.toDouble() },
                )
            }
            Spacer(Modifier.height(14.dp))
            StatsCard(
                title = valueTitle(report, monthName),
                caption = touched?.let { "${it.detail} · ${copLabel(it.amount)}" }
                    ?: "Toca una barra para ver el total.",
            ) {
                TrendChart(
                    report.points,
                    bars = true,
                    selected = picked,
                    onSelect = { picked = it },
                    valueOf = { it.amount },
                )
            }
            Spacer(Modifier.height(14.dp))
            StatsFigures(report, byDay)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (monthSheet) {
        ModalBottomSheet(
            onDismissRequest = { monthSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = NoduqColors.raised,
            contentColor = NoduqColors.ink,
        ) {
            Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 28.dp)) {
                Text("Mes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                ChoiceList(
                    items = monthWindows(vm.statsYear).asReversed().mapIndexed { index, month ->
                        month.name to {
                            monthSheet = false
                            vm.chooseStatsMonth(index + 1)
                        }
                    },
                    chosen = monthName,
                )
            }
        }
    }
    if (yearSheet) {
        ModalBottomSheet(
            onDismissRequest = { yearSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = NoduqColors.raised,
            contentColor = NoduqColors.ink,
        ) {
            Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 28.dp)) {
                Text("Año", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                ChoiceList(
                    items = yearWindows(vm.paymentHistory?.earliestAt).asReversed().map { year ->
                        year.label to {
                            yearSheet = false
                            vm.chooseStatsYear(year.label.toIntOrNull() ?: calendarYear(null))
                        }
                    },
                    chosen = vm.statsYear.toString(),
                )
            }
        }
    }
}

@Composable
private fun StatsTipBanner(onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(start = 12.dp, top = 8.dp, end = 2.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            NoduqIcons.HandTap,
            contentDescription = null,
            tint = NoduqColors.cyan,
            modifier = Modifier.padding(top = 2.dp).size(18.dp),
        )
        Text(
            "Toca cualquier punto o barra en las gráficas para ver el detalle exacto de ese día o mes.",
            color = NoduqColors.ink,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f).padding(start = 10.dp, top = 1.dp, end = 4.dp),
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
                Phosphor.X,
                contentDescription = "Cerrar",
                tint = NoduqColors.muted,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun StatsFigures(report: StatsDto, byDay: Boolean) {
    val unit = if (byDay) "día" else "mes"
    val bucketWord = if (byDay) "Día" else "Mes"
    val emptyWord = if (byDay) "Días" else "Meses"
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HeadlineMetric(
            "Total pagos",
            groupedInt(report.count),
            valueColor = SoftCyan,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        HeadlineMetric(
            "Total valor",
            copLabel(report.amount),
            valueColor = ValueCyan,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
    Spacer(Modifier.height(10.dp))
    val countTone = changeTone(report.countChangePercent)
    val amountTone = changeTone(report.amountChangePercent)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricRow(
            "Prom. pagos / $unit (${report.bucketCount})",
            decimalLabel(report.averageCount),
            icon = NoduqIcons.Calculator,
            iconTint = NoduqColors.cyan,
        )
        MetricRow(
            "Prom. valor / $unit (${report.bucketCount})",
            copLabel(report.averageAmount),
            icon = NoduqIcons.Calculator,
            iconTint = NoduqColors.cyan,
        )
        MetricRow(
            "Prom. / pago",
            copLabel(report.averagePerPayment),
            icon = NoduqIcons.Calculator,
            iconTint = NoduqColors.cyan,
        )
        MetricRow(
            "Clientes únicos",
            groupedInt(report.uniquePayers),
            icon = NoduqIcons.People,
            iconTint = Violet,
        )
        MetricRow(
            "vs periodo anterior (pagos)",
            compareValue(report.countChangePercent),
            icon = if (countTone?.down == true) NoduqIcons.TrendingDown else NoduqIcons.TrendingUp,
            iconTint = countTone?.color ?: NoduqColors.muted,
            valueColor = countTone?.color ?: Color.White,
            badgeColor = countTone?.badge,
        )
        MetricRow(
            "vs periodo anterior (valor)",
            compareValue(report.amountChangePercent),
            icon = if (amountTone?.down == true) NoduqIcons.TrendingDown else NoduqIcons.TrendingUp,
            iconTint = amountTone?.color ?: NoduqColors.muted,
            valueColor = amountTone?.color ?: Color.White,
            badgeColor = amountTone?.badge,
        )
        MetricRow(
            "$bucketWord menor",
            extremeValue(report.low),
            icon = NoduqIcons.TrendingDown,
            iconTint = Amber,
            valueColor = Amber,
        )
        MetricRow(
            "$bucketWord mayor",
            extremeValue(report.peak),
            icon = NoduqIcons.TrendingUp,
            iconTint = ValueCyan,
            valueColor = ValueCyan,
        )
        MetricRow(
            "$emptyWord con ventas / vacíos",
            "${report.bucketsWithSales} / ${report.bucketsEmpty}",
            icon = NoduqIcons.Calendar,
            iconTint = NoduqColors.cyan,
        )
    }
}

@Composable
private fun HeadlineMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.cyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Text(label, color = NoduqColors.muted, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
        )
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color = NoduqColors.cyan,
    valueColor: Color = Color.White,
    badgeColor: Color? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NoduqColors.inset)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Spacer(Modifier.size(16.dp))
        }
        Text(
            label,
            color = NoduqColors.muted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .widthIn(max = 168.dp)
                .then(
                    if (badgeColor == null) {
                        Modifier
                    } else {
                        Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    },
                ),
        )
    }
}

@Composable
private fun StatsCard(title: String, caption: String, body: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NoduqColors.raised)
            .border(1.dp, NoduqColors.line, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(caption, color = NoduqColors.muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        body()
    }
}

@Composable
private fun TrendChart(
    points: List<StatsPointDto>,
    bars: Boolean,
    selected: Int,
    onSelect: (Int) -> Unit,
    valueOf: (StatsPointDto) -> Double,
) {
    if (points.isEmpty()) {
        Text("Sin pagos en este periodo.", color = NoduqColors.muted, fontSize = 14.sp)
        return
    }
    val max = points.maxOf { valueOf(it) }.coerceAtLeast(1.0)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(168.dp)
            .pointerInput(points, bars) {
                detectTapGestures { offset ->
                    val slot = size.width / points.size
                    onSelect((offset.x / slot).toInt().coerceIn(0, points.lastIndex))
                }
            },
    ) {
        val slot = size.width / points.size
        val bottom = size.height - 4.dp.toPx()
        val top = 8.dp.toPx()
        val span = (bottom - top).coerceAtLeast(1f)
        drawLine(
            NoduqColors.line,
            Offset(0f, bottom),
            Offset(size.width, bottom),
            strokeWidth = 1.dp.toPx(),
        )
        if (bars) {
            val barWidth = slot * 0.62f
            points.forEachIndexed { index, point ->
                val height = (valueOf(point) / max * span).toFloat()
                val left = index * slot + (slot - barWidth) / 2f
                drawRoundRect(
                    color = if (index == selected) NoduqColors.cyan else NoduqColors.cyan.copy(alpha = 0.55f),
                    topLeft = Offset(left, bottom - height),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
            }
        } else {
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = index * slot + slot / 2f
                val y = bottom - (valueOf(point) / max * span).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, NoduqColors.cyan, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            points.forEachIndexed { index, point ->
                val x = index * slot + slot / 2f
                val y = bottom - (valueOf(point) / max * span).toFloat()
                drawCircle(
                    color = if (index == selected) Color.White else NoduqColors.cyan,
                    radius = if (index == selected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        axisLabels(points).forEach { label ->
            Text(
                label,
                color = NoduqColors.muted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun axisLabels(points: List<StatsPointDto>): List<String> {
    if (points.size <= 12) return points.map { it.label }
    return points.mapIndexed { index, point ->
        val day = point.label.toIntOrNull()
        if (index == 0 || index == points.lastIndex || (day != null && day % 5 == 0)) point.label else ""
    }
}

private fun selectedIndex(report: StatsDto): Int {
    val peak = report.peak?.key
    val found = report.points.indexOfFirst { it.key == peak }
    return if (found >= 0) found else report.points.lastIndex
}

private fun statsTitle(report: StatsDto, monthName: String): String =
    if (report.grain == "day") "$monthName ${report.year} — Pagos por día" else "${report.year} — Pagos por mes"

private fun valueTitle(report: StatsDto, monthName: String): String =
    if (report.grain == "day") "$monthName ${report.year} — Valor por día" else "${report.year} — Valor por mes"

private val ValueCyan = Color(0xFF00F2FE)
private val SoftCyan = Color(0xFFD7F8FC)
private val Emerald = Color(0xFF34D399)
private val Amber = Color(0xFFFBBF24)
private val Violet = Color(0xFFC084FC)
private val Rose = Color(0xFFF87171)

private data class ChangeTone(val color: Color, val badge: Color, val down: Boolean)

private fun changeTone(percent: Double?): ChangeTone? = when {
    percent == null || percent == 0.0 -> null
    percent > 0 -> ChangeTone(Emerald, Emerald.copy(alpha = 0.10f), down = false)
    else -> ChangeTone(Rose, Rose.copy(alpha = 0.10f), down = true)
}

private fun compareValue(percent: Double?): String =
    if (percent == null) "Sin pagos el periodo anterior" else signedPercent(percent)

private fun extremeValue(point: StatsPointDto?): String {
    if (point == null) return "—"
    return "${point.detail} · ${copLabel(point.amount)}"
}

private fun signedPercent(value: Double): String {
    val tenths = (value * 10.0).roundToInt() / 10.0
    val sign = if (tenths > 0) "+" else ""
    val text = if (tenths % 1.0 == 0.0) tenths.toInt().toString() else tenths.toString().replace('.', ',')
    return "$sign$text%"
}

private fun decimalLabel(value: Double): String {
    val tenths = (value * 10.0).roundToInt() / 10.0
    val whole = tenths.toLong()
    val frac = kotlin.math.abs((tenths * 10).roundToInt() % 10)
    return if (frac == 0) groupedInt(whole) else "${groupedInt(whole)},$frac"
}
