package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class AsteroidManager {
    private float asteroidTimer = 0;
    private float raidDuration = 0;
    private boolean isRaidActive = false;
    private Texture asteroidTex;

    private final float CALM_TIME = 3f; // Seconds of peace
    private final float RAID_TIME = 15f; // Seconds of falling rocks

    public AsteroidManager(Texture texture) {
        this.asteroidTex = texture;
    }
    public void update(float delta, float playerHeight, Stage stage) {
        // Only run for the first 3 stages (Stage 1: 0-30m, Stage 2: 30-60m, Stage 3: 60-90m)
        // 9000 pixels is roughly 90 meters in LibGDX world units.
//        if (playerHeight < 1500 || playerHeight > 9000) {
//            // If we aren't in the "Danger Zone," reset the timers so the
//            // raid starts fresh the moment we hit 15m.
//            if (raidDuration != 0) {
//                System.out.println("DEBUG: Outside Asteroid Zone - Resetting Timers");
//                raidDuration = 0;
//                isRaidActive = false;
//            }
//            return;
//        }

        // raidDuration is our master clock for the phase cycle
        raidDuration += delta;

        if (isRaidActive) {
            handleRaid(delta, playerHeight, stage);
        } else {
            handleCalm(delta);
        }
    }

    private void handleRaid(float delta, float playerHeight, Stage stage) {

        if (asteroidTimer == 0 && raidDuration < 0.1f) {
            System.out.println("DEBUG: RAID STARTED! Spawning asteroids...");
        }

        asteroidTimer += delta;

        // --- DYNAMIC DIFFICULTY ---
        // We calculate the spawn interval based on height
        float spawnInterval = 0.6f; // Default (Stage 1)

        if (playerHeight > 6000) {
            spawnInterval = 0.25f; // Stage 3: Rapid fire
        } else if (playerHeight > 3000) {
            spawnInterval = 0.4f;  // Stage 2: Moderate
        }

        // Time to drop a rock?
        if (asteroidTimer >= spawnInterval) {
            spawnAsteroid(stage);
            asteroidTimer = 0;
        }

        // Switch to CALM after 15 seconds
        if (raidDuration >= RAID_TIME) {
            isRaidActive = false;
            raidDuration = 0; // Reset clock for the next phase
            asteroidTimer = 0; // Reset spawn timer so it's fresh for next raid
        }
    }

    private void handleCalm(float delta) {
        // Switch to RAID after 10 seconds
        if (raidDuration >= CALM_TIME) {
            isRaidActive = true;
            raidDuration = 0; // Reset clock for the next phase
        }
    }

    private void spawnAsteroid(Stage stage) {
        // Spawn mostly on the right side (from 400 to 1000px)
        // Spawning at 800+ means they slide into view from the side!
        float randomX = MathUtils.random(400, 1000);

        float cameraY = stage.getCamera().position.y;
        float screenHeight = stage.getViewport().getWorldHeight();

        // Spawn a bit higher up to give them room to slide in
        float spawnY = cameraY + (screenHeight / 2f) + 200f;

        AsteroidActor asteroid = new AsteroidActor(asteroidTex, randomX, spawnY);
        stage.addActor(asteroid);
    }

    // --- GETTERS ---
    // Use this in GameScreen to trigger a "WARNING" label on your UI
    public boolean isRaidActive() {
        return isRaidActive;
    }
}
