package moe.rukamori.hikki.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MdSpacing.sm),
) {
    val markdownState =
        rememberMarkdownState(
            markdown,
            retainState = true,
        )
    val scrollState = rememberScrollState()
    val imageTransformer = remember { Coil3ImageTransformerImpl }

    Markdown(
        markdownState = markdownState,
        imageTransformer = imageTransformer,
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(contentPadding),
    )
}
