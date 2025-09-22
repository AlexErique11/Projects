/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import client.Interfaces.*;
import client.providers.EmbedTagsCtrlProvider;
import client.providers.FxmlControllerProvider;
import client.providers.MiscCtrlProvider;
import client.providers.NoteCollectionProvider;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import client.scenes.MainCtrl;
import client.scenes.QuoteOverviewCtrl;

public class MyModule implements Module {

    /**
     * Configuration method for the whole app.
     * @param binder Binder is a clone class which can be altered
     */
    @Override
    public void configure(Binder binder) {
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(QuoteOverviewCtrl.class).in(Scopes.SINGLETON);

        binder.bind(NoteService.class).in(Scopes.SINGLETON);
        binder.bind(CollectionSceneController.class).in(Scopes.SINGLETON);
        binder.bind(IMiscCtrl.class).to(MiscCtrl.class);
        binder.bind(INoteCollectionCtrl.class).to(NoteCollectionCtrl.class);
        binder.bind(IVariables.class).to(Variables.class);
        binder.bind(IEmbedTagsCtrl.class).to(EmbedTagsCtrl.class);
        binder.bind(IFxmlController.class).to(FxmlController.class);
        binder.bind(ISrchCTCtrl.class).to(SrchColorTranslateCtrl.class);
        binder.bind(FxmlController.class).toProvider(FxmlControllerProvider.class).in(Scopes.SINGLETON);
        binder.bind(MiscCtrl.class).toProvider(MiscCtrlProvider.class).in(Scopes.SINGLETON);
        binder.bind(NoteCollectionCtrl.class).toProvider(NoteCollectionProvider.class).in(Scopes.SINGLETON);
        binder.bind(EmbedTagsCtrl.class).toProvider(EmbedTagsCtrlProvider.class).in(Scopes.SINGLETON);
        binder.bind(Variables.class).in(Scopes.SINGLETON);

    }
}