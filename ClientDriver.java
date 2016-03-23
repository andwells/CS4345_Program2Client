package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class ClientDriver {

	static DataOutputStream data2server;
	static DataInputStream data4server;
	static Scanner scan;
	static JFrame mainFrame, preFrame;
	static JTextPane chatAreaTextPane, messageBoxTextPane;
	static JList<String> chatMembersList;
	static DefaultListModel<String> listModel;
	static String username;
	static JTextField userNameTF;
	static JButton sendBtn;
	private static Socket sock;
	
	public static void main(String[] args) throws IOException {
		
		sock = new Socket("127.0.0.1", 50000);
		
		// Create an output stream to send data to the server
		data2server = new DataOutputStream(sock.getOutputStream());
		
		// Create an input stream to receive data from the server
		data4server = new DataInputStream(sock.getInputStream());
		
		// display the GUI
		createAndShowGUI();
		
		// Start the Swing Worker on a thread to run in the background
		Thread t2 = new Thread(new HandleServerOutput(chatAreaTextPane));
		t2.start();
	}
	
	public static void createAndShowGUI(){
		// Create PreFrame ////////////////////////////////////////////////////////////
		preFrame = new JFrame("Chat Messenger");
		preFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		preFrame.setSize(400,100);
		preFrame.setLocationRelativeTo(null);
		
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(10,10,10,10));
		preFrame.add(p);

		JLabel unLabel = new JLabel("Username: ");
		p.add(unLabel);
		userNameTF = new JTextField(20);
		p.add(userNameTF);
		JButton joinBtn = new JButton("Join");
		joinBtn.addActionListener(new joinChatButtonListener());
		p.add(joinBtn);
		preFrame.getRootPane().setDefaultButton(joinBtn);
		preFrame.setVisible(true);
		
		// Create MainFrame ////////////////////////////////////////////////////////////
		mainFrame = new JFrame("Chat Messenger");
		mainFrame.setSize(600,600);
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLocationRelativeTo(null);
		
		GridBagLayout layout = new GridBagLayout();
		JPanel mainPanel = new JPanel(layout);
		mainPanel.setBorder(new EmptyBorder(15,15,15,15)); // Give the panel some padding
		GridBagConstraints gb = new GridBagConstraints();
		
		
		// Chat Area ////////////////////////////////////////////////////////////////////
		JPanel chatAreaPanel = new JPanel(); // The text pane will be put inside this panel
		chatAreaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Chat Area"), null)); // Add border and title
		chatAreaTextPane = new JTextPane(); // Create textPane
		chatAreaTextPane.setPreferredSize(new Dimension(350,375));
		chatAreaTextPane.setEditable(false);
		chatAreaTextPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chatAreaPanel.add(chatAreaTextPane); // Add text pane to the corresponding panel
		gb.weightx = .75;
		gb.weighty = .6;
		gb.gridx = 0;
		gb.gridy = 0;
		gb.insets = new Insets(0,0,15,15);
		mainPanel.add(chatAreaPanel, gb);
		
		
		// Chat Members ////////////////////////////////////////////////////////////////
		JPanel chatMembersPanel = new JPanel(); // Create panel to add the textPane to
		chatMembersPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Chat Members"), null)); // Add border and title
		listModel = new DefaultListModel<String>(); // this list model is what will be updated to represent members, then the chatMembersList will look to this
		chatMembersList = new JList<String>(listModel); // Create text pane
		chatMembersList.setPreferredSize(new Dimension(150,375));
		chatMembersList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chatMembersPanel.add(chatMembersList); // Add text pane to the corresponding panel
		gb.weightx = 0.25;
		gb.weighty = .6;
		gb.gridx = 1;
		gb.gridy = 0;
		gb.insets = new Insets(0,0,15,0);
		mainPanel.add(chatMembersPanel, gb);
		
		
		// Message Box /////////////////////////////////////////////////////////////////////
		JPanel messageBoxPanel = new JPanel(); // Create panel to add the textPane to
		messageBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Message Box"), null)); // Add border and title
		messageBoxTextPane = new JTextPane(); // Create text pane
		messageBoxTextPane.setPreferredSize(new Dimension(350,60));
		messageBoxTextPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		messageBoxTextPane.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing"); // This disables the enter key indenting to the next line
		messageBoxPanel.add(messageBoxTextPane); // Add text pane to the corresponding panel
		gb.weightx = 0.75;
		gb.weighty = .3;
		gb.gridx = 0;
		gb.gridy = 1;
		mainPanel.add(messageBoxPanel, gb);
		
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(new sendBtnListener());
		gb.weightx = 0.75;
		gb.weighty = .3;
		gb.gridx = 1;
		gb.gridy = 1;
		mainPanel.add(sendBtn, gb);
		
		mainFrame.add(mainPanel); 
		mainFrame.getRootPane().setDefaultButton(sendBtn); // this sets the enter button to automatically select the sendBtn
	} // End createAndShowGui
	
	// When the joinBtn is clicked, send username to server and open main gui
	static class joinChatButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			username = userNameTF.getText();
			try {
				data2server.writeUTF(username);
			} catch (IOException e) {
				e.printStackTrace();
			}
			preFrame.setVisible(false);
			mainFrame.setVisible(true);
			messageBoxTextPane.requestFocusInWindow();
		}
	} // End joinChatButtonListener
	
	
	// When the sendBtn is clicked, send whatever text is in message text area and clear the text area
	static class sendBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			try {
				data2server.writeUTF(messageBoxTextPane.getText());
			} catch (IOException e) {
				e.printStackTrace();
			}
			messageBoxTextPane.setText("");
		}
	} // End sendBtnListener

	// Handle output from the server
	static class HandleServerOutput extends SwingWorker<Object, String> {
		
		JTextPane textPane; // Local reference of chatAreaTextPane that will be updated
		
		public HandleServerOutput(JTextPane textPane) {
			this.textPane = textPane;
		}
		
		protected Object doInBackground() throws Exception {
			
			try {
				while (true) {// Run until communication with server is interrupted
					String message = data4server.readUTF();
					publish("\n" + message);
				}
			}
			catch (IOException e) {
				publish(e.getMessage() + "\n"); // Public the exception message
			}
			
			return null; // SwingWorker requires a return statement.
		}
		
		protected void process (List<String> list) { // Append the message to the chatAreaTextPane
			for (int index = 0; index < list.size(); index++) {
				StyledDocument doc = textPane.getStyledDocument(); 
				try {
					doc.insertString(doc.getLength(), list.get(index) + "\n", null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	} // End HandleServerOutput
} // Program2GUI class