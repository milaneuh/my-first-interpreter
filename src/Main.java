import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        run(new String(bytes, Charset.forName("UTF-8")));  // Explicit UTF-8 encoding

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
        System.err.println("Error on line: " + line + "; " + context + " : " + message);
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
    public static final String EMPTY_LEXEME = "";
    public static final Object NULL_LITERAL = null;

    public Token(TokenType type, int line) {
        this(type, EMPTY_LEXEME, NULL_LITERAL, line);
    }

    public String toString() {
        return type + " " + lexeme + " " + litteral;
    }
}

class Scanner {
    private static final Map<Character, TokenType> SIMPLE_TOKENS = Map.of(
            '(', TokenType.LEFT_PAREN,
            ')', TokenType.RIGHT_PAREN,
            '{', TokenType.LEFT_BRACE,
            '}', TokenType.RIGHT_BRACE,
            ',', TokenType.COMMA,
            '.', TokenType.DOT,
            '-', TokenType.MINUS,
            '+', TokenType.PLUS,
            ';', TokenType.SEMICOLON,
            '*', TokenType.STAR
    );
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
        tokens.add(new Token(TokenType.EOF, line));  // Using a simplified Token constructor for EOF
        return tokens;
    }

    /**
     * Scans a single character from the input and determines the corresponding token type.
     * Depending on the character, it either adds a specific token, skips whitespace,
     * increments the line counter for newlines, or reports an error for unexpected characters.
     *
     * Special cases:
     * - Handles single-line comments that start with '//'.
     * - If a double quote (") is encountered, invokes a method to process a string literal.
     */
    private void scanToken() {
        char c = advance();
        if (SIMPLE_TOKENS.containsKey(c)) {
            addToken(SIMPLE_TOKENS.get(c));
            return;
        }

        // Special character cases
        switch (c) {
            case '/' -> handleSlash();
            case ' ', '\r', '\t' -> { /* Ignore whitespace */ }
            case '\n' -> line++;
            case '"' -> string();
            default -> Gauntlet.error(line, "Unexpected character.");
        }
    }

    private void handleSlash() {
        if (match('/')) {
            // Ignore the rest of the line as a comment.
            while (peek() != '\n' && !isAtEnd()) advance();
        } else {
            addToken(TokenType.SLASH);
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, Token.NULL_LITERAL);  // Using the constant for NULL_LITERAL
    }

    private void addToken(TokenType type, Object litteral) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, litteral, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Gauntlet.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }
}
