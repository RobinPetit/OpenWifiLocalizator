package be.ulb.owl.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import be.ulb.owl.MainActivity;

/**
 * Function which help to use and manage log (and log file)
 *
 * @author Detobel36
 */
public class LogUtils {

    private static final long MAXTIMELOG = 3600 * 24 * 3 * 1000;  // 3 Dyas in milliseconds
    private static MainActivity _main;


    /**
     *  Checks if external storage is available for read and write
     *
     *  @return true if storage is available to write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /**
     *  Checks if external storage is available to at least read
     *
     *  @return True if storage is available to read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }

    /**
     * Clear too old log
     */
    public static void clearLog() {
        clearLog(MAXTIMELOG);
    }

    /**
     * Clear too old log until a date
     *
     * @param maxTimeLog maximum time (in mili second) life of a file
     */
    public static void clearLog(float maxTimeLog) {
        File logFolder = new File(Environment.getExternalStorageDirectory() + File.separator +
                _main.getAppName() + File.separator + "log");
        if(logFolder.exists()) {
            for (File file : logFolder.listFiles()) {
                if (file != null && file.exists() && file.isFile() && file.getName().contains("logcat_")) {
                    String fileName = file.getName();

                    int pos = fileName.lastIndexOf(".");
                    if(pos != -1 && fileName.substring(pos).equalsIgnoreCase(".txt")) {
                        String nameWithoutExt = fileName.substring(0, pos);

                        String[] split = nameWithoutExt.split("_");

                        if (split.length == 2) {
                            long fileTime = Long.parseLong(split[1]);
                            long actualTime = System.currentTimeMillis();

                            if (actualTime - fileTime > maxTimeLog) {
                                file.delete();
                                Log.i(LogUtils.class.getName(), "Deletion of file: " +
                                        file.getName() + " (too old)");
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Init log système<br/>
     * Log file will be save in <app name>/log/logcat_<timestamp>.txt<br />
     * <br />
     * Information: https://developer.android.com/studio/command-line/logcat.html
     *
     * @param main MainActivity to get context
     */
    public static void initLogSystem(MainActivity main) {
        _main = main;

        if (LogUtils.isExternalStorageWritable() ) {
            File appDirectory = new File( Environment.getExternalStorageDirectory() +
                    File.separator + _main.getAppName());
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat_" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }


            try {
                // clear the previous logcat and then write the new one to the file
                Runtime.getRuntime().exec( "logcat -c");

                String informationLevel;
                if(MainActivity.isDebug()) {
                    informationLevel = "*:I";
                } else {
                    informationLevel = "*:W";
                }
                Runtime.getRuntime().exec( "logcat -f " + logFile + " -v time " + informationLevel);

            } catch ( IOException e ) {
                e.printStackTrace();
            }

            LogUtils.clearLog();

        } /*else if (LogUtils.isExternalStorageReadable() ) {
            // only readable
        } else {
            // not accessible
        }*/
    }


}
