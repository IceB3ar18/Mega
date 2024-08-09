package club.mega.util;

public class PCGRandom {
    private long state;
    private long increment;

    public PCGRandom(long seed, long seq) {
        this.state = 0;
        this.increment = (seq << 1) | 1;
        nextInt(); // Move state to something more random
        this.state += seed;
        nextInt(); // Incorporate seed into state
    }

    public int nextInt() {
        long oldState = state;
        state = oldState * 6364136223846793005L + increment;
        int xorShifted = (int)(((oldState >>> 18) ^ oldState) >>> 27);
        int rot = (int)(oldState >>> 59);
        return (xorShifted >>> rot) | (xorShifted << ((-rot) & 31));
    }

    public int nextInt(int bound) {
        int threshold = -bound % bound;
        while (true) {
            int r = nextInt();
            if (r >= threshold) {
                return r % bound;
            }
        }
    }

    public int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        int bound = max - min + 1;
        return nextInt(bound) + min;
    }
}