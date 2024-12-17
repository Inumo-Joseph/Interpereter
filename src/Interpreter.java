import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;

public class Interpreter {
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:(\\d+)|([a-zA-Z_][a-zA-Z0-9_]*)|([+\\-*/=();])|(\\S))");
    private static final Map<String, Integer> variables = new HashMap<>();
    public static void main(String[] args) throws FileNotFoundException {

        File input = new File("src/input.txt");
        Scanner scanner = new Scanner(input);


        ArrayList<Integer> numLines = new ArrayList<>();
        int i =1;
        while((scanner.hasNextLine())) {
            numLines.add(i);
            try {
                List<Token> tokens = tokenizer(scanner.nextLine());
                parse(tokens, i);

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            i++;
        }

    }

    public static List<Token> tokenizer(String line) throws Exception {
        Matcher matcher = TOKEN_PATTERN.matcher(line);
        List<Token> tokens = new ArrayList<>();

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new Token("Literal", matcher.group(1)));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token("Identifier", matcher.group(2)));
            } else if (matcher.group(3) != null) {
                tokens.add(new Token("Symbol", matcher.group(3)));
            } else if (matcher.group(4) != null) {
                throw new Exception("Unexpected character: " + matcher.group(4));
            }
        }

        tokens.add(new Token("eof", ""));

        return tokens;
    }

    public static void parse(List<Token> tokens, int i) throws Exception {
        TokenPosition pos = new TokenPosition(tokens.iterator());

        while (!pos.current.type.equals("eof")) {
            if (!pos.current.type.equals("Identifier")) {
                throw new Exception("Error on line: " + i + " Expected identifier, found: " + pos.current.value);
            }

            String varName = pos.current.value; // Store variable name

            pos.advance(); // Move to '='

            if (!pos.current.type.equals("Symbol") || !pos.current.value.equals("=")) {
                throw new Exception("Error on line: " + i +" Expected '=', found: " + pos.current.value);
            }

            pos.advance(); // Move to the start of the expression

            int value = parseExpression(pos, i); // Parse the expression

            variables.put(varName, value); // Store the result

            if (!pos.current.type.equals("Symbol") || !pos.current.value.equals(";")) {
                throw new Exception("Error on line: " + i +" Expected ';', found: " + pos.current.value);
            }
            System.out.println("Variable Name "+ varName + " = " +value );
            pos.advance(); // Move to the next statement
        }
    }

    public static int parseExpression(TokenPosition pos, int i) throws Exception {
        int value = parseTerm(pos, i);

        while (pos.current.type.equals("Symbol") && (pos.current.value.equals("+") || pos.current.value.equals("-"))) {
            String operator = pos.current.value;
            pos.advance();
            int right = parseTerm(pos, i);
            value = operator.equals("+") ? value + right : value - right;
        }

        return value;
    }

    public static int parseTerm(TokenPosition pos, int i) throws Exception {
        int value = parseFactor(pos, i);
        while (pos.current.type.equals("Symbol") && (pos.current.value.equals("*") || pos.current.value.equals("/"))) {
            String operator = pos.current.value;
            pos.advance();
            int right = parseFactor(pos, i);
            value = operator.equals("*") ? value * right : value / right;
        }

        return value;
    }

    public static int parseFactor(TokenPosition pos, int i) throws Exception {

        if (pos.current.type.equals("Literal")) {
            int value = Integer.parseInt(pos.current.value);
            pos.advance(); // Move past the literal
            return value;
        } else if (pos.current.type.equals("Identifier")) {
            String varName = pos.current.value;
            if (!variables.containsKey(varName)) {
                throw new Exception("Error on line: " + i +" Uninitialized variable: " + varName);
            }
            pos.advance(); // Move past the identifier
            return variables.get(varName);
        } else if (pos.current.type.equals("Symbol") && pos.current.value.equals("(")) {
            pos.advance(); // Move past '('
            int value = parseExpression(pos, i);
            if (!pos.current.type.equals("Symbol") || !pos.current.value.equals(")")) {
                throw new Exception("Error on line: " + i +" Expected ')', found: " + pos.current.value);
            }
            pos.advance(); // Move past ')'
            return value;
        } else if (pos.current.type.equals("eof"))
        {
            throw new Exception ( "Error on line: " + i +" Syntax Error: Expecting argument");
        }
            else {
            throw new Exception("Compilation Error on line: " + i +" Unexpected token: " + pos.current.value);
        }
    }


    public static class Token {

        String type;
        String value;

        Token(String type, String value){
            this.type=type;
            this.value=value;
        }
    }

    public static class TokenPosition {
        public Token current; // The current token
        public Iterator<Token> iterator;

        TokenPosition(Iterator<Token> iterator) {
            this.iterator = iterator;
            this.current = iterator.next(); // Start with the first token
        }

        public void advance() {
            if (iterator.hasNext()) {
                current = iterator.next();
            } else {
                current = new Token("eof", "");
            }
        }
    }

}

