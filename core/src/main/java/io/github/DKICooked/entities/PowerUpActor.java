package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

public class PowerUpActor extends Actor {
    public enum Type { GHOST, UFO_RIDE, SHIELD }
    private  final Type type;
    private final Texture region;

    public PowerUpActor(Type type, Texture  tex, float x, float y){
        this.type = type;
        this.region = tex;
        setPosition(x, y);
        setSize(42, 42);

        this.setOrigin(Align.center);

        this.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 15, 1.2f, Interpolation.sine), // Move up 15 pixels over 1.2s
            Actions.moveBy(0, -15, 1.2f, Interpolation.sine) // Move back down
        )));

    }

    public Type getType() { return type; }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(region, getX(), getY(), getWidth(), getHeight());
    }
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
