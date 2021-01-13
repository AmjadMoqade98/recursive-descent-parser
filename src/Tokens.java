import java.util.*;

public class Tokens {
    final static String MAIN = "main()";
    final static String CONST = "const";
    final static String VAR = "var";
    final static String INT = "int";
    final static String FLOAT = "float";
    final static String INPUT = "input";
    final static String OUTPUT = "output";
    final static String IF = "if";
    final static String ELSE = "else";
    final static String ENDIF = "endif";
    final static String WHILE = "while";
    final static String OPEN_PARENTHESES = "(";
    final static String CLOSE_PARENTHESE = ")";
    final static String OPEN_BRACE = "{";
    final static String CLOSE_BRACE = "}";
    final static String END_OF_FILE = "$";
    final static String EQUAL = "=";
    final static String SEMICOLON = ";";
    final static Set<String> RELATIONAL_OPERATIONS = new HashSet<>(Arrays.asList("==" , "!=" , "<" , "<=" ,">" ,">=")) ;
    final static List<String> END_STATEMENT = new ArrayList<>(Arrays.asList(Tokens.IF,Tokens.ELSE ,Tokens.ENDIF,Tokens.CLOSE_BRACE,Tokens.SEMICOLON));
    final static List<String> ADD_OPERATORS = new ArrayList<>(Arrays.asList("+" ,"-"));
    final static List<String> MUL_OPERATORS = new ArrayList<>(Arrays.asList("*" ,"/" ,"%"));
    final static List<String> FACTOR_SPLITTERS = new ArrayList<>(){{addAll(ADD_OPERATORS);addAll(MUL_OPERATORS);addAll(END_STATEMENT);addAll(Arrays.asList(")"));}};

}
