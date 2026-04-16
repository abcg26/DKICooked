# To Your Infinity

**To Your Infinity** is a procedurally generated, single-player desktop platformer built using **Java** and the **libGDX** framework. The project serves as a practical demonstration of how concepts from Data Structures and Algorithms (DSA) such as sorting algorithms, collision detection, and procedural generation are integrated into real-world interactive software.

## Overview
In this "Donkey Kong/Jump King" inspired climber, players choose from three unique characters to ascend as high as possible. The game challenges not only the player's reflexes but also their ability to adapt to "Anomalies" that disrupt gameplay.

## Key Technical Features (DSA Integration)

### 1. Leaderboard & Sorting
- **Algorithm:** Insertion Sort.
- **Why:** Since the high-score list is "nearly sorted," Insertion Sort allows for $O(n)$ efficiency when inserting a single new score at the end of a session, ensuring zero lag.

### 2. Procedural Level Generation
- **Logic:** The game world is generated in "chunks" using random themes.
- **Reachability Check:** A custom algorithm calculates the player's maximum jump height and width before placing platforms, ensuring every level is mathematically beatable.

### 3. Collision Detection
- **Hybrid System:** - **Circle-based:** Used for asteroids and UFOs for precise circular overlap.
  - **AABB (Rectangular) + `getSurfaceY`:** Used for platforms to ensure solid landing mechanics and prevent sprite clipping.

### 4. Game Logic Managers
- **Anomaly Manager:** Handles "Magnetic Storms" (control inversion), "UFO Raids," and "Asteroid Rain."
- **Difficulty Scaling:** A state-based manager that increases spawn rates and obstacle speed as the player climbs higher.

## Gameplay Features
- **Characters:**
  - **Alaine:** Zero Gravity (slower fall, higher jump).
  - **Jerick:** Ninja (Double Jump).
  - **Timothy:** Power (Highest single jump).
- **Power-ups:**
  - **Ghost:** Pass through platforms.
  - **UFO:** Automated vertical transport.
- **Anomalies:** Random events including control inversion and projectile hazards.

## Tech Stack
- **Language:** Java
- **Framework:** libGDX
- **Tools:** Gradle for dependency management, ShapeRenderer for visual effects.

## Significance
This project is designed as an educational resource for:
- **Students:** To see abstract DSA concepts in a tangible environment.
- **Educators:** As a visual aid for teaching algorithm efficiency and procedural logic.
- **Developers:** As a reference for game state management and physics implementation in Java.

## Installation & Setup
1. Navigate to the Releases section of this repository.
2. Download ToYourInfinity-1.0.exe.
3. Run the installer and follow the Setup Wizard instructions.
4. Launch the game via the desktop shortcut or the Start menu.

---
*Developed as a study on the practical application of Data Structures and Algorithms in Game Development.*
