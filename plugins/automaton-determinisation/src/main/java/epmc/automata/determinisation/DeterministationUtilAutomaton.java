package epmc.automata.determinisation;

import epmc.automaton.Buechi;
import epmc.automaton.BuechiImpl;
import epmc.automaton.MessagesAutomaton;
import epmc.automaton.UtilAutomaton;
import epmc.expression.Expression;
import epmc.messages.Message;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;

public class DeterministationUtilAutomaton extends UtilAutomaton {
	public static ParityAutomaton newParityAutomaton
    (Expression property, Expression[] expressions) {
        assert property != null;
        Options options = Options.get();
        Buechi buechi = null;
        Log log = options.get(OptionsMessages.LOG);
        Expression usedProperty = property;
        log.send(MessagesAutomaton.COMPUTING_ORIG_BUECHI);
        buechi = new BuechiImpl(usedProperty, expressions, true);
        Message buechiDone = buechi.isDeterministic() ?
                MessagesAutomaton.COMPUTING_BUECHI_DONE_DET
                : MessagesAutomaton.COMPUTING_BUECHI_DONE_NONDET;
        log.send(buechiDone, buechi.getNumLabels(), buechi.getNumStates());
        return new ParityAutomaton(buechi);
    }
}
