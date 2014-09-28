package com.waisblut.workouttimer;

import android.util.Log;

public final class Logger
{
    //Project Constants
    public final static String TIME = "TIME";
    public final static String VIBRATE = "VIBRATE";
    public final static String STATE = "STATE";
    public final static String RESUME_TIME = "RESUME";

    public final static long INITIALTIME = 30000;
    public final static boolean INITIALVIBRATE = false;
    //public final static int INITIALSTATE = 0;

    public final static long MAXIMUMTIME = 3599000l;

    private final static String TAG = "waisblut";

    private final static boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void log(char type, String s)
    {
        if (IS_DEBUG)
        {
            switch (type)
            {
            case 'd':
                Log.d(TAG, s);
                break;

            case 'e':
                Log.e(TAG, s);
                break;

            case 'i':
                Log.i(TAG, s);
                break;

            case 'v':
                Log.v(TAG, s);
                break;

            case 'w':
                Log.w(TAG, s);
                break;

            default:
                break;
            }
        }
    }
}
