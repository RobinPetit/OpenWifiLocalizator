package be.ulb.owl.scanner;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Asynchronous scan wifi
 *
 * @author Detobel36
 */
public class ScanTask extends AsyncTask<Void, Void, HashMap<String, ArrayList<Integer>>> {

    private final Scanner _scanner;


    public ScanTask(Scanner scanner) {
        super();

        _scanner = scanner;
    }


    @Override
    protected HashMap<String, ArrayList<Integer>> doInBackground(Void... nothing) {
        HashMap<String, ArrayList<Integer>> accessPoints = null;

        for (int i = 0; i < 3; i++) {
            accessPoints.putAll(_scanner.getData());

            try {
                this.wait(1000);

            } catch (InterruptedException e) {
                Log.w(getClass().getName(), "Cancel ScanTask");
                accessPoints = null;
                break;
            }

            if (isCancelled()) {
                return null;
            }
        }

        return accessPoints;
    }


    @Override
    protected void onPostExecute(HashMap<String, ArrayList<Integer>> listWifi) {
        if(!isCancelled()) {
            _scanner.newScanFinish(listWifi);
        }
        Log.d(getClass().getName(), "Post execute !");
    }


}

