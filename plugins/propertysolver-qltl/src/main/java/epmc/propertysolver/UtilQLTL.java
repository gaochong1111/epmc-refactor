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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;

public final class UtilQLTL {
    public static Set<Expression> collectQLTLInner(Expression expression) {
        if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return collectQLTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            return collectQLTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            return collectQLTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectQLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectQLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectQLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectQLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    private UtilQLTL() {
    }
}
