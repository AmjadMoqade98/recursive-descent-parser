import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Syntax Parser to Parse the following grammar
 * program     body   $
 * body    lib-decl       main ()       declarations      block
 * lib-decl    (  # include < name >   ;   )*
 * declarations    const-decl       var-decl
 * const-decl   (  const   data-type   name   =    value   ;   )*
 * var-decl     (  var    data-type    name-list   ;   )*
 * name-list     name   (  ,   name  )*
 * data-type     int     |       float
 * name       “user-defined-name”
 * block    {    stmt-list    }
 * stmt-list      statement   (  ;     statement   )*
 * statement  ass-stmt     |     inout-stmt    |      if-stmt     |    while-stmt   |    block  |   
 * ass-stmt  name     =      exp
 * exp  term      (  add-oper   term  )*
 * term  factor   (  mul-oper    factor   )*
 * factor   (     exp     )     |     name     |     value
 * value   “float-number”   |        “int-number”
 * add-sign   +    |   -
 * mul-sign  *    |    /   |   %
 * inout-stmt input    >>    name         |    output     <<    name-value
 * if-stmt  if   (   bool-exp  )  statement     else-part     endif
 * else-part   else     statement   |   
 * while-stmt  while   (   bool-exp    )   {    stmt-list    }
 * bool-exp  name-value       relational-oper        name-vaue
 * name-value   name    |      value
 * relational-oper   ==      |       !=         |     <     |       <=     |     >     |     >=
 */


public class RecursiveDescentParser {

    private static enum VarType {
        Int,
        Float,
    }

    // out code input
    private String input;
    // pointer to the code
    private int index = 0;

    // used after var declaration to check if value should be float of int.
    private VarType currentVarType;

    /**
     * constructor
     * @param input
     */
    public RecursiveDescentParser(String input) {
        input = input.replaceAll("\n", "");
        input = input.replaceAll("\r", "");
        input = input.replaceAll(" ", "");
        this.input = input;
    }

    /**
     * increment our input pointer by specific value
     * @param length
     */
    private void next(int length) {
        if (index + length < (input.length())) index += length;
    }


    /**
     *
     * @param word
     * @return boolean
     * to find if our input at specific index match a certain string
     */
    private boolean matchString(String word) {
        if (index + word.length() >= input.length()) return false;
        if (word.length() == 1) return input.charAt(index) == word.charAt(0);
        return input.substring(index, index + word.length()).equals(word);
    }

    /**
     *
     * @param target
     * @param regex
     * @return boolean
     * check if a given string match a regex
     */
    private boolean matchRegex(String target, String regex) {
        return Pattern.compile(regex).matcher(target).matches();
    }

    /**
     * get our input from current pointer
     * @param token
     * @return Strin
     */
    private String getBeforeToken(String token) {
        if (index + token.length() >= input.length()) return "";
        return input.substring(index, input.substring(index).indexOf(token) + index);
    }

    /**
     * get String from our input from current pointer until we reach a certain token(string)
     * @param tokens
     * @return String
     */
    private String getBeforeTokens(Set<String> tokens) {
        List<Character> list = new ArrayList<>();
        int tempIndex = index;
        while (index < input.length()) {
            for (String token : tokens) {
                if (matchString(token)) {
                    return list.stream().map(String::valueOf).collect(Collectors.joining());
                }
            }
            list.add(input.charAt(index));
            next(1);
        }
        index = tempIndex;
        if (index >= input.length()) return "";
        return list.stream().map(String::valueOf).collect(Collectors.joining());
    }

    /**
     *  used to get the last parameter of statement from the input
     * @return String
     */
    private String getStatementLastParameter() {
        return getBeforeTokens(new HashSet<>(Tokens.END_STATEMENT));
    }

    /**
     *  used to get the factor value of the equation from the input
     * @return String
     */
    private String getFactor() {
        return getBeforeTokens(new HashSet<>(Tokens.FACTOR_SPLITTERS));
    }


    /**
     * the method we should call to start parsing the input
     */
    public void parse() {
        if (program()) {
            System.out.println("legal syntax");
        } else {
            System.out.println("illegal syntax");
        }
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean program() {
        if (body()) {
            if (input.substring(index).equals(Tokens.END_OF_FILE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean body() {
        if (libDecl()) {
            if (matchString(Tokens.MAIN)) {
                next(Tokens.MAIN.length());
                if (declaration()) {
                    if (block()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean libDecl() {
        String libS = getBeforeToken(Tokens.MAIN);
        if (matchRegex(libS, Rejexes.LIBRARIES)) {
            next(libS.length());
            return true;
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean declaration() {
        while (matchString(Tokens.CONST)) {
            next(Tokens.CONST.length());
            if (!constDecl()) return false;
        }
        while (matchString(Tokens.VAR)) {
            next(Tokens.VAR.length());
            if (!varDecl()) return false;
        }
        return true;
    }


    /**
     * non-terminal function
     * @return boolean
     */
    private boolean constDecl() {
        if (dataType()) {
            String name = getBeforeToken(Tokens.EQUAL);
            if (matchRegex(name, Rejexes.NAME)) {
                next(name.length());
                next(Tokens.EQUAL.length());
                if (value()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    public boolean varDecl() {
        if (dataType()) {
            if (nameList()) {
                return true;
            }
        }
        return false;
    }


    /**
     * non-terminal function
     * @return boolean
     */
    private boolean dataType() {
        if (matchString(Tokens.INT)) {
            next(Tokens.INT.length());
            currentVarType = VarType.Int;
            return true;
        } else if (matchString(Tokens.FLOAT)) {
            next(Tokens.FLOAT.length());
            currentVarType = VarType.Float;
            return true;
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean nameList() {
        String names = getBeforeToken(Tokens.SEMICOLON);
        if (matchRegex(names, Rejexes.NAME_LIST)) {
            next(names.length());
            next(Tokens.SEMICOLON.length());
            return true;
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean value() {
        String value = getBeforeToken(Tokens.SEMICOLON);
        if (currentVarType == VarType.Int) {
            if (matchRegex(value, Rejexes.INT)) {
                next(value.length());
                next(Tokens.SEMICOLON.length());
                return true;
            }
        } else if (currentVarType == VarType.Float) {
            if (matchRegex(value, Rejexes.FLOAT)) {
                next(value.length());
                next(1);
                return true;
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean block() {
        if (matchString(Tokens.OPEN_BRACE)) {
            next(Tokens.OPEN_BRACE.length());
            if (stmtList()) {
                if (matchString(Tokens.CLOSE_BRACE)) {
                    next(Tokens.CLOSE_BRACE.length());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean stmtList() {
        while (!matchString(Tokens.CLOSE_BRACE)) {
            if (!statement()) return false;
        }
        return true;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean statement() {
        if (matchString(Tokens.OPEN_BRACE)) {
            if (!block()) return false;
        } else if (matchString(Tokens.IF)) {
            if (!ifStatement()) return false;
        } else if (matchString(Tokens.WHILE)) {
            if (!whileStatement()) return false;
        } else if (matchString(Tokens.INPUT) || matchString(Tokens.OUTPUT)) {
            if (!inOutStatement()) return false;
        } else if (!assignStatement()) return false;

        if (matchString(Tokens.SEMICOLON)) next(Tokens.SEMICOLON.length());
        return true;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean assignStatement() {
        String name = getBeforeToken(Tokens.EQUAL);
        if (matchRegex(name, Rejexes.NAME)) {
            next(name.length());
            next(Tokens.EQUAL.length());
            if (exp()) {
                return true;
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean exp() {
        if (term()) {
            while (addSign()) {
                next(1);
                if (!term()) return false;
            }
            return true;
        }

        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean term() {
        if (factor()) {
            while (mulSign()) {
                next(1);
                if (!factor()) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean addSign() {
        return (matchString("+") || matchString("-"));
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean mulSign() {
        return (matchString("*") || matchString("/") || matchString("%"));
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean factor() {
        if (matchString(Tokens.OPEN_PARENTHESES)) {
            next(Tokens.OPEN_PARENTHESES.length());
            if (!exp()) return false;
            if (matchString(Tokens.CLOSE_PARENTHESE)) {
                next(Tokens.CLOSE_PARENTHESE.length());
                return true;
            }
        } else {
            String factor = getFactor();
            if (matchRegex(factor, Rejexes.FLOAT) || matchRegex(factor, Rejexes.NAME)) {
                return true;
            }
        }
        return false;
    }


    /**
     * non-terminal function
     * @return boolean
     */
    private boolean ifStatement() {
        next(Tokens.IF.length());
        if (matchString(Tokens.OPEN_PARENTHESES)) {
            next(Tokens.OPEN_PARENTHESES.length());
            if (booleanExpresion()) {
                if (matchString(Tokens.CLOSE_PARENTHESE)) {
                    next(Tokens.CLOSE_PARENTHESE.length());
                    if (statement()) {
                        if (elsePart()) {
                            if (matchString(Tokens.ENDIF)) {
                                next(Tokens.ENDIF.length());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean booleanExpresion() {
        String parm1 = getBeforeTokens(Tokens.RELATIONAL_OPERATIONS);
        // if we got a name with length > 0 then for sure one of the tokens exist
        if (matchRegex(parm1, Rejexes.NAME) || matchRegex(parm1, Rejexes.FLOAT)) {
            // since the relational operation length can be 1 or 2
            if (Tokens.RELATIONAL_OPERATIONS.contains(input.substring(index, index + 2))) {
                next(2);
            } else {
                next(1);
            }


            String parm2 = getBeforeToken(Tokens.CLOSE_PARENTHESE);
            if (matchRegex(parm2, Rejexes.NAME) || matchRegex(parm2, Rejexes.FLOAT)) {
                next(parm2.length());
                return true;
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean elsePart() {
        if (matchString(Tokens.ELSE)) {
            next(Tokens.ELSE.length());
            if (matchString(Tokens.ENDIF)) {
                return true;
            } else if (statement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean inOutStatement() {
        if (matchString(Tokens.INPUT)) {
            next(Tokens.INPUT.length());
            if (matchString(">>")) {
                next(2);
                String name = getStatementLastParameter();
                if (matchRegex(name, Rejexes.NAME)) {
                    return true;
                }
            }
        } else if (matchString(Tokens.OUTPUT)) {
            next(Tokens.OUTPUT.length());
            if (matchString("<<")) {
                next(2);
                String outputParameter = getStatementLastParameter();
                if (matchRegex(outputParameter, Rejexes.NAME) || matchRegex(outputParameter, Rejexes.FLOAT)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * non-terminal function
     * @return boolean
     */
    private boolean whileStatement() {
        if (matchString(Tokens.WHILE)) {
            next(Tokens.WHILE.length());
            if (matchString("(")) {
                next(1);
                if (booleanExpresion()) {
                    if (matchString(")")) {
                        next(1);
                        if (block()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
