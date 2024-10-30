package nuricanozturk.dev.k8shell.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig implements PromptProvider {

    @Value("${k8s-shell.prompt}")
    private String prompt;

    @Override
    public AttributedString getPrompt() {
        return new AttributedString(prompt,
                AttributedStyle.BOLD
                        .foreground(AttributedStyle.GREEN)
                        .background(AttributedStyle.BLACK));
    }
}
