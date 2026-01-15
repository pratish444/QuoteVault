package com.yourcompany.quotevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourcompany.quotevault.domain.model.Quote
import com.yourcompany.quotevault.utils.CardStyle

@Composable
fun ShareableQuoteCard(
    quote: Quote,
    style: CardStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                when (style) {
                    CardStyle.MODERN -> Modifier.background(Color(0xFFF5F5F5))
                    CardStyle.CLASSIC -> Modifier.background(Color(0xFFFFF8E1))
                    CardStyle.MINIMAL -> Modifier.background(Color.White)
                    CardStyle.DARK -> Modifier.background(Color(0xFF212121))
                    CardStyle.PASTEL -> Modifier.background(Color(0xFFE1F5FE))
                }
            )
            .then(
                if (style == CardStyle.MODERN || style == CardStyle.CLASSIC) {
                    Modifier.border(
                        width = 4.dp,
                        color = if (style == CardStyle.MODERN) Color(0xFF6366F1) else Color(0xFF8B4513),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            QuoteTextContent(quote = quote, style = style)
            Spacer(modifier = Modifier.height(32.dp))
            BrandingText(style = style)
        }
    }
}

@Composable
private fun QuoteTextContent(
    quote: Quote,
    style: CardStyle
) {
    val textColor = when (style) {
        CardStyle.DARK -> Color.White
        CardStyle.CLASSIC -> Color(0xFF5D4037)
        CardStyle.MINIMAL -> Color.Black
        CardStyle.MODERN -> Color(0xFF212121)
        CardStyle.PASTEL -> Color(0xFF01579B)
    }

    val fontFamily = if (style == CardStyle.CLASSIC) {
        androidx.compose.ui.text.font.FontFamily.Serif
    } else {
        androidx.compose.ui.text.font.FontFamily.SansSerif
    }

    Column {
        Text(
            text = "\"${quote.text}\"",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 8,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "— ${quote.author}",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
            fontFamily = fontFamily,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BrandingText(style: CardStyle) {
    val textColor = when (style) {
        CardStyle.DARK -> Color.White.copy(alpha = 0.6f)
        CardStyle.CLASSIC -> Color(0xFF5D4037).copy(alpha = 0.5f)
        CardStyle.MINIMAL -> Color.Black.copy(alpha = 0.5f)
        CardStyle.MODERN -> Color(0xFF212121).copy(alpha = 0.5f)
        CardStyle.PASTEL -> Color(0xFF01579B).copy(alpha = 0.5f)
    }

    Text(
        text = "Shared via QuoteVault",
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun ModernQuoteCardPreview() {
    ShareableQuoteCard(
        quote = Quote(
            id = "1",
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs",
            category = "Motivation",
            source = ""
        ),
        style = CardStyle.MODERN
    )
}

@Preview(showBackground = true)
@Composable
fun ClassicQuoteCardPreview() {
    ShareableQuoteCard(
        quote = Quote(
            id = "2",
            text = "In the middle of every difficulty lies opportunity.",
            author = "Albert Einstein",
            category = "Wisdom",
            source = ""
        ),
        style = CardStyle.CLASSIC
    )
}

@Preview(showBackground = true)
@Composable
fun MinimalQuoteCardPreview() {
    ShareableQuoteCard(
        quote = Quote(
            id = "3",
            text = "Simplicity is the ultimate sophistication.",
            author = "Leonardo da Vinci",
            category = "Wisdom",
            source = ""
        ),
        style = CardStyle.MINIMAL
    )
}

@Preview(showBackground = true)
@Composable
fun DarkQuoteCardPreview() {
    ShareableQuoteCard(
        quote = Quote(
            id = "4",
            text = "The darkest nights produce the brightest stars.",
            author = "Unknown",
            category = "Motivation",
            source = ""
        ),
        style = CardStyle.DARK
    )
}

@Preview(showBackground = true)
@Composable
fun PastelQuoteCardPreview() {
    ShareableQuoteCard(
        quote = Quote(
            id = "5",
            text = "Keep your face always toward the sunshine—and shadows will fall behind you.",
            author = "Walt Whitman",
            category = "Motivation",
            source = ""
        ),
        style = CardStyle.PASTEL
    )
}