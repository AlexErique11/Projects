package commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.springframework.web.client.RestTemplate;

public class ServerItem {

    private String port;
    private String name;
    private boolean accessible;

    /**
     * Standard constructor for Serveritem
     * @param port -> the port on which server is running
     * @param name -> Name of the server
     * @param accessible -> is the server online??
     */
    public ServerItem(String port, String name, boolean accessible) {
        this.port = port;
        this.name = name;
        this.accessible = accessible;
    }

    /**
     * getter for server's port
     * @return String representing port
     */
    public String getPort() {
        return port;
    }

    /**
     * Standard setter for port
     * @param port -> String representing port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Getter for name
     * @return server's name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     * @param name -> the string representing server's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for server's accessibility
     * @return boolean representing accessibility
     */
    public boolean isAccessible() {
        return accessible;
    }

    /**
     * Setter for server's accessibility
     * @param accessible -> boolean
     */
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }
}
