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

package epmc.jani.type.smg;

import java.util.Map;

import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsCreationSMG implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-smg";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        Map<String, Class<? extends ModelExtensionSemantics>> janiToSemantics = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        assert janiToSemantics != null;
        janiToSemantics.put(ModelExtensionSMG.IDENTIFIER, ModelExtensionSMG.class);
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
            explorerExtensions = new OrderedMap<>();
        }
        explorerExtensions.put(ExplorerExtensionSMG.IDENTIFIER, ExplorerExtensionSMG.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
    }
}
