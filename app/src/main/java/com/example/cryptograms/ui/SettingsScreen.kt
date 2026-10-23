package com.example.cryptograms.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptograms.R
import com.example.cryptograms.data.DifficultyStats
import com.example.cryptograms.data.Settings
import com.example.cryptograms.game.Difficulty
import com.example.cryptograms.game.Language
import com.example.cryptograms.ui.theme.Palette
import com.example.cryptograms.ui.theme.SerifFamily

@Composable
fun SettingsScreen(
    settings: Settings,
    stats: Map<Difficulty, DifficultyStats>,
    onBack: () -> Unit,
    onUpdate: ((Settings) -> Settings) -> Unit,
    onLanguage: (Language) -> Unit,
    onResetStats: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(1) }
    var showHelp by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.settings),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 25.sp,
                color = Palette.TextPrimary,
            )
        }
        HorizontalDivider(color = Palette.Border)
        TabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            contentColor = Palette.TextPrimary,
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(positions[tab]),
                    height = 4.dp,
                    color = Palette.TextSecondary,
                )
            },
            divider = { HorizontalDivider(color = Palette.Border) },
        ) {
            listOf(R.string.tab_global, R.string.tab_cryptograms).forEachIndexed { i, label ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = {
                        Text(
                            stringResource(label),
                            fontSize = 17.sp,
                            color = if (tab == i) Palette.TextPrimary else Palette.TextSecondary,
                        )
                    },
                    modifier = Modifier.height(60.dp),
                )
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            if (tab == 0) {
                Section(stringResource(R.string.section_quotes))
                RowFrame(
                    icon = Icons.Outlined.Translate,
                    title = stringResource(R.string.quote_language),
                    subtitle = stringResource(R.string.quote_language_desc),
                    divider = false,
                    onClick = null,
                ) {
                    LanguagePicker(settings.language, onLanguage)
                }
                Section(stringResource(R.string.section_display))
                SwitchRow(
                    icon = Icons.Outlined.Timer,
                    title = stringResource(R.string.show_timer),
                    subtitle = stringResource(R.string.show_timer_desc),
                    checked = settings.showTimer,
                    onChange = { v -> onUpdate { it.copy(showTimer = v) } },
                    divider = true,
                )
                SwitchRow(
                    icon = Icons.Outlined.Vibration,
                    title = stringResource(R.string.haptics),
                    subtitle = stringResource(R.string.haptics_desc),
                    checked = settings.haptics,
                    onChange = { v -> onUpdate { it.copy(haptics = v) } },
                )
                Section(stringResource(R.string.section_statistics))
                Difficulty.entries.forEach { d ->
                    val s = stats[d] ?: DifficultyStats()
                    InfoRow(
                        icon = Icons.Outlined.EmojiEvents,
                        title = difficultyLabel(d),
                        subtitle = if (s.solved == 0) {
                            stringResource(R.string.stats_none)
                        } else {
                            stringResource(R.string.stats_row, s.solved, s.bestSeconds?.let(::formatTime) ?: "—")
                        },
                    )
                }
                TextButton(onClick = onResetStats, modifier = Modifier.padding(start = 48.dp)) {
                    Text(stringResource(R.string.reset_stats), color = Palette.RedText)
                }
            } else {
                Section(stringResource(R.string.section_assistance))
                SwitchRow(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.show_mistakes),
                    subtitle = stringResource(R.string.show_mistakes_desc),
                    checked = settings.showMistakes,
                    onChange = { v -> onUpdate { it.copy(showMistakes = v) } },
                    divider = true,
                )
                SwitchRow(
                    icon = Icons.Outlined.Search,
                    title = stringResource(R.string.highlight_matching),
                    subtitle = stringResource(R.string.highlight_matching_desc),
                    checked = settings.highlightMatching,
                    onChange = { v -> onUpdate { it.copy(highlightMatching = v) } },
                )
                Section(stringResource(R.string.section_input))
                SwitchRow(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    title = stringResource(R.string.auto_advance),
                    subtitle = stringResource(R.string.auto_advance_desc),
                    checked = settings.autoAdvance,
                    onChange = { v -> onUpdate { it.copy(autoAdvance = v) } },
                )
                Section(stringResource(R.string.section_help))
                InfoRow(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = stringResource(R.string.how_to_play),
                    subtitle = stringResource(R.string.how_to_play_desc),
                    onClick = { showHelp = true },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showHelp) {
        HowToPlaySheet(onDismiss = { showHelp = false })
    }
}

@Composable
private fun LanguagePicker(selected: Language, onSelect: (Language) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Palette.SurfaceHigh)
            .padding(3.dp),
    ) {
        listOf(Language.ENGLISH to "EN", Language.POLISH to "PL").forEach { (language, label) ->
            val active = language == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (active) Color(0xFFBDBDBA) else Color.Transparent)
                    .clickable { onSelect(language) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (active) Palette.Background else Palette.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        color = Palette.TextSecondary,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    divider: Boolean = false,
) {
    RowFrame(icon = icon, title = title, subtitle = subtitle, divider = divider, onClick = { onChange(!checked) }) {
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Palette.Background,
                checkedTrackColor = Color(0xFFBDBDBA),
                checkedBorderColor = Color(0xFFBDBDBA),
                uncheckedThumbColor = Palette.TextSecondary,
                uncheckedTrackColor = Palette.SurfaceHigh,
                uncheckedBorderColor = Palette.TextMuted,
            ),
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    RowFrame(icon = icon, title = title, subtitle = subtitle, divider = false, onClick = onClick) {}
}

@Composable
private fun RowFrame(
    icon: ImageVector,
    title: String,
    subtitle: String,
    divider: Boolean,
    onClick: (() -> Unit)?,
    trailing: @Composable () -> Unit,
) {
    Column(Modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Palette.TextSecondary, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = Palette.TextPrimary, fontSize = 17.sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = Palette.TextSecondary, fontSize = 14.sp, lineHeight = 19.sp)
            }
            trailing()
        }
        if (divider) {
            HorizontalDivider(color = Palette.Border, modifier = Modifier.padding(start = 64.dp))
        }
    }
}
