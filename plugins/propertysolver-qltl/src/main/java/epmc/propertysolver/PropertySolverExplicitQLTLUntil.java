/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.propertysolver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import epmc.automata.determinisation.DeterminisationUtilAutomaton;
import epmc.automata.determinisation.ParityAutomaton;
import epmc.automaton.Automaton;
import epmc.automaton.BuechiTransitionImpl;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBounded;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.value.TypeComplex;
import epmc.qmc.value.TypeMatrix;
import epmc.qmc.value.ValueComplex;
import epmc.qmc.value.ValueMatrix;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.ValueArray;
import epmc.value.ValueIntegerJava;
import epmc.value.ValueObject;

public final class PropertySolverExplicitQLTLUntil implements PropertySolver {
    public final static String IDENTIFIER = "qltl-explicit";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
    // private StateSetExplicit computeForStates;
    private Expression property;
    private StateSet forStates;
    private int pa_initial;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
            this.graph = modelChecker.getLowLevel();
        }
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public StateMap solve() {
        assert property != null;
        assert forStates != null;
        // assert property instanceof ExpressionQuantifier;
        // System.out.println("property: " + property);
        // TODO: forStates is all states set in lowlevel
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        graph.explore(forStatesExplicit.getStatesExplicit());
        // BitSet set = graph.getInitialNodes().clone();
        // System.out.println("set size: " + set.size());
        // set.set(0, set.size(), true);
        // StateSet states = new StateSetExplicit(modelChecker.getLowLevel(), set);
        StateMap result = doSolve(property, forStates);
        return result;
    }
    
    private void genScript(File tmpFile) throws IOException {
    	//读取文件(字符流)
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));
    	BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/qmc.py")));
    	String line = "";
    	while ((line = in.readLine()) != null) {
    		out.write(line);
    		out.newLine();
    	}
        out.flush();
        in.close();
        out.close();
    }

    private StateMap doSolve(Expression property, StateSet states) {
    	// this.forStates = (StateSetExplicit) forStates;
    	// int checkState = ((StateSetExplicit) states).getExplicitIthState(0);
    	// System.out.println(checkState);
    	Expression[] expressions = UtilQLTL.collectQLTLInner(property).toArray(new Expression[0]);
    	ParityAutomaton pa = DeterminisationUtilAutomaton.newParityAutomaton(property, expressions);
//    	System.out.println(pa.getBuechi().getGraph());
//        System.out.println(this.graph);
        Map<String, String> res = product(pa);

        String resultStr = ""; // = property.toString() + " : \n";
        try {
        	File argFile = new File("args.ini");
        	BufferedWriter argsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argFile)));
        	
        	argsWriter.write("[args]\n");
        	for (String key : res.keySet()) {
        		argsWriter.write(key + " = " + res.get(key));
        		argsWriter.newLine();
        	}
        	argsWriter.close();
        	
        	File script = new File("t.py");
        	genScript(script);
        	String[] testArgs = new String[] {"python3", script.getAbsolutePath(), "args.ini"};
        	// System.out.println(Arrays.toString(testArgs));
	        Process proc = Runtime.getRuntime().exec(testArgs);// 执行py文件
	        // OUT
	        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	        
	        // OutputStream from console
	        String line = null;
	        while ((line = in.readLine()) != null) {
	            // System.out.println(line);
	            resultStr += (line + "\n");
	        }
	        in.close();
	        // ErrorStream from console
	        BufferedReader errIn = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	        while ((line = errIn.readLine()) != null) {
	            System.out.println(line);
	        }
	        errIn.close();
	        // wait for sub-thread
	        proc.waitFor();
	        script.delete();
	        argFile.delete();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
        
        // System.out.println(resultStr);
        // TODO: return result
        // resultStr: key = (model_id * pa.size() + pa.inital_id)
        StateSetExplicit checkStates = (StateSetExplicit) states;
        // System.out.println("size: " + checkStates.size());
        Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(pa.getNumStates() * checkStates.getExplicitStateNr(0) +  pa_initial);
//        for (int node = 0; node < graph.getNumNodes(); node++) {
//        	keySet.add(pa.getNumStates() * checkStates.getExplicitStateNr(node) +  pa_initial);
//        }
        // System.out.println("states:" + checkStates);
        ValueArray resultValues = UtilValue.newArray(TypeMatrix.get(TypeComplex.get()).getTypeArray(), checkStates.size()); // size: 1
        getValueArray(resultStr, resultValues, keySet);
        
        return UtilGraph.newStateMap(checkStates.clone(), resultValues);
    }
    
    
    private void getValueArray(String str, ValueArray resultValues, Set<Integer> keySet) {
    	String[] strs = str.split("\n");
    	// System.out.println(strs.length);
    	for (int i = 0; i < strs.length; i++) {
    		String[] mapStr = strs[i].split(":");
    		int state =  Integer.parseInt(mapStr[0].substring(0,mapStr[0].length()-1));
    		if (keySet.contains(state)) {
    			ValueMatrix resultValue = new ValueMatrix(TypeMatrix.get(TypeComplex.get()));
                getValueMatrix(mapStr[1], resultValue);
                resultValues.set(resultValue, i);
                // System.out.println(resultValue);
    		}
    	}
    }
    
    private void getValueMatrix(String mstr, ValueMatrix value) {
    	// System.out.println("mstr: " + mstr);
    	String[] lines = mstr.split("\\$");
    	value.setDimensions(lines.length, lines.length);
    	for (int i = 0; i < lines.length; i++) {
    		// System.out.println("line: " + lines[i]);
    		String[] rows = lines[i].split(",");
    		for (int j = 0; j < rows.length; j++) {
    			ValueComplex complex = UtilValue.newValue(TypeComplex.get(), 0);
    			// System.out.println("complex str: " + rows[j]);
    			complex.set(rows[j]);
    			value.set(complex, i, j);
    		}
    	}
    }
    
    
    private void getSet(Expression exp, Set<ExpressionLiteral> set, Set<ExpressionLiteral> all) {
    	if (exp instanceof ExpressionOperator) {
    		ExpressionOperator expression = (ExpressionOperator)exp;
	    	Operator opt = expression.getOperator();
	    	if (opt == OperatorEq.EQ) {
	    		set.add((ExpressionLiteral)expression.getOperand2());
	    	} else if (opt == OperatorNot.NOT) {
	    		Set<ExpressionLiteral> newSet = new HashSet<ExpressionLiteral>();
	    		getSet((ExpressionOperator)expression.getOperand1(), newSet, all);
	    		set.addAll(Sets.difference(all, newSet));
	    	} else if (opt == OperatorAnd.AND) {
	    		Set<ExpressionLiteral> leftSet = new HashSet<ExpressionLiteral>();
	    		Set<ExpressionLiteral> rightSet = new HashSet<ExpressionLiteral>();
	    		getSet((ExpressionOperator)expression.getOperand1(), leftSet, all);
	    		getSet((ExpressionOperator)expression.getOperand2(), rightSet, all);
	    		set.addAll(Sets.intersection(leftSet, rightSet));
	    	} else if (opt == OperatorOr.OR) {
	    		Set<ExpressionLiteral> leftSet = new HashSet<ExpressionLiteral>();
	    		Set<ExpressionLiteral> rightSet = new HashSet<ExpressionLiteral>();
	    		getSet((ExpressionOperator)expression.getOperand1(), leftSet, all);
	    		getSet((ExpressionOperator)expression.getOperand2(), rightSet, all);
	    		set.addAll(Sets.union(leftSet, rightSet));
	    	}
    	} else if (exp instanceof ExpressionLiteral) {
    		if (exp == ExpressionLiteral.getTrue()) {
    			set.addAll(all);
    		} else if (exp == ExpressionLiteral.getFalse()) {
    			
    		}
    	}
    }
    
    private Set<ExpressionLiteral> getVariableValues() {
		  Map<Expression,  JANIType> varsMap = ((ModelPRISMQMC) modelChecker.getModel()).getModelPRISM()
		       .getModules().get(0).getVariables();
		  JANITypeBounded boundedType = (JANITypeBounded) varsMap.values().toArray()[0]; // if only one variable
		  ExpressionLiteral lower = (ExpressionLiteral) boundedType.getLowerBound();
		  ExpressionLiteral upper = (ExpressionLiteral) boundedType.getUpperBound();
		  int lowerVal = Integer.parseInt(lower.getValue());
		  int upperVal = Integer.parseInt(upper.getValue());
		  Set<ExpressionLiteral> resultList = new HashSet<ExpressionLiteral>();
		  for (int i = lowerVal; i <= upperVal; i++) {
		   ExpressionLiteral literal = new ExpressionLiteral.Builder().setPositional(null)
		    .setType(ExpressionTypeInteger.TYPE_INTEGER)
		    .setValue(""+i).build();
		   resultList.add(literal);
		  }
		  return resultList;
     }
    
    protected class Node2 {
    	private int nodePA;
    	private int nodeModel;
    	
    	public Node2(int n1, int n2) {
    		nodeModel = n1;
    		nodePA = n2;
    	}
    	
		public int getNodePA() {
			return nodePA;
		}
		public void setNodePA(int nodePA) {
			this.nodePA = nodePA;
		}
		public int getNodeModel() {
			return nodeModel;
		}
		public void setNodeModel(int nodeModel) {
			this.nodeModel = nodeModel;
		}
		
		@Override
	    public int hashCode() {
	        int hash = nodePA;
	        hash = nodeModel + (hash << 6) + (hash << 16) - hash;
	        return hash;
	    }
		
		@Override
	    public boolean equals(Object obj) {
			Node2 other = (Node2) obj;
			return (nodePA == (other.getNodePA()) && (nodeModel == (other.getNodeModel())));
		}
    }
    
    private String getMatrix(String str) {
    	String matrixStr = "[";
    	// TODO: format
    	if (str.contains("matrix-unspec-dim")) {
    		if (dimension != -1) {
    			for (int i = 0; i < dimension; i++) {
    				String vecStr = "[";
    				int j;
    				for (j = 0; j < dimension-1; j++) {
    					if (i == j) {
    						vecStr += "1.0000000+0.0000000j,";
    					} else {
    						vecStr += "0.0000000+0.0000000j,";
    					}
    				}
    				if (i == j) {
						vecStr += "1.0000000+0.0000000j,";
					} else {
						vecStr += "0.0000000+0.0000000j,";
					}
    				
    				if (i < dimension-1) {
    					vecStr += "],";
    				} else {
    					vecStr += "]";
    				}
    				matrixStr += vecStr;
    			}
    			matrixStr += "]";
    			return matrixStr;
    		} else {
    			return "[[]]";
    		}
    	}
    	
    	String[] strs = str.split("\n");
    	if (dimension == -1) {
    		dimension = strs.length - 1;
    	} else if (dimension != strs.length - 1) {
    		// Exception
    		return "";
    	}
    	for (int i = 1; i < strs.length; i++) {
    		String vecStr = "[";
    		String[] vecStrs = strs[i].substring(4).split("  ");
    		for (int j = 0; j < vecStrs.length; j++) {
    			String num = vecStrs[j].replace('i', 'j');
    			if (j < vecStrs.length - 1) {
    				vecStr += num + ",";
    			} else {
    				if (i == strs.length - 1) {
    					vecStr += num.substring(0, vecStrs[j].length() - 1);
    				} else {
    					vecStr += num;
    				}
    			}
    		}
    		if (i < strs.length - 1) {
    			vecStr += "],";
    		} else {
    			vecStr += "]";
    		}
    		matrixStr += vecStr;
    	}
    	matrixStr += "]";
    	return matrixStr;
    }
    
    private int dimension = -1; // TODO: delete
    
    private  Map<String, String> product(Automaton automaton) {
    	Map<String, String> res = new HashMap<String, String>();
    	Map<Integer, Node2> stateMap= new HashMap<Integer, Node2>();
    	Map<Node2, Integer> map = new HashMap<Node2, Integer>();
    	Set<ExpressionLiteral> all = getVariableValues();
    	Map<Expression,  JANIType> varsMap = ((ModelPRISMQMC) modelChecker.getModel()).getModelPRISM()
 		       .getModules().get(0).getVariables();
    	ExpressionIdentifier propName = (ExpressionIdentifier) varsMap.keySet().toArray()[0]; 
    	int numNodes = automaton.getBuechi().getGraph().getNumNodes();
    	int nodeNr = 0;
    	List<Integer> states = new ArrayList<Integer>();
    	for (int fromModel = 0; fromModel < graph.getNumNodes(); fromModel++) {
    		for (int fromPA = 0; fromPA < numNodes; fromPA++) {
    			if (automaton.getBuechi().getGraph().getInitialNodes().get(fromPA))
    				pa_initial = fromPA;
    			states.add(nodeNr);
    			Node2 node2 = new Node2(fromModel, fromPA);
    			stateMap.put(nodeNr, node2);
    			map.put(node2, nodeNr++);
    		}
    	}
    	String Q = "{";  
    	for (int fromPA = 0; fromPA < numNodes; fromPA++) {
    		int numSuss = automaton.getBuechi().getGraph().getNumSuccessors(fromPA);
    		for (int sussNr = 0; sussNr < numSuss; sussNr++) {
    			int toPA = automaton.getBuechi().getGraph().getSuccessorNode(fromPA, sussNr);
    			BuechiTransitionImpl content = (BuechiTransitionImpl)(((ValueObject) (automaton.getBuechi().getGraph().getEdgeProperty(CommonProperties.AUTOMATON_LABEL).get(fromPA, toPA))).getObject());
    			Set<ExpressionLiteral> set = new HashSet<ExpressionLiteral>();
    			if (content == null) {
    				set.addAll(all);
    			} else {
    				Expression exp = content.getExpression();
        			getSet((ExpressionOperator)exp, set, all);
    			}
    			for (int fromModel = 0; fromModel < graph.getNumNodes(); fromModel++) {
    				ExpressionLiteral literal = new ExpressionLiteral.Builder().setPositional(null)
    					    .setType(ExpressionTypeInteger.TYPE_INTEGER)
    					    .setValue(""+((ValueIntegerJava)graph.getNodeProperty(propName).get(fromModel)).getInt()).build();
    				if (set.contains(literal)) {
    					int fromNew = map.get(new Node2(fromModel, fromPA));
    					for (int sussNr1 = 0; sussNr1 < graph.getNumSuccessors(fromModel); sussNr1++) {
    						int toModel = graph.getSuccessorNode(fromModel, sussNr1);
    						int toNew = map.get(new Node2(toModel, toPA));
    						String nodeStr = "(" + fromNew + "," + toNew + ")";
    						String edgeStr = (graph.getEdgeProperty(CommonProperties.WEIGHT).get(fromModel, sussNr1)).toString();
    						String q = nodeStr + ":" + getMatrix(edgeStr);
    						Q += q + ",";
    					}
    				}
    			}
    		}
    	}
    	Q = Q.substring(0, Q.length() - 1);
    	Q += "}";
    	String mapStr = "{";
    	for (int state : states) {
    		mapStr += state + ":" +((ParityAutomaton) automaton).getPriorityMap().get(stateMap.get(state).getNodePA()) + ",";
    	}
    	mapStr = mapStr.substring(0, mapStr.length() - 1);
    	mapStr += "}";
    	res.put("stateStr", states.toString());
    	res.put("qStr", Q);
    	res.put("priStr", mapStr);
    	// res.put("classicalStateStr", ""+checkState);
    	return res;
    }

    
    @Override
    public boolean canHandle() {
    	// TODO: the conditions for QLTLSolver
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsDiscreteTime.isDiscreteTime(semantics)
                && !SemanticsContinuousTime.isContinuousTime(semantics)) {
            return false;
        }
        if (ExpressionQuantifier.is(property)) {
            return false;
        }
        
        return true;
    }

    
    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        Set<Expression> inners = UtilQLTL.collectQLTLInner(property);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return required;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    protected ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }


}
