/**
 * Author: Max Carter
 * File Name: ErrorNoInformation.java
 * Date: 03/23/2016
 *
 * Description: GUI that pops up to display an error of username or server name
 */

package gui;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ErrorNoInformation extends JFrame{

	public ErrorNoInformation() {
		super("ERROR");
		setSize(300,100);
		setResizable(false);
		setLocationRelativeTo(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(15,15,15,15));
		add(panel);
		
		JLabel errorLabel = new JLabel("Please fill out both username and server name!");
		errorLabel.setForeground(Color.red);
		panel.add(errorLabel);
	}
}
