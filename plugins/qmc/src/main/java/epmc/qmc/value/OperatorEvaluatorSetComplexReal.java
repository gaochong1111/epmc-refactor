package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSetComplexReal implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeReal.is(types[0])) {
                return null;
            }
            if (!TypeComplex.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorSetComplexReal(this);
        }
    }

    private Type resultType;

    private OperatorEvaluatorSetComplexReal(Builder builder) {
        resultType = builder.types[1];
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        ValueComplex resultComplex = ValueComplex.as(result);
        ValueReal operatorReal = ValueReal.as(operands[0]);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        set.apply(resultComplex.getRealPart(), operatorReal);
        set.apply(resultComplex.getImagPart(), UtilValue.newValue(operatorReal.getType(), 0));
    }
}
