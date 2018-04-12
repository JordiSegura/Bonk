package com.ncs.plataformes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ncs.plataformes.characters.Bonk;
import com.ncs.plataformes.characters.Box;
import com.ncs.plataformes.characters.Coin;
import com.ncs.plataformes.characters.Crab;
import com.ncs.plataformes.characters.Enemy;

public class Scene {
    private GameEngine gameEngine;
    private String scene[];
    private Paint paint;

    private int sceneWidth, sceneHeight;
    private SparseIntArray CHARS;
    private String GROUND, WALLS;
    private int WATERLEVEL, SKY, WATERSKY, WATER;

    private Bonk bonk;

    private List<Coin> coins;
    private List<Enemy> enemies;
    private List<Box> boxes;

    int spawnX;
    int spawnY;

    int score;
    int lives;

    Scene(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        paint = new Paint();
        CHARS = new SparseIntArray();
        WATERLEVEL = 999;
        score = 0;
        lives = 3;
        coins = new ArrayList<>();
        enemies = new ArrayList<>();
        boxes = new ArrayList<>();
    }

    void loadFromFile(int resource) {
        InputStream res = gameEngine.getContext().getResources().openRawResource(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(res));
        List<String> lines = new ArrayList<>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                line = line.trim();
                if (!line.contains("=")) continue;                  // NO VALID LINE
                if (line.startsWith("=")) continue;                 // COMMENT
                String[] parts = line.split("=", 2);
                String cmd = parts[0].trim();
                String args = parts[1].trim();
                String[] parts2;
                switch (cmd) {
                    case "SCENE":
                        lines.add(args);
                        break;
                    case "CHARS":
                        parts2 = args.split(" ");
                        for (String def : parts2) {
                            String[] item = def.split("=");
                            if (item.length != 2) continue;
                            char c = item[0].trim().charAt(0);
                            int idx = Integer.parseInt(item[1].trim());
                            CHARS.put(c, idx);
                        }
                        break;
                    case "GROUND":
                        GROUND = args;
                        break;
                    case "WALLS":
                        WALLS = args;
                        break;
                    case "WATER":
                        parts2 = args.split(",");
                        if (parts2.length != 4) continue;
                        WATERLEVEL = Integer.parseInt(parts2[0].trim());
                        SKY = Integer.parseInt(parts2[1].trim());
                        WATERSKY = Integer.parseInt(parts2[2].trim());
                        WATER = Integer.parseInt(parts2[3].trim());
                        break;
                    case "COIN":
                        parts2 = args.split(",");
                        if (parts2.length != 2) continue;
                        int coinX = Integer.parseInt(parts2[0].trim()) * 16;
                        int coinY = Integer.parseInt(parts2[1].trim()) * 16;
                        Coin coin = new Coin(gameEngine, coinX, coinY);
                        coins.add(coin);
                        break;
                    case "BOX":
                        parts2 = args.split(",");
                        if (parts2.length != 2) continue;
                        int boxX = Integer.parseInt(parts2[0].trim()) * 16;
                        int boxY = Integer.parseInt(parts2[1].trim()) * 16;
                        Box box = new Box(gameEngine, boxX, boxY);
                        boxes.add(box);
                        break;
                    case "CRAB":
                        parts2 = args.split(",");
                        if (parts2.length != 3) continue;
                        int crabX0 = Integer.parseInt(parts2[0].trim()) * 16;
                        int crabX1 = Integer.parseInt(parts2[1].trim()) * 16;
                        int crabY = Integer.parseInt(parts2[2].trim()) * 16;
                        Crab crab = new Crab(gameEngine, crabX0, crabX1, crabY);
                        enemies.add(crab);
                        break;
                    case "SPAWN":
                        parts2 = args.split(",");
                        if (parts2.length != 2) continue;
                        this.spawnX = Integer.parseInt(parts2[0]) * 16;
                        this.spawnY = Integer.parseInt(parts2[1]) * 16;
                        break;
                }
            }
            scene = lines.toArray(new String[0]);
            reader.close();
            sceneHeight = scene.length;
            sceneWidth = scene[0].length();
        } catch (IOException e) {
            Toast.makeText(gameEngine.getContext(), "Error loading scene:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isGround(int r, int c) {
        if (r < 0) return false;
        if (r >= sceneHeight) return false;
        if (c < 0) return false;
        if (c >= sceneWidth) return false;
        char sc = scene[r].charAt(c);
        return (GROUND.indexOf(sc) != -1);
    }

    public boolean isWall(int r, int c) {
        if (r < 0) return false;
        if (r >= sceneHeight) return false;
        if (c < 0) return false;
        if (c >= sceneWidth) return false;
        char sc = scene[r].charAt(c);
        return (WALLS.indexOf(sc) != -1);
    }

    public int getSceneWidth() {
        return sceneWidth;
    }

    public int getSceneHeight() {
        return sceneHeight;
    }

    public int getWaterLevel() {
        return WATERLEVEL;
    }

    public int getWidth() {
        return sceneWidth * 16;
    }

    public int getHeight() {
        return sceneHeight * 16;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    // Scene physics
    void physics(int delta) {

        bonk = gameEngine.getBonk();

        for (Coin coin : coins) coin.physics(delta);
        for (Enemy enemy : enemies) enemy.physics(delta);
        for (Box box : boxes) box.physics(delta);

        Rect bonkRect = bonk.getCollisionRect();

        if (bonkRect != null) {

            for (int i = coins.size() - 1; i >= 0; i--) {
                Coin coin = coins.get(i);
                if (bonkRect.intersect(coin.getCollisionRect())) {
                    gameEngine.getAudio().coin();
                    coins.remove(coin);
                    score += 10;
                    Log.d("ncs", "Score: " + score);
                }
            }

            for (Enemy enemy : enemies) {
                if (bonkRect.intersect(enemy.getCollisionRect())) {
                    gameEngine.getAudio().die();
                    bonk.die();
                    lives--;
                    Log.d("ncs", "Lives: " + lives);
                }
            }

            for (Box box : boxes) {
                if (bonkRect.intersect(box.getCollisionRect())) {
                    gameEngine.win();
                }
            }
        }
    }

    // Scene draw
    void draw(Canvas canvas, int offsetX, int offsetY, int screenWidth, int screenHeight) {
        if (scene == null) return;

        // Compute which tiles will be drawn
        int l = Math.max(0, offsetX / 16);
        int r = Math.min(scene[0].length(), offsetX / 16 + screenWidth / 16 + 2);
        int t = Math.max(0, offsetY / 16);
        int b = Math.min(scene.length, offsetY / 16 + screenHeight / 16 + 2);

        // Do the x-y loops over the visible scene
        for (int y = t; y < b; y++) {

            // Compute the background index (sky / water)
            int bgIdx = SKY;
            if (y == WATERLEVEL) bgIdx = WATERSKY;
            else if (y > WATERLEVEL) bgIdx = WATER;
            Bitmap bgBitmap = gameEngine.getBitmap(bgIdx);

            for (int x = l; x < r; x++) {
                // Draw the background tile
                canvas.drawBitmap(bgBitmap, x * 16, y * 16, paint);

                // Compute the bitmap index for the current tile
                char c = scene[y].charAt(x);
                int index = CHARS.get(c);
                if (index == SKY) continue;
                Bitmap bitmap = gameEngine.getBitmap(index);
                canvas.drawBitmap(bitmap, x * 16, y * 16, paint);
            }
        }

        for (Coin coin : coins) coin.draw(canvas);
        for (Enemy enemy : enemies) enemy.draw(canvas);
        for (Box box : boxes) box.draw(canvas);

    }
}
