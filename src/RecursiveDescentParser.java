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
 * inout-stmt code    >>    name         |    output     <<    name-value
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

    // out code code
    private String code;
    // pointer to the code
    private int codePointer = 0;

    // used after var declaration to check if value should be float of int.
    private VarType currentVarType;

    //list to store errors
    private List<String> errorStack = new ArrayList<>();

    // set to store the user defined names
    private Map<String, VarType> userDefindNames = new HashMap();

    /**
     * constructor
     *
     * @param code
     */
    public RecursiveDescentParser(String code) {
        code = code.replaceAll("\n", "");
        code = code.replaceAll("\r", "");
        code = code.replaceAll(" ", "");
        this.code = code;
    }

    /**
     * increment our code pointer by specific value
     *
     * @param length
     */
    private void next(int length) {
        if (codePointer + length < (code.length())) codePointer += length;
    }


    /**
     * @param word
     * @return boolean
     * to find if our code at specific index match a certain string
     */
    private boolean matchString(String word) {
        try {
            return code.substring(codePointer, codePointer + word.length()).equals(word);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * @param target
     * @param regex
     * @return boolean
     * check if a given string match a regex
     */
    private boolean matchRegex(String target, String regex) {
        return Pattern.compile(regex).matcher(target).matches();
    }

    /**
     * get our code from current pointer
     *
     * @param token
     * @return Strin
     */
    private String getBeforeToken(String token) {
        try {
            return code.substring(codePointer, code.substring(codePointer).indexOf(token) + codePointer);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * get String from our code from current pointer until we reach a certain token(string)
     *
     * @param tokens
     * @return String
     */
    private String getBeforeTokens(Set<String> tokens) {
        List<Character> list = new ArrayList<>();
        int tempIndex = codePointer;
        while (codePointer < code.length()) {
            for (String token : tokens) {
                if (matchString(token)) {
                    codePointer = tempIndex;
                    return list.stream().map(String::valueOf).collect(Collectors.joining());
                }
            }
            list.add(code.charAt(codePointer));
            next(1);
        }
        codePointer = tempIndex;
        if (codePointer >= code.length()) return "";
        return list.stream().map(String::valueOf).collect(Collectors.joining());
    }

    /**
     * used to get the last parameter of statement from the code
     *
     * @return String
     */
    private String getStatementLastParameter() {
        return getBeforeTokens(new HashSet<>(Tokens.END_STATEMENT));
    }

    /**
     * used to get the factor value of the equation from the code
     *
     * @return String
     */
    private String getEquationFactor() {
        return getBeforeTokens(new HashSet<>(Tokens.FACTOR_SPLITTERS));
    }

    private boolean isNameExist(String name) {
        return userDefindNames.containsKey(name);
    }

    private boolean isNameAvailable(String name) {
        return !userDefindNames.containsKey(name);
    }

    private boolean isReservedWord(String name) {
        return Tokens.RESERVED_WORDS.contains(name);
    }

    /**
     * method to print the error stack
     *
     * @param error
     */
    private void error(String error) {
        errorStack.add(error);
    }

    /**
     * the method we should call to start parsing the code
     */
    public void parse() {
        if (program()) {
            System.out.println("valid syntax");
        } else {
            System.out.println("invalid syntax");
//            Collections.reverse(errorStack);
            // print the first log of the occurred error
            System.out.println(errorStack.get(0));
        }
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean program() {
        if (body()) {
            if (code.substring(codePointer).equals(Tokens.END_OF_FILE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean body() {
        if (libDecl()) {
            if (matchString(Tokens.MAIN)) {
                next(Tokens.MAIN.length());
                if (declaration()) {
                    if (block()) {
                        return true;
                    } else {
                        error("error in the body declaration");
                    }
                } else {
                    error("error in the variables declaration");
                }
            } else {
                error("error in the main declaration");
            }
        } else {
            error("error in the libraries declaration");
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean libDecl() {
        String libS = getBeforeToken(Tokens.MAIN);
        if (matchRegex(libS, Rejexes.LIBRARIES)) {
            next(libS.length());
            return true;
        } else {
            error("error in the libraries declaration");
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean declaration() {
        while (matchString(Tokens.CONST)) {
            next(Tokens.CONST.length());
            if (!constDecl()) {
                error("error in the const declaration");
                return false;
            }
        }
        while (matchString(Tokens.VAR)) {
            next(Tokens.VAR.length());
            if (!varDecl()) {
                error("error in the vars declaration");
                return false;
            }
        }
        return true;
    }


    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean constDecl() {
        if (dataType()) {
            String name = getBeforeToken(Tokens.EQUAL);
            if (matchRegex(name, Rejexes.NAME) && !isReservedWord(name) && isNameAvailable(name)) {
                next(name.length());
                userDefindNames.put(name, currentVarType);
                next(Tokens.EQUAL.length());
                if (value()) {
                    return true;
                } else {
                    error("error in the value of consts declaration");
                }
            } else {
                error("error in the name of consts declaration");
            }
        } else {
            error("error in the datatype of consts declaration");
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    public boolean varDecl() {
        if (dataType()) {
            if (nameList()) {
                return true;
            } else {
                error("error in the name list of vars declaration");
            }
        } else {
            error("error in the datatype of vars declaration");
        }
        return false;
    }


    /**
     * non-terminal function
     *
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
        error("error in the datatype declaration");
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean nameList() {
        String names = getBeforeToken(Tokens.SEMICOLON);
        int numberOfCommas = names.split(",").length - 1;
        for (String name : names.split(",")) {
            if (matchRegex(name, Rejexes.NAME) && !isReservedWord(name) && isNameAvailable(name)) {
                next(name.length());
                userDefindNames.put(name, currentVarType);
                if (numberOfCommas > 0) {
                    next(1);
                    numberOfCommas--;
                }
            } else {
                return false;
            }
        }
        next(Tokens.SEMICOLON.length());
        return true;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean value() {
        String value = getBeforeToken(Tokens.SEMICOLON);
        if (currentVarType == VarType.Int) {
            if (StringValidation.isInteger(value)) {
                next(value.length());
                next(Tokens.SEMICOLON.length());
                return true;
            }
        } else if (StringValidation.isFloat(value)) {
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
     *
     * @return boolean
     */
    private boolean block() {
        if (matchString(Tokens.OPEN_BRACE)) {
            next(Tokens.OPEN_BRACE.length());
            if (stmtList()) {
                if (matchString(Tokens.CLOSE_BRACE)) {
                    next(Tokens.CLOSE_BRACE.length());
                    return true;
                } else {
                    error("missing } or there is wrong text before }");
                }
            } else {
                error("error in the statement list");

            }
        } else {
            error("missing { or there is wrong text before }");
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean stmtList() {
        while (!matchString(Tokens.CLOSE_BRACE)) {
            if (!statement()) {
                error("error in the statement declaration ");
                return false;
            }
            if (matchString(Tokens.SEMICOLON)) {
                next(Tokens.SEMICOLON.length());
                if (matchString(Tokens.CLOSE_BRACE)) {
                    error("there is ; after last statement");
                    return false;
                }
            } else {
                if (!matchString(Tokens.CLOSE_BRACE)) {
                    error("missing ;");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean statement() {
        if (matchString(Tokens.OPEN_BRACE)) {
            if (!block()) {
                error("error in the block statement declaration ");
                return false;
            }
        } else if (matchString(Tokens.IF)) {
            if (!ifStatement()) {
                error("error in the if statement declaration ");
                return false;
            }
        } else if (matchString(Tokens.WHILE)) {
            if (!whileStatement()) {
                error("error in the while statement declaration ");
                return false;
            }
        } else if (matchString(Tokens.INPUT) || matchString(Tokens.OUTPUT)) {
            if (!inOutStatement()) {
                error("error in the io statement declaration ");
                return false;
            }
        } else if (!assignStatement()) {
            error("error in the assign statement declaration ");
            return false;
        }

        return true;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean assignStatement() {
        String name = getBeforeToken(Tokens.EQUAL);
        if (matchRegex(name, Rejexes.NAME) && isNameExist(name)) {
            next(name.length());
            next(Tokens.EQUAL.length());
            currentVarType = userDefindNames.get(name);
            if (exp()) {
                return true;
            } else {
                error("error in the assign statement exp declaration ");
            }
        } else {
            error("error in the assign statement name declaration ");
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean exp() {
        if (term()) {
            while (addSign()) {
                next(1);
                if (!term()) {
                    error("error in the add operation declaration ");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean term() {
        if (factor()) {
            while (mulSign()) {
                next(1);
                if (!factor()) {
                    error("error in the mul operation declaration ");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean addSign() {
        return (matchString("+") || matchString("-"));
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean mulSign() {
        return (matchString("*") || matchString("/") || matchString("%"));
    }


    /**
     * non-terminal function
     *
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
            String factor = getEquationFactor();
            if(factor.length() == 0) {
                error("factor is missing or there is wrong text before factor");
                return false;
            }
            if (Character.isDigit(factor.charAt(0))) {
                if (!StringValidation.isInteger(factor) && currentVarType == VarType.Int) {
                    error("error in the factor value, non integer assigned to integer ");
                    return false;
                } else {
                    if (StringValidation.isFloat(factor) || StringValidation.isInteger(factor)) {
                        next(factor.length());
                        return true;
                    }
                    error("error in the factor value, non number assigned to number ");
                    return false;
                }
            } else if (isNameExist(factor)) {
                next(factor.length());
                return true;
            }else {
                error("name not exist");
            }
        }
        error("error in the factor name");
        return false;
    }


    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean ifStatement() {
        if (matchString(Tokens.IF)) {
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
                                } else {
                                    error("missing endif or there is wrong text before endif");
                                }
                            } else {
                                error("error in the else");
                            }
                        } else {
                            error("error in the if statement");
                        }
                    } else {
                        error("missing ) or there is wrong text before )");
                    }
                } else {
                    error("error in the boolean expression");
                }
            } else {
                error("missing ( or there is wrong text before (");
            }
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean booleanExpresion() {
        String parm1 = getBeforeTokens(Tokens.RELATIONAL_OPERATIONS);
        // if we got a name with length > 0 then for sure one of the tokens exist
        if (isNameExist(parm1) || StringValidation.isValue(parm1)) {
            next(parm1.length());
            next(1);
            if (code.charAt(codePointer) == '=') {
                next(1);
            }
            String parm2 = getBeforeToken(Tokens.CLOSE_PARENTHESE);
            if (isNameExist(parm2) || matchRegex(parm2, Rejexes.FLOAT)) {
                next(parm2.length());
                return true;
            }else {
                error("error in the second parameter");
            }
        } else{
            if(isNameExist(parm1) || StringValidation.isValue(parm1)) {
                error("error in the RELATIONAL OPERATIONS ");
            }else {
                error("error in the first parameter");
            }
        }
        return false;
    }

    /**
     * non-terminal function
     *
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
        }else {
            error("else is missing or or there is wrong text before else");
        }
        return false;
    }

    /**
     * non-terminal function
     *
     * @return boolean
     */
    private boolean inOutStatement() {
        if (matchString(Tokens.INPUT)) {
            next(Tokens.INPUT.length());
            if (matchString(">>")) {
                next(2);
                String name = getStatementLastParameter();
                if (isNameExist(name)) {
                    next(name.length());
                    return true;
                } else {
                    error("error in the input parameter");
                }
            } else{
                error("missing >> for input statement or there is wrong text before >>");
            }
        } else if (matchString(Tokens.OUTPUT)) {
            next(Tokens.OUTPUT.length());
            if (matchString("<<")) {
                next(2);
                String value_name = getStatementLastParameter();
                if (isNameExist(value_name) || StringValidation.isValue(value_name)) {
                    next(value_name.length());
                    return true;
                } else {
                    error("error in the output parameter");
                }
            }else {
                error("<< is missing for output or there is wrong text before <<");
            }
        }
        return false;
    }

    /**
     * non-terminal function
     *
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
                    }else{
                        error("missing ) or there is wrong text before )");
                    }
                }
            }else {
                error("missing ( or there is wrong text before (");
            }
        }
        return false;
    }
}
