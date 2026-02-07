package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class PlayerActor extends Actor {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Movement
    private final float moveSpeed = 250f;

    // Jump physics
    private float velocityY = 0f;
    private final float gravity = -1400f;

    // Jump charge
    private float jumpCharge = 0f;
    private final float maxJumpCharge = 900f;
    private final float chargeRate = 1200f;

    // State
    private boolean isCharging = false;
    private boolean isGrounded = true;
    private boolean wasSpacePressed = false; // Track previous frame state
    private Platform currentPlatform = null;

    private final float groundY = 100f; // temporary ground

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean spacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // ===== CHARGING JUMP =====
        if (isGrounded && spacePressed) {
            isCharging = true;
            jumpCharge += chargeRate * delta;
            jumpCharge = Math.min(jumpCharge, maxJumpCharge);
        }

        // ===== RELEASE JUMP =====
        if (isGrounded && isCharging && wasSpacePressed && !spacePressed) {
            velocityY = jumpCharge;
            jumpCharge = 0f;
            isCharging = false;
            isGrounded = false;
        }

        // ===== HORIZONTAL MOVEMENT (GROUND + AIR) =====
        if (!isCharging) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                moveBy(-moveSpeed * delta, 0);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                moveBy(moveSpeed * delta, 0);
            }
        }

        // ===== APPLY GRAVITY =====
        if (!isGrounded) {
            velocityY += gravity * delta;
            moveBy(0, velocityY * delta);
        }

        // ===== GROUND COLLISION =====
        if (getY() < groundY) {
            setY(groundY);
            velocityY = 0f;
            isGrounded = true;
            isCharging = false;
            jumpCharge = 0f;
        }

        // Update previous frame state
        wasSpacePressed = spacePressed;
    }

    public void checkPlatformCollision(Array<Platform> platforms) {
        if (velocityY > 0) {
            // Don't check collision when moving upward
            isGrounded = false;
            currentPlatform = null;
            return;
        }

        for (Platform platform : platforms) {
            if (checkCollisionWithPlatform(platform)) {
                velocityY = 0f;
                isGrounded = true;
                currentPlatform = platform;
                return;
            }
        }

        isGrounded = false;
        currentPlatform = null;
    }

    private boolean checkCollisionWithPlatform(Platform platform) {
        // Check if player is falling onto platform
        float playerBottom = getY();
        float playerLeft = getX();
        float playerRight = getX() + getWidth();

        float platformTop = platform.getY() + platform.getHeight();
        float platformLeft = platform.getX();
        float platformRight = platform.getX() + platform.getWidth();

        // Horizontal overlap
        boolean horizontalOverlap = playerRight > platformLeft && playerLeft < platformRight;

        // Vertical collision (within threshold)
        boolean verticalCollision = playerBottom <= platformTop && playerBottom >= platformTop - 10;

        if (horizontalOverlap && verticalCollision && velocityY <= 0) {
            // Snap player to top of platform to avoid clipping
            setY(platformTop);
            return true;
        }

        return false;
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
