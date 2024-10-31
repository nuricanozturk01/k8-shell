package nuricanozturk.dev.k8shell.k8s;

import nuricanozturk.dev.k8shell.CommandlinePrinter;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.KubernetesData;
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


    @ShellMethod(key = {"show info", "si"}, value = "Prints Kubernetes information")
    public void printKubernetesInfo() {
        final var headers = List.of("Information", "Value");
        final var items = new ArrayList<>(KubernetesData.getInstance().getAllData());

        final var tableContent = componentProvider.renderTable(headers, items);
        commandlinePrinter.print(tableContent, Ansi.Color.GREEN);
    }

    @ShellMethod(key = {"clear info", "ci"}, value = "Clears Kubernetes information")
    public void clearKubernetesInfo() {
        KubernetesData.getInstance().clearData();
    }

    @ShellMethod(key = {"clear pod", "cp"}, value = "Clears pod information")
    public void clearPod() {
        KubernetesData.getInstance().setPod(null);
    }

    @ShellMethod(key = {"clear deployment", "cd"}, value = "Clears deployment information")
    public void clearDeployment() {
        KubernetesData.getInstance().setDeployment(null);
    }

    @ShellMethod(key = {"clear secret", "cs"}, value = "Clears secret information")
    public void clearSecret() {
        KubernetesData.getInstance().setSecret(null);
    }

    @ShellMethod(key = {"clear service", "cse"}, value = "Clears service information")
    public void clearService() {
        KubernetesData.getInstance().setService(null);
    }

    @ShellMethod(key = {"clear namespace", "cn"}, value = "Clears namespace information")
    public void clearNamespace() {
        final var configPath = KubernetesData.getInstance().getConfigPath();
        clearKubernetesInfo();
        KubernetesData.getInstance().setConfigPath(configPath);
    }

    @ShellMethod(key = {"clear config path", "ccp"}, value = "Clears config path information")
    public void clearConfigPath() {
        clearKubernetesInfo();
    }

    @ShellMethod(key = "q", value = "Exit application")
    public void exit() {
        System.exit(0);
    }
}
