package utils;

public class NumberValidation {

	public static boolean isDouble(String str) {
	    try {
	        if(!str.isEmpty())
	        	Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}

	public static boolean isInteger(String str) {
	    try { 
	        if(!str.isEmpty())	    	
	        	Integer.parseInt(str); 
	        return true;
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	}
	
}
