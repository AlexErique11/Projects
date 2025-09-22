package client.providers;

import client.Interfaces.IVariables;
import client.SrchColorTranslateCtrl;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.ServiceLoader;

public class SrchCTCtrlProvider implements Provider<SrchColorTranslateCtrl> {
    @Inject
    private IVariables var;

    /**
     * Override for Getter for SrchColorTranslateCtrl
     * @return Returns the {@code SrchColorTranslateCtrl} class.
     */
    @Override
    public SrchColorTranslateCtrl get() {
        return new SrchColorTranslateCtrl(var);
    }
}
