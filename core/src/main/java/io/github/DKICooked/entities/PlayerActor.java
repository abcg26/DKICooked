package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.gameLogic.physics.PhysicsBody;

public class PlayerActor extends Actor {

    // Physics Engine constants
    private final PhysicsBody body = new PhysicsBody(2000f, 300f, 1300f, -1800f);
    private Array<Platform> platforms = new Array<>();

    // Jump charging state
    private float jumpCharge = 0f;
    private final float maxJumpCharge = 900f;
    private final float chargeRate = 1600f;

    private boolean isCharging = false;
    private boolean isGrounded = false;
    private float jumpCooldown = 0f;
    public boolean facingRight = true;

    // Stun / knockback logic
    private float stunTime = 0f;
    private final float stunDuration = 0.25f;
    private final float bounceForce = 1.25f;

    // Fixed timestep accumulator
    private float accumulator = 0f;
    private static final float STEP = 1f / 180f;

    @Override
    public void act(float delta) {
        super.act(delta);

        // Fixed timestep loop to ensure consistent physics regardless of FPS
        accumulator += delta;
        while (accumulator >= STEP) {
            updatePhysics(STEP);
            accumulator -= STEP;
        }
    }

    private void updatePhysics(float dt) {
        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (jumpCooldown > 0f) jumpCooldown -= dt;
        if (stunTime > 0f) stunTime -= dt;

        float prevY = getY();

        // 1. JUMP CHARGING
        if (isGrounded && space && jumpCooldown <= 0f && !isCharging) {
            isCharging = true;
            jumpCharge = 0f;
        }

        if (isCharging && space) {
            jumpCharge = Math.min(maxJumpCharge, jumpCharge + chargeRate * dt);
        }

        if (isCharging && !space) {
            body.velocityY = jumpCharge;
            isCharging = false;
            isGrounded = false;
            jumpCooldown = 0.15f;
        }

        // 2. HORIZONTAL MOVEMENT
        float input = 0f;
        if (!isCharging && stunTime <= 0f) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }
        body.applyHorizontalInput(input, dt);

        // Move horizontally and bounce off screen edges
        moveBy(body.velocityX * dt, 0);
        if (getX() < 0 || getX() + getWidth() > 800) {
            body.velocityX *= -bounceForce;
            stunTime = stunDuration;
            setX(MathUtils.clamp(getX(), 0, 800 - getWidth()));
        }

        // 3. VERTICAL MOVEMENT & GRAVITY
        if (!isGrounded) {
            body.applyGravity(dt);
        }
        moveBy(0, body.velocityY * dt);

        // 4. SLOPE COLLISION DETECTION
        boolean groundedThisFrame = false;
        float footX = getX() + getWidth() * 0.5f;
        float footY = getY();

        // Check if we crossed a platform line
        for (Platform p : platforms) {
            float surfaceY = p.getSurfaceY(footX);

            // If we are over a platform and falling...
            if (surfaceY != -1 && body.velocityY <= 0) {
                // Check if our feet crossed the surface line this frame
                if (prevY >= surfaceY - 2f && footY <= surfaceY + 10f) {
                    setY(surfaceY);
                    body.velocityY = 0;
                    groundedThisFrame = true;
                    break;
                }
            }
        }

        isGrounded = groundedThisFrame;

        // 5. UPDATE STATE FOR ANIMATION
        if (body.velocityX > 0.1f) facingRight = true;
        else if (body.velocityX < -0.1f) facingRight = false;

        // Cleanup tiny velocities
        if (Math.abs(body.velocityX) < 0.5f) body.velocityX = 0f;
    }

    // API for GameScreen
    public void setPlatforms(Array<Platform> platforms) {
        this.platforms = platforms;
    }

    public boolean isGrounded() { return isGrounded; }
    public boolean isCharging() { return isCharging; }
    public float getJumpCharge() { return jumpCharge; }
    public PhysicsBody getBody() { return body; }
}
