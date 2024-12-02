package nuricanozturk.dev.k8shell.config;

import nuricanozturk.dev.k8shell.k8s.KubernetesData;
import nuricanozturk.dev.k8shell.k8s.CommandInfo;
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
            CommandInfo.SHOW_INFO_KEY_SHORT,
            CommandInfo.SHOW_INFO_KEY_LONG,
            CommandInfo.EXIT_KEY,
            CommandInfo.SELECT_CONFIG_SHORT_CMD,
            CommandInfo.SELECT_CONFIG_LONG_CMD,
            CommandInfo.LIST_CONFIG_SHORT_CMD,
            CommandInfo.LIST_CONFIG_LONG_CMD
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
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Configuration file is not selected. Please select a configuration file.");
        }
    }
}
