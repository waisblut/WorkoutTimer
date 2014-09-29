package com.waisblut.workouttimer.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.waisblut.workouttimer.Logger;

import java.util.Timer;
import java.util.TimerTask;


public class MyIntentService
        extends IntentService
{
    public static final String CURRENT_TIME = "current_time";
    public static final String IS_FINISHED = "is_finished";
    public static final String ACTION_RESP = "com.waisblut.workouttimer.intent.action.MESSAGE_PROCESSED";
    Intent mBroadcastIntent;
    private long mTime;
    private boolean mVibrate;
    private Timer timer;

    public MyIntentService()
    {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Logger.log('i', "Service Started");

        mTime = intent.getLongExtra(Logger.TIME, Logger.INITIALTIME) + System.currentTimeMillis();
        mVibrate = intent.getBooleanExtra(Logger.VIBRATE, Logger.INITIALVIBRATE);
        Logger.log('d', "Tempo = " + mTime);
        Logger.log('d', "Vibrate = " + mVibrate);


        mBroadcastIntent = new Intent();
        mBroadcastIntent.setAction(ACTION_RESP);
        mBroadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        //setCounter();
        setCounter2();

        try
        {
            Thread.sleep(intent.getLongExtra(Logger.TIME, Logger.INITIALTIME));
        }
        catch (Exception e)
        {

        }


        //Looper.loop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Logger.log('i', "Service Destroyed");
        timer.cancel();
    }

    //    private void setCounter()
    //    {
    //        new CountDownTimer(mTime, 500)
    //        {
    //            @Override
    //            public void onTick(long millisUntilFinished)
    //            {
    //                Logger.log('d', "Ticking...." + (millisUntilFinished / 1000));
    //                sendMessage(millisUntilFinished, false);
    //
    //            }
    //
    //            @Override
    //            public void onFinish()
    //            {
    //                Logger.log('d', "CountDown Finished");
    //
    //                if (mVibrate)
    //                {
    //                    vibrate(500);
    //                }
    //
    //                playSound();
    //
    //                sendMessage(0l, true);
    //
    //                Looper.myLooper().quit();
    //            }
    //
    //            private void sendMessage(long millisUntilFinished, boolean isFinished)
    //            {
    //                mBroadcastIntent.putExtra(CURRENT_TIME, millisUntilFinished);
    //                mBroadcastIntent.putExtra(IS_FINISHED, isFinished);
    //                sendBroadcast(mBroadcastIntent);
    //            }
    //        }.start();
    //    }

    private void setCounter2()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                long timeLeft = (mTime - System.currentTimeMillis());

                if (timeLeft >= 0)
                {
                    Logger.log('d', "Ticking...." + (timeLeft));
                    sendMessage(timeLeft, false);
                }
                else
                {
                    Logger.log('d', "DONE");

                    if (mVibrate)
                    {
                        vibrate(500);
                    }

                    playSound();

                    sendMessage(timeLeft, true);

                    timer.cancel();
                }
            }

            private void sendMessage(long millisUntilFinished, boolean isFinished)
            {
                mBroadcastIntent.putExtra(CURRENT_TIME, millisUntilFinished);
                mBroadcastIntent.putExtra(IS_FINISHED, isFinished);
                sendBroadcast(mBroadcastIntent);
            }
        }, 0, 500);
    }

    private void vibrate(int i)
    {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(i);
    }

    private void playSound()
    {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), alarmSound);
        mp.start();
    }
}