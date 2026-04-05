package padaria.com.example.padaria.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValorPermitidoEnumValidator
        implements ConstraintValidator<ValorPermitidoEnum, Enum<?>> {

    private Set<String> valoresPermitidos;

    @Override
    public void initialize(ValorPermitidoEnum annotation) {

        Class<? extends Enum<?>> enumClass = annotation.enumClass();

        Set<String> valoresEnum = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());

        valoresPermitidos = Arrays.stream(annotation.permitidos())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        if (!valoresEnum.containsAll(valoresPermitidos)) {
            throw new IllegalArgumentException("Valores permitidos não pertencem ao enum");
        }
    }

    @Override
    public boolean isValid(Enum<?> valor, ConstraintValidatorContext context) {
        if (valor == null) return true;

        return valoresPermitidos.contains(valor.name());
    }
}