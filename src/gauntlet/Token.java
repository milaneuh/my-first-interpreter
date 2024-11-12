package gauntlet;

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


