package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.DKICooked.Main;
import io.github.DKICooked.entities.LBScore;
import io.github.DKICooked.gameLogic.SaveData;
import io.github.DKICooked.gameLogic.SaveManager;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class LeaderboardScreen extends BaseScreen {
    private final Main main;
    private BitmapFont customFont; // Declared here for class-wide use
    private Texture lbPic;
    private Texture crownTex;

    public LeaderboardScreen(Main main) {
        this.main = main;

        // 1. Initialize assets
        createFonts();
        this.lbPic = new Texture(Gdx.files.internal("LB.png"));
        this.crownTex = new Texture(Gdx.files.internal("crown.png"));

        // 2. Set input processor so buttons work
        Gdx.input.setInputProcessor(stage);

        setupUI();
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 18; // Adjusted size for list readability
        parameter.color = Color.WHITE;
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        parameter.shadowColor = new Color(0, 0, 0, 0.5f);

        customFont = generator.generateFont(parameter);
        generator.dispose();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        //root.debug(); // Turn this on if you need to see where things are leaking
        stage.addActor(root);

        // --- 1. Header Image ---
        Image titleImage = new Image(lbPic);
        titleImage.setOrigin(Align.center);
        titleImage.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 10, 0.7f),
            Actions.moveBy(0, -10, 0.7f)
        )));
        // We give the header a fixed height so it doesn't eat the whole screen
        root.add(titleImage).size(400, 120).padTop(20).row();

        // --- 2. Leaderboard Table ---
        Table scoreTable = new Table();
        SaveData data = SaveManager.load();
        Label.LabelStyle whiteStyle = new Label.LabelStyle(customFont, Color.WHITE);
        Label.LabelStyle goldStyle = new Label.LabelStyle(customFont, Color.valueOf("fed546"));
        Label.LabelStyle headerStyle = new Label.LabelStyle(customFont, Color.valueOf("fed546"));

        scoreTable.padTop(-30);
        // Header dashed line
        // Top dashed line - change colspan to 4
        scoreTable.add(new Label("- - - - - - - - - - - - - - - - - -", headerStyle)).colspan(4).center().row();

// Headers - Add them as separate cells to match the columns below
        scoreTable.add(new Label("| RANK", headerStyle)).left();
        scoreTable.add(new Label("| NAME", headerStyle)).left().padLeft(10);
        scoreTable.add(new Label("", headerStyle)).expandX(); // Header spacer
        scoreTable.add(new Label("| DISTANCE |", headerStyle)).right();
        scoreTable.row();

// Bottom dashed line - change colspan to 4
        scoreTable.add(new Label("- - - - - - - - - - - - - - - - - -", headerStyle)).colspan(4).center().padBottom(5).row();

        // Data Rows
        for (int i = 0; i < data.leaderBoard.size; i++) {
            LBScore entry = data.leaderBoard.get(i);
            Label.LabelStyle currentStyle = (i == 0) ? goldStyle : whiteStyle;

            // Column 1: RANK + CROWN
            Table rankGroup = new Table();
            if (i == 0) {
                Image crown = new Image(crownTex);
                rankGroup.add(crown).size(20, 20).padRight(5);
            }
            rankGroup.add(new Label(getOrdinal(i), currentStyle)).left();
            scoreTable.add(rankGroup).left().width(80); // Fixed width keeps NAME column straight

            // Column 2: NAME
            scoreTable.add(new Label(entry.name, currentStyle)).left().padLeft(10);

            // Column 3: SPACER (The Magic Expand)
            scoreTable.add(new Label("", headerStyle)).expandX().fillX();

            // Column 4: DISTANCE
            scoreTable.add(new Label(entry.score + "M", currentStyle)).right();

            scoreTable.row().padBottom(8);
        }

        // Bottom dashed line\
        //scoreTable.debug();
        scoreTable.add(new Label("- - - - - - - - - - - - - - - - - -", headerStyle)).colspan(4).center().padTop(5).row();

        // ADD SCORE TABLE TO ROOT
        // .expandY() pushes the button to the bottom
        root.add(scoreTable).width(380).expandY().row();

        // --- 3. Navigation ---
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = customFont;
        btnStyle.fontColor = Color.valueOf("fed546");
        btnStyle.overFontColor = Color.valueOf("#ef901f");

        TextButton backBtn = new TextButton("BACK TO MENU", btnStyle);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new MainMenuScreen(main));
            }
        });

        // Add button with padding at the very bottom
        root.add(backBtn).size(220, 50).padBottom(55);
    }

    private String getOrdinal(int i) {
        int n = i + 1;
        if (n >= 11 && n <= 13) return n + "th";
        switch (n % 10) {
            case 1:  return n + "st";
            case 2:  return n + "nd";
            case 3:  return n + "rd";
            default: return n + "th";
        }
    }

    @Override
    public void render(float delta) {
        // Dark space theme background
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (customFont != null) customFont.dispose();
        if (lbPic != null) lbPic.dispose();
        super.dispose();
    }
}
