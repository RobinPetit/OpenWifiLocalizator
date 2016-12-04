package be.ulb.owl.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Detobel36 on 4/12/16.
 */

public class LogUtils {

    private final long maxTimeLog = 12;

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }

    public static void clearLog() {
        File logFolder = new File(Environment.getExternalStorageDirectory() + "/ULBMAP/log");
        for(File file : logFolder.listFiles()) {
            if(file.isFile() && file.getName().contains("logcat_")) {
                String fileName = file.getName();
                String[] split = fileName.split("_");

                if(split.length == 2) {
                    System.currentTimeMillis();
//                    int split[1]
                    // TODO reprendre ici
                }

            }
        }
    }

}
