package de.furkan.fradiofm.util;

import org.apache.http.impl.cookie.RFC2109DomainHandler;

import java.io.*;
import java.util.HashMap;

public class RadioUtil {

    public String getRadioNameById(int id) {
        try {

            HashMap<Integer, String> radioHashmap = new HashMap<>();
            int tempId = 0;
            InputStream in = getClass().getResourceAsStream("/radios.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String url;
            String line = reader.readLine();
            while (line != null) {
                if(!line.startsWith("#") && line.length() > 4) {
                    url = line.split("#")[0];
                    radioHashmap.put(tempId, url);
                    tempId += 1;
                }
                line = reader.readLine();
            }
            return radioHashmap.get(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getRadioCount() {
        int times = 0;
        try {
            InputStream in = getClass().getResourceAsStream("/radios.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                if(line.contains("#") && !line.startsWith("#")) {
                    times+=1;
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        times-=1;
        return times;
    }

    public String getRadioUrlById(int id) {
        try {
            HashMap<Integer, String> radioHashmap = new HashMap<>();

            int tempId = 0;
            InputStream in = getClass().getResourceAsStream("/radios.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String url;
            String line = reader.readLine();
            while (line != null) {
                if(!line.startsWith("#") && line.length() > 4) {
                    url = line.split("#")[1];
                    radioHashmap.put(tempId, url);
                    if (tempId == id) {
                        return radioHashmap.get(id);
                    }
                    tempId += 1;
                }
                line = reader.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Soon.
    // private String[] getAllRadios()

}
