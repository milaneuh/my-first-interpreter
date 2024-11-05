import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Gauntlet {
    private static boolean hasError = false;

    public static void main(String[] args) {
        if(args.length > 1){
            System.out.println("Usage : gauntlet [script]");
            System.exit(64);
        }else {
            try {
                if (args.length == 1){
                    runFile(args[0]);
                }
                else runPrompt();
            }catch (IOException e){
        }
        if (hasError) System.exit(65);            }
    }

    private static void runPrompt() throws IOException  {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        for (;;){
            System.out.println("> ");
            String line = reader.readLine();
            assert line != null : "The input should not be null.";
            run(line);
        }
    }

    private static void runFile(String arg) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(arg));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String src) {
        Scanner scanner = new Scanner(src);
        List<Token> tokens = scanner.scanTokens();

        tokens.forEach(token -> System.out.println(token));

    }

    //Error Handler
    static void error(int line, String message){
        report(line,"",message);
    }

    private static void report(int line, String context, String message) {
        System.err.println(
                "Error on line : "+line+"; "+context+" : "+message
        );
        hasError =true;
    }
}

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}

record Token(TokenType type, String lexeme, Object litteral, int line) {
    public String toString() {
        return type + " " + lexeme + " " + litteral;
    }
}

class Scanner {
    final String source;

    Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens(){
        return null;
    }
}

