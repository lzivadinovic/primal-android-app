package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.utils.isPrimalIdentifier
import net.primal.android.theme.AppTheme

@Composable
fun AvatarThumbnailsRow(
    modifier: Modifier = Modifier,
    avatarUrls: List<Any?>,
    authorInternetIdentifiers: List<String?>,
    onClick: () -> Unit,
) {
    val avatarVisibleWidth = 24.dp
    BoxWithConstraints(modifier = modifier) {
        val maxAvatars = (maxWidth.value / avatarVisibleWidth.value).toInt() - 2
        val avatarsSize = avatarUrls.size
        val avatarsToRender = avatarsSize.coerceAtMost(maxAvatars)
        val avatarsOverflowCount = avatarsSize - avatarsToRender

        avatarUrls.take(avatarsToRender).forEachIndexed { index, imageUrl ->
            AvatarSpacer(width = (index * avatarVisibleWidth.value).dp) {
                AvatarThumbnailListItemImage(
                    modifier = Modifier.size(32.dp),
                    source = imageUrl,
                    hasBorder = true,
                    borderGradientColors = when (authorInternetIdentifiers[index].isPrimalIdentifier()) {
                        true -> listOf(
                            AppTheme.extraColorScheme.brand1,
                            AppTheme.extraColorScheme.brand2,
                        )
                        false -> listOf(
                            Color.White,
                            Color.White,
                        )
                    },
                    onClick = onClick,
                )
            }
        }

        if (avatarsToRender < avatarsSize) {
            AvatarSpacer(width = (avatarsToRender * avatarVisibleWidth.value).dp) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .adjustAvatarBackground(
                            size = 48.dp,
                            hasBorder = true,
                            borderGradientColors = listOf(
                                Color.White,
                                Color.White
                            ),
                        )
                        .background(color = Color(0xFFC8C8C8))
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+${avatarsOverflowCount.coerceAtMost(99)}",
                        fontSize = 12.sp,
                        style = AppTheme.typography.bodySmall,
                        color = Color(0xFF111111),
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarSpacer(
    width: Dp,
    content: @Composable () -> Unit,
) {
    Row {
        Spacer(
            modifier = Modifier
                .height(10.dp)
                .width(width)
        )

        content()
    }
}