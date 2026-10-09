package com.taxiparis.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxiparis.app.data.TaxiState
import com.taxiparis.app.model.LineStatus
import com.taxiparis.app.ui.sections.SquareTaxiCard
import com.taxiparis.app.ui.sections.ModernMetroCircle

/**
 * An interactive onboarding tutorial for new users.
 */
@Composable
fun OnboardingTutorial(onFinish: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val steps = listOf(
        OnboardingStep(
            "Taxi Paris Pro", 
            "Optimisez votre rentabilité avec les données de trafic et d'attente en temps réel.", 
            icon = Icons.Rounded.TaxiAlert
        ),
        OnboardingStep(
            "Aéroports & Attente", 
            "Visualisez instantanément le temps d'attente estimé en station à CDG et Orly.",
            content = {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SquareTaxiCard("CDG", 35, 22.5, "28 min", 0, 1, Modifier.weight(1f))
                    SquareTaxiCard("ORL", 15, 18.2, "45 min", 1, 2, Modifier.weight(1f))
                }
            }
        ),
        OnboardingStep(
            "Trafic Intelligent", 
            "Découvrez la fluidité routière vers les aéroports grâce à nos indicateurs colorés.",
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TrafficStatusInfo(Color(0xFF34C759), "Fluide", "Trajet optimal, circulation libre.", textColor)
                    TrafficStatusInfo(Color(0xFFFF9500), "Modéré", "Ralentissements légers, prévoyez +5-10 min.", textColor)
                    TrafficStatusInfo(Color(0xFFFF3B30), "Bouchon", "Circulation difficile, forte attente prévue.", textColor)
                }
            }
        ),
        OnboardingStep(
            "Réseau Ferré", 
            "Restez informé des incidents Métro/RER impactant vos clients.",
            content = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModernMetroCircle(LineStatus("1", "1", "Normal", "", "INFO", false))
                        ModernMetroCircle(LineStatus("4", "4", "Incident", "", "WARNING", true))
                        ModernMetroCircle(LineStatus("14", "14", "Normal", "", "INFO", false))
                        ModernMetroCircle(LineStatus("A", "A", "Bloqué", "", "BLOQUANTE", true))
                    }
                    Surface(
                        color = Color(0xFFFF3B30).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Alerte : Incident majeur sur le RER B",
                            color = Color(0xFFFF3B30),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ),
        OnboardingStep(
            "Widgets de Bureau", 
            "Accédez aux infos cruciales sans même ouvrir l'application. Entièrement personnalisables.",
            content = {
                Box(modifier = Modifier.scale(0.85f).shadow(12.dp, RoundedCornerShape(32.dp))) {
                    WidgetPreview(TaxiState(0, 0, 0L, false, isWidgetTransparent = true))
                }
            }
        ),
        OnboardingStep(
            "Navigation Waze & Maps", 
            "Lancez vos itinéraires en un clic depuis l'application ou le widget avec votre GPS favori.",
            icon = Icons.Rounded.Navigation
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) { it } + 
                     fadeIn(animationSpec = tween(400))) togetherWith 
                    (slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow)) { -it } + 
                     fadeOut(animationSpec = tween(400)))
                } else {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) { -it } + 
                     fadeIn(animationSpec = tween(400))) togetherWith 
                    (slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it } + 
                     fadeOut(animationSpec = tween(400)))
                }
            },
            label = "OnboardingTransition"
        ) { currentStep ->
            val data = steps[currentStep]
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (data.icon != null) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (data.content != null) {
                    Box(modifier = Modifier.heightIn(min = 200.dp), contentAlignment = Alignment.Center) {
                        data.content.invoke()
                    }
                }

                Spacer(Modifier.height(48.dp))
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = secondaryTextColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = { step-- }) {
                    Text("Retour", color = secondaryTextColor)
                }
            } else {
                TextButton(onClick = onFinish) {
                    Text("Passer", color = secondaryTextColor)
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.indices.forEach { i ->
                    val size by animateDpAsState(
                        targetValue = if (i == step) 10.dp else 6.dp,
                        label = "indicatorSize"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(size)
                            .clip(CircleShape)
                            .background(
                                if (i == step) MaterialTheme.colorScheme.primary 
                                else secondaryTextColor.copy(alpha = 0.3f)
                            )
                            .clickable { step = i }
                    )
                }
            }

            Button(
                onClick = {
                    if (step < steps.size - 1) step++ else onFinish()
                },
                shape = CircleShape,
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    if (step < steps.size - 1) Icons.AutoMirrored.Rounded.ArrowForward 
                    else Icons.Rounded.Check, 
                    null
                )
            }
        }
    }
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val content: (@Composable () -> Unit)? = null
)

@Composable
private fun TrafficStatusInfo(color: Color, label: String, desc: String, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(16.dp)) {}
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(desc, color = textColor.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}
