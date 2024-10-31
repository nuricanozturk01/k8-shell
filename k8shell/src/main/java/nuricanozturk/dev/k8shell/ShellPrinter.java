package nuricanozturk.dev.k8shell;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Component;

@Component
public class ShellPrinter {

    public void print(String message) {
        System.out.println(message);
    }

    public void print(String message, Ansi.Color color) {
        AnsiConsole.systemInstall();
        System.out.println(Ansi.ansi().fg(color).a(message).reset());
        AnsiConsole.systemUninstall();
    }

    public void printSuccess(String message) {
        print(message, Ansi.Color.GREEN);
    }

    public void printError(String message) {
        print(message, Ansi.Color.RED);
    }

    public void print(String prefix, Ansi.Color prefixColor, String message, Ansi.Color messageColor) {
        String output = Ansi.ansi()
                .fg(prefixColor).a(prefix).reset()
                .a(": ")
                .fg(messageColor).a(message).reset()
                .toString();

        System.out.println(output);
    }

    public void print(String prefix, String message) {
        String output = Ansi.ansi()
                .fg(Ansi.Color.YELLOW).a(prefix).reset()
                .a(": ")
                .fg(Ansi.Color.GREEN).a(message).reset()
                .toString();

        System.out.println(output);
    }
}
