package com.ncs.plataformes.characters;

import android.graphics.Canvas;

import com.ncs.plataformes.GameEngine;

public class Box extends Character {

    public Box(GameEngine gameEngine, int x, int y) {
        super(gameEngine, x, y);
    }

    private static final int[][] ANIMATIONS = new int[][]{
            new int[]{38}
    };

//    Això fa que es dibuixi una caixa més damunt
    @Override
    public void draw(Canvas canvas) {
        try {
            int[] animation = getAnimations()[state];
            int bitmap = animation[sprite];
            canvas.drawBitmap(gameEngine.getBitmap(bitmap), x, y, null);
            canvas.drawBitmap(gameEngine.getBitmap(bitmap), x, y - 16, null);
            sprite++;
            sprite %= animation.length;
            if (collisionRect != null) canvas.drawRect(collisionRect, super.getPaint());
        } catch (Exception ignored) {
        }
    }

    @Override
    void updatePhysics(int delta) {

    }

//    El top és y - 16 per a que detecti la segona caixa dibuixada (veure la funció draw)
    @Override
    void updateCollisionRect() {
        collisionRect.set(x, y - 16, x + 16, y + 16);
    }

    @Override
    int[][] getAnimations() {
        return ANIMATIONS;
    }
}
