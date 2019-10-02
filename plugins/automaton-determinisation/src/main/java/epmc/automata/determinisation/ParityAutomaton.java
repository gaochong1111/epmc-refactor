package epmc.automata.determinisation;

import java.util.HashMap;
import java.util.Set;

import epmc.automata.determinisation.AutomatonScheweParity.Builder;
import epmc.automaton.AutomatonParity;
import epmc.automaton.AutomatonSafra;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiTransition;
import epmc.expression.Expression;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.Value;
import epmc.value.ValueObject;

	
public final class ParityAutomaton implements AutomatonParity, AutomatonSafra {
	private HashMap<Integer, Integer> priorityMap;
	public final static String IDENTIFIER = "schewe-parity";
    private final AutomatonSchewe inner;
    private Buechi buechi;
    
	public ParityAutomaton(Buechi buechi) {
		this.buechi = buechi;
		this.priorityMap = new HashMap<Integer, Integer>();
		GraphExplicit graph = buechi.getGraph();
		Set<Object> edgeProperties = graph.getEdgeProperties();
		for (int node = 0; node < graph.getNumNodes(); node++) {
            int numSucc = graph.getNumSuccessors(node);
            if (numSucc > 0) {
            	for (Object property : edgeProperties) {
            		EdgeProperty prop = graph.getEdgeProperty(property);
                    ValueObject value = (ValueObject)prop.get(node, 0);
                    BuechiTransition trans = value.getObject();
                    BitSet accSet = trans.getLabeling();
                    int pri = (accSet.nextSetBit(0));
                    priorityMap.put(node, pri);
            	}
            }
        }
		AutomatonSchewe.Builder scheweBuilder = new AutomatonSchewe.Builder();
        scheweBuilder.setParity(true);
        scheweBuilder.setBuechi(buechi);
        this.inner = scheweBuilder.build();
	}
	
	public HashMap<Integer, Integer> getPriorityMap() {
		return priorityMap;
	}
    

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public int getInitState() {
        return inner.getInitState();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState)
    {
        inner.queryState(modelState, automatonState);
    }

    @Override
    public int getNumStates() {
        return inner.getNumStates();
    }

    @Override
    public Object numberToState(int number) {
        return inner.numberToState(number);
    }

    @Override
    public Object numberToLabel(int number) {
        return inner.numberToLabel(number);
    }

    @Override
    public Expression[] getExpressions() {
        return inner.getExpressions();
    }


    @Override
    public int getSuccessorState() {
        return inner.getSuccessorState();
    }

    @Override
    public int getSuccessorLabel() {
        return inner.getSuccessorLabel();
    }

    @Override
    public int getNumPriorities() {
        return inner.getNumPriorities();
    }

    @Override
    public String toString() {
        return inner.toString();
    }
    
    public Buechi getBuechi() {
    	return buechi;
    }
}
