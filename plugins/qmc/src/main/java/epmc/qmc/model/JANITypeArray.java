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
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class JANITypeArray implements JANIType {
	public final static String IDENTIFIER = "array";
    private final static String KIND = "kind";
    private final static String VECTOR = "vector";
    private final static String SIZE = "size";
    private final static String MATRIX = "matrix";
    private final static String ROW = "row";
    private final static String COL = "col";
	
    private final JANIType entryType;
    private transient ModelJANI model;
    private int row;
    private int col;

    JANITypeArray(JANIType entryType, int row, int col, Positional positional) {
        this.entryType = entryType;
        this.row = row;
        this.col = col;
    }
    
    JANITypeArray(JANIType entryType, int col, Positional positional) {
        this.entryType = entryType;
        this.row = 0;
        this.col = col;
    }
    
    JANITypeArray(JANIType entryType, Positional positional) {
        this.entryType = entryType;
        this.row = 0;
        this.col = 0;
    }

    @Override
    public JANIType replace(Map<Expression, Expression> map) {
        return this;
    }

    public void checkExpressionConsistency(Map<Expression, Type> types) {
    }

    @Override
    public TypeArray toType() {
        return entryType.toType().getTypeArray();
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonValue generate() {
    	JsonObjectBuilder result = Json.createObjectBuilder();
    	if (row == 0) {
    		result.add(KIND, VECTOR);
    		result.add(SIZE, Integer.toString(col));
    	} else {
    		result.add(KIND, MATRIX);
    		result.add(ROW, Integer.toString(row));
    		result.add(COL, Integer.toString(col));
    	}
        
        return result.build();
    }
    @Override
    public Value getDefaultValue() {
        TypeArray type = toType();
        Value entryDefault = entryType.getDefaultValue();
        ValueArray result = type.newValue();
        int resultSize = result.size();
        for (int i = 0; i < resultSize; i++) {
            result.set(entryDefault, i);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
