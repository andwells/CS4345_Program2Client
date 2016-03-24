/**
 * Author: Max Carter, Robert Walker, Andrew Wells
 * File Name: ClientDriver.java
 * Date: 03/19/2016
 *
 * Description: Driver for the client side of the chat program. Includes GUI and interaction
 */


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class ClientDriver {

	static DataOutputStream data2server;
	static DataInputStream data4server;
	static JFrame mainFrame,preFrame;
	static JTextPane chatAreaTextPane, messageBoxTextPane;
	static JList<String> chatMembersList;
	static DefaultListModel<String> listModel;
	static JTextField usernameTextField, serverNameTextField;
	static JButton sendBtn;
	static String serverName;
	private static Socket sock;
	
	public static void main(String[] args){
		
		// prepare and display the first window that prompts for username and server name
		createAndShowPreGUI();
	}
	
	// This method is purely for testing
	public static void populateMemberList() {
		listModel.addElement("Test1");
		listModel.addElement("Test2");
		listModel.addElement("Test3");
		listModel.addElement("Test4");
		listModel.addElement("Test5");
		listModel.addElement("Test6");
		listModel.addElement("Test7");
		listModel.addElement("Test8");
	} // End populateMemberList
	
	public static void createAndShowPreGUI() {
		
		// Create PreFrame ////////////////////////////////////////////////////////////
		preFrame = new JFrame("Chat Messenger");
		preFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		preFrame.setSize(400,150);
		preFrame.setLocationRelativeTo(null); // Center the frame to the screen
		
		GridBagLayout preLayout = new GridBagLayout();
		JPanel prePanel = new JPanel(preLayout);
		prePanel.setBorder(new EmptyBorder(15,15,15,15));
		preFrame.add(prePanel);
		GridBagConstraints c = new GridBagConstraints();
		
		// Username Label /////////////////////////////////////
		JLabel usernameLabel = new JLabel("Username:"); 
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5; 	c.weighty = 0.5;
		c.gridx = 0; 		c.gridy = 0;
		prePanel.add(usernameLabel,c);
		
		// Server Name Label //////////////////////////////////
		JLabel serverNameLabel = new JLabel("Server (IP/Name):");
		c.weightx = 0.5;	c.weighty = 0.5;
		c.gridx = 0;		c.gridy = 1;
		prePanel.add(serverNameLabel,c);
		
		// Default Server Button ////////////////////////////////////////
		JButton defaultServerButton = new JButton("Default Server");
		defaultServerButton.addActionListener(new defaultServerButtonListener());
		c.weightx = 0.5;	c.weighty = 0.5;
		c.gridx = 0;		c.gridy = 2;
		prePanel.add(defaultServerButton,c);
		
		// Username Text Field ////////////////////////////////
		usernameTextField = new JTextField(20);
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.5;	c.weighty = 0.5;
		c.gridx = 1;		c.gridy = 0;
		prePanel.add(usernameTextField,c);
		
		// Server Name Text Field /////////////////////////////
		serverNameTextField = new JTextField(20);
		c.weightx = 0.5;	c.weighty = 0.5;
		c.gridx = 1;		c.gridy = 1;
		prePanel.add(serverNameTextField,c);
		
		// Join Button ////////////////////////////////////////
		JButton joinButton = new JButton("Join");
		joinButton.addActionListener(new joinChatButtonListener());
		c.weightx = 0.5;	c.weighty = 0.5;
		c.gridx = 1;		c.gridy = 2;
		prePanel.add(joinButton,c);
		
		preFrame.getRootPane().setDefaultButton(joinButton); // Set the join button to be clickable by the ENTER key
		preFrame.setVisible(true); 
		
	} // End creatAndShowPreGUI
	
	public static void createAndShowMainGUI(){
				
		// Create MainFrame ////////////////////////////////////////////////////////////
		mainFrame = new JFrame("Chat Messenger");
		mainFrame.setSize(600,600);
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLocationRelativeTo(null); // Center the frame to the screen
		
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
		gb.weightx = .75;	gb.weighty = .6;
		gb.gridx = 0;		gb.gridy = 0;
		gb.insets = new Insets(0,0,15,15);
		mainPanel.add(chatAreaPanel, gb);
		
		// Chat Members ////////////////////////////////////////////////////////////////
		JPanel chatMembersPanel = new JPanel(); // Create panel to add the textPane to
		chatMembersPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Chat Members"), null)); // Add border and title
		listModel = new DefaultListModel<String>(); // this list model is what will be updated to represent members, then the chatMembersList will look to this
		chatMembersList = new JList<String>(listModel); // Create text pane
		chatMembersList.setPreferredSize(new Dimension(150,375));
		chatMembersList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chatMembersList.addMouseListener(new doubleClickListener()); // Add the double click function to the list
		chatMembersPanel.add(chatMembersList); // Add text pane to the corresponding panel
		gb.weightx = 0.25;	gb.weighty = .6;
		gb.gridx = 1;		gb.gridy = 0;
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
		gb.weightx = 0.75;	gb.weighty = .3;
		gb.gridx = 0;		gb.gridy = 1;
		mainPanel.add(messageBoxPanel, gb);
		
		// Send Button /////////////////////////////////////////////////////////////////////
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(new sendBtnListener());
		gb.weightx = 0.75;	gb.weighty = .3;
		gb.gridx = 1;		gb.gridy = 1;
		mainPanel.add(sendBtn, gb);
		
		mainFrame.add(mainPanel); 
		mainFrame.getRootPane().setDefaultButton(sendBtn); // this sets the enter button to automatically select the sendBtn
	} // End createAndShowGui
	
	// Send username to server and open main gui
	static class joinChatButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			String username = usernameTextField.getText();
			serverName = serverNameTextField.getText();
			
			if (username.equals("") || serverName.equals("")){
				ErrorNoInformation error = new ErrorNoInformation();
				error.setVisible(true);
			}
			else {
				try {
					// prepare but do not display the second window that is the actual chat interface
					createAndShowMainGUI();
					
					
					sock = new Socket(serverName, 50000);
					
					// Create an output stream to send data to the server
					data2server = new DataOutputStream(sock.getOutputStream());
					
					// Create an input stream to receive data from the server
					data4server = new DataInputStream(sock.getInputStream());
					
					// Start the Swing Worker on a thread to run in the background
					Thread t2 = new Thread(new HandleServerOutput(chatAreaTextPane));
					t2.start();

					//First piece of data that server is expecting is username
					data2server.writeUTF(username);
				}
				catch(UnknownHostException uhEx)//Could not find in DNS
				{
					JOptionPane.showMessageDialog(null, String.format("Could not locate host %s", serverName), "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
				catch(ConnectException cEx)//If server cannot be reached (off, sleeping, firewall issue, etc.)
				{
					JOptionPane.showMessageDialog(null, String.format("Could not connect to host %s. Error Message: ", serverName, cEx.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				
				//Hid preFrame, show chat box
				preFrame.setVisible(false);
				mainFrame.setVisible(true);
				
				//Update title to reflect connection
				mainFrame.setTitle(String.format("Connected to %s as %s", serverName, username));
				messageBoxTextPane.requestFocusInWindow();
			}
		}
	} // End joinChatButtonListener
	
	// The default server name is set to 127.0.0.1
	static class defaultServerButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			serverNameTextField.setText("127.0.0.1");
		}
		
	} // End defaultServerButtonListener
	
	// Send whatever text is in message text area and clear the text area
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

	// When the user double clicks on a members name, the name is added to format private messaging
	static class doubleClickListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				messageBoxTextPane.setText("/" + chatMembersList.getSelectedValue() + ":");
			}
		}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
		
	} // End doubleClickListener
	
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
					if(message.startsWith("/list:")){
						listModel.clear();
						buildList(message);
					}
					else
					{
						publish(message);
					}
				}
			}
			catch (IOException e) {
				publish(e.getMessage() + "\n"); // Public the exception message
			}
			return null; // SwingWorker requires a return statement.
		}
		//Uses list from server to add elements to the JList sidepanel
		private void buildList(String msg){
			final Pattern pattern = Pattern.compile("/list:(.+?)/list");
			final Matcher matcher = pattern.matcher(msg);
			matcher.find();
			String names[] = matcher.group(1).split(";");
			for(String s: names){
				listModel.addElement(s);
			}
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