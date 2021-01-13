/**
 * Constant class for the used rejex in the program
 */
public class Rejexes {
    final static String INT = "[0-9][0-9]*";
    final static String FLOAT = "[0-9][0-9]*(\\.([0-9])+)?";
    final static String LIBRARIES = "(#include((<[^>]+>));)*";
    final static String NAME = "[a-zA-Z][a-zA-Z0-9]*";
    final static String NAME_LIST = "([a-zA-Z][a-zA-Z0-9]*)(,[a-zA-Z][a-zA-Z0-9]*)*";
}
