package io.github.DKICooked.gameLogic.generationLogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;

public class LevelGenerator {

    // These constants should match your Player physics capabilities
    private static final float MAX_JUMP_HEIGHT = 170f;
    private static final float MIN_JUMP_HEIGHT = 130f;
    private static final float MAX_JUMP_WIDTH = 250f;

    private static final float SCREEN_WIDTH = 800f;
    private static final float MARGIN = 100f; // Keep platforms away from screen edges

    // Track the "Anchor" from the previous chunk to ensure continuity
    private float lastX = SCREEN_WIDTH / 2;
    private float lastY = 100f;

    /**
     * Generates a new chunk of platforms.
     * @param chunkYStart The world Y coordinate where this chunk begins.
     * @param chunkHeight The vertical size of the chunk.
     * @return An array of Platforms forming a guaranteed path.
     */
    public Array<Platform> generateChunk(float chunkYStart, float chunkHeight) {
        Array<Platform> platforms = new Array<>();

        // IF THIS IS THE VERY BEGINNING, ADD A FLOOR
        if (chunkYStart == 0) {
            // A wide, flat platform at the very bottom
            platforms.add(new Platform(0, 50, 800, 50));
            lastX = 400;
            lastY = 50;
        }

        float targetChunkTop = chunkYStart + chunkHeight;

        while (lastY < targetChunkTop - 100f) {

            // 1. Calculate the 'Jump' to the next platform
            float jumpX = MathUtils.random(-MAX_JUMP_WIDTH, MAX_JUMP_WIDTH);
            float jumpY = MathUtils.random(MIN_JUMP_HEIGHT, MAX_JUMP_HEIGHT);

            float nextX = MathUtils.clamp(lastX + jumpX, MARGIN, SCREEN_WIDTH - MARGIN);
            float nextY = lastY + jumpY;

            // 2. Randomize Platform visual properties
            float width = MathUtils.random(100f, 200f);
            float slope = MathUtils.random(-25f, 25f); // The vertical tilt

            // 3. Create the Platform line (Left Point to Right Point)
            float x1 = nextX - (width / 2);
            float x2 = nextX + (width / 2);
            float y1 = nextY - (slope / 2);
            float y2 = nextY + (slope / 2);

            platforms.add(new Platform(x1, y1, x2, y2));

            // 4. Update Anchor for the next iteration
            lastX = nextX;
            lastY = nextY;
        }

        // Optional: Add a few "Decoy" platforms that aren't part of the main path
        addDecoys(platforms, chunkYStart, chunkHeight);

        return platforms;
    }

    private void addDecoys(Array<Platform> platforms, float yStart, float height) {
        for (int i = 0; i < 3; i++) {
            float dx = MathUtils.random(MARGIN, SCREEN_WIDTH - MARGIN);
            float dy = MathUtils.random(yStart, yStart + height);
            float dw = MathUtils.random(80, 120);
            platforms.add(new Platform(dx - dw/2, dy, dx + dw/2, dy + MathUtils.random(-30, 30)));
        }
    }
}
