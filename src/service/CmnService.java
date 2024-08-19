package service;

public class CmnService {
    
    
    // "#1" -> 1
    public static int strToNum(String numStr) {
        String numStrAfter = new StringBuilder(numStr).delete(0, 1).toString();
        return Integer.valueOf(numStrAfter);
    }
    
    
}
