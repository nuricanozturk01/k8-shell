package nuricanozturk.dev.k8shell.k8s.command;

import nuricanozturk.dev.k8shell.k8s.CommandInfo;
import nuricanozturk.dev.k8shell.k8s.KubernetesData;
import nuricanozturk.dev.k8shell.printer.CommandlinePrinter;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.ArrayList;
import java.util.List;

@ShellComponent
@ShellCommandGroup("Store Kubernetes Information")
public class KubernetesInfoCommand extends AbstractShellComponent {
    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;

    public KubernetesInfoCommand(final CommandlinePrinter commandlinePrinter, final ComponentProvider componentProvider) {
        this.commandlinePrinter = commandlinePrinter;
        this.componentProvider = componentProvider;
    }


    @ShellMethod(key = {CommandInfo.SHOW_INFO_KEY_LONG, CommandInfo.SHOW_INFO_KEY_SHORT},
            value = CommandInfo.SHOW_INFO_DESC)
    public void printKubernetesInfo() {
        final var headers = List.of("Information", "Value");
        final var items = new ArrayList<>(KubernetesData.getInstance().getAllData());

        final var tableContent = componentProvider.renderTable(headers, items);
        commandlinePrinter.print(tableContent, Ansi.Color.GREEN);
    }

    @ShellMethod(key = {CommandInfo.CLEAR_INFO_KEY_LONG, CommandInfo.CLEAR_INFO_KEY_SHORT},
            value = CommandInfo.CLEAR_INFO_DESC)
    public void clearKubernetesInfo() {
        KubernetesData.getInstance().clearData();
    }

    @ShellMethod(key = {CommandInfo.CLEAR_POD_KEY_LONG, CommandInfo.CLEAR_POD_KEY_SHORT},
            value = CommandInfo.CLEAR_POD_DESC)
    public void clearPod() {
        KubernetesData.getInstance().setPod(null);
    }

    @ShellMethod(key = {CommandInfo.CLEAR_DEPLOYMENT_KEY_LONG, CommandInfo.CLEAR_DEPLOYMENT_KEY_SHORT},
            value = CommandInfo.CLEAR_DEPLOYMENT_DESC)
    public void clearDeployment() {
        KubernetesData.getInstance().setDeployment(null);
    }

    @ShellMethod(key = {CommandInfo.CLEAR_SECRET_KEY_LONG, CommandInfo.CLEAR_SECRET_KEY_SHORT},
            value = CommandInfo.CLEAR_SECRET_DESC)
    public void clearSecret() {
        KubernetesData.getInstance().setSecret(null);
    }

    @ShellMethod(key = {CommandInfo.CLEAR_SERVICE_KEY_LONG, CommandInfo.CLEAR_SERVICE_KEY_SHORT},
            value = CommandInfo.CLEAR_SERVICE_DESC)
    public void clearService() {
        KubernetesData.getInstance().setService(null);
    }

    @ShellMethod(key = {CommandInfo.CLEAR_NAMESPACE_KEY_LONG, CommandInfo.CLEAR_NAMESPACE_KEY_SHORT},
            value = CommandInfo.CLEAR_NAMESPACE_DESC)
    public void clearNamespace() {
        final var configPath = KubernetesData.getInstance().getConfigPath();
        clearKubernetesInfo();
        KubernetesData.getInstance().setConfigPath(configPath);
    }

    @ShellMethod(key = {CommandInfo.CLEAR_CONFIG_PATH_KEY_LONG, CommandInfo.CLEAR_CONFIG_PATH_KEY_SHORT},
            value = CommandInfo.CLEAR_CONFIG_PATH_DESC)
    public void clearConfigPath() {
        clearKubernetesInfo();
    }

    @ShellMethod(key = CommandInfo.EXIT_KEY,
            value = CommandInfo.EXIT_DESC)
    public void exit() {
        System.exit(0);
    }

}
