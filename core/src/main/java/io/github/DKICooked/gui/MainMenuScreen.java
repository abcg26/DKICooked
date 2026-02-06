package io.github.DKICooked.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

public class MainMenuScreen extends BaseScreen {
    private final Texture startText;
    private final Texture tutText;
    private final Texture setText;
    private final Texture exitText;
    private final Texture titleText;
    private final Texture subTitleText;


    public MainMenuScreen() {
        super();
        startText = new Texture(Gdx.files.internal("Start.png"));
        tutText = new Texture(Gdx.files.internal("tutorial.png"));
        setText = new Texture(Gdx.files.internal("settings.png"));
        exitText = new Texture(Gdx.files.internal("exit.png"));

        titleText = new Texture(Gdx.files.internal("DonkeyKong.png"));
        subTitleText = new Texture(Gdx.files.internal("Infinity.png"));

        Image title = new Image(titleText);
        Image subTitle = new Image(subTitleText);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        title.setScaling(Scaling.fit);
        subTitle.setScaling(Scaling.fit);

        table.top().center();
        table.add(title).width(Gdx.graphics.getWidth() * 0.70f).bottom().padBottom(-100).row();
        table.add(subTitle).width(Gdx.graphics.getWidth() * 0.9f).top().padBottom(50).row();

        ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
        startStyle.imageUp = new TextureRegionDrawable( new TextureRegion(startText));
        ImageButton startButton = new ImageButton(startStyle);

        ImageButton.ImageButtonStyle tutStyle = new ImageButton.ImageButtonStyle();
        tutStyle.imageUp = new TextureRegionDrawable( new TextureRegion(tutText));
        ImageButton tutButton = new ImageButton(tutStyle);

        ImageButton.ImageButtonStyle settStyle = new ImageButton.ImageButtonStyle();
        settStyle.imageUp = new TextureRegionDrawable( new TextureRegion(setText));
        ImageButton settButton = new ImageButton(settStyle);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.imageUp = new TextureRegionDrawable( new TextureRegion(exitText));
        ImageButton exitButton = new ImageButton(exitStyle);

        startButton.getImage().setScaling(Scaling.fit);
        tutButton.getImage().setScaling(Scaling.fit);
        settButton.getImage().setScaling(Scaling.fit);
        exitButton.getImage().setScaling(Scaling.fit);

        table.add(startButton).width(170).height(80).padTop(-60).center().row();
        table.add(tutButton).width(200).height(80).padTop(-20).center().row();
        table.add(settButton).width(200).height(80).padTop(-20).padBottom(50).center().row();
        table.add(exitButton).width(100).height(40).center().row();

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Start Button was clicked");
            }
        });

        tutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Tutorial Button was clicked");
            }
        });

        settButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Setting Button was clicked");
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Exit Button was clicked");
            }
        });

    }
    @Override
    public void dispose() {
        super.dispose();
        startText.dispose();
        tutText.dispose();
        setText.dispose();
        exitText.dispose();
        titleText.dispose();
        subTitleText.dispose();
    }
}
