package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import nuricanozturk.dev.k8shell.CommandlinePrinter;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.config.PropertyService;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;

import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static nuricanozturk.dev.k8shell.config.KubernetesClientConfig.BEAN_API_CLIENT;

@ShellComponent
public class ConfigurationFile extends AbstractShellComponent {
    private final PropertyService propertyService;
    private final ApplicationContext context;
    private final CommandlinePrinter commandlinePrinter;

    @Value("${k8s-shell.config-path.dir}")
    private String kubeDirPath;

    public ConfigurationFile(final PropertyService propertyService, final ApplicationContext context, CommandlinePrinter commandlinePrinter) {
        this.propertyService = propertyService;
        this.context = context;
        this.commandlinePrinter = commandlinePrinter;
    }

    @ShellMethod(key = {"sc", "set-config"}, value = "Change the configuration file")
    public void select() {
        final var configFiles = getFilesInDir(kubeDirPath);

        final var items = configFiles.stream()
                .map(f -> SelectorItem.of(f.getName(), f.getAbsolutePath()))
                .toList();

        final var context = new ComponentProvider.SingleSelectionBuilder<String>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(items)
                .setMessage("Select a configuration file")
                .setPrintResults(true)
                .build();

        final var selectedPath = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No configuration file selected."));

        KubernetesData.getInstance().clearData();
        propertyService.setConfigPath(selectedPath);
        KubernetesData.getInstance().setConfigPath(selectedPath);

        final var newClient = (ApiClient) this.context.getBean(BEAN_API_CLIENT);
        Configuration.setDefaultApiClient(newClient);
    }

    @ShellMethod(key = {"list config", "lc"}, value = "Show the current configuration files in .kube directory")
    public void list() {
        final var configFiles = getFilesInDir(kubeDirPath);

        final var headers = List.of("Configuration Files");
        final var items = configFiles.stream()
                .map(f -> new String[]{f.getName()})
                .collect(Collectors.toList());

        final var renderedTable = new ComponentProvider().renderTable(headers, items);
        commandlinePrinter.print(renderedTable, Ansi.Color.GREEN);
    }

    private List<File> getFilesInDir(final String dirPath) {
        final var kubeDir = new File(dirPath);
        final var configFiles = kubeDir.listFiles();

        if (configFiles == null || configFiles.length == 0) {
            throw new ItemNotFoundException("No configuration files found. Please check your kube config directory!");
        }

        return Arrays.stream(configFiles).toList();
    }
}
