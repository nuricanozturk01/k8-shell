package nuricanozturk.dev.k8shell.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class PropertyService {
    private final AtomicReference<String> configPath;

    public PropertyService(@Value("${k8s-shell.config-path.default}") String configPath) {
        this.configPath = new AtomicReference<>(configPath);
    }

    public void updateProperty(String value) {
        configPath.set(value);
    }

    public String getProperty() {
        return configPath.get();
    }
}