package be.ulb.owl.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import be.ulb.owl.R;

/**
 * Help to create dialog box
 *
 * @author Detobel36
 */

public class DialogUtils {

    /**
     * Show an information box (with juste an "ok" button)
     *
     * @param parent of the box (MainActivity for example)
     * @param title number of the string (R.string) for the title of the box
     * @param message number of the string (R.string) for the message in the box
     */
    public static void infoBox(Context parent, int title, int message) {
        infoBox(parent, parent.getResources().getString(title), parent.getResources().getString(message));
    }

    /**
     * Show an information box (with juste an "ok" button)
     *
     * @param parent of the box (MainActivity for example)
     * @param title of the box
     * @param message in the box
     */
    public static void infoBox(Context parent, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }



}
