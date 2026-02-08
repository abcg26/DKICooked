package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import io.github.DKICooked.render.DebugRenderer;

public class Platform extends Actor {
    private Color color;

    public Platform(float x, float y, float width, float height) {
        setPosition(x, y);
        setSize(width, height);
        this.color = new Color(1f, 1f, 1f, 1f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        DebugRenderer.begin(getStage().getCamera());
        DebugRenderer.renderer.setColor(color);
        DebugRenderer.renderer.rect(getX(), getY(), getWidth(), getHeight());
        DebugRenderer.end();

        batch.begin();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
