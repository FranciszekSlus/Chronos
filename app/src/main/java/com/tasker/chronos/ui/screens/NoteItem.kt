package com.tasker.chronos.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import com.tasker.chronos.ui.components.GlassCard
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteItem(
    note: Note,
    category: NoteCategory?,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (note.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Przypieta",
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (note.isLocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Zablokowana",
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (note.content.isNotBlank() || note.isLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                val preview = if (note.isLocked) {
                    "Notatka zabezpieczona haslem"
                } else {
                    val plain = note.content
                        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
                        .replace(Regex("<[^>]*>"), "")
                        .replace("&nbsp;", " ")
                        .replace("&amp;", "&")
                        .trim()
                    plain.take(100) + if (plain.length > 100) "..." else ""
                }
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (category != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = category.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(android.graphics.Color.parseColor(category.color))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.updatedAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
