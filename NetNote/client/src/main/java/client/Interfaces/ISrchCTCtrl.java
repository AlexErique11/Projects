package client.Interfaces;

import javafx.collections.ObservableList;
import java.util.Locale;

public interface ISrchCTCtrl {

    void applyLocalizedText();

    void switchLanguage(Locale newLocale);

    Locale loadSavedLanguage();

    void filterNotes();

    void changeColor(String s);

    void saveColorToFile(String s);

    void readSavedColor();

    String readUserCss();

    boolean checkIfEmpty();

    void loadFXMLWithBundle(Locale locale);

    void saveLanguagePreference(Locale locale);
    String readCssForMarkdown();
    void saveCssForMarkdown(String css);

}
