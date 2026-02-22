package io.github.DKICooked.gameLogic.generationLogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;

public class LevelGenerator {
    private static final float SCREEN_WIDTH = 800f;
    private static final float MARGIN = 100f;

    // Physical constraints derived from PlayerActor
    private final float MAX_JUMP_HEIGHT = 180f;
    private final float MAX_JUMP_WIDTH = 300f;

    private float lastX = 400f;
    private float lastY = 50f;

    public Array<Platform> generateChunk(float chunkYStart, float chunkHeight) {
        Array<Platform> platforms = new Array<>();
        float chunkTop = chunkYStart + chunkHeight;

        // 1. CONDITIONAL SAFETY NET
        // Only place the wide floor if this is the absolute beginning of the game.
        if (chunkYStart == 0) {
            float netWidth = SCREEN_WIDTH - 40;
            platforms.add(new Platform(20, 50, 20 + netWidth, 50));
            lastX = 400f; // Start player in the center
            lastY = 50f;
        }

        // 2. THE ANCHOR SYSTEM: Generate the "Main Vein"
        while (lastY < chunkTop - 100f) {
            // Calculate the "Reachability Donut"
            // We want to move at least 40% of max height, but no more than 90%
            float jumpY = MathUtils.random(MAX_JUMP_HEIGHT * 0.4f, MAX_JUMP_HEIGHT * 0.9f);
            // Force a minimum horizontal jump distance of 150 pixels
            float jumpX = MathUtils.random(150f, MAX_JUMP_WIDTH * 0.8f);
            if (MathUtils.randomBoolean()) jumpX *= -1; // Randomly go left or right

            float nextX = MathUtils.clamp(lastX + jumpX, MARGIN, SCREEN_WIDTH - MARGIN);
            float nextY = lastY + jumpY;

            // GEOMETRY CLEANUP: 100% horizontal, minimum 1.5x player width
            float width = MathUtils.random(100f, 160f);
            Platform potential = createPlatformFromCenter(nextX, nextY, width, 0);

            // 3. THE HEAD-BONK FILTER
            if (isPathClear(potential, platforms)) {
                platforms.add(potential);
                lastX = nextX;
                lastY = nextY;
            }
        }
        return platforms;
    }

    private boolean isPathClear(Platform newP, Array<Platform> existing) {
        for (Platform other : existing) {
            float xDist = Math.abs(newP.x1 - other.x1);
            float yDist = Math.abs(newP.y1 - other.y1);

            // 1. THE VERTICAL COLUMN RULE
            // If platforms are within 150px horizontally, they are "stacking"
            if (xDist < 150f) {
                // They MUST be at least 200px apart vertically to allow for a jump arc
                if (yDist < 200f) return false;
            }

            // 2. THE DIAGONAL BONK RULE
            // Even if they don't overlap perfectly, if one is just slightly
            // above and to the side, it will catch the player's head.
            if (xDist < 200f && yDist < 160f) {
                return false;
            }
        }
        return true;
    }

    private Platform createPlatformFromCenter(float x, float y, float width, float slope) {
        // Force slope to 0 for geometry cleanup
        return new Platform(x - width/2, y, x + width/2, y);
    }
}
