/*
 * Name: Md. Shahadet Hossain
 * Project Name: Generator of Random Partial CP-Net
 * Last Updated: 20th May 2021
 */


import java.io.BufferedReader;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class CPNetGenerator {
	
	public static boolean printLog=false;
	public static int numberOfV;
	public static float p;
	public static float r;
	public static float alpha;
	public static float prefOrder;
	public static int domainSize;
	public static int constraints;
	public static int noOfTuples;
	public static int numberOfPreferenceOrderinCPT;
	public static int maximumParentCount;
	public static int countOfDependencies = 0;			//count the dependency to compute density
	public static boolean fixedNumberOfParents = false;
	public static char[] variables;
	public static Map<Character,Character[]> parentChild=null;			//child is the key and parents are the values
	public static Map<VariableTuples, Set<Tuples>> hardConstants=null;
	public static Map<CPNetVariableTuples, Set<Tuples>> cpNet=null;
	public static int fileCounter=1;
	public static double phaseTransition=0;
	public static void main(String[] args) {
		
		//to generate tightness varying
		//for(int var=10;var<=20;var+=5) {
			
		//for(fileCounter =1;fileCounter<=10;fileCounter++) {
			
			String fileName ="src/input.txt";
			takeInput(fileName);
			
			//change the variable
			//numberOfV=var;

			//String ouputFileName ="src/sample_with_"+numberOfV+"_"+p+"_"+prefOrder+"_"+fileCounter+".txt";
			String ouputFileName ="src/inputSPCDTOQ1_n3.txt";
			System.out.println("Num of v:"+numberOfV +" P="+p+" r="+r+" alpha="+alpha);
			
			domainSize = (int) Math.round(Math.pow((double)numberOfV, alpha));
			//domainSize = 5;
			System.out.println("Domain Size ="+domainSize);

			constraints = (int)Math.round(r*numberOfV* Math.log(numberOfV));
			System.out.println("Number Of Constraints ="+constraints);
			
			noOfTuples = (int)Math.round(p*domainSize*domainSize);
			System.out.println("Number of Incompatible Tuples ="+noOfTuples);
			
			numberOfPreferenceOrderinCPT = (int)Math.round( ((float)domainSize * (domainSize-1) / 2.0) * prefOrder);
			System.out.println("Number of Preference order ="+numberOfPreferenceOrderinCPT);
			
			phaseTransition = 1.0 - Math.exp(-1*((double)alpha/r));
			System.out.println("Phase Transition:"+phaseTransition);
			variables = generateVariables();
			System.out.println("[Generated Variable]");
			printVariables();
			
			parentChild = generateParentChild();
			System.out.println("[Generated Dependencies-Parent Clield Relation]");
			
			printParentCleind();
			
			
			hardConstants = generateHardConstantsV2();
			System.out.println("[Generated Hard Constraints]");
			cpNet = generateCPNetV2();
			PrintStream fileOut = null;
			PrintStream stdout = System.out;
			try {
				fileOut = new PrintStream(ouputFileName);
				System.setOut(fileOut);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			printInput();
			print();
			System.setOut(stdout);
			System.out.println("-------------------------END of Generator Log------------------");
		//}
		//}
	}
	public static Map<CPNetVariableTuples, Set<Tuples>> generateCPNetV2(){
		Map<CPNetVariableTuples, Set<Tuples>> cpNet =  new LinkedHashMap<CPNetVariableTuples, Set<Tuples>>();
		Random rand = new Random();
		//Take all the child one by one and generate the CP-table for all the combination of parents value
		for(Character c : parentChild.keySet()) {
			Set<Tuples> setTuples = new HashSet<Tuples>();
			//no parents
			if(parentChild.get(c).length == 0) {
				//Child and parent is the same (X1,X1)
				CPNetVariableTuples vars = new CPNetVariableTuples(c+"", c);
				while(setTuples.size()<numberOfPreferenceOrderinCPT) {						
					//choose two number randomly(within domain) and add into the arrayTuple. 
					//before that we need to test that order satisfies the rule of no loop and transitivity
					int a,b;
					a= 1+ rand.nextInt(domainSize);
					b= 1+ rand.nextInt(domainSize);
					boolean addedToTheList = false;
					Tuples tempTuple= new Tuples(a, b);
					//must not be same
					if(a!=b) {
						if(setTuples.size()<1) {
							//no need to test for the first tuple
							setTuples.add(tempTuple);
							addedToTheList=true;
						}
						else {
							//make sure there is no symmetry [if 1<2 then 2<1 is not possible]
							if(!isSymmetry(setTuples, tempTuple )) {
								setTuples.add(tempTuple);
								addedToTheList = true;
							}
						}
						
						//add the transitive tuples to the list
						if(addedToTheList) {
							addTransitiveTuples(setTuples);
						}
						
						if(setTuples.size()>=numberOfPreferenceOrderinCPT) {
							break;
						}
					}
					
				}
				cpNet.put(vars, setTuples);
			}
			//one or more parent
			else if(parentChild.get(c).length >= 1) {
				Character[] parents = parentChild.get(c);
				ArrayList<String> listCombination = new ArrayList<String>();
				String combination="";
				int parentCount = parentChild.get(c).length;
				for(int i=0;i<parentCount;i++)
				{
					combination= combination+"1";
				}
				if(printLog)System.out.println("Test: CPT for:"+c +"parents:"+combination);
				//for three variable this loop will be 111->333(for domain size 3)
				for(int i=Integer.valueOf(combination);i<=domainSize*Integer.valueOf(combination);i++) {
					int newCom=i;
					boolean take=true;
					//check whether the combination is in between initial combination (1111) and maximum possible combination for each variable(4444 for domain size4) 
					while(newCom>0) {
						if((newCom%10 > domainSize) || (newCom%10 <= 0)) {
							take = false;
							break;
						}
						newCom = newCom/10;
					}
					int newComb2=i;
					// if the "take" is true then the combination holds all the parents with one of their values from the domain
					if(take) {
						int parentId=0;
						String s="";
						while(newComb2>0) {
							if(parentId!=0) 
								s+=",";
							
							//For adding domain with variable name
							s= s+Character.toLowerCase(parents[parentId++])+newComb2%10 ;
							
							//For adding domain without variable name
							//s= s+newComb2%10 ;
							//parentId++;
							
							newComb2/=10;
							
						}
						listCombination.add(s);
					}
				}

				//for each combination create the preferences 
				for(String combinationP:listCombination) {
					setTuples = new HashSet<Tuples>();
					
					//Make sure number of tuples meets the configuration

					CPNetVariableTuples vars = new CPNetVariableTuples(combinationP, c);
					while(setTuples.size()<numberOfPreferenceOrderinCPT) {						
						//choose two number randomly(within domain) and add into the arrayTuple. 
						//before that we need to test that order satisfies the rule of no loop and transitivity
						int a,b;
						a= 1+ rand.nextInt(domainSize);
						b= 1+ rand.nextInt(domainSize);
						boolean addedToTheList = false;
						Tuples tempTuple= new Tuples(a, b);
						//must not be same
						if(a!=b) {
							if(setTuples.size()<1) {
								//no need to test for the first tuple
								setTuples.add(tempTuple);
								addedToTheList=true;
							}
							else {
								//make sure there is no symmetry [if 1<2 then 2<1 is not possible]
								if(!isSymmetry(setTuples, tempTuple )) {
									setTuples.add(tempTuple);
									addedToTheList = true;
								}
							}
							
							//add the transitive tuples to the list
							if(addedToTheList) {
								addTransitiveTuples(setTuples);
							}
							
							if(setTuples.size()>=numberOfPreferenceOrderinCPT) {
								break;
							}
						}
						
					}
					cpNet.put(vars, setTuples);
				}
			}
		}
		return cpNet;
	}
	
	public static Map<VariableTuples, Set<Tuples>> generateHardConstantsV2(){
		Map<VariableTuples, Set<Tuples>> hardConsts =  new HashMap<VariableTuples, Set<Tuples>>();
		Random rand = new Random();

		Set<Tuples> domTuples = new HashSet<Tuples>();
		
		//Take "Constraints" number of tuples of variable and assign preferences between those variables
		while(hardConsts.keySet().size()< constraints) {
			int varA = rand.nextInt(numberOfV-1);
			int varB = rand.nextInt(numberOfV-1);
			//two variable should not be the same. 
			if(varA==varB) {
				continue;
			}
			//Need to test symmetric. Constraints is possible both ways but we can not take same pair
			// If there is (A,B) we would not take (B,A)
			VariableTuples varTupleRev = new VariableTuples(variables[varB],variables[varA]);
			if(hardConsts.keySet().contains(varTupleRev))
				continue;
			
			VariableTuples varTuple = new VariableTuples(variables[varA],variables[varB]);
			//if we generate (2,3) we can generate (2,4).
			while (domTuples.size() < noOfTuples) {
				int domainNo1 =  1+rand.nextInt(domainSize);
				int domainNo2 =  1+rand.nextInt(domainSize);
				Tuples t1 = new Tuples(domainNo1, domainNo2);
				domTuples.add(t1);
			}
			hardConsts.put(varTuple, domTuples);
			
			domTuples = new HashSet<Tuples>();
		}
		return hardConsts;
	}
	
	public static int getTotalConstraints(Map<VariableTuples, Set<Tuples>> hardConsts) {
		int i = 0;
		for(VariableTuples arr:hardConsts.keySet()) {
			i+= hardConsts.get(arr).size();
		}
		return i;
	}
	public static Map<Character,Character[]> generateParentChild(){
		Random rand = new Random();
		Map<Character, Character[]> parentChild = new HashMap<Character, Character[]>();
		int i = 0;
		Character[] arr = {};
		parentChild.put(variables[i], arr);
		//Assuming Number of variable is minimum 2. 2nd variable is the child of first variable.
		i++;
		countOfDependencies++;
		Character[] arr2 = {variables[i-1]};
		parentChild.put(variables[i],arr2);
		//from 3rd to nth variable we will calculate dependencies based on configuration
		for(i =2;i<numberOfV;i++)
		{
			int parentCount=i;
			
			//If fixParents flag is on. only maximumParentCount will be considered for all variables
			if(fixedNumberOfParents)
			{
				if(parentCount>maximumParentCount)
					parentCount = maximumParentCount;
			}
			//If fixParents flag is of then pick a random number R. that R will be number of parents and parents will be 0->R-1 variables
			else {
				int randomParentCount = rand.nextInt(i);						//generates a random value from 0 to i-1
				//parent count should be less or equal to the maxParentCount
				if(randomParentCount>maximumParentCount)
					parentCount = maximumParentCount;
				else
					parentCount = randomParentCount+1;
			}
			
			
			countOfDependencies+=parentCount;
			
			Character[] parentsArray = new Character[parentCount];
			
			
			//take all the possible parents into a list
			ArrayList<Integer> tempParentlist = new ArrayList<Integer>();
	        for (int var=0; var<i; var++) {
	        	tempParentlist.add(new Integer(var));
	        }
	        //shuffle the list and take first "numberOfParents" variables as parents for randomization
	        Collections.shuffle(tempParentlist);
	        
			for(int j = 0;j<parentCount;j++) {
				parentsArray[j] = variables[tempParentlist.get(j)];
			}
			Arrays.sort(parentsArray);
			parentChild.put(variables[i], parentsArray);
		}		
		return parentChild;
	}
	
	public static char[] generateVariables() {
		char[] vars = new char[numberOfV];
		char a = 'A';
		for(int i =0;i<numberOfV;i++) {
			vars[i] = a;
			a++;
		}
		return vars;
		
	}
	
	public void generateAndTestTuplesWithTransitivity() {
		
	}
	
	//***************************** Utility Functions  ******************************//
	
	//**********CP-Net Generation Utility Functions  *********************//
	
	//************Test Symmetry******************
	public static boolean isSymmetry(Set<Tuples> tuples, Tuples tuple) {
		//if there is a tuple exist with is symmetry the function will return true
		for(Tuples t:tuples) {
			if(t.a == tuple.b && t.b == tuple.a)
				return true;
		}
		return false;
	}
	
	//**Add transitive tuples to the set of preferences*******
	public static void addTransitiveTuples(Set<Tuples> tuples) {

		//example: if there is (1,2) and (2,3), we will add (1,3)
		Set<Tuples> addTuples = new TreeSet<Tuples>(tupleComparatorCPNet); 
		for(Tuples t1: tuples) {
			for(Tuples t2: tuples) {
				if(t2.a == t1.b)
				{
					addTuples.add(new Tuples(t1.a, t2.b));
				}
				else if(t1.a == t2.b)
				{
					addTuples.add(new Tuples(t2.a, t1.b));
				}
			}
		}
		//While adding transitive tuples make sure not of add symmetric tuples.
		for(Tuples newTuple : addTuples) {
			//remove the semantic tuples caused by transitivity to ensure there is no loop
			for(Tuples t:tuples) {
				if( t.a == newTuple.b && t.b == newTuple.a ||  t.a == newTuple.a && t.b == newTuple.b) {
					tuples.remove(t);
					break;
				}
			}
			tuples.add(newTuple);
			if(printLog)System.out.println("Added tuples transitive:["+tuples.size()+"] added:["+newTuple.a+","+newTuple.b+"]");
			//if(tuples.size()>= numberOfPreferenceOrderinCPT)
				//break;
		}
	}
	public static void printInput() {
//		n=5
//				p=0.4
//				a=0.7
//				r=0.3
//				o=0.4
//				maxParents=5
//				fixParents=true
		System.out.println("#=========== INPUTS ========================#");
		System.out.println("#number of variables(n) = "+numberOfV);
		System.out.println("#p = "+p);
		System.out.println("#alpha = "+alpha);
		System.out.println("#r = "+r);
		System.out.println("#order or partialness(o) = "+prefOrder);
		System.out.println("#maximum number of parents = "+maximumParentCount);
		System.out.println("#fixParents = "+fixedNumberOfParents);
		System.out.println("#------- Computation for generator-----");
		System.out.println("#domain size (n^alpha) = "+domainSize);
		System.out.println("#Number Of Constraints(r nlogn) ="+constraints);
		System.out.println("#Number of Incompatible Tuples(p d^2)="+noOfTuples);
		System.out.println("#Number of Preference order(d(d-1)/2) ="+numberOfPreferenceOrderinCPT);	
		System.out.println("#Phase transition [1 - e^{-a/r}] :"+phaseTransition);
	}
	public static void print() {
		System.out.println("#=========== OUTPUTS ========================#");
		printVariables();
		printParentCleind();		
		//*****************Print Domain*********************
		String s= "domains = "+domainSize;
		System.out.println(s);
		//*****************Print Hard Constants*********************
		s= "hard_constraints = "+constraints+"\n";
		int i=0;
		for(VariableTuples c:hardConstants.keySet()) {
			i++;
			s+= c.a+","+c.b+":";
			
			for(int j= 1;j<=hardConstants.get(c).size();j++)
			{
				for(Tuples t:hardConstants.get(c)) {
					if(j!=1)
						s+=", ";
					j++;
					s= s+ t.a+" "+t.b;
				}

			}
			s+='\n';
		}
		System.out.println(s);
		
		//*****************Print CP-Net*********************
		System.out.print("cp_net = ");
		i=0;
		for(CPNetVariableTuples c:cpNet.keySet()) {
			s= "\n"+c.a+","+c.b+":";
			for(int j= 1;j<=cpNet.get(c).size();j++)
			{
				for(Tuples t:cpNet.get(c)) {
					if(j!=1)
						s+=", ";
					j++;
					
					s= s+ t.a+">"+t.b;
				}

			}
			System.out.print(s);
		}
		
		
	}
	public static void printVariables() {
		//*****************Print Variables*********************
		String s = "variables = ";
		for(int i =0;i<variables.length;i++) {
			s+=variables[i];
		}
		System.out.println(s);
	}
	public static void printParentCleind() {
		//*****************Print Parent and Child*********************
		String s= "parent = ";
		int i=0;
		for(Character c:parentChild.keySet()) {
			if(i!=0)
				s+=",";
			i++;
			Character[] arr = parentChild.get(c);

			for(int j= 0;j<arr.length;j++)
			{
				s= s+ arr[j];
			}
		}
		System.out.println(s);
		double dencityOfGraph = countOfDependencies*1.0/ ((numberOfV*(numberOfV-1))/2.0);
		//System.out.println("Density of Graph: "+dencityOfGraph);
	}
	public static void takeInput(String fileName) {
		String line;
		String[] tokens;
		try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                tokens = line.split("=");
                if(tokens.length == 2) {
                	if(tokens[0].trim().equalsIgnoreCase("n"))
                		numberOfV= Integer.parseInt(tokens[1]);
                	else if(tokens[0].trim().equalsIgnoreCase("p"))
                		p= Float.parseFloat(tokens[1]);
                	else if(tokens[0].trim().equalsIgnoreCase("r"))
                		r= Float.parseFloat(tokens[1]);
                	else if(tokens[0].trim().equalsIgnoreCase("a"))
                		alpha= Float.parseFloat(tokens[1]);
                	else if(tokens[0].trim().equalsIgnoreCase("o"))
                		prefOrder= Float.parseFloat(tokens[1]);
                	else if(tokens[0].trim().equalsIgnoreCase("maxParents"))
                		maximumParentCount= Integer.parseInt(tokens[1]);
                	else if(tokens[0].trim().equalsIgnoreCase("fixParents"))
                		fixedNumberOfParents= Boolean.parseBoolean(tokens[1]);
                		
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
	
	public static Comparator<Tuples> tupleComparator = new Comparator<Tuples>(){
		@Override
		public int compare(Tuples t1, Tuples t2) {
			if((t1.a == t2.a) || (t1.b==t2.b))
				return 0;
			else
				return -1;
        }
	};
	public static Comparator<Tuples> tupleComparatorCPNet = new Comparator<Tuples>(){
		@Override
		public int compare(Tuples t1, Tuples t2) {
			if((t1.a == t2.a) && (t1.b==t2.b))
				return 0;
			else
				return -1;
        }
	};
	public static Comparator<VariableTuples> variableTupleComparator = new Comparator<VariableTuples>(){
		@Override
		public int compare(VariableTuples t1, VariableTuples t2) {
			if((t1.a == t2.a) && (t1.b==t2.b))
				return 0;
			else
				return -1;
        }
	};
}