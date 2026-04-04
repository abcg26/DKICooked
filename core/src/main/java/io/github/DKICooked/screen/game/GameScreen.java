package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.audio.SoundPlayer;
import io.github.DKICooked.entities.*;
import io.github.DKICooked.gameLogic.SaveData;
import io.github.DKICooked.gameLogic.SaveManager;
import io.github.DKICooked.gameLogic.WorldManager;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class GameScreen extends BaseScreen {
    private final String selection;
    private enum State { PLAYING, DYING, GAMEOVER }
    private State currentState = State.PLAYING;
    private float deathTimer = 0f;
    private static final float DEATH_DURATION = 1.5f;

    private static final float SCREEN_WIDTH  = 800f;
    private static final float SCREEN_HEIGHT = 600f;

    private final Main main;
    private final WorldManager world;
    private final PlayerActor player;
    private final PlayerSprite sprite;
    private final SoundPlayer soundPlayer;

    private Texture platformTileTexture, playerFallenTexture, backgroundTexture, railTexture;
    private Texture titleTex, retryTex, whitePixel, asteroidTex, anomalyTex;
    private PlatformTiles platformTile;

    private enum RaidType { NONE, ASTEROIDS, UFO, MAGNETIC_STORM }
    private RaidType activeRaid = RaidType.NONE;
    private RaidType lastActiveRaid = RaidType.NONE;

    private float raidEndHeight = 0;
    private Animation<TextureRegion> ufoHorizontalAnim;
    private final UfoManager ufoManager;
    private final MagneticStormManager msManger;
    private final AsteroidManager asteroidManager;

    private BitmapFont scoreFont;
    private Label scoreLabel, finalScoreLabel;
    private final StringBuilder scoreBuilder = new StringBuilder();

    private final Stage uiStage;
    private float backgroundTintAlpha = 0f;
    private final float FADE_SPEED = 1.5f;
    private float anomalyTimer = 0;

    private int highestChunkReached = 0;
    private int lastSnapChunk = -1;
    private boolean paused = false;
    private boolean escWasPressed = false;
    private PausedScreen pauseOverlay;

    private int recordHeight = 0;
    private Table gameOverTable;
    private TextField nameInput;
    private TextButton submitBtn;
    private TextButton retryBtn;
    private TextButton quitBtn;

    public GameScreen(Main main, String selection) {
        this.main = main;
        this.selection = selection;
        playerFallenTexture = new Texture(Gdx.files.internal("dead.png"));
        anomalyTex = new Texture(Gdx.files.internal("emer.png"));
        soundPlayer = new SoundPlayer();
        soundPlayer.playMusic();

        this.uiStage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));

        asteroidTex = new Texture(Gdx.files.internal("asteroid.png"));
        this.asteroidManager = new AsteroidManager(asteroidTex);
        this.world = new WorldManager();

        platformTileTexture = new Texture(Gdx.files.internal("wallTile.jpg"));
        platformTile = new PlatformTiles(platformTileTexture);

        player = new PlayerActor(soundPlayer);
        player.setSize(40, 60);
        player.setPosition(400, 150);
        player.setPlatforms(world.getActivePlatforms());
        player.initStats(selection);

        stage.addActor(player);
        sprite = new PlayerSprite(selection);

        backgroundTexture = new Texture(Gdx.files.internal("background.png"));
        railTexture = new Texture(Gdx.files.internal("rail.png"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        railTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Texture f1 = new Texture(Gdx.files.internal("ufoH1.png"));
        Texture f2 = new Texture(Gdx.files.internal("ufoH2.png"));
        ufoHorizontalAnim = new Animation<>(0.3f, new TextureRegion(f1), new TextureRegion(f2));
        ufoHorizontalAnim.setPlayMode(Animation.PlayMode.LOOP);

        this.ufoManager = new UfoManager(ufoHorizontalAnim);
        this.msManger = new MagneticStormManager();

        // Initial input setup
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        setupUI();
        snapCamera(0);
    }

    private void updateLogic(float delta) {
        boolean escDown = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
        if (escDown && !escWasPressed) {
            paused = !paused;
            pauseOverlay.toggle(paused);
        }
        escWasPressed = escDown;

        if (currentState == State.PLAYING) {
            world.update(player.getY());
            player.setPlatforms(world.getActivePlatforms());

            int currentChunk = world.getCurrentChunk();
            if (currentChunk > highestChunkReached) highestChunkReached = currentChunk;
            if (currentChunk != lastSnapChunk) {
                snapCamera(currentChunk);
                lastSnapChunk = currentChunk;
            }

            if (activeRaid != RaidType.NONE) checkCollisions();
            if (highestChunkReached > 0 && currentChunk < highestChunkReached - 1) startDeathSequence();

            float moveDir = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDir = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDir = 1;

            if (activeRaid == RaidType.MAGNETIC_STORM) moveDir *= -1;
            player.handleHorizontalMovement(moveDir, delta);

            int currentHeight = (int) (player.getY() / 100f);
            if (currentHeight > recordHeight) recordHeight = currentHeight;

            scoreBuilder.setLength(0);
            scoreBuilder.append("Best: ").append(recordHeight).append("m");
            scoreLabel.setText(scoreBuilder);

        } else if (currentState == State.DYING) {
            deathTimer += delta;
            float bounce = (float) Math.sin(deathTimer * 5) * 50f;
            float fall = 300f * deathTimer;
            player.setY(player.getY() + (bounce * (1 - deathTimer)) - (fall * delta));
            player.rotateBy(400 * delta);

            if (deathTimer >= DEATH_DURATION) showGameOverScreen();
        }
        stage.act(delta);
    }

    private void startDeathSequence() {
        currentState = State.DYING;
        activeRaid = RaidType.NONE;
        backgroundTintAlpha = 0;
        player.setDead(true);
        player.clearActions();
        player.setOrigin(player.getWidth() / 2f, player.getHeight() / 2f);
    }

    private void showGameOverScreen() {
        currentState = State.GAMEOVER;
        paused = true;

        // Force UI focus
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        Gdx.input.setInputProcessor(multiplexer);

        SaveData data = SaveManager.load();
        boolean isHighScore = data.isHighScore(recordHeight);



        // Ensure data exists and check leaderboard
        if (data == null || data.leaderBoard == null || data.leaderBoard.size < 10) {
            isHighScore = true;
        } else if (recordHeight > data.leaderBoard.get(data.leaderBoard.size - 1).score) {
            isHighScore = true;
        }

        if (finalScoreLabel != null) finalScoreLabel.setText("Final Score: " + recordHeight + "m");

        gameOverTable.setVisible(true);
        gameOverTable.getColor().a = 0;
        gameOverTable.addAction(Actions.fadeIn(0.4f));

        if (isHighScore) {
            nameInput.setVisible(true);
            submitBtn.setVisible(true);
            retryBtn.setVisible(false);
            nameInput.setText("");
            uiStage.setKeyboardFocus(nameInput);
        } else {
            nameInput.setVisible(false);
            submitBtn.setVisible(false);
            retryBtn.setVisible(true);
        }

        gameOverTable.invalidateHierarchy();
    }

    private void snapCamera(int chunkIndex) {
        float newY = chunkIndex * SCREEN_HEIGHT;
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(SCREEN_WIDTH / 2f, newY + SCREEN_HEIGHT / 2f, 0);
        cam.update();
    }

    private void checkCollisions() {
        if (currentState != State.PLAYING) return;
        for (var actor : stage.getActors()) {
            if (actor instanceof AsteroidActor meteor) {
                if (com.badlogic.gdx.math.Intersector.overlaps(meteor.getCollisionCircle(), player.getCollisionRect())) {
                    startDeathSequence();
                    break;
                }
            }
            if (actor instanceof UfoActor ufo) {
                if (com.badlogic.gdx.math.Intersector.overlaps(ufo.getCollisionCircle(), player.getCollisionRect())) {
                    startDeathSequence();
                    break;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        // 1. Clear the screen
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);

        // 2. Update Game Logic (ONLY if not paused)
        if (!paused) {
            updateLogic(delta);
            if (currentState == State.PLAYING) {
                handleAnomalyLogic(delta);
            }
        }

        // 3. UPDATE UI LOGIC (ALWAYS - even if paused)
        // This is what makes the PausedScreen animation actually move!
        uiStage.act(delta);

        SpriteBatch batch = (SpriteBatch) stage.getBatch();

        // 4. Background Rendering (Parallax)
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        boolean isEffectivelyMagnetic = (activeRaid == RaidType.MAGNETIC_STORM) ||
            (activeRaid == RaidType.NONE && lastActiveRaid == RaidType.MAGNETIC_STORM);

        if (isEffectivelyMagnetic && backgroundTintAlpha > 0) {
            batch.setColor(msManger.getStormTint(backgroundTintAlpha));
        } else {
            float currentGreenBlue = 1f - (backgroundTintAlpha * 0.7f);
            batch.setColor(1f, currentGreenBlue, currentGreenBlue, 1f);
        }

        float starScrollV = (player.getY() * 0.05f) / backgroundTexture.getHeight();
        batch.draw(backgroundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 0, starScrollV + 1, 1, starScrollV);

        float railScrollV = (player.getY() * 0.3f) / railTexture.getHeight();
        batch.draw(railTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 0, (railScrollV / 2f), 1, (railScrollV / 2f) + 0.5f);

        batch.setColor(Color.WHITE);
        batch.end();

        // 5. Game World Rendering (Platforms and Player)
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        for (Platform p : world.getActivePlatforms()) {
            platformTile.render(batch, p);
        }
        sprite.draw(batch, player);
        batch.end();

        // Draw the stage (for actors added to the world stage)
        stage.draw();

        // 6. Effects (Magnetic Glitch)
        if (isEffectivelyMagnetic && backgroundTintAlpha > 0) {
            batch.setProjectionMatrix(uiStage.getCamera().combined);
            batch.begin();
            msManger.drawGlitch(batch, backgroundTintAlpha);
            batch.end();
        }

        // 7. Anomaly Text Pulse
        if (backgroundTintAlpha > 0) {
            anomalyTimer += delta;
            batch.setProjectionMatrix(uiStage.getCamera().combined);
            batch.begin();
            if (!MathUtils.randomBoolean(0.03f)) {
                float pulse = 0.6f + (float) Math.sin(anomalyTimer * 6f) * 0.4f;
                batch.setColor(1, 1, 1, pulse * backgroundTintAlpha);
                batch.draw(anomalyTex, (SCREEN_WIDTH / 2f) - 285f, 420, 570f, 85f);
                batch.setColor(Color.WHITE);
            }
            batch.end();
        }

        // 8. UI Rendering (Always last so it stays on top)
        uiStage.draw();
    }

    private void handleAnomalyLogic(float delta) {
        float py = player.getY();
        if (py >= 1500 && activeRaid == RaidType.NONE) {
            int choice = MathUtils.random(1, 3);
            if (choice == 1) activeRaid = RaidType.ASTEROIDS;
            else if (choice == 2) activeRaid = RaidType.UFO;
            else activeRaid = RaidType.MAGNETIC_STORM;
            raidEndHeight = py + 2000f;
        }

        if (activeRaid != RaidType.NONE) {
            backgroundTintAlpha += delta * FADE_SPEED;
            if (backgroundTintAlpha > 1f) backgroundTintAlpha = 1f;
            if (activeRaid == RaidType.ASTEROIDS) asteroidManager.update(delta, py, stage);
            else if (activeRaid == RaidType.UFO) ufoManager.update(delta, stage);
            else if (activeRaid == RaidType.MAGNETIC_STORM) msManger.update(delta, stage);
            if (py >= raidEndHeight || py < 1500) stopAllRaids();
        } else {
            backgroundTintAlpha -= delta * FADE_SPEED;
            if (backgroundTintAlpha <= 0f) {
                backgroundTintAlpha = 0f;
                lastActiveRaid = RaidType.NONE;
            }
        }
    }

    private void stopAllRaids() {
        lastActiveRaid = activeRaid;
        activeRaid = RaidType.NONE;
        ufoManager.stop();
        msManger.stop(stage);
    }

    public void setupUI() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 2f;
        parameter.borderColor = Color.BLACK;
        scoreFont = generator.generateFont(parameter);
        generator.dispose();

        scoreLabel = new Label("Best: 0m", new Label.LabelStyle(scoreFont, Color.WHITE));
        Table scoreTable = new Table();
        scoreTable.setFillParent(true);
        scoreTable.top().left().pad(20);
        scoreTable.add(scoreLabel);
        uiStage.addActor(scoreTable);

        Texture pauseTex = new Texture(Gdx.files.internal("Pause.png"));
        ImageButton pauseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(pauseTex)));
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.top().right();
        uiTable.add(pauseButton).size(40, 40).pad(10);
        uiStage.addActor(uiTable);

        pauseOverlay = new PausedScreen(
            () -> { // Resume function
                paused = false;
                pauseOverlay.toggle(false);
            },
            () -> { // Quit function (The "Give Up" logic)
                paused = false;
                pauseOverlay.toggle(false); // Hide the pause menu
                showGameOverScreen();       // Trigger your existing GameOver UI
            },
            main
        );
        uiStage.addActor(pauseOverlay);

        pauseButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { paused = !paused; pauseOverlay.toggle(paused); }
        });

        setupGameOverUI();
    }

    private void setupGameOverUI() {
        whitePixel = createWhitePixel();
        TextureRegionDrawable whiteDrawable = new TextureRegionDrawable(new TextureRegion(whitePixel));

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = scoreFont;
        btnStyle.fontColor = Color.valueOf("f8c72c");
        btnStyle.overFontColor =  Color.valueOf("#ef901f");;

        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        tfStyle.font = scoreFont;
        tfStyle.fontColor = Color.valueOf("f8c72c");
        tfStyle.cursor = whiteDrawable.tint(Color.valueOf("f8c72c"));
        tfStyle.background = whiteDrawable.tint(new Color(0.2f, 0.2f, 0.2f, 0.8f));

        finalScoreLabel = new Label("Final Score: 0m", new Label.LabelStyle(scoreFont, Color.WHITE));
        nameInput = new TextField("", tfStyle);
        nameInput.setMaxLength(3);
        nameInput.setAlignment(Align.center);
        submitBtn = new TextButton("SUBMIT", btnStyle);
        retryBtn = new TextButton("RETRY", btnStyle);
        quitBtn = new TextButton("QUIT", btnStyle);

        gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.center();
        gameOverTable.setBackground(whiteDrawable.tint(new Color(0, 0, 0, 0.8f)));

        titleTex = new Texture(Gdx.files.internal("GO.png"));
        Image goImage = new Image(titleTex);
        goImage.setOrigin(Align.center);
        goImage.addAction(Actions.forever(Actions.sequence(Actions.moveBy(0, 10, 0.8f), Actions.moveBy(0, -10, 0.8f))));

        gameOverTable.add(goImage).size(400, 75).padBottom(20).row();
        gameOverTable.add(finalScoreLabel).padBottom(20).row();
        gameOverTable.add(nameInput).size(150, 50).padBottom(5).row();
        gameOverTable.add(submitBtn).size(200, 50).padBottom(5).row();
        gameOverTable.add(retryBtn).size(100, 50).center().row();
        gameOverTable.add(quitBtn).size(100, 50).center();

        submitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String name = nameInput.getText().toUpperCase().trim();
                if (name.isEmpty()) name = "AAA";
                SaveData data = SaveManager.load();
                data.addScore(name, recordHeight);
                SaveManager.save(data);
                nameInput.setVisible(false);
                submitBtn.setVisible(false);
                retryBtn.setVisible(true);
                soundPlayer.stopMusic();
                main.setScreen(new LeaderboardScreen(main));
            }
        });

        retryBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                soundPlayer.stopMusic();
                main.setScreen(new GameScreen(main, selection));
            }
        });

        quitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                soundPlayer.stopMusic();
                main.setScreen(new MainMenuScreen(main));
            }
        });

        gameOverTable.setVisible(false);
        uiStage.addActor(gameOverTable);
    }

    private Texture createWhitePixel() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture t = new Texture(pixmap);
        pixmap.dispose();
        return t;
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); uiStage.getViewport().update(width, height, true); }

    @Override
    public void dispose() {
        uiStage.dispose();
        if (sprite != null) sprite.dispose();
        if (scoreFont != null) scoreFont.dispose();
        if (playerFallenTexture != null) playerFallenTexture.dispose();
        if (platformTileTexture != null) platformTileTexture.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (titleTex != null) titleTex.dispose();
        if (whitePixel != null) whitePixel.dispose();
        if (asteroidTex != null) asteroidTex.dispose();
        if (anomalyTex != null) anomalyTex.dispose();
        if (msManger != null) msManger.dispose();
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
        }
    }
}
