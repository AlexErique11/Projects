package client.providers;

import client.EmbedTagsCtrl;
import client.Interfaces.IVariables;
import client.NoteCollectionCtrl;
import client.NoteService;
import client.Variables;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class EmbedTagsCtrlProvider implements Provider<EmbedTagsCtrl> {
    @Inject
    private IVariables var;
    @Inject
    private NoteService noteService;
    @Inject
    private ServerUtils server;


    /**
     * Override of get method for Embedded Tags Ctrl
     * @return Returns the {@code EmbedTagsCtrl} class.
     */
    @Override
    public EmbedTagsCtrl get() {
        return new EmbedTagsCtrl(var, noteService, server);
    }
}
