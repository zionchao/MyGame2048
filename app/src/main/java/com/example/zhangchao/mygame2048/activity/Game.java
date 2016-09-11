package com.example.zhangchao.mygame2048.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zhangchao.mygame2048.R;
import com.example.zhangchao.mygame2048.config.Config;
import com.example.zhangchao.mygame2048.view.GameView;

public class Game extends Activity implements View.OnClickListener{

    private static Game mGame;
    private TextView mTvScore;
    private TextView mTvHighScore;
    private int mHighScore;
    private TextView mTvGoal;
    private int mGoal;
    private Button mBtnRestart;
    private Button mBtnRevert;
    private Button mBtnOptions;
    private GameView mGameView;

    public Game()
    {
        mGame=this;
    }
    public static Game getGameActivity()
    {
        return mGame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mGameView=new GameView(this);
        FrameLayout frameLayout= (FrameLayout) findViewById(R.id.game_panel);
        RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.game_panel_rl);
        relativeLayout.addView(mGameView);
    }

    private void initView() {
        mTvScore = (TextView) findViewById(R.id.score);
        mTvGoal = (TextView) findViewById(R.id.tv_Goal);
        mTvHighScore = (TextView) findViewById(R.id.record);
        mBtnRestart = (Button) findViewById(R.id.btn_restart);
        mBtnRevert = (Button) findViewById(R.id.btn_revert);
        mBtnOptions = (Button) findViewById(R.id.btn_option);
        mBtnRestart.setOnClickListener(this);
        mBtnRevert.setOnClickListener(this);
        mBtnOptions.setOnClickListener(this);
        mHighScore = Config.mSp.getInt(Config.KEY_HIGH_SCORE, 0);
        mGoal = Config.mSp.getInt(Config.KEY_GAME_GOAL, 2048);
        mTvHighScore.setText("" + mHighScore);
        mTvGoal.setText("" + mGoal);
        mTvScore.setText("0");
        setScore(0, 0);
    }

    public void setScore(int score, int flag) {
        switch (flag) {
            case 0:
                mTvScore.setText("" + score);
                break;
            case 1:
                mTvHighScore.setText("" + score);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_restart:
                mGameView.startGame();
                setScore(0,0);
                break;
            case R.id.btn_revert:
                mGameView.revertGame();
                break;
        }
    }

    public void setGoal(int num) {
        mTvGoal.setText(String.valueOf(num));
    }
}
