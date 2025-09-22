package client.providers;

import client.FxmlController;
import client.Interfaces.IVariables;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FxmlControllerProvider implements Provider<FxmlController> {
    @Inject
    private IVariables var;
    @Inject
    private ServerUtils server;

    /**
     * Override of the FXMLController Get
     * @return Returns the {@code FXMLController} class.
     */
    @Override
    public FxmlController get() {
        return new FxmlController(var, server);
    }
}
