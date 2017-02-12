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
    private final int _timeBeforeTest;

    /**
     * Start a scan wifi task
     *
     * @param scanner the scanner manager
     * @param timeBeforeTest time before the test in second
     */
    public ScanTask(Scanner scanner, int timeBeforeTest) {
        super();

        _scanner = scanner;
        _timeBeforeTest = timeBeforeTest;
    }


    @Override
    protected HashMap<String, ArrayList<Integer>> doInBackground(Void... nothing) {
        HashMap<String, ArrayList<Integer>> accessPoints = new HashMap<String, ArrayList<Integer>>();

        if(_timeBeforeTest > 0) {
            try {
                Thread.sleep(_timeBeforeTest*1000);

            } catch (InterruptedException e) {
                return null;
            }
        }

        for (int i = 0; i < 3; i++) {
            accessPoints.putAll(_scanner.getData());

            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                Log.w(getClass().getName(), "Cancel ScanTask");
                accessPoints = null;
                break;
            }

            if (isCancelled()) {
                break;
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

