import java.util.regex.Pattern;

public class CustomMatcher {

    public static boolean matchWord(String target , String word) {
        if (target.equals(word)) {
            return true;
        }
        return false;
    }

    public static boolean matchRegex(String target , String regex) {
        if (Pattern.compile(regex).matcher(target).matches()) {
            return true;
        }
        return false;
    }


}
