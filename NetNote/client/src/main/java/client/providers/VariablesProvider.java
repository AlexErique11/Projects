package client.providers;

import client.*;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VariablesProvider implements Provider<Variables> {
    @Inject
    private Provider<NoteCollectionCtrl> nc;
    @Inject
    private Provider<EmbedTagsCtrl> em;
    @Inject
    private Provider<FxmlController> fx;
    @Inject
    private Provider<SrchColorTranslateCtrl> srch;
    @Inject
    private Provider<MiscCtrl> misc;
    @Inject
    private ServerUtils serverUtils;


    /**
     * Override of the Variables Get Class
     * @return Returns the {@code Variables} class getter but overridden.
     */
    @Override
    public Variables get() {
        return new Variables(nc.get(),em.get(),fx.get(), srch.get(), misc.get(), serverUtils);
    }
}
