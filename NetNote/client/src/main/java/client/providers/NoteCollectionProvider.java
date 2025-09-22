package client.providers;

import client.Interfaces.IVariables;
import client.NoteCollectionCtrl;
import client.Variables;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class NoteCollectionProvider implements Provider<NoteCollectionCtrl> {
    @Inject
    private IVariables var;
    @Inject
    private ServerUtils server;


    /**
     * Override of getter for NoteCollectionControl class
     * @return Returns {@code NoteCollectionCtrl} class.
     */
    @Override
    public NoteCollectionCtrl get() {
        return new NoteCollectionCtrl(var, server);
    }
}
