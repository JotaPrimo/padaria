package padaria.com.example.padaria.utils;

public class StringValidator {
    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
