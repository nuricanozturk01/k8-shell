package nuricanozturk.dev.k8shell.k8s;

import nuricanozturk.dev.k8shell.CommandlinePrinter;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.util.Command;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.ArrayList;
import java.util.List;

@ShellComponent
@ShellCommandGroup("Store Kubernetes Information")
public class KubernetesInfo extends AbstractShellComponent {
    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;

    public KubernetesInfo(final CommandlinePrinter commandlinePrinter, final ComponentProvider componentProvider) {
        this.commandlinePrinter = commandlinePrinter;
        this.componentProvider = componentProvider;
    }


    @ShellMethod(key = {Command.SHOW_INFO_KEY_LONG, Command.SHOW_INFO_KEY_SHORT},
            value = Command.SHOW_INFO_DESC)
    public void printKubernetesInfo() {
        final var headers = List.of("Information", "Value");
        final var items = new ArrayList<>(KubernetesData.getInstance().getAllData());

        final var tableContent = componentProvider.renderTable(headers, items);
        commandlinePrinter.print(tableContent, Ansi.Color.GREEN);
    }

    @ShellMethod(key = {Command.CLEAR_INFO_KEY_LONG, Command.CLEAR_INFO_KEY_SHORT},
            value = Command.CLEAR_INFO_DESC)
    public void clearKubernetesInfo() {
        KubernetesData.getInstance().clearData();
    }

    @ShellMethod(key = {Command.CLEAR_POD_KEY_LONG, Command.CLEAR_POD_KEY_SHORT},
            value = Command.CLEAR_POD_DESC)
    public void clearPod() {
        KubernetesData.getInstance().setPod(null);
    }

    @ShellMethod(key = {Command.CLEAR_DEPLOYMENT_KEY_LONG, Command.CLEAR_DEPLOYMENT_KEY_SHORT},
            value = Command.CLEAR_DEPLOYMENT_DESC)
    public void clearDeployment() {
        KubernetesData.getInstance().setDeployment(null);
    }

    @ShellMethod(key = {Command.CLEAR_SECRET_KEY_LONG, Command.CLEAR_SECRET_KEY_SHORT},
            value = Command.CLEAR_SECRET_DESC)
    public void clearSecret() {
        KubernetesData.getInstance().setSecret(null);
    }

    @ShellMethod(key = {Command.CLEAR_SERVICE_KEY_LONG, Command.CLEAR_SERVICE_KEY_SHORT},
            value = Command.CLEAR_SERVICE_DESC)
    public void clearService() {
        KubernetesData.getInstance().setService(null);
    }

    @ShellMethod(key = {Command.CLEAR_NAMESPACE_KEY_LONG, Command.CLEAR_NAMESPACE_KEY_SHORT},
            value = Command.CLEAR_NAMESPACE_DESC)
    public void clearNamespace() {
        final var configPath = KubernetesData.getInstance().getConfigPath();
        clearKubernetesInfo();
        KubernetesData.getInstance().setConfigPath(configPath);
    }

    @ShellMethod(key = {Command.CLEAR_CONFIG_PATH_KEY_LONG, Command.CLEAR_CONFIG_PATH_KEY_SHORT},
            value = Command.CLEAR_CONFIG_PATH_DESC)
    public void clearConfigPath() {
        clearKubernetesInfo();
    }

    @ShellMethod(key = Command.EXIT_KEY,
            value = Command.EXIT_DESC)
    public void exit() {
        System.exit(0);
    }

}
