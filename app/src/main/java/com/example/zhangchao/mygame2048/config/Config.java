package com.example.zhangchao.mygame2048.config;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by ZhangChao on 2016/9/11.
 */
public class Config extends Application {

    public static SharedPreferences mSp;
    public static int mGameGoal;
    public static int mGameLines;
    public static int mItemSize;
    public static int SCORE=0;
    public static String SP_HIGH_SCORE="SP_HIGHSCORE";
    public static String KEY_HIGH_SCORE="KEY_HighScore";
    public static String KEY_GAME_LINES = "KEY_GAMELINES";

    public static String KEY_GAME_GOAL = "KEY_GameGoal";

    @Override
    public void onCreate() {
        super.onCreate();
        mSp=getSharedPreferences(SP_HIGH_SCORE,0);
        mGameGoal=mSp.getInt(KEY_GAME_LINES,4);
        mGameGoal = mSp.getInt(KEY_GAME_GOAL, 2048);
        mItemSize = 0;
    }
}
