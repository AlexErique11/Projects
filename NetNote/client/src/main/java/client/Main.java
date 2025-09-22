package client;

import static com.google.inject.Guice.createInjector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;

import com.google.inject.Injector;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import client.RefreshThread;

public class Main extends Application {

    static final Injector INJECTOR = createInjector(new MyModule());
    public static final MyFXML FXML = new MyFXML(INJECTOR);
    private RefreshThread refreshThread;

	/**
	 * General constructor for the main file
	 * @param args Arguments
	 * @throws URISyntaxException Uniform Resource Identifier Exception
	 * @throws IOException Input/output Exception
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		launch();
	}

	/**
	 * Starts the application
	 * @param primaryStage The stage of the primary application
	 * @throws Exception General Exception
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		var serverUtils = INJECTOR.getInstance(ServerUtils.class);
		if (!serverUtils.isServerAvailable()) {
			var msg = "Server needs to be started before the client,"
					+" but it does not seem to be available. Shutting down.";
			System.err.println(msg);
			return;
		}
		Locale locale = loadSavedLanguage();

		// Load the NoteController and FXML
		var note = FXML.load(Variables.class, locale,"client", "scenes", "note.fxml");

		// Initialize MainCtrl
		var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
		mainCtrl.initialize(primaryStage, note);

		primaryStage.setOnCloseRequest(event -> {
			System.out.println("Window is closing...");
			//refreshThread.stopThread();
		});
	}

	/**
	 * Loads saved language previously chosen
	 * @return Returns the locale
	 */
	private Locale loadSavedLanguage() {
		try (InputStream input = new FileInputStream("config.properties")) {
			Properties props = new Properties();
			props.load(input);
			String lang = props.getProperty("language");
			return Locale.forLanguageTag(lang.replace('_', '-'));
		} catch (IOException e) {
			return Locale.ENGLISH; // Fallback to English
		}
	}
}
