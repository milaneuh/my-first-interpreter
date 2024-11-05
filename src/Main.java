import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class Gauntlet {
    private static boolean hasError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage : gauntlet [script]");
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
        run(new String(bytes, Charset.defaultCharset()));

        if (hasError) System.exit(65);
    }

    private static void run(String src) {
        Scanner scanner = new Scanner(src);
        List<Token> tokens = scanner.scanTokens();

        assert tokens != null : "Tokens should not be null.";
        tokens.forEach(System.out::println);
    }

    //Error Handler
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String context, String message) {
        System.err.println("Error on line : " + line + "; " + context + " : " + message);
        hasError = true;
    }
}

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}

record Token(TokenType type, String lexeme, Object litteral, int line) {
    public String toString() {
        return type + " " + lexeme + " " + litteral;
    }
}

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        assert source != null : "Source should not be null.";
        this.source = source;
    }

    boolean isAtEnd() {
        return current >= source.length();
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            default -> Gauntlet.error(line, "Unexpected character.");
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object litteral) {
        assert type != null;
        assert litteral != null;

        String text = source.substring(start, current);
        tokens.add(new Token(type, text, litteral, line));
    }
}

