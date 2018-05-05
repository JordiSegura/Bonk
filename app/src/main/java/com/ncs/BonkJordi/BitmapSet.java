package com.ncs.BonkJordi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

class BitmapSet {

    private Bitmap[] bitmaps;

    Bitmap getBitmap(int index) {
        return bitmaps[index];
    }

    BitmapSet(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Bitmap bitmapsBMP = BitmapFactory.decodeResource(context.getResources(), com.ncs.BonkJordi.R.raw.bonk, opts);
        Matrix rot1 = new Matrix();
        Matrix rot2 = new Matrix();
        rot2.setScale(-1, 1);

        InputStream in = context.getResources().openRawResource(com.ncs.BonkJordi.R.raw.bonkinfo);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(":");
                if (parts.length != 7) continue;
                int id = Integer.parseInt(parts[0]);
                count = (id > count) ? id : count;
            }
            bitmaps = new Bitmap[count + 1];
            in.reset();
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(":");
                if (parts.length != 7) continue;    // empty lines are skipped
                int id = Integer.parseInt(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int w = Integer.parseInt(parts[3]);
                int h = Integer.parseInt(parts[4]);
                int r = Integer.parseInt(parts[5]);
                Matrix m = (r == 1) ? rot2 : rot1;
                Bitmap bitmap = Bitmap.createBitmap(bitmapsBMP, x, y, w, h, m, true);
                bitmaps[id] = bitmap;
            }
            reader.close();
        } catch (Exception ignored) {
        }
        bitmapsBMP.recycle();
    }
}
