package com.example.zhangchao.mygame2048.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.GridLayout;

import com.example.zhangchao.mygame2048.activity.Game;
import com.example.zhangchao.mygame2048.bean.GameItem;
import com.example.zhangchao.mygame2048.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangChao on 2016/9/11.
 */
public class GameView extends GridLayout implements View.OnTouchListener {
    private GameItem[][] mGameMatrix;
    private List<Point> mBlanks;
    private int mGameLines;
    private int mStartX,mStartY,mEndX,mEndY;
    private List<Integer> mCallList;
    private int mKeyItemNum=-1;
    private int[][]mGameMatrixHistory;
    private int mScoreHistory;
    private int mHighScore;
    private int mTarget;


    public GameView(Context context) {
        super(context);
        mTarget= Config.mSp.getInt(Config.KEY_GAME_GOAL,2048);
        initGameMatrix();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameMatrix();
//        initGameView(Config.mItemSize);
    }

    private void initGameMatrix() {
        removeAllViews();
        mScoreHistory=0;
        Config.SCORE=0;
        Config.mGameLines=Config.mSp.getInt(Config.KEY_GAME_LINES,4);
        mGameLines=Config.mGameLines;
        mGameMatrix=new GameItem[mGameLines][mGameLines];
        mGameMatrixHistory=new int[mGameLines][mGameLines];
        mCallList=new ArrayList<Integer>();
        mBlanks=new ArrayList<Point>();
        mHighScore=Config.mSp.getInt(Config.KEY_HIGH_SCORE,0);
        setColumnCount(mGameLines);
        setRowCount(mGameLines);
        setOnTouchListener(this);
        DisplayMetrics metrics=new DisplayMetrics();
        WindowManager wm= (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display display=wm.getDefaultDisplay();
        display.getMetrics(metrics);
        Config.mItemSize=metrics.widthPixels/Config.mGameLines;
        initGameView(Config.mItemSize);
    }

    private void initGameView(int cardSize) {
        removeAllViews();
        GameItem card;
        for (int i=0;i<mGameLines;i++)
        {
            for (int j=0;j<mGameLines;j++)
            {
                card=new GameItem(getContext(),0);
                addView(card,cardSize,cardSize);
                mGameMatrix[i][j]=card;
                mBlanks.add(new Point(i,j));
            }
        }

        addRandomNum();
        addRandomNum();

    }

    private void addRandomNum() {
        getBlanks();
        if(mBlanks.size()>0)
        {
            int randomNum= (int) (Math.random()*mBlanks.size());
            Point randomPoint=mBlanks.get(randomNum);
            mGameMatrix[randomPoint.x][randomPoint.y].setNum(
                    Math.random()>0.2d?2:4);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                saveHistoryMatrix();
                mStartX= (int) event.getX();
                mStartY= (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mEndX= (int) event.getX();
                mEndY= (int) event.getY();
                judgeDirection(mEndX-mStartX,mEndY-mStartY);
                if(isMoved())
                {
                    addRandomNum();
                    Game.getGameActivity().setScore(Config.SCORE,0);
                }
                checkCompleted();
                break;
        }
        return true;
    }

    private void saveHistoryMatrix() {
        mScoreHistory = Config.SCORE;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                mGameMatrixHistory[i][j] = mGameMatrix[i][j].getNum();
            }
        }
    }

    private int getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return (int) metrics.density;
    }

    private void judgeDirection(int offsetX, int offsetY) {
        int density=getDeviceDensity();
        int slideDis=5*density;
        int maxDis=200*density;
        boolean flagNormal=(Math.abs(offsetX)>slideDis||
        Math.abs(offsetY)>slideDis)&&(Math.abs(offsetX)<maxDis)&&
                (Math.abs(offsetY)<maxDis);
        boolean flagSuper=Math.abs(offsetX)>maxDis||Math.abs(offsetY)>maxDis;
        if(flagNormal&&!flagSuper)
        {
            if (Math.abs(offsetX)>Math.abs(offsetY))
            {
                if(offsetX>slideDis)
                    swipeRight();
                else
                    swipeLeft();
            }else
            {
                if(offsetY>slideDis)
                    swipeDown();
                else
                    swipeUp();
            }
        }else if(flagSuper)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            final EditText et=new EditText(getContext());
            builder.setTitle("Back Door")
                    .setView(et)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(!TextUtils.isEmpty(et.getText()))
                            {
                                addSuperNum(Integer.parseInt(et.getText().toString()));
                                checkCompleted();
                            }
                        }
                    })
                    .setNegativeButton("ByeBye", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }


    }

    private void addSuperNum(int num) {
        if(checkSuperNum(num)){
            getBlanks();
            if(mBlanks.size()>0)
            {
                int randomNum= (int) (Math.random()*mBlanks.size());
                Point randomPoint=mBlanks.get(randomNum);
                mGameMatrix[randomPoint.x][randomPoint.y].setNum(num);
                animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
            }
        }
    }

    private void animCreate(GameItem target) {
        ScaleAnimation sa=new ScaleAnimation(0.1f,1,0.1f,1,
                Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        sa.setDuration(100);
        target.setAnimation(null);
        target.getItemView().startAnimation(sa);
    }

    private boolean checkSuperNum(int num) {
        boolean flag = (num == 2 || num == 4 || num == 8 || num == 16
                || num == 32 || num == 64 || num == 128 || num == 256
                || num == 512 || num == 1024);
        return flag;
    }

    private void swipeRight() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCallList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCallList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCallList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCallList.size(); j++) {
                mGameMatrix[i][j].setNum(0);
            }
            int index = mCallList.size() - 1;
            for (int m = mGameLines - mCallList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(mCallList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCallList.clear();
            index = 0;
        }
    }

    private void swipeLeft() {
        for(int i=0;i<mGameLines;i++)
        {
            for(int j=0;j<mGameLines;j++)
            {
                int currentNum=mGameMatrix[i][j].getNum();
                if(currentNum!=0)
                {
                    if(mKeyItemNum==-1)
                    {
                        mKeyItemNum=currentNum;
                    }else
                    {
                        if(mKeyItemNum==currentNum)
                        {
                            mCallList.add(mKeyItemNum*2);
                            Config.SCORE+=mKeyItemNum*2;
                            mKeyItemNum=-1;
                        }else
                        {
                            mCallList.add(currentNum);
                            mKeyItemNum=currentNum;
                        }
                    }
                }else
                    continue;
            }
            if(mKeyItemNum!=-1)
                mCallList.add(mKeyItemNum);
            for (int j=0;j<mCallList.size();j++)
                mGameMatrix[i][j].setNum(mCallList.get(j));
            for (int m=mCallList.size();m<mGameLines;m++)
            {
                mGameMatrix[i][m].setNum(0);
            }
            mKeyItemNum=-1;
            mCallList.clear();
        }
    }

    private void swipeDown() {
        for (int i=mGameLines-1;i>=0;i--)
        {
            for (int j=mGameLines-1;j>=0;j--)
            {
                int currentNum=mGameMatrix[j][i].getNum();
                if(currentNum!=0)
                {
                    if (mKeyItemNum==-1)
                        mKeyItemNum=currentNum;
                    else
                    {
                        if(mKeyItemNum==currentNum){
                            mCallList.add(mKeyItemNum*2);
                            Config.SCORE+=mKeyItemNum*2;
                            mKeyItemNum=-1;
                        }else
                        {
                            mCallList.add(mKeyItemNum);
                            mKeyItemNum=currentNum;
                        }

                    }
                }else
                    continue;
            }
            if(mKeyItemNum!=-1)
                mCallList.add(mKeyItemNum);

            for (int j=0;j<mGameLines-mCallList.size();j++)
            {
                mGameMatrix[i][j].setNum(0);
            }
            int index=mCallList.size()-1;
            for (int m=mGameLines-mCallList.size();m<mGameLines;m++)
            {
                mGameMatrix[m][i].setNum(mCallList.get(index));
                index--;
            }

            // 重置行参数
            mKeyItemNum = -1;
            mCallList.clear();
            index = 0;
        }
    }

    private void swipeUp() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCallList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCallList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCallList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mCallList.size(); j++) {
                mGameMatrix[j][i].setNum(mCallList.get(j));
            }
            for (int m = mCallList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(0);
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCallList.clear();
        }
    }

    private void checkCompleted() {
        int result=checkNums();
        if (result==0)
        {
           if(Config.SCORE>mHighScore)
           {
               SharedPreferences.Editor editor=Config.mSp.edit();
               editor.putInt(Config.KEY_HIGH_SCORE,Config.SCORE);
               editor.apply();
               Game.getGameActivity().setScore(Config.SCORE,1);
               Config.SCORE=0;
           }
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            builder.setTitle("Game Over")
                    .setPositiveButton("Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGame();
                        }
                    }).create().show();
            Config.SCORE=0;
        }else if (result == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Mission Accomplished")
                    .setPositiveButton("Again",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    // 重新开始
                                    startGame();
                                }
                            })
                    .setNegativeButton("Continue",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    // 继续游戏 修改target
                                    SharedPreferences.Editor editor = Config.mSp.edit();
                                    if (mTarget == 1024) {
                                        editor.putInt(Config.KEY_GAME_GOAL, 2048);
                                        mTarget = 2048;
                                        Game.getGameActivity().setGoal(2048);
                                    } else if (mTarget == 2048) {
                                        editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                        mTarget = 4096;
                                        Game.getGameActivity().setGoal(4096);
                                    } else {
                                        editor.putInt(Config.KEY_GAME_GOAL, 4096);
                                        mTarget = 4096;
                                        Game.getGameActivity().setGoal(4096);
                                    }
                                    editor.apply();
                                }
                            }).create().show();
            Config.SCORE = 0;
        }
    }

    public void startGame() {
        initGameMatrix();
        initGameView(Config.mItemSize);
    }

    private int checkNums() {
        getBlanks();
        if(mBlanks.size()==0)
        {
            for (int i=0;i<mGameLines;i++)
            {
                for(int j=0;j<mGameLines;j++)
                {
                    if(j<mGameLines-1)
                    {
                        if (mGameMatrix[i][j].getNum()==mGameMatrix[i][j+1].getNum())
                            return 1;
                    }
                    if (i<mGameLines-1)
                    {
                        if (mGameMatrix[i][j].getNum()==mGameMatrix[i+1][j].getNum())
                            return 1;
                    }
                }
            }
            return 0;
        }
        for (int i=0;i<mGameLines;i++)
        {
            for (int j=0;j<mGameLines;j++)
            {
                if(mGameMatrix[i][j].getNum()==mTarget)
                    return 2;
            }
        }
        return 1;
    }

    public void getBlanks() {
        mBlanks.clear();
        for (int i=0;i<mGameLines;i++)
        {
            for (int j=0;j<mGameLines;j++)
            {
                if (mGameMatrix[i][j].getNum()==0)
                    mBlanks.add(new Point(i,j));
            }
        }
    }

    public boolean isMoved() {
        for (int i=0;i<mGameLines;i++)
        {
            for (int j=0;j<mGameLines;j++)
            {
                if(mGameMatrixHistory[i][j]!=mGameMatrix[i][j].getNum())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void revertGame() {
        int sum=0;
        for(int []element:mGameMatrixHistory)
        {
            for (int i:element)
                sum+=i;
        }
        if(sum!=0)
        {
            Game.getGameActivity().setScore(mScoreHistory,0);
            Config.SCORE=mScoreHistory;
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    mGameMatrix[i][j].setNum(mGameMatrixHistory[i][j]);
                }
            }
        }
    }
}
