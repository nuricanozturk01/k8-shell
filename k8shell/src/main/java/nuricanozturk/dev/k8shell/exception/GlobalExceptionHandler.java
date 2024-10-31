package nuricanozturk.dev.k8shell.exception;

import io.kubernetes.client.openapi.ApiException;
import nuricanozturk.dev.k8shell.ShellPrinter;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.annotation.ExceptionResolver;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;


@Component
public class GlobalExceptionHandler implements CommandExceptionResolver {
    private final ShellPrinter shellPrinter;

    public GlobalExceptionHandler(ShellPrinter shellPrinter) {
        this.shellPrinter = shellPrinter;
    }

    @ExceptionResolver({InterruptedException.class, InvocationTargetException.class, ApiException.class, ItemNotFoundException.class})
    @Override
    public CommandHandlingResult resolve(Exception ex) {
        return switch (ex) {
            case InterruptedException e -> {
                shellPrinter.printError(e.getMessage());
                yield CommandHandlingResult.of(e.getMessage());
            }
            case InvocationTargetException e -> {
                shellPrinter.printError(e.getMessage());
                yield CommandHandlingResult.of(e.getMessage());
            }
            case ApiException e -> {
                shellPrinter.printError(e.getMessage());
                yield CommandHandlingResult.of(e.getMessage());
            }
            case ItemNotFoundException e -> {
                shellPrinter.printError(e.getMessage());
                yield CommandHandlingResult.of(e.getMessage());
            }
            default -> {
                shellPrinter.printError(ex.getMessage());
                yield CommandHandlingResult.of(ex.getMessage());
            }
        };
    }
}
