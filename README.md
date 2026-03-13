# 🌾 Pix'Farm

> **Un jeu de gestion de ferme pixel-art développé en Java / JavaFX**  
> Plantez, récoltez, élevez des animaux et accomplissez des quêtes pour devenir le meilleur fermier !

---

## 📸 Aperçu

```
┌─────────────────────────────────────────────────────┐
│  Niveau 3        10 000 $        ☀️ Soleil (×1)      │
├──────────┬──────────────────────────────────────────┤
│          │  🌱  🌿  🌾  🔒  🔒  🔒  🔒  🔒  🔒  🔒  │
│ NAVIGATION│  🔒  🔒  🔒  🔒  🔒  🔒  🔒  🔒  🔒  🔒  │
│          │                                          │
│ 🛒 Marchand│                                          │
│ 🎒 Sac    │                                          │
│ 📋 Quêtes │                                          │
│ 🏠 Grange │                                          │
│          │                                          │
│ 💾 Save   │                                          │
└──────────┴──────────────────────────────────────────┘
│  🌾 Blé   🥕 Carotte  🥔 Patate  🍅 Tomate  ...      │
└─────────────────────────────────────────────────────┘
```

---

## 🚀 Lancement

### Prérequis

| Outil | Version minimale |
|-------|-----------------|
| Java (JDK) | 17 |
| JavaFX SDK | 17 |
| IntelliJ IDEA | 2022+ (recommandé) |

### Installation

1. Cloner ou télécharger le projet
2. Ouvrir dans **IntelliJ IDEA** : `File → Open → ProjetFarmbyFarm`
3. Configurer JavaFX dans les dépendances du module
4. Vérifier que `src` est marqué comme **Resources Root** (`File → Project Structure → Modules`)
5. **Build → Rebuild Project** pour copier les assets
6. Lancer `FarmView.MainApp`

---

## 🎮 Gameplay

### Menu principal

Au lancement, trois **slots de sauvegarde** sont disponibles. Chaque slot peut être chargé, créé ou supprimé indépendamment.

### Boucle de jeu

```
Acheter des graines (Marchand)
        ↓
Sélectionner une graine (barre du bas)
        ↓
Cliquer sur une parcelle débloquée → plantation
        ↓
Attendre la croissance (3 stades visuels)
        ↓
Cliquer sur la parcelle prête (✅) → récolte
        ↓
Vendre ou livrer les récoltes (Quêtes)
```

### Débloquer des parcelles

La grille de départ est **7 × 10**. Seule la case (0,0) est débloquée au départ.  
Cliquer sur la case 🛒 affiche le prix de la prochaine parcelle. Le coût augmente à chaque achat.

---

## 🌦️ Météo

La météo change automatiquement toutes les **60 secondes** et affecte la vitesse de croissance.

| Météo | Multiplicateur | Effet visuel |
|-------|:--------------:|--------------|
| ☀️ Soleil | ×1.0 | Normal |
| 🌧️ Pluie | ×1.5 | Teinte bleue |
| ⚡ Orage | ×2.0 | Teinte violette |
| 🔥 Sécheresse | ×0.5 | Teinte chaude délavée |

---

## 🌱 Cultures

Chaque culture a 3 stades de croissance visuels. Le `growthTime` est en **ticks** (1 tick = 700 ms).

| Culture | Stades | Ticks | Prix d'achat | Prix de vente |
|---------|:------:|:-----:|:------------:|:-------------:|
| 🌾 Blé | 3 | 2 | 5 $ | 15 $ |
| 🥕 Carotte | 3 | 5 | 180 $ | 400 $ |
| 🥔 Patate | 3 | 8 | 850 $ | 2 100 $ |
| 🍅 Tomate | 3 | 10 | 3 500 $ | 6 500 $ |
| 🍋 Citron | 3 | 15 | 15 000 $ | 24 000 $ |
| 🍓 Fraise | 3 | 20 | 60 000 $ | 150 000 $ |
| 🌽 Maïs | 3 | 25 | 250 000 $ | 575 000 $ |
| 🍍 Ananas | 3 | 30 | 950 000 $ | 3 000 000 $ |

---

## 📋 Quêtes

Le **Tableau des Quêtes** génère des commandes aléatoires de récoltes.

- Chaque quête demande une quantité d'un crop spécifique
- La récompense (argent + XP) est calculée selon votre niveau
- Refuser ou livrer toutes les quêtes déclenche un **cooldown de 5 minutes**
- Le timer s'affiche en direct dans l'interface

---

## 🐄 Animaux *(Niveau 5 requis)*

La **Grange** se débloque au niveau 5. Vous pouvez y acheter et gérer des animaux.

| Animal | Produit | Nourriture |
|--------|---------|-----------|
| 🐔 Poulet | Œuf | Blé |
| 🐄 Vache | Lait | — |
| 🐷 Cochon | — | — |
| 🐑 Mouton | Laine | — |

---

## 📈 Progression

- Chaque récolte rapporte **1 000 XP**
- Les quêtes rapportent **20 × niveau XP**
- L'XP nécessaire pour monter de niveau augmente de **×1.5** à chaque level up
- Le niveau débloque des fonctionnalités (Grange au niveau 5)

---

## 💾 Sauvegardes

Les sauvegardes sont stockées dans le dossier `saves/` à la racine du projet :

```
saves/
├── save1.txt
├── save2.txt
└── save3.txt
```

La sauvegarde est manuelle via le bouton **💾 Sauvegarder** dans la sidebar.

---

## 🗂️ Structure du projet

```
src/
├── Farm/                        # Modèles métier
│   ├── Culture.java             # Classe abstraite culture
│   ├── Crops/                   # Blé, Carotte, Patate, Tomate, Citron, Fraise, Maïs, Ananas
│   ├── Plot.java                # Parcelle de terrain
│   ├── Farms.java               # Ferme principale (état global)
│   ├── Inventory.java           # Inventaire
│   ├── Quest.java               # Quête
│   ├── Animals.java             # Classe abstraite animaux
│   ├── Animal/                  # Chicken, Cow, Pig, Sheep
│   └── Enclosure/               # Gestion des enclos
│
├── FarmEngine/                  # Moteur de jeu
│   ├── GameTimer.java           # Timer JavaFX (ticks, météo, marché)
│   └── SaveSystem.java          # Lecture/écriture des sauvegardes
│
├── FarmController/              # Contrôleurs JavaFX (MVC)
│   ├── MainController.java      # Écran principal
│   ├── MenuController.java      # Menu & sélection de slot
│   ├── SaveChoiceController.java
│   ├── StoreController.java     # Boutique
│   ├── QuestController.java     # Tableau des quêtes
│   ├── InventoryController.java
│   ├── BarnController.java      # Grange
│   └── AnimalShopController.java
│
├── FarmView/                    # Point d'entrée + CSS
│   ├── MainApp.java             # Application JavaFX
│   ├── main.css                 # Style principal (pixel-art bois/or)
│   ├── menu.css                 # Style menu
│   ├── savechoice.css           # Style sélection de slot
│   └── quest.css                # Style tableau des quêtes
│
├── FXML/                        # Vues FXML
│   ├── MainView.fxml
│   ├── MenuView.fxml
│   ├── SaveChoiceView.fxml
│   ├── QuestView.fxml
│   ├── StoreView.fxml
│   ├── InventoryView.fxml
│   ├── BarnView.fxml
│   └── AnimalShopView.fxml
│
└── Sprite/images/               # Assets graphiques
    ├── Icons/                   # Icônes des cultures (barre du bas)
    ├── Grow/                    # Sprites de croissance par culture (3 stades)
    ├── Fonds + Graphics/        # Textures (herbe, sol, menu, logo)
    ├── Sun.png / Rain.png / Storm.png / Drought.png   # Icônes météo
    └── ...
```

---

## 🛠️ Architecture technique

Le projet suit le pattern **MVC** :

- **Model** → `Farm/` : toute la logique métier, sans dépendance JavaFX
- **View** → `FXML/` + `FarmView/*.css` : structure et style des écrans
- **Controller** → `FarmController/` : liaison entre la vue et le modèle

Le `GameTimer` utilise un `Timeline` JavaFX avec trois boucles indépendantes :

```
Timeline (700 ms) → tick() → growing() sur toutes les cultures + update UI
Timeline (60 s)   → updateWeather() + update UI
Timeline (30 s)   → updateMarketFluctuation() + update UI
```

---

## 👤 Auteur

Projet réalisé par **AlexisM**  
*Pix'Farm — Farm My Farm*
