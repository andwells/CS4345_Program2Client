package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

public class EstablishGUI {

	static DataOutputStream data2server;
	static DataInputStream data4server;
	static Scanner scan;
	static JFrame frame, preFrame;
	static String username;
	static JTextField userNameTF;
	static JTextArea chatBox,chatMembers,message;
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
		
		Thread t2 = new Thread(new HandleServerOutput(chatBox));
		t2.start();
	}
	
	public static void createAndShowGUI(){
		// Create PreFrame ////////////////////////////////////////////////////////////
		preFrame = new JFrame("ChatWithMe");
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
		
		
		// Create Frame ////////////////////////////////////////////////////////////////////
 		frame = new JFrame("ChatWithMe");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600,600);
		frame.setLocationRelativeTo(null);
		
		
		//Create Panel and Add Panel to Frame
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10,10,10,10));
		frame.add(panel);
		
		chatBox = new JTextArea(20,35); // Chat Box
		chatBox.setEditable(false); 
		
		chatMembers = new JTextArea(20,15); // Chat Members
		chatMembers.setEditable(false);
		
		message = new JTextArea(5,35); // Message Area
		message.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
		
		sendBtn = new JButton("Send"); // Send Button
		sendBtn.addActionListener(new sendBtnListener()); // Set what the sendBtn does
		
		panel.add(chatBox, BorderLayout.WEST);
		panel.add(chatMembers, BorderLayout.EAST);
		
		JPanel bottom = new JPanel();
		bottom.add(message, BorderLayout.WEST);
		bottom.add(sendBtn, BorderLayout.EAST);
		
		panel.add(bottom, BorderLayout.LINE_START);
		frame.getRootPane().setDefaultButton(sendBtn);
	}
	
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
			frame.setVisible(true);
			message.requestFocusInWindow();
		}
	} // End joinChatButtonListener
	
	
	// When the sendBtn is clicked, send whatever text is in message text area and clear the text area
	static class sendBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			try {
				data2server.writeUTF(message.getText());
			} catch (IOException e) {
				e.printStackTrace();
			}
			message.setText("");
		}
	} // End sendBtnListener

	// Handle output from the server
	static class HandleServerOutput extends SwingWorker<Object, String> {
		
		JTextArea textArea;
		
		public HandleServerOutput(JTextArea textArea) {
			this.textArea = textArea;
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
		
		protected void process (List<String> list) {
			for (int index = 0; index < list.size(); index++) {
				textArea.append(list.get(index) + "\n");
			}
		}
	} // End HandleServerOutput
} // Program2GUI class


