package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.DKICooked.Main;

public class PausedScreen extends Table {
    private final Main main;
    private BitmapFont pauseFont;
    private Texture dimTex, pausedLabel;
    private Image pausedImage;

    // onQuit now points to a method in GameScreen that triggers Game Over
    public PausedScreen(Runnable onResume, Runnable onQuit, Main main) {
        this.main = main;
        setFillParent(true);
        createFonts();

        // 1. Background Overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.7f));
        pixmap.fill();
        this.dimTex = new Texture(pixmap);
        pixmap.dispose();

        Image bg = new Image(dimTex);
        bg.setFillParent(true);
        addActor(bg);

        // 2. Button Style
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = pauseFont;
        btnStyle.fontColor = Color.valueOf("f8c72c");
        btnStyle.overFontColor = Color.valueOf("#ef901f");
        // 3. Header & Animation
        pausedLabel = new Texture(Gdx.files.internal("Paused.png"));
        pausedImage = new Image(pausedLabel);
        pausedImage.setOrigin(Align.center);
        pausedImage.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 15, 0.8f),
            Actions.moveBy(0, -15, 0.8f)
        )));

        Container<Image> pauseWrapper = new Container<>(pausedImage);
        add(pauseWrapper).size(400, 100).padBottom(50).row();

        // 4. Buttons
        TextButton resumeBtn = new TextButton("RESUME", btnStyle);
        TextButton quitBtn = new TextButton("QUIT", btnStyle);

        add(resumeBtn).padBottom(20).row();
        add(quitBtn).row();

        // 5. Listeners
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onResume.run();
            }
        });

        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // This will hide the pause menu and trigger showGameOverScreen() in GameScreen
                onQuit.run();
            }
        });

        setVisible(false);
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.borderWidth = 2f;
        pauseFont = generator.generateFont(parameter);
        generator.dispose();
    }

    public void toggle(boolean show) {
        setVisible(show);
    }

    public void dispose() {
        if (pauseFont != null) pauseFont.dispose();
        if (dimTex != null) dimTex.dispose();
        if (pausedLabel != null) pausedLabel.dispose();
    }
}
