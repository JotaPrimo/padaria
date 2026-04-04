package padaria.com.example.padaria.dto;

import lombok.Getter;

@Getter
public class ResponseApi<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private ResponseApi(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseApi<T> ok(String message, T data) {
        return new ResponseApi<>(true, message, data);
    }

    public static <T> ResponseApi<T> ok(String message) {
        return new ResponseApi<>(true, message, null);
    }

    public static <T> ResponseApi<T> erro(String message) {
        return new ResponseApi<>(false, message, null);
    }
}
