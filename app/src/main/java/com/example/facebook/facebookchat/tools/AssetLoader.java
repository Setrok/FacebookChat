package com.example.facebook.facebookchat.tools;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by TilSeier on 06.12.2017.
 */

public class AssetLoader {

    private static Typeface fontMain;
    private static Typeface fontMainBold;

    private static boolean isLoaded = false;

    public static void loadAssets(Context context){

        if (!isLoaded) {

            //FONTS
            fontMain = Typeface.createFromAsset(context.getAssets(), "fonts/Centaur.ttf");
            fontMainBold = Typeface.createFromAsset(context.getAssets(), "fonts/MtCentaurBold.otf");

            isLoaded = true;

        }

    }

    public static Typeface getMainFont(){
        return fontMain;
    }

    public static Typeface getMainBoldFont(){
        return fontMainBold;
    }

}
