package client.Interfaces;

import client.Interfaces.IVariables;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import org.springframework.http.HttpEntity;

public interface IMiscCtrl{

    boolean createPopup(String text);

    boolean resolve();

    void createWarning(String s);

    void createFormattedWarning(String formattedMessage);

    void setStateFromOldController(IVariables oldController);

    void keyBoardShortcuts(KeyEvent event);

    void tagsKeyboardShortcuts(KeyEvent event);

    void keyBoardShortcutsBugs(KeyEvent event);

    void setMarkDownTutorial(String s);

    void showNonBlockingWarning(String message);

    String markdownToHtml(String markdown);

    void saveHtmlToFile(String htmlContent, Long noteId);

    void setupKeybindListener();

    void setAvailable();
}
