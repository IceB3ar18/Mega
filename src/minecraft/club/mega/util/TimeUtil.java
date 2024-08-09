package club.mega.util;

public class TimeUtil {
    private long pastMS;
    private long lastMS = 0L;
    private long lastTime = getCurrentTime();

    public TimeUtil() {
        reset();
    }

    public void reset() {
        lastTime = getCurrentTime();
    }

    public long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    public long getLastTime() {
        return lastTime;
    }

    public long getDifference() {
        return getCurrentTime() - lastTime;
    }

    public boolean hasTimePassed(long milliseconds) {
        return getDifference() >= milliseconds;
    }
    public final boolean delay(long delay) {
        if (System.currentTimeMillis() - pastMS > delay) {
            reset();
            return true;
        }
        return false;
    }

}
