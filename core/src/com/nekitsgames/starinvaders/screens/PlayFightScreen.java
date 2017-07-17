/*
* Copyright (c) 20016 - 2017, NG Tech and/or its affiliates. All rights reserved.
* GNI GPL v3 licence . Use is subject to license terms
*/

package com.nekitsgames.starinvaders.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.nekitsgames.starinvaders.API.logAPI.LogSystem;
import com.nekitsgames.starinvaders.StarInvaders;
import com.nekitsgames.starinvaders.classes.Ammunition;
import com.nekitsgames.starinvaders.classes.AmmunitionType;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Fight screen
 *
 * @author Nikita Serba
 * @version 1.0
 * @since 2.1
 */
public class PlayFightScreen implements Screen {

    private static int SHIP_WIDTH;
    private static int SHIP_HEIGHT;
    private static int SHIP_X;
    private static int SHIP_Y;

    private static String SHIP_FILE;
    private static String SHIP_SOUND;

    private static int SHIP_ONE_STEP_KEY;

    private static int current_amunition = 0;
    private static int amunition_count;
    private static ArrayList<Ammunition> ammunitions;
    private static AmmunitionType[] ammunitionTypes;

    private static String image_path;
    private static String music_path;

    private static boolean showFPS;
    private static String FPSLabel;

    private static int ship = 1;
    private static int ship_armour = 100;

    private static String lazer_texture;
    private final int GREEN_LAZER_DAMAGE = 5;
    private final int GREEN_ROCKET_DAMAGE = 30;
    int cof2 = 1;
    long lastCofChange = TimeUtils.nanoTime();
    private OrthographicCamera camera;
    private StarInvaders game;
    private Properties prop;
    private GlyphLayout glyphLayout;
    private long ammunition_last;
    private Texture shipImage;
    private Texture hearthImage;
    private Music spaceSound;
    private Rectangle shipRect;
    private int hearthHeight;
    private int hearthWidth;
    private int hp;
    private Texture badShipTexture;
    private Rectangle badShipRect;
    private Texture greenLazerTexture;
    private ArrayList<Rectangle> greenLazerRects;
    private boolean isBadShipAlive = false;
    private int badShipHP = 100;
    private long greenLazerLast = 0;
    private ArrayList<Rectangle> greenRocketRects;
    private Texture greenRocketTexture;
    private long greenRocketLast = 0;
    private boolean isOnLeft = false;


    /**
     * Init game screen
     *
     * @param game - game class
     * @throws IOException if can't read properties
     * @since 2.1
     */
    public PlayFightScreen(StarInvaders game) throws IOException {
        game.log.Log("Initializing main game screen", LogSystem.INFO);

        prop = new Properties();

        ship = (int) game.settingsGameData.get("ship", 1);

        prop.load(new FileInputStream("properties/main.properties"));
        music_path = prop.getProperty("dir.sound");
        image_path = prop.getProperty("dir.images");
        SHIP_SOUND = prop.getProperty("app.music");


        prop.load(new FileInputStream("properties/ship.properties"));
        SHIP_WIDTH = (int) (game.WIDTH * Double.parseDouble(prop.getProperty("ship." + ship + ".width")));
        SHIP_HEIGHT = SHIP_WIDTH;
        SHIP_Y = (int) (Double.parseDouble(prop.getProperty("ship." + ship + ".y")) * game.WIDTH);
        SHIP_ONE_STEP_KEY = (int) (game.WIDTH * Double.parseDouble(prop.getProperty("ship." + ship + ".step.key")));
        SHIP_FILE = prop.getProperty("ship." + ship + ".texture");
        lazer_texture = prop.getProperty("ship." + ship + ".lazer");

        ship_armour = (int) game.settingsGameData.get("armour.percent", 0);

        prop.load(new FileInputStream("properties/amunition.properties"));
        amunition_count = Integer.parseInt(prop.getProperty("amunition.count"));
        ammunitionTypes = new AmmunitionType[amunition_count];
        for (int i = 0; i < amunition_count; i++) {
            ammunitionTypes[i] = new AmmunitionType(
                    Double.parseDouble(prop.getProperty("amunition." + (i + 1) + ".width")),
                    Double.parseDouble(prop.getProperty("amunition." + (i + 1) + ".height")),
                    Double.parseDouble(prop.getProperty("amunition." + (i + 1) + ".step")),
                    Integer.parseInt(prop.getProperty("amunition." + (i + 1) + ".wait_time")),
                    (i == 0) ? lazer_texture : prop.getProperty("amunition." + (i + 1) + ".texture"),
                    prop.getProperty("amunition." + (i + 1) + ".sound"),
                    prop.getProperty("amunition." + (i + 1) + ".expl_image"),
                    image_path,
                    music_path,
                    game,
                    Integer.parseInt(prop.getProperty("amunition." + (i + 1) + ".hp_asteroid")),
                    Integer.parseInt(prop.getProperty("amunition." + (i + 1) + ".damage"))
            );
        }
        prop.load(new FileInputStream("properties/strings.us.properties"));
        FPSLabel = prop.getProperty("fps.label");

        prop.load(new FileInputStream("properties/game.properties"));
        hearthImage = new Texture(image_path + prop.getProperty("hearth.texture"));
        hearthWidth = (int) (game.WIDTH * Double.parseDouble(prop.getProperty("hearth.width")));
        hearthHeight = hearthWidth;

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.WIDTH, game.HEIGHT);

        SHIP_X = game.WIDTH / 2 - SHIP_WIDTH / 2;

        shipImage = new Texture(image_path + SHIP_FILE);

        spaceSound = Gdx.audio.newMusic(Gdx.files.internal(music_path + SHIP_SOUND));

        spaceSound.setLooping(true);
        spaceSound.play();

        shipRect = new Rectangle();
        shipRect.x = SHIP_X;
        shipRect.y = SHIP_Y;
        shipRect.width = SHIP_WIDTH;
        shipRect.height = SHIP_HEIGHT;

        badShipTexture = new Texture(image_path + "ship13.png");
        badShipRect = new Rectangle(game.WIDTH / 2 - shipRect.width / 2, game.HEIGHT - shipRect.height, shipRect.width, shipRect.height);
        greenLazerTexture = new Texture(image_path + "green_lazer.png");
        greenLazerRects = new ArrayList<>();
        greenRocketTexture = new Texture(image_path + "spr_missile2.png");
        greenRocketRects = new ArrayList<>();

        ammunitions = new ArrayList<>();

        hp = 100;

        isBadShipAlive = true;
        spawnGreenRocket(badShipRect.x + badShipRect.width / 2, badShipRect.y - 10);
        badShipHP = 100;
    }

    /**
     * Move bad ship
     *
     * @param x - distance
     * @since 2.1
     */
    private void moveBadShip(float x) {
        badShipRect.x += x;
        if (badShipRect.x < 0)
            badShipRect.x = 0;
        if (badShipRect.x > (game.WIDTH - shipRect.width))
            badShipRect.x = game.WIDTH - shipRect.width;
    }

    /**
     * Spawn new ammunition
     *
     * @param x - new ammunition X position
     * @param y - new ammunition Y position
     * @since 2.1
     */
    private void spawnAmmunition(int x, int y) {
        ammunitions.add(new Ammunition(ammunitionTypes[current_amunition], x, y));
        ammunitionTypes[current_amunition].setLast(TimeUtils.nanoTime());
        ammunitionTypes[current_amunition].getStartSound().play();
    }

    /**
     * Spawn new green lazer
     *
     * @param x - new ammunition X position
     * @param y - new ammunition Y position
     * @since 2.1
     */
    private void spawnGreenLazer(float x, float y) {
        greenLazerRects.add(new Rectangle(x, y, 12, 12));
        greenLazerLast = TimeUtils.nanoTime();
        ammunitionTypes[0].getStartSound().play();
    }

    /**
     * Spawn new green rocket
     *
     * @param x - new ammunition X position
     * @param y - new ammunition Y position
     * @since 2.1
     */
    private void spawnGreenRocket(float x, float y) {
        greenRocketRects.add(new Rectangle(x, y, 64, 64));
        greenRocketLast = TimeUtils.nanoTime();
        ammunitionTypes[1].getStartSound().play();
    }

    /**
     * Die
     *
     * @throws IOException if can't access properties files
     * @since 1.1
     */
    private void die() throws IOException {
        game.setScreen(new GameEndScreen(game, -1));
        dispose();
    }

    /**
     * Render game
     *
     * @param delta - delta time
     * @since 1.1
     */
    @Override
    public void render(float delta) {
        try {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            camera.update();

            game.batch.setProjectionMatrix(camera.combined);

            game.batch.begin();

            if (isBadShipAlive)
                game.batch.draw(badShipTexture, badShipRect.x, badShipRect.y, shipRect.width, shipRect.height);

            game.batch.draw(shipImage, shipRect.x, shipRect.y, shipRect.getWidth(), shipRect.getHeight());
            for (Ammunition lazerRect : ammunitions) {
                game.batch.draw(lazerRect.getType().getMainTexture(), lazerRect.getRect().x, lazerRect.getRect().y, lazerRect.getRect().getWidth(), lazerRect.getRect().getHeight());
            }
            for (Rectangle lazerRect : greenLazerRects) {
                game.batch.draw(greenLazerTexture, lazerRect.x, lazerRect.y, lazerRect.getWidth(), lazerRect.getHeight());
            }
            for (Rectangle lazerRocket : greenRocketRects) {
                game.batch.draw(greenRocketTexture, lazerRocket.x, lazerRocket.y, lazerRocket.getWidth(), lazerRocket.getHeight());
            }
            if (showFPS) {
                String fps = FPSLabel.replace("%FPS%", "" + Gdx.graphics.getFramesPerSecond());
                glyphLayout = new GlyphLayout(game.fontData, fps);
                game.fontData.draw(game.batch, fps, game.WIDTH - glyphLayout.width, game.HEIGHT - glyphLayout.height);
            }

            game.batch.draw(hearthImage, 0, game.HEIGHT - hearthHeight, hearthWidth, hearthHeight);

            String hps = hp + "%";
            glyphLayout = new GlyphLayout(game.fontData, hps);
            game.fontData.draw(game.batch, hps, hearthWidth + 20, game.HEIGHT - (hearthHeight - glyphLayout.height) / 1.5f);

            game.batch.end();

            if (Gdx.input.isKeyPressed(Input.Keys.UP) && TimeUtils.millis() - ammunition_last > 300) {
                current_amunition++;
                ammunition_last = TimeUtils.millis();
            }

            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && TimeUtils.millis() - ammunition_last > 300) {
                current_amunition--;
                ammunition_last = TimeUtils.millis();
            }

            if (current_amunition < 0)
                current_amunition = (ammunitionTypes.length - 1);
            if (current_amunition > (ammunitionTypes.length - 1))
                current_amunition = 0;

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                shipRect.x -= SHIP_ONE_STEP_KEY * Gdx.graphics.getDeltaTime();
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                shipRect.x += SHIP_ONE_STEP_KEY * Gdx.graphics.getDeltaTime();
            if (shipRect.x > game.WIDTH - SHIP_WIDTH)
                shipRect.x = game.WIDTH - SHIP_WIDTH;
            if (shipRect.x < 0)
                shipRect.x = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) & TimeUtils.nanoTime() - ammunitionTypes[current_amunition].getLast() > ammunitionTypes[current_amunition].getWait_time())
                spawnAmmunition((int) (shipRect.x + SHIP_WIDTH / 2), (int) (shipRect.y + SHIP_HEIGHT));

            ArrayList<Ammunition> rm1 = new ArrayList<>();
            List<Rectangle> rm2 = new ArrayList<>();
            List<Rectangle> rm3 = new ArrayList<>();

            for (Ammunition amun : ammunitions) {
                amun.getRect().y += amun.getType().getStep() * Gdx.graphics.getDeltaTime();
                ArrayList<Integer> rm = new ArrayList<>();
                for (Rectangle amun2 : greenRocketRects) {
                    if (amun.getRect().overlaps(amun2)) {
                        rm1.add(amun);
                        rm3.add(amun2);
                    }
                }
                if (badShipRect.overlaps(amun.getRect()) & isBadShipAlive) {
                    badShipHP -= amun.getType().getDamage();
                    rm1.add(amun);
                }
                if (amun.getRect().y > game.HEIGHT)
                    rm1.add(amun);
            }

            for (Rectangle amun : greenLazerRects) {
                amun.y -= ammunitionTypes[0].getStep() * Gdx.graphics.getDeltaTime();
                if (amun.overlaps(shipRect)) {
                    hp -= GREEN_LAZER_DAMAGE;
                    rm2.add(amun);
                }
            }


            for (Rectangle amun : greenRocketRects) {
                amun.y -= ammunitionTypes[1].getStep() * Gdx.graphics.getDeltaTime();
                if (amun.x > shipRect.x + shipRect.width / 2)
                    amun.x -= ammunitionTypes[1].getStep() * Gdx.graphics.getDeltaTime();
                if (amun.x < shipRect.x + shipRect.width / 2)
                    amun.x += ammunitionTypes[1].getStep() * 1.1f * Gdx.graphics.getDeltaTime();
                if (amun.overlaps(shipRect)) {
                    hp -= GREEN_ROCKET_DAMAGE;
                    rm3.add(amun);
                }
            }


            for (Ammunition i : rm1) {
                int tmp = ammunitions.indexOf(i);
                if (tmp > -1)
                    ammunitions.remove(tmp);
            }


            for (Rectangle i : rm2) {
                int tmp = greenLazerRects.indexOf(i);
                if (tmp > -1)
                    greenLazerRects.remove(tmp);
            }


            for (Rectangle i : rm3) {
                int tmp = greenRocketRects.indexOf(i);
                if (tmp > -1)
                    greenRocketRects.remove(tmp);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
                try {
                    game.setScreen(new PauseScreen(game, this));
                } catch (IOException e) {
                    e.printStackTrace();
                    game.log.Log("Error: " + e.getMessage(), LogSystem.ERROR);
                    Gdx.app.exit();
                }

            ArrayList<Integer> indexes = new ArrayList<>();

            if (isBadShipAlive == false)
                try {
                    game.setScreen(new WinScreen(game));
                } catch (IOException e) {
                    e.printStackTrace();
                    game.log.Log("Error: " + e.getMessage(), LogSystem.ERROR);
                    Gdx.app.exit();
                }

            // --------------------- AI SECTION ---------------------

            if (isBadShipAlive) {
                //MOVE
                for (Ammunition amun : ammunitions) {
                    int cof = 1;
                    if (badShipRect.x + badShipRect.width == game.WIDTH)
                        isOnLeft = true;
                    if (badShipRect.x == 0)
                        isOnLeft = false;
                    if (isOnLeft)
                        cof = -1;
                    if ((badShipRect.x < amun.getRect().x + 50) & ((badShipRect.x + badShipRect.width) > amun.getRect().x)) {
                        moveBadShip(SHIP_ONE_STEP_KEY * 0.5f * Gdx.graphics.getDeltaTime() * cof);
                    }
                }

                if ((badShipRect.x - 30 < shipRect.x) & (badShipRect.x + badShipRect.width + 30 > shipRect.x) & (TimeUtils.nanoTime() - greenLazerLast > 100000000)) {
                    spawnGreenLazer(badShipRect.x + badShipRect.width / 2, badShipRect.y - 10);
                }

                if (TimeUtils.nanoTime() - greenRocketLast > 10000000000l) {
                    spawnGreenRocket(badShipRect.x + badShipRect.width / 2, badShipRect.y - 10);
                }

                if (TimeUtils.nanoTime() - lastCofChange > 4000000000l) {
                    lastCofChange = TimeUtils.nanoTime();
                    cof2 = MathUtils.randomBoolean() ? 1 : -1;
                }

            }

            // ----------------- END OF AI SECTION ------------------
            if (isBadShipAlive & badShipHP < 0)
                isBadShipAlive = false;

            if (hp <= 0)
                die();
            if (hp > 200)
                hp = 200;
        } catch (Exception e) {
            e.printStackTrace();
            game.log.Log("Error: " + e.getMessage(), LogSystem.FATAL);
        }

    }


    /**
     * Clean
     *
     * @since 2.1
     */
    @Override
    public void dispose() {
        game.log.Log("Disposing main game screen", LogSystem.INFO);
        camera = null;
        game = null;
        prop = null;
        shipImage.dispose();
        shipImage = null;
        spaceSound.dispose();
        spaceSound = null;
        shipRect = null;
    }

    /**
     * Show screen
     *
     * @since 2.1
     */
    @Override
    public void show() {
        try {
            prop.load(new FileInputStream("properties/defaults.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            game.log.Log("Error: " + e.getMessage(), LogSystem.ERROR);
            Gdx.app.exit();
        }
        showFPS = (boolean) game.settingsGame.get("FPS.show", Boolean.parseBoolean(prop.getProperty("settings.FPS.show")));
        game.log.Log("Show FPS - " + showFPS, LogSystem.INFO);
    }

    /**
     * Resize sgame
     *
     * @param width  - new width
     * @param height - new height
     * @since 2.1
     */
    @Override
    public void resize(int width, int height) {

    }

    /**
     * Pause game
     *
     * @since 2.1
     */
    @Override
    public void pause() {

    }

    /**
     * Resume game
     *
     * @since 2.1
     */
    @Override
    public void resume() {

    }

    /**
     * Hide game
     *
     * @since 2.1
     */
    @Override
    public void hide() {

    }
}