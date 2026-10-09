# Documentation Technique - Application Taxi Paris

## 1. Présentation Générale
L'application **Taxi Paris** est un outil professionnel conçu pour les chauffeurs de taxi et VTC parisiens. Elle permet de centraliser les informations critiques pour l'optimisation des trajets : temps d'attente aux aéroports (CDG/Orly), état du trafic routier en temps réel, perturbations sur le réseau RATP/SNCF, et météo locale.

## 2. Fonctionnalités Principales

### 2.1. Suivi des Aéroports
- **Temps d'attente réels** : Récupérés directement depuis les données de Paris Aéroport pour CDG et Orly.
- **Alertes personnalisables** : Notifications automatiques lorsque le temps d'attente dépasse un seuil défini par l'utilisateur (ex: > 30 min).
- **Temps de trajet & Trafic** : Calcul du temps de route depuis la position actuelle vers les aéroports avec indicateur de fluidité (Vert/Orange/Rouge).

### 2.2. Réseau de Transport (Métro/RER)
- **Tableau de bord du trafic** : État en temps réel de toutes les lignes de métro et RER.
- **Gestion des incidents** : Affichage détaillé des messages d'information et de la sévérité des perturbations (Bloquante, Incident, Normal).
- **Filtrage des lignes** : Possibilité de ne suivre que les lignes pertinentes pour l'activité du chauffeur.

### 2.3. Widgets Intelligents
- **Widgets Glance** : Plusieurs widgets pour l'écran d'accueil (CDG, Orly, Métro).
- **Personnalisation** : Opacité réglable (jusqu'à la transparence totale), taille de police ajustable, et choix des couleurs de texte.

### 2.4. Optimisation Batterie (Smart Refresh)
- **Géofencing logiciel** : L'application réduit la fréquence de rafraîchissement si l'utilisateur est éloigné de la zone Île-de-France ou d'un rayon défini, afin de préserver la batterie.

---

## 3. Architecture Technique

L'application suit les principes de la **Clean Architecture** simplifiée avec un pattern **MVVM** (Model-View-ViewModel).

### 3.1. Composants Clés
- **UI (Jetpack Compose)** : Interface moderne et réactive.
- **ViewModel** : Gestion de l'état UI et logique métier.
- **Repository** : Point d'accès unique aux données (Réseau + Local).
- **DataStore (Preferences)** : Source de vérité unique (Single Source of Truth) stockant l'état global de l'application et les préférences.
- **WorkManager** : Gestion des tâches de fond (UpdateWorker) pour mettre à jour les données même quand l'application est fermée.

### 3.2. Flux de Données
1. Le `UpdateWorker` récupère les données via les APIs.
2. Les données sont persistées dans `TaxiPrefs` (DataStore).
3. Le `MainViewModel` observe le flux `taxiState` du DataStore.
4. L'interface Compose se met à jour automatiquement.

---

## 4. Sources de Données (APIs & Services)

| Service | Source | Usage |
| :--- | :--- | :--- |
| **Taxi Wait Times** | Paris Aéroport (Scraping) | Temps d'attente CDG/Orly |
| **Trafic Métro/RER** | PRIM (Île-de-France Mobilités) | État des lignes et incidents |
| **Météo** | Open-Meteo | Température et conditions à Paris |
| **Routage & Trafic** | OSRM / Waze | Temps de trajet et état de la route |
| **Localisation** | Google Play Services | Calcul de distance et Smart Refresh |

---

## 5. Guide de Conformité et Légalité

### 5.1. Respect des Normes de Commercialisation (Google Play Store)
- **Permissions** : L'accès à la localisation (`ACCESS_FINE_LOCATION`) est justifié par le calcul des temps de trajet et le Smart Refresh. Une interface d'onboarding explique clairement cet usage avant la demande système.
- **Accessibilité** : L'application utilise les `semantics` de Compose pour garantir la compatibilité avec TalkBack.
- **Performance** : L'utilisation de `WorkManager` respecte les contraintes Android sur les tâches de fond et la gestion de l'énergie.
- **Design** : Interface Material 3 respectant les guidelines Android.

### 5.2. Analyse de Légalité
- **Contenu** : L'application ne contient aucun contenu illégal, haineux, ou protégé par des droits d'auteur de tiers sans autorisation (les logos officiels sont remplacés par des éléments graphiques génériques).
- **Données Personnelles (RGPD)** : 
    - Les données de localisation sont traitées localement pour les calculs de distance.
    - Aucune donnée personnelle n'est envoyée vers un serveur tiers à des fins de tracking publicitaire.
    - **Note importante** : Pour une publication commerciale, une Politique de Confidentialité est obligatoire (lien à fournir dans la console Google Play).

### 5.3. Points de Vigilance (Propriété Intellectuelle)
- **Scraping** : La récupération des données sur `infotaxi.parisaeroport.fr` est effectuée par parsing HTML. Bien que toléré pour un usage restreint, il est recommandé pour une exploitation commerciale à grande échelle de vérifier les conditions d'utilisation de Paris Aéroport ou de solliciter un accès API officiel si disponible.
- **API PRIM** : L'utilisation des données IDFM nécessite une clé API (présente dans le code : `KfWoIx4QJ3wbpJwgiqPY4ALTIyJS4Qpm`). Assurez-vous que cette clé respecte les quotas de votre compte développeur IDFM.

## 6. Installation et Build

1. Cloner le projet.
2. Ouvrir avec **Android Studio Ladybug (ou plus récent)**.
3. Synchroniser Gradle.
4. Build & Run sur un appareil physique (recommandé pour la localisation) ou un émulateur avec Play Services.

---
**Verdict de conformité** : L'application est structurellement saine, respecte les bonnes pratiques de développement Android modernes et ne présente aucun caractère illégal manifeste. Elle est prête pour une phase de test de pré-production avant soumission sur les stores.
