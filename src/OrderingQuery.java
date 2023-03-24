import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrderingQuery {

	public static String inputFile="src/inputSPCPaper.txt";
	public static Map<CPNetVariableTuples, Set<Tuples>> cpNet;
	public static char[] variables;
	public static String[] dependencies;
	public static boolean dominates = false;
	public static void main(String[] args) {
		cpNet =  new HashMap<CPNetVariableTuples, Set<Tuples>>();
		Outcome o1 = new Outcome();
		o1.values = new int[] {1,1,1};
		Outcome o2 = new Outcome();
		o2.values = new int[] {2,2,2};
		takeInput(inputFile);
		SearchPartialCP.numberOfvariables = variables.length;
		dominates = doesDominates(o1, o2,cpNet,dependencies);
		
	}

	public static boolean doesDominates(Outcome o1, Outcome o2,Map<CPNetVariableTuples, Set<Tuples>> cpNet,String[] dependencies) {
		System.out.println("testing for solution (Ordering Query):"+o1+" Solution "+o2);
		if(o1.equals(o2)) {
			//System.out.println("Solution 2 dominates solution 1. Both are Equal");
			return true;
		}
		int pointer = 0;
		Outcome tempOutcome = null;
		ArrayList<Outcome> al=new ArrayList<Outcome>();
		
		for(pointer=0;pointer < o2.values.length;pointer++) {
			//if both has the same value a1b1c1 and a1b1c2, a1 and b1 will be skipped and moved to check C
			if(o1.values[pointer] == o2.values [pointer])
				continue;
			char var = (char)(pointer+'A');
			//if o2 is a1,b2,c1- and var is C then this function will return the preference order for (1,2,C)
			Set<Tuples> setTuples = CommonUtilities.findCorrespondingSetFromCPNet(var, o2.values, cpNet, dependencies);
			for(Tuples t : setTuples) {
				//o1>o2
				if(t.a == o1.values[pointer] && t.b == o2.values[pointer]) {
					System.out.println("Solution 1 is preferred than Solution 2");
					return true;
				}
				//o2>o1
				else if(t.b == o1.values[pointer] && t.a == o2.values[pointer]) {
					System.out.println("Solution 1 is preferred than Solution 2");
					return false;
				}
			}
			//o1~o2
			System.out.println("Solution 1 and solution 2 indifference");
			return false;
		}
		return false;
	}	
	// Take the input from the file
	public static void takeInput(String fileName) {
		String line;
		String[] tokens;
		try {
			FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                tokens = line.split("=");
                if(tokens.length == 2) {
                	if(tokens[0].trim().equalsIgnoreCase("variables"))
                		variables= tokens[1].trim().toCharArray();
                	else if(tokens[0].trim().equalsIgnoreCase("cp_net")) {
                		String[] cpTokens;
                		
                		String a;
                		char b;
                		CPNetVariableTuples var;
                		
                		String[] cpTokensTuples;
                		int a1,b1;
                		Tuples tuple;
                		Set<Tuples> setTuples;
                		
                		//a1,c1,B: 1 2,3 2 
                		while((line = bufferedReader.readLine()) != null) {
                			cpTokens = line.split(":");
                			//first part is cpNetvariableuples	a1,c1,B
                			a = cpTokens[0].substring(0, cpTokens[0].length()-2);	//a1,c1
                			b = cpTokens[0].charAt(cpTokens[0].length()-1);			//B
                			var = new CPNetVariableTuples(a,b);
                			
                			//second part is tuples of that variable 1 2,3 2
                			cpTokensTuples = cpTokens[1].trim().split(",");
                			setTuples = new HashSet<Tuples>();
                			for(String s:cpTokensTuples) {
                				s= s.trim();
                				a1 = Character.getNumericValue(s.charAt(0));
                				b1 = Character.getNumericValue(s.charAt(2));
                				tuple = new Tuples(a1, b1);
                				setTuples.add(tuple);
                			}
                			cpNet.put(var, setTuples);          			
                		}
                		
                	}
                	else if(tokens[0].trim().equalsIgnoreCase("parent"))
                		dependencies= tokens[1].trim().split(",");
                }
            }
		}
		catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }catch(Exception e) {
        	e.printStackTrace();
        }
	}
}
