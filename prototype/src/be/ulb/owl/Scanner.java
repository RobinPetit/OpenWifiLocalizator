import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Scanner {

    private String _network;
    private HashMap<String, ArrayList<Float>> _accesPoints;
    private Runtime _r;

    public Scanner () {
        _network = "wlp3s0";
        _accesPoints = new HashMap<String, ArrayList<Float>>();
        _r = Runtime.getRuntime();
    }

    private Float avg (ArrayList<Float> tmp) {
        Float sum = 0.0f;
        for (Float elem : tmp) {
            sum += elem;
        }
        return sum/tmp.size();
    }

    private BufferedReader getData () {
        BufferedReader bufferedreader = null;
        try {
            Process p = _r.exec("iw dev "+_network+" scan");
            InputStream in = p.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);
            InputStreamReader inread = new InputStreamReader(buf);
            bufferedreader = new BufferedReader(inread);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return bufferedreader;
    }

    private void parse (BufferedReader data) {
        try {
            String key = "";
            String line = "";
            while ((line = data.readLine()) != null) {
                if ((line.substring(0, 3)).equals("BSS")) {
                    key = line.substring(4, 21);
                }
                else if (line.substring(0, 7).equals("\tsignal")) {
                    if (!_accesPoints.containsKey(key)) {
                        System.out.println("put");
                        _accesPoints.put(key, new ArrayList<Float>());
                        _accesPoints.get(key).add(Float.valueOf(line.substring(8, line.length()-4)));
                    }
                    else {
                        System.out.println("add");
                        _accesPoints.get(key).add(Float.valueOf(line.substring(8, line.length()-4)));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void scan () {
        for (int i = 0; i < 5; i++) {
            parse(getData());
        }
    }

    public String text () {
        String res = "";
        Set set = _accesPoints.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            ArrayList<Float> tmp = (ArrayList)me.getValue();
            res += me.getKey()+" : "+avg(tmp)+"\n";
        }
        return res;
    }

}
