package padaria.com.example.padaria.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import padaria.com.example.padaria.dto.ResponseApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors
                    .computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Os dados enviados são inválidos.");
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ResponseApi<Void>> handleNegocio(NegocioException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseApi.erro(ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ResponseApi<Void>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseApi.erro(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseApi<Void>> handleRotaNaoEncontrada(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseApi.erro("O endpoint '%s' não existe.".formatted(ex.getResourcePath())));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseApi<Void>> handleMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ResponseApi.erro("O método '%s' não é suportado para este endpoint.".formatted(ex.getMethod())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseApi<Void>> handleAcessoNegado(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseApi.erro("Acesso negado."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseApi<Void>> handleNaoAutenticado(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseApi.erro("Credenciais inválidas."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseApi<Void>> handleMensagemIlegivel(HttpMessageNotReadableException ex) {

        // 1️⃣ tenta tratar enum (regex fallback)
        ResponseEntity<ResponseApi<Void>> response = tratarErroEnumViaMensagem(ex);
        if (response != null) return response;

        // 2️⃣ fallback genérico
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseApi.erro("Erro ao processar a requisição."));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ResponseApi<Void>> handleOrdenacaoInvalida(PropertyReferenceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseApi.erro(
                        "Parâmetro de ordenação inválido: '%s'. Verifique os campos disponíveis.".formatted(ex.getPropertyName())
                ));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ResponseApi<Void>> handleDataAccessInvalido(InvalidDataAccessApiUsageException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseApi.erro("Parâmetro de consulta inválido."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseApi<Void>> handleEstadoIlegal(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseApi.erro(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Void>> handleErroGenerico(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseApi.erro("Ocorreu um erro interno. Tente novamente mais tarde."));
    }

    private ResponseEntity<ResponseApi<Void>> tratarErroEnumViaMensagem(HttpMessageNotReadableException ex) {

        String mensagem = ex.getMessage();

        if (mensagem != null && mensagem.contains("not one of the values accepted")) {

            Pattern valorPattern = Pattern.compile("from String \"(.*?)\"");
            Matcher valorMatcher = valorPattern.matcher(mensagem);

            String valorInformado = valorMatcher.find() ? valorMatcher.group(1) : "desconhecido";

            Pattern valoresPattern = Pattern.compile("\\[(.*?)\\]");
            Matcher valoresMatcher = valoresPattern.matcher(mensagem);

            String valores = valoresMatcher.find() ? valoresMatcher.group(1) : "";

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseApi.erro(
                            "Valor inválido '%s'. Valores aceitos: %s."
                                    .formatted(valorInformado, valores)
                    ));
        }

        return null;
    }
}
