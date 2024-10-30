package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.models.V1Pod;
import nuricanozturk.dev.k8shell.KubernetesData;
import org.springframework.shell.component.view.control.ListView;
import org.springframework.shell.component.view.control.MenuView;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;

import java.util.List;

@ShellComponent
@ShellCommandGroup("Kubernetes Commands")
public class KubernetesCommands extends AbstractShellComponent {

    private final CoreV1Api api;

    public KubernetesCommands(CoreV1Api api) {
        this.api = api;
    }

    @ShellMethod(key = {"list pods", "lp", "list-pods"}, value = "List some pods")
    public void listPods() {
        try {
            final var namespace = KubernetesData.getInstance().getNamespace();

            if (namespace == null) {
                System.out.println("Namespace is not set.");
                return;
            }
            final var allPods = api.listNamespacedPod(namespace).executeWithHttpInfo();
            final var list = allPods.getData();

            for (V1Pod pod : list.getItems()) {
                System.out.printf("Namespace: %s, Pod Name: %s%n",
                        pod.getMetadata().getNamespace(),
                        pod.getMetadata().getName());
            }
            var view = new MenuView();
            view.setTitle("Pods");
            view.setItems(List.of(new MenuView.MenuItem("Back"), new MenuView.MenuItem("Exit")));

            getViewComponentBuilder()
                    .build(view)
                    .runAsync()
                    .await();
        } catch (ApiException e) {
            System.err.printf("ApiException: %s (HTTP status: %d)%n", e.getMessage(), e.getCode());
        }
    }

}
