package de.furkan.fradiofm.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class RadioUtil {

    public String getRadioNameById(int id) {
        try {

            HashMap<Integer, String> radioHashmap = new HashMap<>();
            int tempId = 0;

            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Furkan\\Documents\\radios.txt"));
            String url;
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#") && line.length() > 4) {
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
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Furkan\\Documents\\radios.txt"));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("#") && !line.startsWith("#")) {
                    times += 1;
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        times -= 1;
        return times;
    }

    public String getRadioUrlById(int id) {
        try {
            HashMap<Integer, String> radioHashmap = new HashMap<>();
            int tempId = 0;
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Furkan\\Documents\\radios.txt"));
            String url;
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#") && line.length() > 4) {
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
