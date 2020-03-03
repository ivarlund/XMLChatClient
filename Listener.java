import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;

/**
 * Listener class of ChatClient.
 * Extends thread and listens for incoming
 * data until shut down by main class.
 *
 * @author Ivar Lund
 * ivarnilslund@gmail.com
 */
public class Listener extends Thread {

    private InputStreamReader input;
    private BufferedReader reader;
    private final Socket clientSocket;
    private boolean alive = true;
    private JTextArea display;

    /**
     * Class constructor. Takes one parameter provided at initialization.
     *
     * @param socket the socket to be listened too.
     */
    public Listener(Socket socket, JTextArea display) {
        this.clientSocket = socket;
        this.display = display;
        setupListener();
    }

    /**
     * Worker method of this class. listens to input stream for data and displays
     * data to STDOUT. Lifespan determined by while loop.
     */
    public void run() {
        Document doc = new Document();
        DocType dtd = new DocType("message", "1//PW//Example//123", "https://atlas.dsv.su.se/~pierre/i/05_ass/ip1/2/2.1.3/message.dtd");
        doc.setDocType(dtd);
        String msg = null;
        while (alive) {
            try {
                msg = reader.readLine();
                SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
                doc = builder.build(new StringReader(msg));
            } catch (JDOMException e) {
                updateDisplay("KUNDE INTE PARSA ETT MEDDELANDE: " + msg);
            } catch (IOException e) {
                System.out.println("Server connection lost");
                System.exit(1);
                e.printStackTrace(System.out);
            }
            try {
                String name = doc.getRootElement().getChild("header").getChild("id").getChild("name").getValue();
                String email = doc.getRootElement().getChild("header").getChild("id").getChild("email").getValue();
                String body = doc.getRootElement().getChild("body").getValue();

                updateDisplay(name + " (" + email + ") : " + body);

                if (alive) {
                    XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                    System.out.println("INPUT ====================== INPUT");
                    out.output(doc, System.out);
                    System.out.println("INPUT ====================== INPUT");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Setter for variable determining the lifespan of the method run()
     *
     * @param alive pass false to kill thread.
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Initiates input stream and buffer.
     */
    private void setupListener() {
        try {
            input = new InputStreamReader(clientSocket.getInputStream());
            reader = new BufferedReader(input);
        } catch (IOException e) {
            System.out.println("Error: could not set up input.");
            e.printStackTrace();
        }
    }

    /**
     * Updates display text.
     *
     * @param str text to update with.
     */
    private void updateDisplay(String str) {
        display.append(str);
    }

}
