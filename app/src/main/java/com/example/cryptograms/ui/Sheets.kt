package com.example.cryptograms.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptograms.R
import com.example.cryptograms.data.DifficultyStats
import com.example.cryptograms.game.GameState
import com.example.cryptograms.ui.theme.Palette
import com.example.cryptograms.ui.theme.SerifFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsSheet(
    state: GameState,
    stats: DifficultyStats,
    onDismiss: () -> Unit,
    onNewPuzzle: () -> Unit,
    onRevealSolution: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Palette.Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(if (state.isSolved) R.string.solved_title else R.string.in_progress_title),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                color = Palette.TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(difficultyLabel(state.puzzle.difficulty), color = Palette.TextSecondary, fontSize = 15.sp)

            if (state.isSolved) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "“${state.puzzle.originalText}”",
                    fontFamily = SerifFamily,
                    fontSize = 19.sp,
                    color = Palette.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "— ${state.puzzle.author}",
                    color = Palette.TextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat(stringResource(R.string.result_time), formatTime(state.elapsedSeconds))
                Stat(stringResource(R.string.result_mistakes), "${state.mistakes}")
                Stat(stringResource(R.string.result_hints), "${state.hintsUsed}")
                if (!state.isSolved) {
                    Stat(
                        stringResource(R.string.result_progress),
                        "${state.solvedLetters}/${state.puzzle.cipherLetters.size}",
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Palette.Border)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat(stringResource(R.string.result_solved_count), "${stats.solved}")
                Stat(stringResource(R.string.result_best), stats.bestSeconds?.let(::formatTime) ?: "—")
            }
            Spacer(Modifier.height(26.dp))
            PrimaryButton(stringResource(R.string.new_puzzle), onNewPuzzle)
            if (!state.isSolved) {
                TextButton(onClick = onRevealSolution, modifier = Modifier.padding(top = 6.dp)) {
                    Text(stringResource(R.string.give_up), color = Palette.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Palette.TextPrimary)
        Text(label, fontSize = 13.sp, color = Palette.TextSecondary)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Palette.TextPrimary,
            contentColor = Palette.Background,
        ),
    ) {
        Text(text, fontSize = 17.sp)
    }
}

private data class TutorialPage(val icon: ImageVector, val title: Int, val text: Int)

private val tutorialPages = listOf(
    TutorialPage(Icons.Outlined.Key, R.string.tut_goal_title, R.string.tut_goal_text),
    TutorialPage(Icons.Outlined.TouchApp, R.string.tut_select_title, R.string.tut_select_text),
    TutorialPage(Icons.Filled.SwapHoriz, R.string.tut_cipher_title, R.string.tut_cipher_text),
    TutorialPage(Icons.Outlined.Restore, R.string.tut_mistakes_title, R.string.tut_mistakes_text),
    TutorialPage(Icons.Outlined.Lightbulb, R.string.tut_hints_title, R.string.tut_hints_text),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HowToPlaySheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pager = rememberPagerState { tutorialPages.size }
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.how_to_play_title),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Palette.TextPrimary,
            )
            HorizontalDivider(
                color = Palette.Border,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            HorizontalPager(
                state = pager,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.padding(top = 24.dp),
            ) { index ->
                val page = tutorialPages[index]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Palette.SurfaceHigh)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(page.icon, null, tint = Palette.TextPrimary, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(page.title), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Palette.TextPrimary)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(page.text),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Palette.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(tutorialPages.size) { i ->
                    val active = i == pager.currentPage
                    Box(
                        Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) Palette.TextPrimary else Palette.TextMuted)
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.padding(horizontal = 24.dp)) {
                val last = pager.currentPage == tutorialPages.lastIndex
                PrimaryButton(stringResource(if (last) R.string.got_it else R.string.next)) {
                    if (last) {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    } else {
                        scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }
                }
            }
        }
    }
}
