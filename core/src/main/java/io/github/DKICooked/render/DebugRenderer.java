package io.github.DKICooked.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Camera;

public class DebugRenderer {
    public static final ShapeRenderer renderer = new ShapeRenderer();

    public static void begin(Camera camera) {
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
    }

    public static void end() {
        renderer.end();
    }
}
