package uk.ac.ebi.intact.app.internal.utils;

public class TimeUtils {
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
        }
    }
}
