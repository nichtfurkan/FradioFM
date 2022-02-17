package de.furkan.fradiofm.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MemoryUtils {

    private static final Log logger = LogFactory.getLog(MemoryUtils.class);

    /**
     * Returns used memory in MB
     */
    public static double usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return usedMemory(runtime);
    }

    /**
     * Returns max memory available MB
     */
    public static double maxMemory() {
        Runtime runtime = Runtime.getRuntime();
        return maxMemory(runtime);
    }

    static double usedMemory(Runtime runtime) {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        double usedMemory = (double)(totalMemory - freeMemory) / (double)(1024 * 1024);
        return usedMemory;
    }

    static double maxMemory(Runtime runtime) {
        long maxMemory = runtime.maxMemory();
        double memory = (double)maxMemory / (double)(1024 * 1024);
        return memory;
    }

    public static void printMemoryInfo() {
        StringBuffer buffer = getMemoryInfo();
        logger.info(buffer.toString());
    }

    public static StringBuffer getMemoryInfo() {
        StringBuffer buffer = new StringBuffer();

        freeMemory();

        Runtime runtime = Runtime.getRuntime();
        double usedMemory = usedMemory(runtime);
        double maxMemory = maxMemory(runtime);

        NumberFormat f = new DecimalFormat("###,##0.0");

        String lineSeparator = System.getProperty("line.separator");
        buffer.append("Used memory: `" + f.format(usedMemory) + "` MB").append(lineSeparator);
        buffer.append("Max available memory: `" + f.format(maxMemory) + "` MB").append(lineSeparator);
        return buffer;
    }

    public static void freeMemory() {
        System.gc();
        System.runFinalization();
    }

}


