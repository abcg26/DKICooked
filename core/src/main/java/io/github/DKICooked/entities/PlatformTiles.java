package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlatformTiles {
    private Texture tileTexture;
    private float tileSize = 16f;

    public  PlatformTiles (Texture tileTexture) {
       this.tileTexture = tileTexture;
    }

    public void render(SpriteBatch batch, Platform platform) {
        float width = Math.abs(platform.x2 - platform.x1);
        float height = Math.abs(platform.y2 - platform.y1);

        if (width > height) {
            int numTiles = (int) (width/tileSize);
            float startX = Math.min(platform.x1, platform.x2);

            for (int i = 0; i < numTiles; i++) {
                batch.draw(tileTexture, startX + (i * tileSize), platform.y1 - platform.thickness, tileSize, platform.thickness);
            }
        } else {
            // Vertical Wall
            int numTiles = (int) (height / tileSize);
            float startY = Math.min(platform.y1, platform.y2);
            float wallThickness = 16f;

            for (int i = 0; i < numTiles; i++) {
                batch.draw(tileTexture, platform.x1 - (wallThickness / 2), startY + (i * tileSize), wallThickness, tileSize);
            }
        }
    }
}
