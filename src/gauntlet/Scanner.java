package gauntlet;

import java.util.ArrayList;
import java.util.List;

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
            if ('*' == peekNext()) {
                //Multi lines comment
                // Ignore until we encounter "*/"
                while ('*' != peekPrevious() && '/' != peek() && !isAtEnd()) {
                    advance();
                }
                //Consume the "/"
                advance();
            } else {
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
