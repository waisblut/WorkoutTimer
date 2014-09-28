package com.waisblut.workouttimer.view;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.waisblut.workouttimer.Logger;
import com.waisblut.workouttimer.R;

public class ActivityMain
        extends FragmentActivity
{
    Dialog mDlgEditTime, mDlgAbout;
    EditText mEdtEditTime;
    SharedPreferences mSp;
    Long mMilliseconds;
    private FragmentMain mFragmentMain;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 14)
        {
            if (getActionBar() != null) getActionBar().setIcon(R.drawable.ic_gym_white);
        }

        this.getWindow().setBackgroundDrawable(null);

        if (savedInstanceState == null)
        {
            mFragmentMain = new FragmentMain();
            getFragmentManager().beginTransaction().add(R.id.container, mFragmentMain).commit();
            mSp = getPreferences(Context.MODE_PRIVATE);
        }

        setUpDialogEditTime();
        setUpDialogAbout();
    }

    @Override
    public void onAttachFragment(Fragment fragment)
    {
        super.onAttachFragment(fragment);
        Logger.log('d', "ATTACHED FRAGMENT");
        mFragmentMain = (FragmentMain) fragment;

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Logger.log('i', "RESUMING ACTIVITY");
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        Logger.log('i', "RESTARTING ACTIVITY");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mDlgAbout.dismiss();
        mDlgEditTime.dismiss();
        Logger.log('i', "DESTROYING ACTIVITY");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        Logger.log('i', "RESTORING SAVED INSTANCE");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem itemVibrate = menu.findItem(R.id.action_vibrate);

        if (mSp == null)
        {
            mSp = getPreferences(Context.MODE_PRIVATE);
        }

        itemVibrate.setChecked(mSp.getBoolean(Logger.VIBRATE, Logger.INITIALVIBRATE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_tempo:
            mEdtEditTime.setText("");
            mDlgEditTime.show();
            break;

        case R.id.action_vibrate:
            item.setChecked(!item.isChecked());
            mFragmentMain.setVibrate(item.isChecked());
            mSp.edit().putBoolean(Logger.VIBRATE, item.isChecked()).apply();
            break;

        case R.id.action_about:
            mDlgAbout.show();

        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpDialogEditTime()
    {
        mDlgEditTime = new Dialog(this);
        mDlgEditTime.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlgEditTime.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDlgEditTime.setContentView(R.layout.dialog_edit_time);

        mDlgEditTime.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        mEdtEditTime = new EditText(this);
        mEdtEditTime = (EditText) mDlgEditTime.findViewById(R.id.dialog_edit_time_edtTime);

        setKeyPress();

        setTextWatcher();
    }

    private void setUpDialogAbout()
    {
        Button btnAbout;
        mDlgAbout = new Dialog(this);

        mDlgAbout.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlgAbout.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDlgAbout.setContentView(R.layout.dialog_about);

        btnAbout = (Button) mDlgAbout.findViewById(R.id.dialog_about_btnOk);

        btnAbout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mDlgAbout.dismiss();
            }
        });
    }

    private void setKeyPress()
    {
        mEdtEditTime.setOnKeyListener(new OnKeyListener()
        {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    Logger.log('d', "ENTER");
                    mDlgEditTime.dismiss();

                    mFragmentMain.mEditTime = mEdtEditTime.getText().toString();

                    mSp.edit().putLong(Logger.TIME, mMilliseconds).apply();

                    return true;
                }
                return false;
            }
        });
    }

    private void setTextWatcher()
    {
        TextWatcher textWatcher = new TextWatcher()
        {
            private boolean repeating = false;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (repeating)
                {
                    repeating = false;
                    return;
                }

                s = unMask(s);

                repeating = true;

                s = setUpString(s);

                mEdtEditTime.setText(s);

                if (unMask(s).length() > 0)
                {
                    mEdtEditTime.setSelection(s.length() - 1);
                }
                else
                {
                    mEdtEditTime.setSelection(s.length());
                }

                if (getLongMillis(s) > Logger.MAXIMUMTIME)
                {
                    mMilliseconds = Logger.MAXIMUMTIME;
                }
                else
                {
                    mMilliseconds = getLongMillis(s);
                }


                mFragmentMain.setTxtText(mMilliseconds);
            }

            private CharSequence setUpString(CharSequence s)
            {
                if (s.length() > 2)
                {
                    s = inserirDoisPontos(s);
                }

                if (unMask(s).length() > 0)
                {
                    s = inserirS(s);
                }

                if (unMask(s).length() > 2)
                {
                    s = inserirM(s);
                }
                return s;
            }

            private Long getLongMillis(CharSequence s)
            {
                String strMin = "0", strSeg;
                Long lMillis = 0l;

                if (s.toString().contains(":"))
                {
                    strMin = unMask(s.toString().split(":")[0]).toString();
                    strSeg = unMask(s.toString().split(":")[1]).toString();
                }
                else
                {
                    strSeg = unMask(s).toString();
                }

                try
                {
                    lMillis += Long.parseLong(strMin) * 60000;
                }
                catch (Exception ignored)
                {
                }

                try
                {
                    lMillis += Long.parseLong(strSeg) * 1000;
                }
                catch (Exception ignored)
                {

                }

                return lMillis;
            }

            private CharSequence unMask(CharSequence s)
            {
                s = s.toString().replace(":", "").replace("s", "").replace("m", "");

                return s;
            }

            private CharSequence inserirDoisPontos(CharSequence s)
            {
                StringBuilder sb = new StringBuilder(s.toString());

                sb.insert(s.length() - 2, ':');

                s = sb.toString();

                return s;
            }

            private CharSequence inserirS(CharSequence s)
            {
                StringBuilder sb = new StringBuilder(s.toString());

                sb.insert(s.length(), 's');

                s = sb.toString();

                return s;
            }

            private CharSequence inserirM(CharSequence s)
            {
                StringBuilder sb = new StringBuilder(s.toString());

                sb.insert(sb.indexOf(":"), "m");

                s = sb.toString();

                return s;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                //mFragmentMain.setTxtText(mFragmentMain.convertStringToMillis(s.toString()));
                //mFragmentMain.mEditTime = mEdtEditTime.getText().toString();
            }
        };

        mEdtEditTime.addTextChangedListener(textWatcher);
    }
}