package client;


import client.Interfaces.IVariables;
import commons.Note;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalTime;

public class RefreshThread {

    private IVariables var = null;
    private Thread thread;
    private ObservableList<Note> allNotes = FXCollections.observableArrayList();

    /**
     * This is a constructor for the Thread. It takes a Note Controller
     * @param var the NoteController of the app so all changes are synced to the app
     */
    @Inject
    public RefreshThread(IVariables var) {
        this.var = var;
    }

    private final Runnable task = () -> {
        while (true) { // Infinite loop to keep the thread running

            try {
                // Sleep for 5 seconds (5000 milliseconds)
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted, stopping...");
                Thread.currentThread().interrupt(); // Restore the interrupt status
                break; // Exit the loop if thread is interrupted
            }
            System.out.println("Task executed at: " + LocalTime.now());
            var.setNotes(allNotes);
        }
    };

    /**
     * This method starts the thread
     */
    public void startThread() {
        thread = Thread.ofVirtual().start(task);
        System.out.println("Starting thread");
    }

    /**
     * This method stops the thread if it is running
     */
    public void stopThread() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt(); // Interrupt the thread
            System.out.println("Thread interrupt called.");
        }
    }
}
