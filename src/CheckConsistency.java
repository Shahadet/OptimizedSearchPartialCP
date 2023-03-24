import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CheckConsistency {
	//Check Consistency 
	public static boolean checkConsistency(Map<VariableTuples, Set<Tuples>> C ,int[] k, char var, Integer currentDomain) {
		Set<VariableTuples> setOfKeysHardConstraints =null;
		Set<Tuples> setTuples=null;
		boolean consistency = true;
		
		setOfKeysHardConstraints =  C.keySet();
		
		//Search in the key-set to find the variable from the hard constraints 
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			//Need to test consistency both way (A,B) and (B,A) for assigning value for B
			//Trying to assign currentDomain to var. Checking whether any (Var,A),(var,B) or (var,X) is inconsistent or not
			if(keyWithVar.a == var) {
				setTuples = C.get(keyWithVar);
				for(Tuples t: setTuples) {
					if(t.a == currentDomain || t.a*-1 == currentDomain)
					{
						//check is the corresponding variable is empty (not assigned any value)
						if(k[keyWithVar.b-'A'] == 0 )
						{
							//No forward check
							continue;
						}
						//check if the corresponding variable's value is inconsistent
						else if (t.b == k[keyWithVar.b-'A']){
							//System.out.println("Inconsistent for ["+keyWithVar.a+t.a+","+keyWithVar.b+t.b+"]");
							return false;
						}
					}
				}
			}
			//Trying to assign currentDomain to var. Checking whether any (A,Var),(B,var) or (X,var) is inconsistent or not
			else if(keyWithVar.b == var) {
				setTuples = C.get(keyWithVar);
				for(Tuples t: setTuples) {
					if(t.b == currentDomain || t.b*-1 == currentDomain)
					{
						//check is the corresponding variable is empty (not assigned any value)
						if(k[keyWithVar.a-'A'] == 0 )
						{
							//No forward check
							continue;
						}
						//check if the corresponding variable's value is inconsistent
						else if (t.a == k[keyWithVar.a-'A'] ||  t.a*-1 == k[keyWithVar.a-'A']){
							//System.out.println("Inconsistent for ["+keyWithVar.a+t.a+","+keyWithVar.b+t.b+"]");
							return false;
						}
					}
				}
			}
			
			//not breaking here: want to assign a1->b1 and also a1->c3
		}
		
		return consistency;
	}
	//Array index started from 1
	public static Map<Integer,List<Integer>> applyArcConsistency(int domainSize, Map<VariableTuples, Set<Tuples>> HC) {
		Set<VariableTuples> setOfKeysHardConstraints =  HC.keySet();
		Set<Tuples> setTuples=null;
		Map<Integer,List<Integer>> domains = SearchPartialCP.domains;
		
		//Check consistency for each arc/edge
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			int[] numberOfConstraintsForEachDomOfA = new int[domainSize+1];
			int[] numberOfConstraintsForEachDomOfB = new int[domainSize+1];
			//(A,B: 1 1, 3 3) or (B,A: 3 2, 2 3)
			setTuples = HC.get(keyWithVar);

			//there would be no Xi in A if there is (Xi,Yi->Yn)
			for(Tuples t:setTuples) {
				numberOfConstraintsForEachDomOfA[t.a]++;
				numberOfConstraintsForEachDomOfB[t.b]++;
			}
			// for A,B: 1 2, 1 3, 1 5, 2 2, 2 3 , 1 4, 1 1
			//numberOfConstraintsForEachDomOfA will have [5][2][0][0][0]---> domain 1 will be removed from A
			//numberOfConstraintsForEachDomOfB will have [1][2]2][1][1]
			
			for(int i =1;i<=domainSize;i++) {
				if(numberOfConstraintsForEachDomOfA[i]==domainSize) 
					SearchPartialCP.domains.get(keyWithVar.aInt).remove((Object)i);
				if(numberOfConstraintsForEachDomOfB[i]==domainSize)
					SearchPartialCP.domains.get(keyWithVar.bInt).remove((Object)i);
			}

		}
		return domains;
	}
		
	public static boolean checkConsistencyFC(char currentVar,int currentDom,Map<VariableTuples, Set<Tuples>> hCMap,int[] assignments,Map<Integer,List<Integer>> domains) {
		boolean findSolution = true;
		Set<VariableTuples> setOfKeysHardConstraints =  hCMap.keySet();
		Set<Tuples> setTuples=null;
		List<Integer> listCurrentDomain=null;
		//List<Integer> listDomainNew=null;
		//Check consistency for current assigned variable and selected domain
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			boolean requireUpdateDomain=false;	// Will be true if current assignment removes one or more domain value from other variable
			//For (varX, B) or (varX, C) where B and C is not assigned yet
			if(keyWithVar.a == currentVar && assignments[keyWithVar.bInt-1]<=0) {
				requireUpdateDomain=false;
				setTuples = hCMap.get(keyWithVar);
				listCurrentDomain =  domains.get(keyWithVar.bInt);
				for(Tuples t:setTuples) {
					//(x1,b1) or (x1,c2) -> B:{-b1,b2,b3} or C:{c1,-c2,c3}
					//i.e. for -> [A,B:1 2, 1 3, 2 1, 2 3] if current variable is A and current domain/assignment is 2 this will remove 
					// 1 and 3 from B's domain so the domain of B would be {2}
					if(t.a ==  currentDom) {
						requireUpdateDomain = true;
						//listDomainNew = new ArrayList<Integer>();
						//Create a updated list of domains
						for(int i=0;i<listCurrentDomain.size();i++) {
							//
							if(listCurrentDomain.get(i) == t.b)
								listCurrentDomain.set(i, t.b*-1);
							//else if(i>0)					//not already removed  
								//listDomainNew.add(i);
						}
					}
				}
				//replace the old domain set with new
				//if(requireUpdateDomain)
					//domains.put(keyWithVar.bInt, listDomainNew);				
			}
			//For (B, varX) or (C, varX) where B and C is not assigned yet
			if(keyWithVar.b==currentVar && assignments[keyWithVar.aInt-1]<=0) {
				requireUpdateDomain=false;
				setTuples = hCMap.get(keyWithVar);
				listCurrentDomain =  domains.get(keyWithVar.aInt);
				//listDomainNew = new ArrayList<Integer>();
				for(Tuples t:setTuples) {
					//(b1,x1) or (c2,x1) -> B:{-b1,b2,b3} or C:{c1,-c2,c3}
					if(t.b ==  currentDom) {
						requireUpdateDomain = true;
						//Create a updated list of domains
						for(int i=0;i<listCurrentDomain.size();i++) {
							if(listCurrentDomain.get(i) == t.a)
								listCurrentDomain.set(i, t.a*-1);
							//else
								//listDomainNew.add(i);
						}
					}
				}
				//replace the old domain set with new
				//if(requireUpdateDomain)
					//domains.put(keyWithVar.aInt, listDomainNew);		
			}
		}
		
		//Check whether a solution exist for each variable
		boolean foundSolutionPerVar = false;
		for(int i=1;i<=SearchPartialCP.numberOfvariables;i++) {
			listCurrentDomain = domains.get(i);
			for(Integer dom: listCurrentDomain)
			{
				if(dom>0) {
					foundSolutionPerVar = true;
					break;
				}
			}
			if(!foundSolutionPerVar)
				return false;
			foundSolutionPerVar = false;
		}
		return findSolution;
	}
	public static boolean checkConsistencyFLA(int currentVar,int currentDom,Map<VariableTuples, Set<Tuples>> hCMap,int[] assignments,Map<Integer,List<Integer>> domains) {
		boolean findSolution = true;
		Set<VariableTuples> setOfKeysHardConstraints =  hCMap.keySet();
		Set<Tuples> setTuples=null;
		List<Integer> listDomain=null;
		List<Integer> listDomainNew=null;
		Map<Integer,List<Integer>> locDomains ;
		/*
		 * We will have a local copy of the set of domain. The local copy will contain all the domains that is not
		 * restricted by the previous assignments. We will negate the domains restricted by current assignment
		 * While refreshing we will make those positive again
		 * Example: Assignment [A1B2] Current Variable C, Assignment:3
		 * Domain Set: C{3,4,5} D{1,2,5}
		 * After Iteration C{3,4,5} D{-1,-2,5}
		 * After changing the assignment from 3 to 4
		 */
		//Copy to local domain Set discarding the negative one from the caller
		locDomains = CommonUtilities.cloneMapHCDom(domains,true);
		
		
		//Check consistency for current assigned variable and selected domain
		for(VariableTuples keyWithVar:setOfKeysHardConstraints) {
			//For (varX, B) or (varX, C) where B and C is not assigned yet
			if(keyWithVar.aInt == currentVar && assignments[keyWithVar.bInt-1]<=0) {
				setTuples = hCMap.get(keyWithVar);
				listDomain =  locDomains.get(keyWithVar.bInt);
				listDomainNew = new ArrayList<Integer>();
				for(Tuples t:setTuples) {
					//(x1,b1) or (x1,c2) -> B:{-b1,b2,b3} or C:{c1,-c2,c3}
					if(t.a ==  currentDom) {
						//Create a updated list of domains
						for(Integer i:listDomain) {
							if(i == t.b)
								listDomainNew.add(1*-1);
							else
								listDomainNew.add(i);
						}
					}
				}
				//replace the old domain set with new
				SearchPartialCP.domains.put(keyWithVar.bInt, listDomainNew);				
			}
			//For (B, varX) or (C, varX) where B and C is not assigned yet
			if(keyWithVar.bInt==currentDom && assignments[keyWithVar.aInt-1]<=0) {
				setTuples = hCMap.get(keyWithVar);
				listDomain =  SearchPartialCP.domains.get(keyWithVar.aInt);
				listDomainNew = new ArrayList<Integer>();
				for(Tuples t:setTuples) {
					//(b1,x1) or (c2,x1) -> B:{-b1,b2,b3} or C:{c1,-c2,c3}
					if(t.b ==  currentDom) {
						//Create a updated list of domains
						for(Integer i:listDomain) {
							if(i == t.a)
								listDomainNew.add(1*-1);
							else
								listDomainNew.add(i);
						}
					}
				}
				//replace the old domain set with new
				SearchPartialCP.domains.put(keyWithVar.aInt, listDomainNew);		
			}
		} 
		//Check whether a solution exist for each variable
		boolean foundSolutionPerVar = false;
		for(int i=1;i<=SearchPartialCP.numberOfvariables;i++) {
			listDomain = SearchPartialCP.domains.get(i);
			for(Integer dom: listDomain)
			{
				if(dom>0) {
					foundSolutionPerVar = true;
					break;
				}
			}
			if(!foundSolutionPerVar)
				return false;
			foundSolutionPerVar = false;
		}
		return findSolution;
	}
	public static VariableTuples getVariableTuplesConstraints(int previousOrCurrentVar, int currentOrNextVar) {
		for(VariableTuples bT:SearchPartialCP.hardConstraints.keySet()) {
			//as the variables are already sorted first variable value will be smaller than the second
			if(bT.aInt == previousOrCurrentVar && bT.bInt == currentOrNextVar) {
				return bT;
			}
		}
		return null;
	}
}
