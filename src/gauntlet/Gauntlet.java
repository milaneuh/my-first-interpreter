package gauntlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Gauntlet {
    private static boolean hasError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: gauntlet [script]");
            System.exit(64);
        } else {
            if (args.length == 1) {
                runFile(args[0]);
            } else runPrompt();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        for (; ; ) {
            System.out.println("> ");
            String line = reader.readLine();
            assert line != null : "The input should not be null.";
            run(line);
            hasError = false;
        }
    }

    private static void runFile(String arg) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(arg));
        run(new String(bytes, StandardCharsets.UTF_8));  // Explicit UTF-8 encoding

        if (hasError) System.exit(65);
    }

    private static void run(String src) {
        Scanner scanner = new Scanner(src);
        List<Token> tokens = scanner.scanTokens();

        assert tokens != null : "Tokens should not be null.";
        tokens.forEach(System.out::println);
    }

    // Error Handler
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String context, String message) {
        System.err.println("Error on line: " + line + ". \n        " + context + " : " + message);
        hasError = true;
    }
}
