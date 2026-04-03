package padaria.com.example.padaria.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SenhaValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaValida {

    String message() default "A senha deve ter no mínimo 8 caracteres e conter letras maiúsculas, minúsculas, números e caracteres especiais (@#$%^&+=!).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
