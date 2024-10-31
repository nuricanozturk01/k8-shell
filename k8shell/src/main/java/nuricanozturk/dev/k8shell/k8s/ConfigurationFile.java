package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.ShellPrinter;
import nuricanozturk.dev.k8shell.ComponentProvider;
import nuricanozturk.dev.k8shell.config.PropertyService;
import org.fusesource.jansi.Ansi;

import org.springframework.context.ApplicationContext;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nuricanozturk.dev.k8shell.config.KubernetesClientConfig.BEAN_API_CLIENT;

@ShellComponent
public class ConfigurationFile extends AbstractShellComponent {
    private final PropertyService propertyService;
    private final ApplicationContext context;
    private final CoreV1Api api;
    private final ShellPrinter shellPrinter;
    private final ComponentProvider componentProvider;

    public ConfigurationFile(PropertyService propertyService, ApplicationContext context, CoreV1Api api, ShellPrinter shellPrinter, ComponentProvider componentProvider) {
        this.propertyService = propertyService;
        this.context = context;
        this.api = api;
        this.shellPrinter = shellPrinter;
        this.componentProvider = componentProvider;
    }

    @ShellMethod(key = {"sc", "set-config"}, value = "Change the configuration file")
    public void changeConfigFile() {
        final var kubeDir = new File(System.getProperty("user.home") + "/.kube");
        final var configFiles = kubeDir.listFiles();

        if (configFiles == null || configFiles.length == 0) {
            System.out.println("No configuration files found in ~/.kube directory.");
            return;
        }

        final var items = Arrays.stream(configFiles)
                .map(f -> SelectorItem.of(f.getName(), f.getAbsolutePath()))
                .toList();

        final var context = new ComponentProvider.SingleSelectionBuilder<String>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(items)
                .setMessage("Select a configuration file")
                .setDefaultItem(null)
                .setPrintResults(true)
                .build();

        final var selectedPath = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseGet(() -> {
                    shellPrinter.print("No pod selected.", Ansi.Color.RED);
                    return null;
                });

        KubernetesData.getInstance().clearData();

        propertyService.updateProperty(selectedPath);
        KubernetesData.getInstance().setConfigPath(selectedPath);

        final var newClient = (ApiClient) this.context.getBean(BEAN_API_CLIENT);
        Configuration.setDefaultApiClient(newClient);
    }

    @ShellMethod(key = {"change namespaces", "set-namespaces", "sn"}, value = "Change namespace")
    public void changeNamespace() throws Exception {
        final var namespaces = api.listNamespace().executeWithHttpInfo();

        if (namespaces.getData() == null) {
            System.out.println("No namespaces found.");
            return;
        }

        final var items = namespaces.getData().getItems().stream()
                .map(n -> SelectorItem.of(Objects.requireNonNull(n.getMetadata()).getName(), n.getMetadata().getName()))
                .toList();

        final var context = new ComponentProvider.SingleSelectionBuilder<String>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(items)
                .setMessage("Select Namespace")
                .setDefaultItem(null)
                .setPrintResults(false)
                .build();
        final var selectedNamespace = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseGet(() -> {
                    System.out.println("No namespace selected.");
                    return null;
                });

        KubernetesData.getInstance().setPod(null);
        KubernetesData.getInstance().setSecret(null);
        KubernetesData.getInstance().setService(null);
        KubernetesData.getInstance().setDeployment(null);
        KubernetesData.getInstance().setNamespace(selectedNamespace);

        shellPrinter.printSuccess("Namespace selected: " + selectedNamespace);
    }

    @ShellMethod(key = {"ln", "list namespaces"}, interactionMode = InteractionMode.INTERACTIVE, value = "List namespaces")
    public void listNamespaces() throws Exception {
        final var namespaces = api.listNamespace().executeWithHttpInfo();

        if (namespaces.getData() == null || namespaces.getData().getItems().isEmpty()) {
            System.out.println("No namespaces found.");
        }

        final var items = namespaces.getData().getItems().stream()
                .map(n -> new String[]{Objects.requireNonNull(n.getMetadata()).getName()})
                .collect(Collectors.toList());


        final var result = this.componentProvider.toTable(List.of("Namespaces"), items);

        shellPrinter.printSuccess(result);
    }

}
