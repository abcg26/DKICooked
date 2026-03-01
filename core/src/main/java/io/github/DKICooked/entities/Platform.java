package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Platform {
    public float x1, y1, x2, y2;
    public float thickness = 10f;

    public Platform(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public float getSurfaceY(float x) {
        float wallVisualWidth = 16f;
        float detectionBuffer = 2f; // Makes landing slightly more forgiving

        if (Math.abs(x1 - x2) < 2f) { // Wall
            float half = (wallVisualWidth / 2f) + detectionBuffer;
            if (x >= x1 - half && x <= x1 + half) {
                return Math.max(y1, y2);
            }
        } else {
            // Horizontal Platform
            if (x >= Math.min(x1, x2) && x <= Math.max(x1, x2)) {
                return y1;
            }
        }
        return -1;
    }


    public void draw(ShapeRenderer renderer) {
        float width = Math.abs(x2 - x1);
        float height = Math.abs(y2 - y1);

        if (width > height) {
            renderer.line(x1, y1, x2, y2);
            renderer.line(x1, y1 - thickness, x2, y2 - thickness);
            renderer.line(x1, y1, x1, y1 - thickness);
            renderer.line(x2, y2, x2, y2 - thickness);
        } else {
            float wallThickness = 8f;
            renderer.rect(x1 - (wallThickness / 2), y1, wallThickness, height);
        }
    }
}
