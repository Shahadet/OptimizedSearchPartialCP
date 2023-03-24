
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class SearchPartialCP {
	
	public static String inputFile="src/inputSPCpaper.txt";
	public static String ouputFileName="src/outcome.txt";
	
	public static boolean constraintPropagation = false; //If this value is false AC/FLA/FC will not work
	public static boolean useDominanceTesting = true;	//if false it will run ordering query
	public static boolean applyArcConsistency = false;
	public static boolean useForwardCheck = false;
	public static boolean useFullLookAhead = false;
	public static boolean applyVariableOrdering = false;
	
	public static char[] variables;
	public static int numberOfvariables;
	public static String[] dependencies;
	public static int[][] dependenciesInt;	//if c depends on A,B  [3][0]=1, [3][1] =2
	public static String[] childs;
	public static Map<CPNetVariableTuples, Set<Tuples>> cpNet;
	public static int domainSize=0;
	public static Map<VariableTuples, Set<Tuples>> hardConstraints=null;
	public static List<Outcome> outcomeList;
	public static LoggerSPC logger;
	public static Map<Integer,List<Integer>> domains;		//indexed from 1->A,1->a1
	public static int[] variableCalledByParents;
	
	
	public static void main(String []args) {
		
		//CPNetGenerator.main(null);
		
		cpNet =  new HashMap<CPNetVariableTuples, Set<Tuples>>();
		hardConstraints =  new HashMap<VariableTuples, Set<Tuples>>();
		outcomeList = new ArrayList<Outcome>();
		variableCalledByParents =  new int[numberOfvariables];
		takeInput(inputFile);
		
		printDependencies();
		printChilds();
		printHardConstraints(hardConstraints);
		
		domains  = CommonUtilities.generateDomainsForVariable();
		if(applyVariableOrdering)
			CommonUtilities.generateVariableOrderWithConstraints(hardConstraints);
		//Assignment k is an array of integer denoting the value for each variable
		int[] k =  new int[numberOfvariables];
		
		
		long start1 = System.currentTimeMillis();
		System.out.println("--------------Search Partial CP-net-----------------------");
		try {
			logger = new LoggerSPC();
			//Apply Arc Consistency
			if(applyArcConsistency) 
				domains =  CheckConsistency.applyArcConsistency(domainSize, hardConstraints);
			searchPartialCP(cpNet,hardConstraints,domains,k);
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("--------------Search Partial CP-net End ------------------");
		long end1 = System.currentTimeMillis();
		System.out.println("Elapsed Time in  seconds: "+ (end1-start1)/1000.0); 
		
		System.out.println("Produced Outcomes ("+outcomeList.size()+"):");
		for(Outcome o: outcomeList) {
			printAssignment(o.values);
		}
		//Write the outcomes into a file
		PrintStream fileOut = null;
		try {
			fileOut = new PrintStream(ouputFileName);
			System.setOut(fileOut);
			for(Outcome o: outcomeList) {
				printAssignment(o.values);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	//Parameter- CPnet, Constraints,domain set, assignments, 
	public static List<Outcome> searchPartialCP(Map<CPNetVariableTuples, Set<Tuples>> N,Map<VariableTuples, Set<Tuples>> C, 
									Map<Integer,List<Integer>> ds,	int[] k) {
		//														Choose any variable X with no parents in N
		//The first variable from k unassigned is chosen
		int variable = 0; //variable(0->A,1->B)
		char chosenVar=' ';
		if(applyVariableOrdering)
			variable = CommonUtilities.nextVariablebyOrdering(k,C,dependenciesInt);
		else
			variable = CommonUtilities.nextVariable(k);
		chosenVar= (char)('A'+variable);

		//Create a set of local result or assignments 			Set: R = {} (i.e. initialize the set of local results)
		List<Outcome> outcomeR = new ArrayList<Outcome>();
		
		
		//take the set of domain for the chosen variable		Set: P = D(X)
		
		//Having a local copy of domain set to refresh the domain set. Used for constraint propagation
		Map<Integer,List<Integer>> locDomains;
		locDomains = CommonUtilities.cloneMapHCDom(ds,true);
		
		List<Integer> domainSetP = new ArrayList<Integer>();
		List<Integer> domainSet = locDomains.get(variable+1);
		for(Integer i:domainSet)	
		{
			domainSetP.add(i);
		}
		//for all the value from the domain						while P i not empty do
		while(domainSetP.size()> 0) {
			if(constraintPropagation) {
				//Refresh Domain to the state when current variable is called for Constraint Propagation
				locDomains = CommonUtilities.cloneMapHCDom(ds,true);
			}
			Map<VariableTuples, Set<Tuples>> ci=null;
			//take the best among all the currently 				Choose xi E P such that there is no xj E P-{xi} with xj>xi
			//available possible values for that variable
			Integer chosenDom = 0;
			chosenDom = CommonUtilities.chooseBestFromCPNet(N,chosenVar,domainSetP,k,dependencies);
			
			//System.out.println("Chosen value from domain of "+chosenVar+" :"+chosenDom);
			
			//remove the best value from the domain list			Set: P= P- {xi}
			domainSetP.remove(chosenDom);
			
			//Strengthening the constraints							Strengthen the constraints C by X = xi to obtain Ci
			AtomicBoolean constraintsAdded = new AtomicBoolean(false);
			ci = strengtheningConstraints(C, chosenVar, chosenDom, constraintsAdded);
			
			//System.out.println("Strengthening Constraints: Added-["+constraintsAdded+"]");
			//printHardConstraints(ci);
			
			//														if(There exist xk E D(X) - {xi} such that xk > xi and Ck c= Ci ) 
			//														or (Ci is inconsistent)		then
			//															continue with the next iteration
			
			//Check Consistency 
			boolean isConsistent = true;
			if(useForwardCheck && constraintPropagation)
				isConsistent = CheckConsistency.checkConsistencyFC(chosenVar, chosenDom, ci, k,locDomains);
			else if(useFullLookAhead && constraintPropagation)
				isConsistent = CheckConsistency.checkConsistencyFLA(chosenVar, chosenDom, ci, k, locDomains);
			else
				isConsistent = CheckConsistency.checkConsistency(ci,k, chosenVar, chosenDom);
			
			if(!isConsistent)
				continue;
			
			//Check if there is any other preferred value with lower constraints
			boolean foundBetterValueLessOrSameConstraints = findBetterValueLessOrSameConstraints(N,ci,k,chosenVar, chosenDom);
			if(foundBetterValueLessOrSameConstraints && constraintsAdded.get()) {
				System.out.println("Found a better value with same or less constraints");
				continue;
			}
			//														else
			//															Let K' be the partial assignment induced by X = xi and Ci
			//Partial Assignment
			int[] kPrime =  new int[numberOfvariables];
			kPrime[chosenVar-'A']= chosenDom;
			k[chosenVar-'A']= chosenDom;
			//partialAssignment(kPrime,ci,chosenVar,chosenDom);
			
			
			//combine previous assignment with current assignment
			for(int i=0;i<numberOfvariables;i++) {
				if(k[i]>0)
					kPrime[i] = k[i];
			}
			
			//System.out.println("Result of Partial assignment");
			//printAssignment(kPrime);
			
			
			//															Construct the sub partial CP-net Ni by removing variables assigned by K' from N
			//															Let Ni1,Ni2,...Nil be the component of ni that are connected either by edges of Ni or by the constraints Ci
			//															for each k E {1,2, ... l} do
			//																Sik = Search-Partial-CP(Nik, Ci, K U K');
		
			if(!(new Outcome(kPrime).isTotal()) ) {
 				outcomeR = searchPartialCP(N, ci,locDomains, kPrime);
			}
			else {
				//														end for
				//														if Sik != null for all k E {1,2,..,l} then
				//															for o E K' x Si1 x Si2 x ... x Sil do
				//																if K U o' > K U o does not hold for each o' E R
			    //																then add o to R
			    //Add to the list of out comes based on dominance testing or ordering query
				Outcome newOutcome = new Outcome(kPrime);
				boolean outcomeDominates = false;
				if(outcomeList.size()==0)
				{
					outcomeList.add(newOutcome);
					System.out.println("Adding first outcome: "+newOutcome);
					logger.info(newOutcome.toString());
				}
				else {
					for(Outcome o:outcomeList) {
						if(useDominanceTesting) {
							//System.out.println("Dominance Testing for:("+newOutcome+") Outcome #"+(outcomeList.size()+1));
							outcomeDominates = DominanceTesting.doesDominates(o, newOutcome,cpNet,dependencies,0);
						}
						else
							outcomeDominates = OrderingQuery.doesDominates(o, newOutcome,cpNet,dependencies);
						if(outcomeDominates)
							break;
					}
					if(!outcomeDominates) {
						System.out.println("Not dominated by other outcomes. Adding the outcome#"+outcomeList.size()+1+":"+newOutcome);
						outcomeList.add(newOutcome);
						logger.info(newOutcome.toString());
					}
				}
			}
		}
		return outcomeR;
	}
	//partial assignment of variables
	public static void partialAssignment(int[] kPrime, Map<VariableTuples, Set<Tuples>> Ci, char currentVar,Integer value) {
		Set<VariableTuples> setOfKeysHardConstraints =  Ci.keySet();
		Set<Tuples> setTuples=null;
		//Try to set value one by one from the domain.
		//1. No constraints with that value for that variable
		//2. Constraints present but there is corresponding value that can be assigned.
		
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			//(A,B: 1 1, 3 3) or (B,A: 3 2, 2 3)
			if(keyWithVar.a == currentVar) // || keyWithVar.b == currentVar) {
			{	
				
				setTuples = Ci.get(keyWithVar);
				for(Tuples t: setTuples) {
					if(t.a <0 || t.a==value) {
						//If not assigned already
						if(kPrime[keyWithVar.b-'A'] ==0) {
							kPrime[keyWithVar.b-'A'] = t.b;
							//Check further assignment possible or not for hard Constraints
							partialAssignment(kPrime, Ci, keyWithVar.b, t.b);
						}
					}
					//else if(t.b <0)
						//kPrime[keyWithVar.a-'A'] = t.a;
				}
			}
		}
	}
	//Check Consistency 
	public static boolean checkConsistencyOld(Map<VariableTuples, Set<Tuples>> C ,int[] k, char var, Integer currentDomain) {
		Set<VariableTuples> setOfKeysHardConstraints =  C.keySet();
		Set<Tuples> setTuples=null;
		boolean consistency = true;
		int checkWithDomainValue = currentDomain*-1;
		//Search in the key-set to find the variable from the hard constraints 
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			//only checking a as the direction is like "->" if chosen a and then user choose b
			if(keyWithVar.a == var) {
				setTuples = C.get(keyWithVar);
				for(Tuples t: setTuples) {
					if(t.a == checkWithDomainValue)
					{
						//check whether the other value is already assigned and consistant
						if(k[keyWithVar.b - 'A'] > 0 && k[keyWithVar.b - 'A'] != t.b)
						{
							consistency = false;
						}
					}
				}
			}
			
		}

		return consistency;
	}
	
	//Check if there is any other preferred value
	public static boolean findBetterValueLessOrSameConstraints(Map<CPNetVariableTuples, Set<Tuples>> N,Map<VariableTuples, Set<Tuples>> C,int[] k, char var, Integer currentDomain) {
		Set<Tuples> setTuples=null;
		Set<CPNetVariableTuples> setOfKeysCPNet =  N.keySet();
		boolean betterValue = false;
		String aOfCpNet="";
		//find the dependency of the current variable.
		String dependents = dependencies[(var-'A')];
		//Construct the first part of the key of cp-net based on assignments of dependencies
		if(dependents == null || dependents.trim().equals(""))
			aOfCpNet +=var;		//"A,A": 1 2, 3 4
		else
		{
			// "1,2,3,D: 1 3, 4 3"
			for(int i = 0;i<dependents.length();i++) {
				if(i!=0)
					aOfCpNet+=",";
				aOfCpNet = aOfCpNet+Character.toLowerCase(dependents.charAt(i))+ k[dependents.charAt(i)-'A'];
			}
		}
		//Search in the key-set to find the variable from the cp-net
		for(CPNetVariableTuples keyWithVar:setOfKeysCPNet) {
			//if the variable exist in the second part of the key [1,2,3,X] if the X exist
			if(keyWithVar.b == var && keyWithVar.a.equals(aOfCpNet))
				setTuples = N.get(keyWithVar);
		}
		//check if any other value from the given variable is preferable and has lower/same constraints
		//Set<VariableTuples> setOfKeysHardConstraints =  C.keySet();
		for(Tuples t:setTuples) {
			//for each better value check whether that increases constraints
			if(t.b == currentDomain) {
				betterValue = true;
			}
		}
		if(betterValue)// && !reduceConstrints)
			return true;
		else
			return false;
	}
	//Strengthening the constraints	
	public static Map<VariableTuples, Set<Tuples>> strengtheningConstraints(Map<VariableTuples, Set<Tuples>> C, char var, Integer assignment, AtomicBoolean constraintsRedused) {
		Map<VariableTuples, Set<Tuples>> newConstraintsSet;
		newConstraintsSet = CommonUtilities.cloneMapHC(C);
		Set<Tuples> setTuples=null;
		Set<VariableTuples> setOfKeysHardConstraints =  newConstraintsSet.keySet();
		//Search in the key-set to find the variable from the hard constraints 
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			if(keyWithVar.a == var) {
				setTuples = newConstraintsSet.get(keyWithVar);
				for(Tuples t: setTuples) {
					if(t.a == assignment) {
						//t.a*=-1;
						//constraintsRedused.set(true);
					}
				}
			}
			
		}

		return newConstraintsSet;
	}
	
	
	// Take the input from the file
	public static void takeInput(String fileName) {
		String line;
		String[] tokens;
		try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	if(line.trim().equals("") || line.trim().charAt(0) == '#')			//comment in the file
            		continue;
                tokens = line.split("=");
                //**********************Variables**************************************
            	if(tokens[0].trim().equalsIgnoreCase("variables") && tokens.length == 2) {
            		variables= tokens[1].trim().toCharArray();
            		numberOfvariables = variables.length;
            	}
            	//**********************Dependencies**************************************
            	else if(tokens[0].trim().equalsIgnoreCase("parent") && tokens.length == 2) {
            		dependencies= tokens[1].trim().split(",");
            		dependenciesInt = new int[numberOfvariables][5];
            		//generate children based on dependencies
            		childs = new String[numberOfvariables];
            		for(int i =0;i<numberOfvariables;i++) {
            			childs[i]="";
            		}
            		int count = 0;					
            		for(String s: dependencies) {	
            			if(s!=null & !s.trim().equals("")) {
            				for(int i=0;i<s.length();i++) {
            					childs[s.charAt(i)-'A'] += (char)(65+count);
            					dependenciesInt[count][i] = s.charAt(i)-'A'+1;
            				}
            			}
            			count++;
            		}
            	}
            	//**********************Domains**************************************
            	else if(tokens[0].trim().equalsIgnoreCase("domains") && tokens.length == 2)
            		domainSize = Integer.parseInt(tokens[1].trim());
            	
            	//**********************Hard-Constraints *****************************
            	else if(tokens[0].trim().equalsIgnoreCase("hard_constraints") && tokens.length == 2 && !"".equals(tokens[1].trim()) ) {
            		//hard_constraints = 2
            		int numberOfConstraints = Integer.parseInt(tokens[1].trim());
            		for(int i=0;i<numberOfConstraints;i++) {          		
            			//A,B: 1 1 ,  1  2
            			String hardConstraint = bufferedReader.readLine();
            			String[] varAndTuples = hardConstraint.trim().split(":");
            			VariableTuples varTuple = new VariableTuples(varAndTuples[0].trim().charAt(0), varAndTuples[0].trim().charAt(2));
            			//1 1 , 1 2
            			String[] tuplesHardCons = varAndTuples[1].trim().split(",");
            			int a1,b1;
                		Tuples tuple;
            			Set<Tuples> setTuples = new HashSet<Tuples>();
            			for(String s:tuplesHardCons) {
            				s= s.trim();
            				a1 = Character.getNumericValue(s.charAt(0));
            				b1 = Character.getNumericValue(s.charAt(2));
            				tuple = new Tuples(a1, b1);
            				setTuples.add(tuple);
            			}
            			hardConstraints.put(varTuple, setTuples);
            		}
            	}
            	//**********************CP-Net *****************************
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
            			if(line.trim().equals(""))
            				continue;
            			cpTokens = line.split(":");
            			//first part is cpNetvariableuples	a1,c1,B
            			a = cpTokens[0].substring(0, cpTokens[0].length()-2).trim();	//a1,c1
            			b = cpTokens[0].charAt(cpTokens[0].length()-1);			//B
            			var = new CPNetVariableTuples(a,b);
            			setTuples = new HashSet<Tuples>();
            			//Check as there may not be preference order.
            			if(cpTokens.length>1 && !cpTokens[1].trim().equals("")) {
            				//second part is tuples of that variable 1>2,3>2. 
            				cpTokensTuples = cpTokens[1].trim().split(",");
                			for(String s:cpTokensTuples) {
                				s= s.trim();
                				a1 = Character.getNumericValue(s.charAt(0));
                				b1 = Character.getNumericValue(s.charAt(2));
                				tuple = new Tuples(a1, b1);
                				setTuples.add(tuple);
                			}
            			}
            			cpNet.put(var, setTuples);          			
            		}
            		
            	}
                
            }
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }catch(Exception e) {
        	e.printStackTrace();
        }
	}
	// Test Function: Print the assignments of each variable
	public static void printAssignment(int[] assignments) {
		String var="";
		for(int i=0;i<assignments.length;i++) {
			if(assignments[i]!=0)
				var+= (char)('A'+i)+" ";
		}
		System.out.print(var+": ");
		for(int i=0;i<assignments.length;i++) {
			if(assignments[i]!=0)
				System.out.print(assignments[i]+" ");
		}
		System.out.println();
	}
	// Print the Global CP-Net
	public static void printCPNET() {
		int i=0;
		String s="";
		for(CPNetVariableTuples c:cpNet.keySet()) {
			if(i!=0)
				s+=", ";
			i++;
			s+= "\n\""+c.a+","+c.b+"\":";
			//if(cpNet.get(c).size()>1) {
			s+="[";
			//}
			if(cpNet.get(c).size()==0) {
				s+="[]";
			}
			for(int j= 1;j<=cpNet.get(c).size();j++)
			{
				for(Tuples t:cpNet.get(c)) {
					if(j!=1)
						s+=", ";
					j++;
					
					s= s+ "[\""+Character.toLowerCase(c.b)+t.a+"\", \""+Character.toLowerCase(c.b)+t.b+"\"]";
					//s= s+ "[\""+t.a+"\", \""+t.b+"\"]";
				}

			}
			//if(cpNet.get(c).size()>1) {
			s+="]";
			//};
		}
		System.out.println(s);
	}
	// Print the hard constraints
	public static void printHardConstraints(Map<VariableTuples, Set<Tuples>> constr) {
		int i=0;
		String s="";
		for(VariableTuples c:constr.keySet()) {
			if(i!=0)
				s+=", ";
			i++;
			s+= c.a+","+c.b+"\":";
			//if(cpNet.get(c).size()>1) {
			s+="[";
			//}
			if(constr.get(c).size()==0) {
				s+="[]";
			}
			for(int j= 1;j<=constr.get(c).size();j++)
			{
				for(Tuples t:constr.get(c)) {
					if(j!=1)
						s+=", ";
					j++;
					
					s= s+ "["+t.a+", "+t.b+"]";
					//s= s+ "[\""+t.a+"\", \""+t.b+"\"]";
				}

			}
			//if(cpNet.get(c).size()>1) {
			s+="]";
			//};
		}
		System.out.println(s);
	}
	
	//print dependencies 
	public static void printDependencies() {
		int i=0;
//		for(char c:variables) {
//			System.out.print(c);
//			System.out.println(" Depends on "+dependencies[i]);
//			i++;
//		}
		for(int j=0;j<numberOfvariables;j++) {
			System.out.print((char)(j+65));
			System.out.print(" Depends on ");
			for(int k=0;k<5;k++)
			{
				if(dependenciesInt[j][k]>0) {
					System.out.print((char)(dependenciesInt[j][k]+65-1));
				}
			}
			System.out.println();
		}
	}

	//print dependencies 
	public static void printChilds() {
		int i=0;
		for(char c:variables) {
			System.out.print(c);
			System.out.println(" Has following children "+childs[i]);
			i++;
		}
	}
}