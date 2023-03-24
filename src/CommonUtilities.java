import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonUtilities {
	public static int[]varWithConstraintsCount;
	// Choose a variable for which there is no parent or all the parents of that variable is assigned
	public static int nextVariable(int[] k) {
		int nextVar=0;// 0=A
		for(int i=0;i<SearchPartialCP.numberOfvariables;i++) {
			if(k[i]<=0) {
				nextVar =i;
				break;
			}
		}
		return nextVar;
	}
	
	// Choose a variable for which there is no parent or all the parents of that variable is assigned
	public static int nextVariablebyOrdering(int[] k,Map<VariableTuples, Set<Tuples>> hardConstraints, int[][] dependencies) {
		int nextVar=0;// 0=A
		int[] varWithNoParents = new int[SearchPartialCP.numberOfvariables];
		int count =0;
		for(int i=0;i<SearchPartialCP.numberOfvariables;i++) {
			if(k[i]>0)
				continue;
			boolean allParentsAssigned= false;
			for(int j=0;  j<5 && dependencies[i][j]>0;j++) {
				//[K starts from 0 but dependencies=1 means A] 
				if(k[dependencies[i][j]-1] >0 ) {
					allParentsAssigned = true;
				}
				else if(k[dependencies[i][j]-1] <=0 ) {
					allParentsAssigned = false;
					break;
				}
			}
			if(allParentsAssigned) {
				varWithNoParents[count] =i;
				count++;
			}
		}
		int maxConstrraints=0;
		for(int i=0;i<count;i++) {
			if(varWithConstraintsCount[varWithNoParents[i]] >= maxConstrraints) {
				maxConstrraints = varWithConstraintsCount[varWithNoParents[i]];
				nextVar = varWithNoParents[i];
			}
		}
		
		return nextVar;
	}
	public static void generateVariableOrderWithConstraints(Map<VariableTuples, Set<Tuples>> hardConstraints) {
		varWithConstraintsCount = new int[SearchPartialCP.numberOfvariables];
		Set<VariableTuples> setOfKeysHardConstraints =  hardConstraints.keySet();
		//Search in the key-set to find the variable from the hard constraints 
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			varWithConstraintsCount[keyWithVar.aInt]++;
			varWithConstraintsCount[keyWithVar.bInt]++;
		}
	}
	
	
	//retrieve the expected row of the CP-Net
	public static Set<Tuples> findCorrespondingSetFromCPNet(char var, int[] k,Map<CPNetVariableTuples, Set<Tuples>> N, String[] dependencies) {
		Set<Tuples> setTuples=null;
		Set<CPNetVariableTuples> setOfKeysCPNet =  N.keySet();
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
					aOfCpNet+=",";									//"A,A": 1 2, 3 4
				// "1,2,3,D: 1 3, 4 3"
				aOfCpNet = aOfCpNet+Character.toLowerCase(dependents.charAt(i))+ k[dependents.charAt(i)-'A'];
			}
		}
		//Search in the key-set to find the variable from the cp-net
		for(CPNetVariableTuples keyWithVar:setOfKeysCPNet) {
			//if the variable exist in the second part of the key [1,2,3,X] if the X exist
			if(keyWithVar.b == var && keyWithVar.a.equals(aOfCpNet))
				setTuples = N.get(keyWithVar);
		}
		return setTuples;
	}
	
	//Obsolete: was developed to follow the algorithm of the paper
	public static List<Outcome> mergeOutcomesX(List<Outcome> dest,List<Outcome> src){
		List<Outcome> outcomeList = new ArrayList<Outcome>();
		if(src == null || src.isEmpty())
			return dest;
		//for first temporary or complete outcomes
		if(dest.isEmpty())
		{
			for(Outcome o2: src) {
				Outcome temp = new Outcome(o2);
				outcomeList.add(temp);
			}
			return outcomeList;
		}
		
		// Cases like [a1,b1,c1 a1,b1,c2] and [a1,b2,c1 a1,b2,c2]   -> [a1,b1,c1  a1,b1,c2  a1,b2,c1  a1,b2,c2]  
		for(int i =0;i<SearchPartialCP.numberOfvariables;i++) {
			if(dest.get(0).values[i] > 0 && src.get(0).values[i] > 0 && dest.get(0).values[i] != src.get(0).values[i]) {
				for(Outcome o: dest) {
					Outcome temp = new Outcome(o);
					outcomeList.add(temp);
				}
				for(Outcome o: src) {
					Outcome temp = new Outcome(o);
					outcomeList.add(temp);
				}
				return outcomeList;
			}
		}
		
		// Cases like [a1,b1 a1,b2] and [a1,c1 a1,c2 a1,c3]   -> [a1,b1,c1  a1,b1,c2  a1,b1,c3  a1,b2,c1  a1,b2,c2  a1,b2,c3]  
		for(Outcome o1:dest) {
			for(Outcome o2: src) {
				Outcome temp = new Outcome(o1);
				temp.combine(o2);
				outcomeList.add(temp);
			}
		}
		return outcomeList;
	}
	//choose the best value for the 
	public static Integer chooseBestFromCPNet(Map<CPNetVariableTuples, Set<Tuples>> N, char var,List<Integer> domainSetP, int[] k,String[] dependencies) {
		Set<Tuples> setTuples=null;
		Set<CPNetVariableTuples> setOfKeysCPNet =  N.keySet();
		
		String aOfCpNet="";
		//find the dependency of the current variable.
		String dependents = dependencies[(var-'A')];
		//check whether all the parent pariables are assigned or not
		for(int i = 0;i<dependents.length();i++) {
			if(k[dependents.charAt(i)-'A'] == 0)
				return -1;
		}
		
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
		Integer best=domainSetP.get(0);
		//No Preference Order
		if(setTuples == null || setTuples.isEmpty())
			return best;
		boolean foundBetter = true;
		while(foundBetter) {
			foundBetter = false;
			for(Tuples t:setTuples) {
				if(t.b == best && domainSetP.contains(t.a)) {
					best= t.a;
					foundBetter = true;
					break;
				}
			}
		}
		return best;
	}

	public static Map<Integer, List<Integer>> generateDomainsForVariable() {
		SearchPartialCP.domains = new HashMap<Integer, List<Integer>>();
		for(int i =1;i<=SearchPartialCP.numberOfvariables;i++) {
			List<Integer> domain= new ArrayList<Integer>();
			for(int j=1;j<=SearchPartialCP.domainSize;j++) {
				domain.add(j);
			}
			SearchPartialCP.domains.put(i, domain);
		}
		return SearchPartialCP.domains;
	}
	
	//Clone a Map Hard Copy
	public static Map<VariableTuples, Set<Tuples>> cloneMapHC( Map<VariableTuples, Set<Tuples>> source){
		Map<VariableTuples, Set<Tuples>> newMap = new HashMap<VariableTuples, Set<Tuples>>();
		Set<VariableTuples> allKey = source.keySet();
		
		for(VariableTuples vt:allKey) {
			VariableTuples newVarTuple = new VariableTuples(vt.a, vt.b);
			Set<Tuples> newTuples = new HashSet<Tuples>();
			for(Tuples t: source.get(vt)) {
				newTuples.add( new Tuples(t.a, t.b));
			}
			newMap.put(newVarTuple, newTuples);
		}
		return newMap;
	}
	
	//Clone a Map Hard Copy
	public static Map<Integer, List<Integer>> cloneMapHCDom( Map<Integer,List<Integer>> source, boolean withoutNegValue){
		Map<Integer, List<Integer>> newMap = new HashMap<Integer, List<Integer>>();
		Set<Integer> allKey = source.keySet();
		for(Integer v:allKey) {
			List<Integer> newTuples = new ArrayList<Integer>();
			for(Integer t: source.get(v)) {
				if(withoutNegValue && t<=0)
					continue;
				else
					newTuples.add(t);
			}
			newMap.put(v, newTuples);
		}
		return newMap;
	}
	
}
