package com.waisblut.workouttimer.view;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.waisblut.workouttimer.Logger;
import com.waisblut.workouttimer.R;
import com.waisblut.workouttimer.service.MyIntentService;

import java.util.concurrent.TimeUnit;

public class FragmentMain
        extends Fragment
        implements OnClickListener
{
    protected SharedPreferences mSp;
    protected long mSeconds, mCurrentMillisecond;
    protected String mEditTime;
    @SuppressWarnings("unused")
    private AppState mAppState = null;
    private Intent mIntentService;
    private ResponseReceiver mReceiver;
    private View mView;
    private TextView mTxtTime;
    private ImageButton mBtnPlay, mBtnPause, mBtnResume, mBtnStop;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_main, container, false);

        super.onCreate(savedInstanceState);

        setBackground(mView, null);

        attachView(mView);

        setButtonsClick();

        //init();

        setPreferences();

        setCountDownTimerLength();

        mAppState = AppState.fromValue(mSp.getInt(Logger.STATE, 0));
        Logger.log('d', "STATE=" + mAppState.name());

        setScreenState();

        return mView;
    }

    @SuppressWarnings("deprecation")
    private void setBackground(View v, Drawable background)
    {
        if (Build.VERSION.SDK_INT < 16)
        {
            v.setBackgroundDrawable(background);
        }
        else
        {
            v.setBackground(background);
        }

    }

    private void setScreenState()
    {
        if (isMyServiceRunning(MyIntentService.class))
        {
            Logger.log('e', "SERVICE IS RUNNING");
            setUpPlayState();
        }
        else
        {
            switch (mAppState)
            {
            case PAUSED:
                mCurrentMillisecond = mSp.getLong(Logger.RESUME_TIME, 0l);
                setTxtText(mCurrentMillisecond);
                setUpPausedState();
                break;

            //            case PLAYING:
            //                setUpPlayState();
            //                break;

            default:
                setUpInitialState();
                break;
            }
        }
    }

    private void setUpPlayState()
    {
        showImgButton(mBtnPause);
        hideImgButton(mBtnPlay);
        hideImgButton(mBtnResume);

        toogleImgButton(mBtnStop, true);

        setCountDownTimerLength();

        setBackground(mView, getResources().getDrawable(R.drawable.background_red_state));

        mAppState = AppState.PLAYING;
        mSp.edit().putInt(Logger.STATE, mAppState.getCode()).apply();
    }

    private void setUpInitialState()
    {
        mAppState = AppState.STOPPED;

        showImgButton(mBtnPlay);
        hideImgButton(mBtnPause);
        hideImgButton(mBtnResume);

        toogleImgButton(mBtnStop, false);

        mCurrentMillisecond = 0;

        mView.setBackgroundColor(Color.GREEN);
        setBackground(mView, getResources().getDrawable(R.drawable.background_green_state));

        setTxtText(mSp.getLong(Logger.TIME, Logger.INITIALTIME));
        mSp.edit().putInt(Logger.STATE, mAppState.getCode()).apply();
    }

    private void registerReceiver()
    {
        mIntentService = new Intent(getActivity(), MyIntentService.class);
        mReceiver = new ResponseReceiver(this);
        IntentFilter filter = new IntentFilter(MyIntentService.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Logger.log('i', "DESTROYING FRAGMENT");

        mSp.edit().putInt(Logger.STATE, mAppState.getCode()).apply();

        switch (mAppState)
        {
        case PAUSED:
            mSp.edit().putLong(Logger.RESUME_TIME, mCurrentMillisecond).apply();
            break;

        case PLAYING:
            mSp.edit().putLong(Logger.RESUME_TIME, Logger.INITIALTIME).apply();
            break;

        case STOPPED:
            mSp.edit().putLong(Logger.RESUME_TIME, Logger.INITIALTIME).apply();
            break;

        }


    }

    @Override
    public void onResume()
    {
        super.onResume();

        setScreenState();

        registerReceiver();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver();
    }

    private void unregisterReceiver()
    {
        if (mReceiver != null) getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btnPlay:
            play();
            break;

        case R.id.btnPause:
            pause();
            break;

        case R.id.btnResume:
            resume();
            break;

        case R.id.btnStop:
            stop();
            break;
        }

        mBtnPlay.requestFocus();
    }

    private void showImgButton(ImageButton imgButton)
    {
        imgButton.setVisibility(View.VISIBLE);
    }

    private void hideImgButton(ImageButton imgButton)
    {
        imgButton.setVisibility(View.INVISIBLE);
    }

    private void toogleImgButton(ImageButton imgButton, Boolean b)
    {
        imgButton.setEnabled(b);
    }

    protected void attachView(View view)
    {
        mTxtTime = (TextView) view.findViewById(R.id.txtTime);
        mBtnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        mBtnPause = (ImageButton) view.findViewById(R.id.btnPause);
        mBtnResume = (ImageButton) view.findViewById(R.id.btnResume);
        mBtnStop = (ImageButton) view.findViewById(R.id.btnStop);
    }

    private void setButtonsClick()
    {
        mBtnPlay.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnResume.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnPlay.requestFocus();
    }

    private void setCountDownTimerLength()
    {
        try
        {
            mSeconds = Integer.parseInt(mEditTime) * 1000;
        }
        catch (Exception e)
        {
            mSeconds = mSp.getLong(Logger.TIME, Logger.INITIALTIME);
        }
    }

    protected void setTxtText(long millisUntilFinished)
    {
        String strFormat = "%02d:%02d";
        mTxtTime.setText("" + String.format(strFormat, TimeUnit.MILLISECONDS.toMinutes(
                                                    millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                                    TimeUnit.MILLISECONDS.toHours(
                                                            millisUntilFinished)),

                                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(
                                                    millisUntilFinished))));
    }

    private void play()
    {
        setUpPlayState();

        setCountDownTimerLength();

        //registerReceiver();

        mIntentService.putExtra(Logger.TIME, mSeconds);
        mIntentService.putExtra(Logger.VIBRATE, mSp.getBoolean(Logger.VIBRATE,
                                                               Logger.INITIALVIBRATE));

        getActivity().startService(mIntentService);
    }

    private void pause()
    {
        setUpPausedState();

        mSp.edit().putLong(Logger.RESUME_TIME, mCurrentMillisecond).apply();

        getActivity().stopService(mIntentService);
    }

    private void setUpPausedState()
    {
        mAppState = AppState.PAUSED;

        showImgButton(mBtnResume);
        hideImgButton(mBtnPause);
        hideImgButton(mBtnPlay);

        toogleImgButton(mBtnStop, true);

        setBackground(mView, getResources().getDrawable(R.drawable.background_yellow_state));

        mSp.edit().putInt(Logger.STATE, mAppState.getCode()).apply();
    }

    private void resume()
    {
        mAppState = AppState.PLAYING;

        showImgButton(mBtnPause);
        hideImgButton(mBtnResume);
        hideImgButton(mBtnPlay);

        toogleImgButton(mBtnStop, true);

        setBackground(mView, getResources().getDrawable(R.drawable.background_red_state));

        mIntentService.putExtra(Logger.TIME, mCurrentMillisecond);
        mIntentService.putExtra(Logger.VIBRATE, mSp.getBoolean(Logger.VIBRATE,
                                                               Logger.INITIALVIBRATE));

        getActivity().startService(mIntentService);

        mSp.edit().putInt(Logger.STATE, mAppState.getCode()).apply();
    }

    private void stop()
    {
        setUpInitialState();

        getActivity().stopService(mIntentService);
    }

    protected void setVibrate(boolean checked)
    {
        mSp.getBoolean(Logger.VIBRATE, checked);
    }

    protected void setPreferences()
    {
        mSp = getActivity().getPreferences(Context.MODE_PRIVATE);

        mSp.getBoolean(Logger.VIBRATE, Logger.INITIALVIBRATE);
        mSp.getLong(Logger.TIME, Logger.INITIALTIME);
        mSp.getLong(Logger.RESUME_TIME, 0l);
    }

    //    protected long convertStringToMillis(String s)
    //    {
    //        long ret;
    //
    //        try
    //        {
    //            ret = Long.parseLong(s) * 1000;
    //        }
    //        catch (Exception e)
    //        {
    //            ret = 0l;
    //        }
    //
    //        return ret;
    //    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    protected enum AppState
    {
        STOPPED(0),
        PLAYING(1),
        PAUSED(2);

        protected int code;

        AppState(int i)
        {
            this.code = i;
        }

        protected static AppState fromValue(int value)
        {
            for (AppState my : AppState.values())
            {
                if (my.code == value)
                {
                    return my;
                }
            }

            return null;
        }

        protected int getCode()
        {
            return code;
        }
    }

    public static class ResponseReceiver
            extends BroadcastReceiver
    {
        private FragmentMain fragmentMain;

        @SuppressWarnings("UnusedDeclaration")
        public ResponseReceiver()
        {

        }

        public ResponseReceiver(FragmentMain fragmentMain) {this.fragmentMain = fragmentMain;}

        @Override
        public void onReceive(Context context, Intent intent)
        {
            fragmentMain.mCurrentMillisecond = intent.getLongExtra(MyIntentService.CURRENT_TIME,
                                                                   99l);
            fragmentMain.setTxtText(fragmentMain.mCurrentMillisecond);

            if (intent.getBooleanExtra(MyIntentService.IS_FINISHED, false))
            {
                fragmentMain.stop();
            }
        }
    }
}