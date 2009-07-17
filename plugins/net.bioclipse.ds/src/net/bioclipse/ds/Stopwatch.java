package net.bioclipse.ds;

/**
 * 
 * @author ola
 *
 */
public class Stopwatch {
    private long start;
    private long stop;

    public void start() {
        start = System.currentTimeMillis(); // start timing
    }

    public void stop() {
        stop = System.currentTimeMillis(); // stop timing
    }

    public long elapsedTimeMillis() {
        return stop - start;
    }

    //return nice string
    public String toString() {

        int seconds = (int) ((elapsedTimeMillis() / 1000) % 60);
        int minutes = (int) ((elapsedTimeMillis() / 1000) / 60);
        if (minutes>0)
            return "" + minutes + " mins and " + seconds + " s";
        else
            return "" + seconds + " s";
    }
}