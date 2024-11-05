package nuricanozturk.dev.k8shell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.standard.ShellCommandGroup;

@SpringBootApplication
@EnableAspectJAutoProxy
public class K8shellApplication {
    public static void main(final String[] args) {
        SpringApplication.run(K8shellApplication.class, args);
    }
}
