package padaria.com.example.padaria.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaValidator implements ConstraintValidator<SenhaValida, String> {

    private static final String SENHA_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        if (senha == null) {
            return true; // @NotNull cuida do null — responsabilidade única
        }
        return senha.matches(SENHA_REGEX);
    }
}
