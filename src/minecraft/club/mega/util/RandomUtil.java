package club.mega.util;

import net.minecraft.client.Minecraft;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomUtils.nextDouble;

public final class RandomUtil {

    public static int nextInt(int origin, int bound) {
        return origin == bound ? origin : ThreadLocalRandom.current().nextInt(origin, bound);
    }

    public static long nextLong(long origin, long bound) {
        return origin == bound ? origin : ThreadLocalRandom.current().nextLong(origin, bound);
    }

    public static float nextFloat(double origin, double bound) {
        return origin == bound ? (float)origin : (float)ThreadLocalRandom.current().nextDouble((double)((float)origin), (double)((float)bound));
    }

    public static float nextFloat(float origin, float bound) {
        return origin == bound ? origin : (float)ThreadLocalRandom.current().nextDouble((double)origin, (double)bound);
    }

    public static double nextDouble(double origin, double bound) {
        return origin == bound ? origin : ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    public static double nextSecureInt(int origin, int bound) {
        if (origin == bound) {
            return (double)origin;
        } else {
            SecureRandom secureRandom = new SecureRandom();
            int difference = bound - origin;
            return (double)(origin + secureRandom.nextInt(difference));
        }
    }

    public static double nextSecureDouble(double origin, double bound) {
        if (origin == bound) {
            return origin;
        } else {
            SecureRandom secureRandom = new SecureRandom();
            double difference = bound - origin;
            return origin + secureRandom.nextDouble() * difference;
        }
    }
    public double smooth (double max, double min, double time, boolean randomizing, double randomStrength) {
        min += 1;
        double radians = Math.toRadians((System.currentTimeMillis() * time % 360) - 180);
        double base = (Math.tanh(radians) + 1) / 2;
        double delta = max - min;
        delta *= base;
        double value = min + delta;
        if(randomizing)value *= ThreadLocalRandom.current().nextDouble(randomStrength,1);
        return Math.ceil(value *1000) / 1000;
    }
    public static float nextSecureFloat(double origin, double bound) {
        if (origin == bound) {
            return (float)origin;
        } else {
            SecureRandom secureRandom = new SecureRandom();
            float difference = (float)(bound - origin);
            return (float)(origin + (double)(secureRandom.nextFloat() * difference));
        }
    }

    public static boolean get(final int chance) {
        return Math.round(getRandomNumber(0, 100)) <= chance;
    }

    public static String randomNumber(final int length) {
        return random(length, "0123456789".toCharArray());
    }
    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    public double getRandomDouble(double min, double max) {
        return threadLocalRandom.nextDouble(min, max);
    }
    public static double getRandomNumber(final double min, final double max) {
        return ((Math.random() * (max - min)) + min);
    }
    public double getRandomGaussian(double average) {
        return threadLocalRandom.nextGaussian() * average;
    }
    public static String randomString(final int length) {
        return random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray());
    }

    private static String random(final int length, final char[] chars) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++)
            stringBuilder.append(chars[(new Random()).nextInt(chars.length)]);
        return stringBuilder.toString();
    }
    public static double randomSin() {
        return Math.sin(nextDouble(0.0, 6.283185307179586));
    }

    public static double randomBetween(double min, double max) {
        SecureRandom secureRandom = new SecureRandom();
        return min + secureRandom.nextDouble() * (max - min);
    }
}
