package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.config.PropertyService;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.control.ListView;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.geom.HorizontalAlign;
import org.springframework.shell.geom.VerticalAlign;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.*;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import static nuricanozturk.dev.k8shell.config.KubernetesClientConfig.BEAN_API_CLIENT;

@ShellComponent
public class ConfigurationFile extends AbstractShellComponent {
    private final PropertyService propertyService;
    private final ApplicationContext context;
    private final CoreV1Api api;

    public ConfigurationFile(PropertyService propertyService, ApplicationContext context, CoreV1Api api) {
        this.propertyService = propertyService;
        this.context = context;
        this.api = api;
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

        final var component = new SingleItemSelector<>(getTerminal(), items, "Select Config file", null);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());

        final var context = component.run(SingleItemSelector.SingleItemSelectorContext.empty());
        final var path = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseGet(() -> {
            System.out.println("No file selected.");
            return null;
        });

        propertyService.updateProperty(path);
        final var newClient = (ApiClient) this.context.getBean(BEAN_API_CLIENT);
        Configuration.setDefaultApiClient(newClient);
        KubernetesData.getInstance().setNamespace(null);
    }

    @ShellMethod(key = {"change namespaces", "set-namespaces", "sn"}, value = "Change namespace")
    public void changeNamespace() throws Exception {
        final var namespaces = api.listNamespace().executeWithHttpInfo();

        if (namespaces.getData() == null) {
            System.out.println("No namespaces found.");
            return;
        }

        final var items = namespaces.getData().getItems().stream()
                .map(n -> SelectorItem.of(n.getMetadata().getName(), n.getMetadata().getName()))
                .toList();

        final var component = new SingleItemSelector<>(getTerminal(), items, "Select Namespace", null);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());

        final var context = component.run(SingleItemSelector.SingleItemSelectorContext.empty());
        final var namespace = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseGet(() -> {
            System.out.println("No namespace selected.");
            return null;
        });

        KubernetesData.getInstance().setNamespace(namespace);
        System.out.println("Namespace set to: " + namespace);
    }

    @ShellMethod(key = {"ln", "list namespaces"}, interactionMode = InteractionMode.INTERACTIVE, value = "List namespaces")
    public void listNamespaces() throws Exception {
        final var namespaces = api.listNamespace().executeWithHttpInfo();
        if (namespaces.getData() == null) {
            System.out.println("No namespaces found.");
        }
        final var items = namespaces.getData().getItems().stream()
                .map(n -> new String[]{"- ", n.getMetadata().getName()})
                .toArray(String[][]::new);

        final var tableModel = new ArrayTableModel(items);
        final var tableBuilder = new TableBuilder(tableModel);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);
        tableBuilder.on(CellMatchers.column(0)).addSizer(new AbsoluteWidthSizeConstraints(2));


    }

}
