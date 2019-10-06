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
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.graph.CommonProperties;
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
        assert property instanceof ExpressionQuantifier;
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        graph.explore(forStatesExplicit.getStatesExplicit());
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        System.out.println(quantifiedProp);
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        StateMap result = doSolve(quantifiedProp, forStates, dirType.isMin());
        
        
        if (!propertyQuantifier.getCompareType().isIs()) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            assert op != null;
            result = result.applyWith(op, compare);
        }
        return result;
    }

    private StateMap doSolve(Expression property, StateSet states, boolean min) {
    	this.forStates = (StateSetExplicit) forStates;
    	Expression[] expressions = UtilQLTL.collectQLTLInner(property).toArray(new Expression[0]);
    	ParityAutomaton pa = DeterminisationUtilAutomaton.newParityAutomaton(property, expressions);
    	System.out.println(pa.getBuechi().getGraph());
        System.out.println(this.graph);
        product(pa);
        System.exit(0);
        return null;
    }
    
    void getSet(Expression exp, Set<ExpressionLiteral> set, Set<ExpressionLiteral> all) {
    	
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
    	if (str.contains("matrix")) {
    		return "[[]]";
    	}
    	String matrixStr = "[";
    	String[] strs = str.split("\n");
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
    
    private String[] product(Automaton automaton) {
    	String[] res = new String[4];
    	Map<Integer, Node2> stateMap= new HashMap<Integer, Node2>();
    	Map<Node2, Integer> map = new HashMap<Node2, Integer>();
    	Set<ExpressionLiteral> all = getVariableValues();
    	Map<Expression,  JANIType> varsMap = ((ModelPRISMQMC) modelChecker.getModel()).getModelPRISM()
 		       .getModules().get(0).getVariables();
    	ExpressionIdentifier propName = (ExpressionIdentifier) varsMap.keySet().toArray()[0]; 
    	int numNodes = automaton.getBuechi().getGraph().getNumNodes();
    	int nodeNr = 0;
    	List<Integer> states = new ArrayList<Integer>();
    	List<Integer> initStates = new ArrayList<Integer>();
    	for (int fromModel = 0; fromModel < graph.getNumNodes(); fromModel++) {
    		for (int fromPA = 0; fromPA < numNodes; fromPA++) {
    			if (automaton.getBuechi().getGraph().getInitialNodes().get(fromPA) && 
    					graph.getInitialNodes().get(fromModel))
    				initStates.add(nodeNr); 
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
    	res[0] = states.toString();
    	res[1] = Q;
    	res[2] = mapStr;
    	res[3] = initStates.toString();
    	for (String str : res) {
    		System.out.println(str);
    	}
    	return res;
//    	
//    	
//    	for (Object propName : graph.getNodeProperties()) {
//    		if (propName instanceof ExpressionIdentifierStandard) {
//    			NodeProperty prop = graph.getNodeProperty(propName);
//    		}
//    	}
//    	
//    	Expression[] expressions = automaton.getExpressions();
//    	//expressionProps = new NodeProperty[expressions.length];
//    	for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
//    		Expression exp = expressions[exprNr];
//    		System.out.println("********************");
//			System.out.println(modelChecker.getRequiredNodeProperties(exp, forStates));
//			System.out.println("********************");
//			System.out.println(exp);
//        }
//    	
//    	System.out.println(((ValueObject) (automaton.getBuechi().getGraph().getEdgeProperty(CommonProperties.AUTOMATON_LABEL).get(0, 1))));
//        ProductGraphExplicit prodGraph = new ProductGraphExplicit.Builder()
//                .setModel(graph)
//                //.setModelInitialNodes(forStates.getStatesExplicit())
//                .setAutomaton(automaton)
//                .addGraphProperties(graph.getGraphProperties())
//                .addNodeProperty(CommonProperties.PLAYER)
//                .addNodeProperty(CommonProperties.STATE)
//                .addEdgeProperty(CommonProperties.WEIGHT)
//                .build();
//        GraphExplicitWrapper prodWrapper = new GraphExplicitWrapper(prodGraph);
//        prodWrapper.addDerivedGraphProperties(prodGraph.getGraphProperties());
//        prodWrapper.addDerivedNodeProperty(CommonProperties.STATE);
//        prodWrapper.addDerivedNodeProperty(CommonProperties.PLAYER);
//        prodWrapper.addDerivedNodeProperty(CommonProperties.AUTOMATON_LABEL);
//        prodWrapper.addDerivedNodeProperty(CommonProperties.NODE_AUTOMATON);
//        prodWrapper.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
//        prodWrapper.addDerivedEdgeProperty(CommonProperties.WEIGHT);
//        prodWrapper.explore();
//        System.out.println(prodWrapper);
    }

    
    @Override
    public boolean canHandle() {
    	// TODO: the conditions for QLTLSolver
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (ExpressionTemporalFinally.is(property)) {
        	return true;
        }
//        Semantics semantics = modelChecker.getModel().getSemantics();
//        if (!SemanticsDiscreteTime.isDiscreteTime(semantics)
//                && !SemanticsContinuousTime.isContinuousTime(semantics)) {
//            return false;
//        }
//        if (!ExpressionQuantifier.is(property)) {
//            return false;
//        }
        // handleSimplePCTLExtensions();
//        ExpressionQuantifier propertyQuantifier = ExpressionQuantifier.as(property);
//        if (!UtilPCTL.isPCTLPathUntil(propertyQuantifier.getQuantified())) {
//            return false;
//        }
//        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
//        GraphExplicit graph = modelChecker.getLowLevel();
//        System.out.println(graph);
//        StateSet allStates = UtilGraph.computeAllStatesExplicit(graph);
//        System.exit(0);
//        for (Expression inner : inners) {
//            modelChecker.ensureCanHandle(inner, allStates);
//        }
//        if (allStates != null) {
//            allStates.close();
//        }
        return false;
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
        // System.out.println("property: " + property);
        // ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Set<Expression> inners = UtilQLTL.collectQLTLInner(property); // propertyQuantifier.getQuantified()
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
