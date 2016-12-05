package be.ulb.owl.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Detobel36
 */
public class LogUtils {

    private static final long MAXTIMELOG = 259200000; // 3 days (i hope)

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
        File logFolder = new File(Environment.getExternalStorageDirectory() + "/OWL/log");
        if(logFolder != null && logFolder.exists()) {
            for (File file : logFolder.listFiles()) {
                if (file != null && file.exists() && file.isFile() && file.getName().contains("logcat_")) {
                    String fileName = file.getName();

                    int pos = fileName.lastIndexOf(".");
                    if(pos != -1 && fileName.substring(pos).equalsIgnoreCase(".txt")) {
                        String nameWitoutExt = fileName.substring(0, pos);

                        String[] split = nameWitoutExt.split("_");

                        if (split.length == 2) {
                            long fileTime = Long.parseLong(split[1]);
                            long actualTime = System.currentTimeMillis();

                            if (actualTime - fileTime > MAXTIMELOG) {
                                file.delete();
                                Log.i(LogUtils.class.getName(), "Suppression du fichier: " +
                                        file.getName() + " (trop vieux)");
                            }
                        }
                    }

                }
            }
        }
    }

}
