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
            accessPoints = mergeAccePoint(accessPoints, _scanner.getData());

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

        Log.i(getClass().getName(), "-------------------------------");
        Log.i(getClass().getName(), "Scan wifi: ");
        for(String bss : accessPoints.keySet()) {
            Log.i(getClass().getName(), bss + ": " + accessPoints.get(bss));
        }
        Log.i(getClass().getName(), "-------------------------------");


        return accessPoints;
    }

    private HashMap<String, ArrayList<Integer>> mergeAccePoint(HashMap<String, ArrayList<Integer>> res,
                                                               HashMap<String, Integer> lastScan) {
        for(String bss : lastScan.keySet()) {
            if(res.containsKey(bss)) {
                ArrayList<Integer> listScan = res.get(bss);
                listScan.add(lastScan.get(bss));
            } else {
                ArrayList<Integer> listScan = new ArrayList<Integer>();
                listScan.add(lastScan.get(bss));
                res.put(bss, listScan);
            }
        }

        return res;
    }



    @Override
    protected void onPostExecute(HashMap<String, ArrayList<Integer>> listWifi) {
        if(!isCancelled()) {
            Log.i(getClass().getName(), "Scan have finish :)");
            _scanner.newScanFinish(listWifi);
        } else {
            Log.i(getClass().getName(), "Scan task have been cancel !");
        }
    }


}

