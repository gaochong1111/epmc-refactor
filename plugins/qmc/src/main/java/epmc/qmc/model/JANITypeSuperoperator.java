package epmc.qmc.model;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.type.JANIType;
import epmc.qmc.expression.ContextExpressionQMC;
import epmc.qmc.value.TypeSuperOperator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeSuperoperator implements JANIType {
	
	public final static String IDENTIFIER = "superoperator";
    private final static String KIND = "kind";
    private final static String SUPEROPERATOR = "superoperator";
    private final static String SIZE = "size";
	
    private transient ModelJANI model;
    private int size;

    public JANITypeSuperoperator(ContextExpressionQMC contextExpression, int hilbertDimension, Positional positional) {
    	this.size = hilbertDimension;
    }

    @Override
    public JANIType replace(Map<Expression, Expression> map) {
        return this;
    }

    public void checkExpressionConsistency(Map<Expression, Type> types) {
    }

    @Override
    public TypeSuperOperator toType() {
        return TypeSuperOperator.get();
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public JANINode parse(JsonValue value) {
        return parseAsJANIType(value);
    }

    @Override 
    public JANIType parseAsJANIType(JsonValue value) {
        return null;
    }

    @Override
    public JsonValue generate() {
    	JsonObjectBuilder result = Json.createObjectBuilder();
		result.add(KIND, SUPEROPERATOR);
		result.add(SIZE, Integer.toString(size));
        return result.build();
    }

    @Override
    public Value getDefaultValue() {
        return UtilValue.newValue(toType(), 0);
    }
    
    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
