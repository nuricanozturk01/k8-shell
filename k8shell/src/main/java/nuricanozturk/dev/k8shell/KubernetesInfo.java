package nuricanozturk.dev.k8shell;

import org.fusesource.jansi.Ansi;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("Store Kubernetes Information")
public class KubernetesInfo extends AbstractShellComponent {
    private final ShellPrinter shellPrinter;
    private final ComponentProvider componentProvider;

    public KubernetesInfo(ShellPrinter shellPrinter, ComponentProvider componentProvider) {
        this.shellPrinter = shellPrinter;
        this.componentProvider = componentProvider;
    }


    @ShellMethod(key = {"show info", "si"}, value = "Prints Kubernetes information")
    public void printKubernetesInfo() {
        final var headers = List.of("Information", "Value");
        final var items = new ArrayList<>(KubernetesData.getInstance().getAllData());

        final var tableContent = componentProvider.toTable(headers, items);
        shellPrinter.print(tableContent, Ansi.Color.GREEN);
    }

    @ShellMethod(key = {"clear info", "ci"}, value = "Clears Kubernetes information")
    public void clearKubernetesInfo() {
        KubernetesData.getInstance().clearData();
    }
}
