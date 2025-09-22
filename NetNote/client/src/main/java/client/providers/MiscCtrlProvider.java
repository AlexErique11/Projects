package client.providers;

import client.Interfaces.IVariables;
import client.MiscCtrl;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MiscCtrlProvider implements Provider<MiscCtrl>{

    @Inject
    private IVariables variables;
    @Inject
    private ServerUtils server;

    /**
     * Override of the MiscCtrl Get function
     * @return Returns the {@code MiscCtrl} class.
     */
    @Override
    public MiscCtrl get() {
        return new MiscCtrl(variables, server);
    }
}
