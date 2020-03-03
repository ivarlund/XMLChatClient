
/**
 * Client of this XML chat implementation.
 * Connects to a server via a Socket and
 * can send and receive messages from the server.
 *
 * @author Ivar Lund
 * ivlu1468
 * ivarnilslund@gmail.com
 */

import javax.swing.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.DocType;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Main class. Needs a server to operate.
 *
 * @author Ivar Lund
 */
public class XMLClient extends JFrame {

    private Socket socket;
    private PrintWriter output;

    private Listener listener;

    private String hostName = "localhost"/*"atlas.dsv.su.se"*/;
    private int portNr = 2000/*9494*/;

    private JTextField name = new JTextField(20);
    private JTextField mail = new JTextField(20);
    private JTextField homepage = new JTextField(20);
    private JTextField body = new JTextField(20);
    private JTextArea display;

    /**
     * Class constructor.
     */
    public XMLClient() {
        super("XML Chat Client");

        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        JPanel bottom = new JPanel();
        JPanel bottomLeft = new JPanel();
        JPanel bottomRight = new JPanel();

        display = new JTextArea();
        JScrollPane pane = new JScrollPane(display);
        JButton send = new JButton("Press me!");
        send.addActionListener(new SendListener());

        top.setLayout(new BorderLayout());
        bottomLeft.setLayout(new GridLayout(0, 1, 20, 0));
        bottomRight.setLayout(new GridLayout(0, 1, 20, 0));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

        bottomLeft.add(new JLabel("Name:"));
        bottomLeft.add(new JLabel("E-mail:"));
        bottomLeft.add(new JLabel("Homepage:"));
        bottomLeft.add(new JLabel("Message:"));
        bottomLeft.add(new JLabel("Send:"));

        bottomRight.add(name);
        bottomRight.add(mail);
        bottomRight.add(homepage);
        bottomRight.add(body);
        bottomRight.add(send);

        bottom.add(bottomLeft);
        bottom.add(bottomRight);

        top.add(pane, BorderLayout.CENTER);

        add(top, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setSize(720, 540);
        addWindowListener(new WindowClosing());
//        setDefaultCloseOperation(new WindowClosing());
        setVisible(true);

        setupIO(hostName, portNr);
    }

    /**
     * Initiates I/O, socket and listener thread.
     *
     * @param hostName passed from class constructor
     * @param portNr   passed from class constructor
     */
    private void setupIO(String hostName, int portNr) {
        try {
            socket = new Socket(hostName, portNr);
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Connection setup failed... \nterminated");
            System.exit(1);
        }
        listener = new Listener(socket, display);
        listener.start();
    }

    /**
     * Converts the document to a String and sends it to the connected server.
     *
     * @param doc the Document to be sent.
     */
    public void sendMsg(Document doc) {
        if (socket.isConnected()) {
            Format format = Format.getCompactFormat();
            format.setLineSeparator("");
            XMLOutputter xmlOutputter = new XMLOutputter(format);
            String message = xmlOutputter.outputString(doc);
            output.println(message);
        }
    }

    /**
     * Creates a Document file after the Public .dtd file "https://atlas.dsv.su.se/~pierre/i/05_ass/ip1/2/2.1.3/message.dtd"
     *
     * @return the completed Document.
     */
    private Document createDoc() {
        Document doc = new Document();
        DocType dtd = new DocType("message", "1//PW//Example//123", "https://atlas.dsv.su.se/~pierre/i/05_ass/ip1/2/2.1.3/message.dtd");
        doc.setDocType(dtd);

        Element message = new Element("message");
        Element header = new Element("header");
        Element protocol = new Element("protocol");
        Element type = new Element("type");
        Element version = new Element("version");
        Element command = new Element("command");
        Element id = new Element("id");
        Element name = new Element("name");
        Element email = new Element("email");
        Element homepage = new Element("homepage");
        Element host = new Element("host");
        Element body = new Element("body");

        message.addContent(header);
        message.addContent(body);
        header.addContent(protocol);
        header.addContent(id);
        protocol.addContent(type);
        protocol.addContent(version);
        protocol.addContent(command);
        type.addContent("CTTP");
        version.addContent("1.0");
        command.addContent("MESS");
        id.addContent(name);
        id.addContent(email);
        id.addContent(homepage);
        id.addContent(host);
        name.addContent(this.name.getText());
        email.addContent(this.mail.getText());
        homepage.addContent(this.homepage.getText());
        host.addContent(this.hostName);
        body.addContent(this.body.getText());
        doc.setRootElement(message);

        try {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            System.out.println("OUTPUT ============================ OUTPUT");
            out.output(doc, System.out);
            System.out.println("OUTPUT ============================ OUTPUT");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * Closes socket & streams as well as listener thread and then terminates
     * program.
     */
    private void killIO() {
        try {
            socket.close();
            listener.setAlive(false);
            System.out.println("Thank you for using this chattclient \nSuccessful termination");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Could not close socket properly \nUnsuccessful termination");
            System.exit(1);
        }
    }

    /**
     * Terminates I/O when window is closed.
     */
    private class WindowClosing extends WindowAdapter {

        /**
         * Method call for WindowListenre
         */
        public void windowClosing(WindowEvent e) {
            killIO();
        }
    }

    /**
     * ActionListener for 'send' button.
     */
    private class SendListener implements ActionListener {
        /**
         * Method call for ActionListener
         */
        public void actionPerformed(ActionEvent e) {
            Document doc = createDoc();
            try {
                sendMsg(doc);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Main method. takes hostName as 1st argument and portNr as second argument.
     *
     * @param args holds arguments from user.
     */
    public static void main(String[] args) {
        new XMLClient();
    }
}
