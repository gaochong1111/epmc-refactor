package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorBaseKet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorBaseKet implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            built = true;
            if (operator != OperatorBaseKet.BASE_KET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            return new OperatorEvaluatorBaseKet(this);
        }
    }

    private final TypeMatrix resultType;
    private final Value resultBuffer;
    private final OperatorEvaluator set;
    
    private OperatorEvaluatorBaseKet(Builder builder) {
        resultType = TypeMatrix.get(TypeComplex.get());
        resultBuffer = resultType.newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeMatrix.get(TypeComplex.get()), TypeMatrix.get(TypeComplex.get()));
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value op1 = operands[0];
        Value op2 = operands[1];
        if (result == op1 || result == op2) {
            apply(resultBuffer, op1, op2);
            set.apply(result, resultBuffer);
            return;
        }
        if (ValueArray.is(op1)) {
            UtilValueQMC.vectorToKet(ValueMatrix.as(result), ValueArray.as(op1), op2);
        } else {
            UtilValueQMC.toBaseKet(ValueMatrix.as(result), op1, op2);
        }
    }
}
