package nuricanozturk.dev.k8shell.exception;

import io.kubernetes.client.openapi.ApiException;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.annotation.ExceptionResolver;
import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionHandler implements CommandExceptionResolver {

    @ExceptionResolver({ApiException.class, ItemNotFoundException.class})
    @Override
    public CommandHandlingResult resolve(final Exception ex) {
        return switch (ex) {
            case ApiException e -> CommandHandlingResult.of("\u001B[31m" + e.getMessage() + "\u001B[0m\n");
            case ItemNotFoundException e -> CommandHandlingResult.of("\u001B[31m" + e.getMessage() + "\u001B[0m\n");
            default -> CommandHandlingResult.of("\u001B[31m" + ex.getMessage() + "\u001B[0m\n");
        };
    }
}
