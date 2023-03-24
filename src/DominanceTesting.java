import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DominanceTesting {

	public static String inputFile="src/inputSPC.txt";
	public static Map<CPNetVariableTuples, Set<Tuples>> cpNet;
	public static char[] variables;
	public static String[] dependencies;
	public static boolean dominates = false;
	public static void main(String[] args) {
		cpNet =  new HashMap<CPNetVariableTuples, Set<Tuples>>();
		Outcome o1 = new Outcome();
		o1.values = new int[] {5 ,4 ,4 ,1, 5, 4, 5, 5, 5, 1};
		Outcome o2 = new Outcome();
		o2.values = new int[] {5 ,2 ,4 ,1 ,5 ,4 ,5 ,5, 5 ,4  };
		takeInput(inputFile);
		int pointerLimit =0;
		SearchPartialCP.numberOfvariables = variables.length;
		dominates = doesDominates(o1, o2,cpNet,dependencies,pointerLimit);
		if(!dominates)
			System.out.println("Not dominating");
	}

	public static boolean doesDominates(Outcome o1, Outcome o2,Map<CPNetVariableTuples, Set<Tuples>> cpNet,String[] dependencies,int pointerLimit) {
		dominates = false;
		//System.out.println("Dominance testing for solution:"+o1+" Solution "+o2);
		if(o1.equals(o2)) {
			//System.out.println("Solution 2("+o2+") is dominated by solution 1("+o1+")");
			dominates = true;
		}
		if(dominates)
			return dominates;
		ArrayList<Outcome> al=new ArrayList<Outcome>();
		Outcome tempOutcome = null;
		int pointer = o2.values.length-1;
		for(;pointer>=pointerLimit;pointer--) {
			char var = (char)(pointer+'A');
			//if o2 is a1,b2,c1- the this function will return the preference order for (1,2,C)
			Set<Tuples> setTuples = CommonUtilities.findCorrespondingSetFromCPNet(var, o2.values, cpNet, dependencies);
			for(Tuples t : setTuples) {
				if(t.b == o2.values[pointer]) {
					//update the outcome with the better one
					tempOutcome = new Outcome(o2);
					tempOutcome.values[pointer] = t.a;
					al.add(tempOutcome);
					/*
					if(pointer <= o2.values.length) {
						pointer=o2.values.length+1;
					}*/
				}
			}
			while(!al.isEmpty() && !dominates) {
				Outcome outcome = al.get(al.size()-1);
				al.remove(al.size()-1);
				dominates = doesDominates(o1, outcome,cpNet,dependencies,pointer+1);
				if(dominates)
					break;
			}
		}
		return dominates;
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
