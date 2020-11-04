import java.util.*;
import java.io.*;

public class Normalizer {
	public static HashMap<Integer, String> map = new HashMap<Integer, String>();
	public static HashMap<String, String> dates = new HashMap<String, String>();
	
	public static void main(String[] args) throws IOException {
		String[] scrubbed = scrubLines();
		String c = System.getProperty("user.dir");
		File file = new File(c+"/normalize.txt");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		

		for(String s : scrubbed) {
			String pureString = deNumber(s);
			writeToFile(pureString.toLowerCase(), bw);
		}
		bw.close();
	}
	
	//scrubs each line of original file for punctuation, initializations, capitalizations, and mathematical symbols
	//returns String array containing indexed list of scrubbed lines
	public static String[] scrubLines() {
		
		String c = System.getProperty("user.dir");
		String[] lines = new String[1000];
		String[] newLines = new String[1000];
		int index = 0;
		
		//makes an array of lines from the txt file
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(c+"/list.txt"));
			String line = reader.readLine();
			while (line != null) {
				lines[index] = line;
				index++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		index = 0;
		
		//individually scrubs each line - this code segment can be modified as needed with relatively low risk of breaking anything
		for (String line : lines) {
			newLines[index] = line.replace("U.S.A.", "u s a").replace("U.S.","u s").replace("USA", "u s a").replace("US", "u s").replace("NBA","n b a").
					replace("NFL", "n f l").replace("NCAA", "n c a a").replace("TV","t v") .replace("DJ", "d j").replace("a.m.", "a m").
					replace("?", "").replace("!","").replace(",", "").replace("-", " - ").replace("/", " / ").
					replace("*", " times ").replace("+", " plus ").replace("%", " percent ").replace(" ft", " feet ").
					replace(" cm ", " centimeters ").replace(" ml ", " milliliters ").replace(" km", " kilometers ").
					replace("p.m.", "p m").replace("a.m", "a m").replace(" x ", " times ").replace("=", "equals").
					replace("AM ", " a m").replace("#", "number ").replace("LAX", "l a x").replace("SFO", "s f o").
					replace("JFK", "j f k").replace(" tv", " t v ").replace(" AM ", " a m ").replace("ESPN", " e s p n ").
					replace("MSNBC", " m s n b c ").replace(" ac ", " a c ").replace(" AC ", " a c ").replace("HDMI", " h d m i ").
					replace("PM", " p m ").replace("pm", " p m ").replace("p.m", " p m ").replace("fm", " f m ").replace("FM", " f m ").
					replace("NPR", "n p r").replace("KQED", "k q e d").replace("KYIT", "k y i t").replace("KEFC", "k e f c").
					replace("OK", "o k").replace("DNC", "d n c").replace("CNN", "c n n").replace("CNBC", "c n b c").
					replace("KFC", "k f c").toLowerCase();
			
			//remove periods from the end and spaces from the beginning
			String tmp = newLines[index];
			if (tmp.endsWith(".")) {
				tmp = tmp.substring(0, tmp.length()-1);
				newLines[index] = tmp;
			}
			
			if (tmp.startsWith(" ")) {
				tmp = tmp.substring(1, tmp.length());
				newLines[index] = tmp;
			}
			newLines[index] = newLines[index].replace(".", " point ");
			index++;
		}
		
		//kill the dates
		for (int i = 0; i < newLines.length; i++) {
			newLines[i] = fixDates(newLines[i]);
		}
		
		return newLines;
		
	}
	
	//this method checks each line for dates, and translates them 
	public static String fixDates(String line) {
		fillDates();
		String[] words = line.split("\\s+");
		
		for (int i = 0; i < words.length; i++) {
			if(dates.containsKey(words[i])){
				words[i] = dates.get(words[i]);
			}
		}
		
		//now, let's reassemble the line as a String
		String cleanLine = "";
		
		for (String word : words) {
			cleanLine += word + " ";
		}
		
		return cleanLine;
	}
	
	
	
	//checks if a given 'word' is a dollar amount
	public static boolean isDollar(String s) {
		if (s.contains("$")) {
			return true;
		} 
		
		return false;
	}
	
	//converts dollar amounts to English words
	public static String wordDollar(String s){
		s = s.substring(1);
		StringBuilder sb = new StringBuilder();
		int amt = Integer.parseInt(s);
		
		if(isYear(s)) {
			sb.append(newYear(s) + " dollars");
			return sb.toString();
		} else if (amt < 1000) {
			sb.append(convert(amt) + " dollars");
		} else {
			sb.append(deNumber(s) + " dollars");
		}
		
		return sb.toString();		
	}
	
	//replaces all digits in a string with (usually) appropriate English counterparts
	public static String deNumber(String s) {
		fillMap();
		StringBuilder numEditor = new StringBuilder();
		StringBuilder finalString = new StringBuilder();
		String[] words = s.split("\\s+");
		String[] fixedWords = new String[words.length];
		int index = 0;
		
		for (String word : words) {
			
			if(isDollar(word)) {
				fixedWords[index] = wordDollar(word);
			} else if(isYear(word)) {
				fixedWords[index] = newYear(word);
				
				//this next if checks to see whether it's a time
				
			} else if (word.contains(":") && word.length() >= 4 && word.length() <= 5) {
				
				String[] nums = word.split(":");
				
				//one last check to make sure this is really a time
				if (!isNumeric(nums[0])) {
					
				} else if (!nums[1].equals("00")) {
					if(Integer.parseInt(nums[1]) < 10) {
						String mins = " oh" + convert(Integer.parseInt(nums[1]));
						fixedWords[index] = map.get(Integer.parseInt(nums[0])) + mins;
					} else if (Integer.parseInt(nums[1]) > 10) {
						String mins = convert(Integer.parseInt(nums[1]));
						fixedWords[index] = map.get(Integer.parseInt(nums[0])) + mins;
					}
				} else {
						fixedWords[index] = map.get(Integer.parseInt(nums[0]));
					}
				
			} else if(word.length() > 9) {
				fixedWords[index] = word;
			} else if (isNumeric(word)) {
				int num = Integer.parseInt(word);
				if(num==0){
		            fixedWords[index] = map.get(0);
		        }
				
		        if(num >= 1000000000){
		            int extra = (int)(num/1000000000);
		            numEditor.append(convert(extra) + " billion");
		            num = num%1000000000;
		        }
		 
		        if(num >= 1000000){
		            int extra = (int) (num/1000000);
		            numEditor.append(convert(extra) + " million");
		            num = num%1000000;
		        } 
		 
		        if(num >= 1000){
		            int extra = (int) (num/1000);
		            numEditor.append(convert(extra) + " thousand");
		            num = num%1000;
		        } 
		 
		        if(num > 0){
		            numEditor.append(convert(num));
		        } else if (num == 0) {
		        	numEditor.append("zero");
		        }
		 
		        fixedWords[index] = numEditor.toString().trim();
		        numEditor.setLength(0);
				} else {
				fixedWords[index] = word;
				}
				index++;
			}
		
			for (String word : fixedWords) {
				
				finalString.append(word + " ");
			}

			return finalString.toString();
		}
	
	//checks to see whether a 'word' is a four digit number
	//since they're always pronounced the same way, we treat them all as years
	public static boolean isYear(String s) {
		for (char c : s.toCharArray()) {
			if (s.length() != 4)
				return false;
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}
	
	//utility method to check whether a 'word' is a number
	public static boolean isNumeric(String word) {

        // null or empty
        if (word == null | word.length() == 0) {
            return false;
        }

        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;

    }
	
	//uses the map to convert any smaller digits to English words
	//hundreds, tens and ones require a little more finesse than 
	//thousands, millions and billions - this method is used by deNumber
	public static String convert(int num){
		 
        StringBuilder sb = new StringBuilder();
 
        if (num > 100) {
        	if(num % 100 == 0){
        		int numHundred = (num/100);
        		sb.append(" " + map.get(numHundred)+ " hundred");
        		num=num%100;
        	} else {
        		int numHundred = (num/100);
        		if (num % 100 < 10) {
        			sb.append(" " + map.get(numHundred) + " oh");
        			num = num%100;
        		} else {
        		sb.append(" " + map.get(numHundred));
        		num=num%100;
        		}
        	}
        }
        
        if(num == 100){
    		sb.append(" a hundred");
    		num=num%100;
        }
 
        if(num > 0){
            if(num>0 && num<=20){
                sb.append(" "+ map.get(num));
            }else{
                int numTen = (num/10);
                sb.append(" "+ map.get(numTen*10));
 
                int numOne= (num%10);
                if(numOne>0){
                    sb.append(" " + map.get(numOne));
                }
            }
        }
 
        return sb.toString();
    }
	
	//converts any four digit number to English words
	//example: humans would almost never read "2361" as "two thousand three hundred and sixty one"
	//this method takes that number and outputs "twenty three sixty one"
	public static String newYear(String s) {
		fillMap();
		
		//here we split the number in half using substrings, and work with its constituent parts
		
		int actualNumber = Integer.parseInt(s);
		String year = "";
		int mid = s.length() / 2;
		String[] halves = {s.substring(0, mid), s.substring(mid)};
		int secondHalf = Integer.parseInt(halves[1]);
		
		//handle the 20-ought's
		if(halves[0].equals("20") && secondHalf < 10 && secondHalf > 0) {
			return "two thousand " + map.get(secondHalf);
		}

		year = convert(Integer.parseInt(halves[0]));
		
		//handle hard thousands
		if (actualNumber % 1000 == 0) {
			return map.get(Integer.parseInt(halves[0])/10) + " " + "thousand";
		}
		
		//handle the 1's and teens (2001, 1906, etc)
		if (secondHalf <= 20) {
			if (secondHalf >= 10) {
				year += " " + map.get(secondHalf);
				return year;
			} else if (secondHalf < 10 && secondHalf > 0) {
				year += " oh " + map.get(secondHalf);
				return year;
			} else {
				return year += " hundred";
			}
		} else {
			int numTens = secondHalf / 10;
			year += " " + map.get(numTens * 10);

			int numOnes = secondHalf % 10;

			if (numOnes > 0) {
				return year += " " + map.get(numOnes);
			}
		}
		return year;
	}
	
	//utility method to fill a hashmap with keys and values denoting digits and their English counterparts
	//these token strings are used as building blocks for larger numbers
	public static void fillMap(){
        map.put(0, "Zero");
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");
        map.put(4, "Four");
        map.put(5, "Five");
        map.put(6, "Six");
        map.put(7, "Seven");
        map.put(8, "Eight");
        map.put(9, "Nine");
        map.put(10, "Ten");
        map.put(11, "Eleven");
        map.put(12, "Twelve");
        map.put(13, "Thirteen");
        map.put(14, "Fourteen");
        map.put(15, "Fifteen");
        map.put(16, "Sixteen");
        map.put(17, "Seventeen");
        map.put(18, "Eighteen");
        map.put(19, "Nineteen");
        map.put(20, "Twenty");
        map.put(30, "Thirty");
        map.put(40, "Forty");
        map.put(50, "Fifty");
        map.put(60, "Sixty");
        map.put(70, "Seventy");
        map.put(80, "Eighty");
        map.put(90, "Ninety");
    }
	
	//the same utility method for dates
	public static void fillDates() {
		dates.put("1st", "first");
		dates.put("2nd", "second");
		dates.put("3rd", "third");
		dates.put("4th", "fourth");
		dates.put("5th", "fifth");
		dates.put("6th", "sixth");
		dates.put("7th", "seventh");
		dates.put("8th", "eighth");
		dates.put("9th", "ninth");
		dates.put("10th", "tenth");
		dates.put("11th", "eleventh");
		dates.put("12th", "twelveth");
		dates.put("13th", "thirteenth");
		dates.put("14th", "fourteenth");
		dates.put("15th", "fifteenth");
		dates.put("16th", "sixteenth");
		dates.put("17th", "seventeenth");
		dates.put("18th", "eighteenth");
		dates.put("19th", "nineteenth");
		dates.put("20th", "twentieth");
		dates.put("21st", "twentyfirst");
		dates.put("22nd", "twentysecond");
		dates.put("23rd", "twentythird");
		dates.put("24th", "twentyfourth");
		dates.put("25th", "twentyfifth");
		dates.put("26th", "twentysixth");
		dates.put("27th", "twentyseventh");
		dates.put("28th", "twentyeighth");
		dates.put("29th", "twentyninth");
		dates.put("30th", "thirtieth");
		dates.put("31st", "thirtyfirst");
	}
	
	//writes lines to the normalize file 
	public static void writeToFile(String line, BufferedWriter bw) throws IOException {
		bw.write(line +"\n");	
	}
	
}
