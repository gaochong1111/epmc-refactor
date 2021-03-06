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

package epmc.expression.standard.evaluatordd;

import java.util.List;
import java.util.Map;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.operator.OperatorLe;

public final class EvaluatorDDOperatorVectorLe implements EvaluatorDD {
    public final static String IDENTIFIER = "operator-vector-le";

    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private DD dd;
    private boolean closed;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        this.variables = variables;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean canHandle() {
        return UtilEvaluatorDD.canIntegerVectorOperator(expression, OperatorLe.LE, variables);
    }

    @Override
    public void build() {
        dd = UtilEvaluatorDD.applyVector(expression, variables, getContextDD()::twoCplLe);
    }

    @Override
    public DD getDD() {
        assert dd != null;
        return dd;
    }

    @Override
    public List<DD> getVector() {
        return null;
    }

    @Override
    public void close() {
        closed = UtilEvaluatorDD.close(closed, dd, null);
    }

    private ContextDD getContextDD() {
        return ContextDD.get();
    }
}
