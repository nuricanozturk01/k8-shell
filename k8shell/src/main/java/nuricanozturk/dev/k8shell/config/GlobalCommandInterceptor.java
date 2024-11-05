package nuricanozturk.dev.k8shell.config;

import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.util.Command;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class GlobalCommandInterceptor {
    private static final List<String> ALLOWED_COMMANDS = List.of(
            "help",
            "exit",
            Command.SHOW_INFO_KEY_SHORT,
            Command.SHOW_INFO_KEY_LONG,
            Command.EXIT_KEY,
            Command.SELECT_CONFIG_SHORT_CMD,
            Command.SELECT_CONFIG_LONG_CMD,
            Command.LIST_CONFIG_SHORT_CMD,
            Command.LIST_CONFIG_LONG_CMD
    );

    @Around(value = "@annotation(shellMethod)", argNames = "point,shellMethod")
    public Object checkBeforeCommandExecution(final ProceedingJoinPoint point, final ShellMethod shellMethod) throws Throwable {
        final var command = shellMethod.key();
        if (command == null || command.length == 0) {
            return point.proceed();
        }

        Arrays.stream(command)
                .filter(c -> !ALLOWED_COMMANDS.contains(c))
                .findFirst()
                .ifPresent(this::checkIsExistsConfigFile);

        return point.proceed();
    }

    private void checkIsExistsConfigFile(String ignored) {
        final var path = KubernetesData.getInstance().getConfigPath();
        if (path == null || path.isBlank() || path.isEmpty()) {
            throw new IllegalStateException("Configuration file is not selected. Please select a configuration file.");
        }
    }
}
