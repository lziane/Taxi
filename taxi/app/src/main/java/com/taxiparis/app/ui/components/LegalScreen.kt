package com.taxiparis.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The legal terms and conditions screen.
 */
@Composable
fun LegalScreen(
    isMandatory: Boolean,
    onAccept: () -> Unit,
    onBack: () -> Unit,
    onRevoke: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Conditions Générales",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            if (!isMandatory) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fermer")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = """
                        ARTICLE 1 : NATURE DU SERVICE ET ACCEPTATION
                        L’Application est un outil d’aide indicatif, indépendant, non affilié aux entités officielles (RATP, IDFM, Groupe ADP, Infotaxi). L’accès est conditionné à l’acceptation expresse des présentes CGU.

                        ARTICLE 2 : EXCLUSION ABSOLUE DE GARANTIE
                        L’application est fournie "en l'état" et "selon disponibilité", sans garantie de continuité, d'exactitude ou de fiabilité des données (Scraping Infotaxi, API PRIM, OSRM).

                        ARTICLE 3 : LIMITATION DE RESPONSABILITÉ
                        Responsabilité limitée au maximum légal (Art. 1231-3 Code Civil). Exclusion de tout dommage indirect : perte de chance, manque à gagner, perte de CA ou retards professionnels.

                        ARTICLE 4 : SÉCURITÉ ROUTIÈRE
                        Le chauffeur est seul responsable de sa conduite et du respect du Code de la Route (Art. R412-6-1). Le développeur décline toute responsabilité en cas d'accident ou d'infraction.

                        ARTICLE 5 : MODIFICATION ET LOI APPLICABLE
                        Le développeur peut modifier les CGU ou couper le service à tout moment. Loi française applicable. Compétence exclusive : Tribunaux de Paris.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isMandatory) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { checked = !checked }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "J'ai lu et j'accepte sans réserve les conditions générales d'utilisation (CGU).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onAccept,
                enabled = checked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Accepter et Continuer", fontWeight = FontWeight.Bold)
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Fermer", fontWeight = FontWeight.Bold)
                }
                
                if (onRevoke != null) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = onRevoke,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retirer mon consentement", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(start = 8.dp)) {
        Text("• ", fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
