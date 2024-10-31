package nuricanozturk.dev.k8shell.component;

import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComponentProvider {
    public String renderTable(final List<String> headers, final List<String[]> items) {
        items.addFirst(headers.toArray(String[]::new));

        final var arrayTableModel = new ArrayTableModel(items.toArray(String[][]::new));
        final var tableBuilder = new TableBuilder(arrayTableModel);

        tableBuilder.addHeaderAndVerticalsBorders(BorderStyle.fancy_light);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);
        return tableBuilder.build().render(80);
    }

    public static class SingleSelectionBuilder<T> {
        private Terminal terminal;
        private List<SelectorItem<T>> items;
        private String message;
        private ResourceLoader resourceLoader;
        private TemplateExecutor templateExecutor;
        private boolean printResults = true;

        public SingleSelectionBuilder() {

        }

        public SingleSelectionBuilder<T> setTerminal(final Terminal terminal) {
            this.terminal = terminal;
            return this;
        }


        public SingleSelectionBuilder<T> setResourceLoader(final ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
            return this;
        }

        public SingleSelectionBuilder<T> setTemplateExecutor(final TemplateExecutor templateExecutor) {
            this.templateExecutor = templateExecutor;
            return this;
        }

        public SingleSelectionBuilder<T> setPrintResults(final boolean printResults) {
            this.printResults = printResults;
            return this;
        }

        public SingleSelectionBuilder<T> setItems(final List<SelectorItem<T>> items) {
            this.items = items;

            return this;
        }

        public SingleSelectionBuilder<T> setMessage(final String message) {
            this.message = message;
            return this;
        }

        public SingleItemSelector<T, SelectorItem<T>> build() {
            final var component = new SingleItemSelector<>(terminal, items, message, null);
            component.setResourceLoader(resourceLoader);
            component.setTemplateExecutor(templateExecutor);
            component.setPrintResults(printResults);
            return component;
        }
    }
}
