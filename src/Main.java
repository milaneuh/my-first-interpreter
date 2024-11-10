import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    EOF;
    public static final Map<Character, TokenType> SIMPLE_TOKENS = Map.of(
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

    public static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("and", TokenType.AND),
            Map.entry("class", TokenType.CLASS),
            Map.entry("else", TokenType.ELSE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("for", TokenType.FOR),
            Map.entry("fun", TokenType.FUN),
            Map.entry("if", TokenType.IF),
            Map.entry("nil", TokenType.NIL),
            Map.entry("or", TokenType.OR),
            Map.entry("print", TokenType.PRINT),
            Map.entry("return", TokenType.RETURN),
            Map.entry("super", TokenType.SUPER),
            Map.entry("this", TokenType.THIS),
            Map.entry("true", TokenType.TRUE),
            Map.entry("var", TokenType.VAR),
            Map.entry("while", TokenType.WHILE)
    );
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

    private void scanToken() {
        char c = advance();
        if (TokenType.SIMPLE_TOKENS.containsKey(c)) {
            addToken(TokenType.SIMPLE_TOKENS.get(c));
        } else {
            //number case
            if (isDigit(c)) {
                number();
            } else {
                // Special character cases
                switch (c) {
                    case '/' -> handleSlash();
                    case ' ', '\r', '\t' -> { /* Ignore whitespace */ }
                    case '\n' -> line++;
                    case '"' -> string();
                    default -> {
                        if (isDigit(c)) {
                            number();
                        } else if (isAlpha(c)) {
                            identifier();
                        } else {
                            Gauntlet.error(line, "Unexpected character.");
                        }
                    }
                }
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String txt = source.substring(start, current);
        addToken(TokenType.KEYWORDS.getOrDefault(txt, TokenType.IDENTIFIER));
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a float
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            do advance();
            while (isDigit(peek()));
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void handleSlash() {
        if (match('/')) {
            if('*' == peekNext()){
                //Multi lines comment
                // Ignore until we encounter "*/"
                while ('*' != peekPrevious() && '/' != peek() && !isAtEnd()){
                    advance();
                }
                //Consume the "/"
                advance();
            }else {
                //Single line comment
                while (peek() != '\n' && !isAtEnd()) advance();
            }
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

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char peekPrevious() {
        assert current > 0 : "peekPrevious() can not be called if current is zero.";
        return source.charAt(current - 1);
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

abstract class Expr {
    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right){
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }
}

