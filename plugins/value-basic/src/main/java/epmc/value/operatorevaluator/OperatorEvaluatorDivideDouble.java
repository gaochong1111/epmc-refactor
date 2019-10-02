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

package epmc.value.operatorevaluator;

import epmc.operator.Operator;
import epmc.operator.OperatorDivide;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueDouble;

public final class OperatorEvaluatorDivideDouble implements OperatorEvaluator {
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
            if (operator != OperatorDivide.DIVIDE) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            for (Type type : types) {
                if (!TypeDouble.is(type) && !TypeInteger.is(type)) {
                    return null;
                }
            }
            if (!TypeDouble.is(types[0]) && !TypeDouble.is(types[1])
                    && !TypeDouble.is(TypeReal.get())) {
                return null;
            }
            return new OperatorEvaluatorDivideDouble(this);
        }
    }

    private OperatorEvaluatorDivideDouble(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeDouble.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        double op1 = UtilValue.getDoubleOrInt(operands[0]);
        double op2 = UtilValue.getDoubleOrInt(operands[1]);
        if (op2 == 0.0) {
            ValueDouble.as(result).set(0);            
        } else {
            ValueDouble.as(result).set(op1 / op2);
        }
    }
}
