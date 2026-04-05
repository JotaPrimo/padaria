package padaria.com.example.padaria.enums;

import java.util.Arrays;
import java.util.List;

public enum MotivoCancelamento {
    CANCELADO_PELO_CLIENTE,
    CANCELADO_PELA_PADARIA,
    OUTRO;

    public static List<String> opcoes() {
        return Arrays.stream(MotivoCancelamento.values()).map(Enum::name).toList();
    }
}
