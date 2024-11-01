package kopo.frontservice.util;

public class SafeUtil {

    public static int safeParseInt(String input, int defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue; // 기본값 반환
        }
    }
}
