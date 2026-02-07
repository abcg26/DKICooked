package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PlayerActor extends Actor {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private float velocityY = 0f;
    private final float gravity = -1200f;
    private final float jumpVelocity = 500f;
    private final float moveSpeed = 300f;

    private final float groundY = 100f; // temporary ground

    @Override
    public void act(float delta) {
        super.act(delta);

        // LEFT / RIGHT
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveBy(-moveSpeed * delta, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveBy(moveSpeed * delta, 0);
        }

        // GRAVITY
        velocityY += gravity * delta;
        moveBy(0, velocityY * delta);

        // GROUND COLLISION (temporary)
        if (getY() <= groundY) {
            setY(groundY);
            velocityY = 0;
        }

        // JUMP
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && getY() <= groundY) {
            velocityY = jumpVelocity;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        batch.begin();
    }
}
