public class StringValidation {


    public static boolean isInteger(String input) {
        try { //Try to make the input into an integer
            Integer.parseInt(input);
            return true; //Return true if it works
        } catch (Exception e) {
            return false; //If it doesn't work return false
        }
    }

    public static boolean isFloat(String input) {
        try { //Try to make the input into an integer
            Float.parseFloat(input);
            return true; //Return true if it works
        } catch (Exception e) {
            return false; //If it doesn't work return false
        }
    }

    public static boolean isValue(String input) {
        return (isInteger(input) || isFloat(input));
    }


}
