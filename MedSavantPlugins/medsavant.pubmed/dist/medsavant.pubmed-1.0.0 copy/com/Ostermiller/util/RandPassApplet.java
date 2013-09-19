/*
 * Generate random passwords.
 * Copyright (C) 2003 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Random+Password+Generator+Applet
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package com.Ostermiller.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.Ostermiller.util.*;

/**
 * An applet that will let the user generate random passwords.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.02.00
 */
public class RandPassApplet extends JApplet {

	private JTextArea display = new JTextArea();
	private JButton clearButton = new JButton("Clear");
	private JButton generateButton = new JButton("Generate");
	private char[] passwordAlphabet = RandPass.NONCONFUSING_ALPHABET;
	private JTextField alphabetField = new JTextField(new String(passwordAlphabet));
	private char[] passwordFirstAlphabet = new char[0];
	private JTextField alphabetFirstField = new JTextField(new String(passwordFirstAlphabet));
	private char[] passwordLastAlphabet = new char[0];
	private JTextField alphabetLastField = new JTextField(new String(passwordLastAlphabet));
	private int passwordLength = 8;
	private JTextField passwordLengthField = new JTextField("" + passwordLength);
	private RandPass randPass = new RandPass(passwordAlphabet);
	private GridLayout preferencesPanelLayout = new GridLayout(4,2);
	private JPanel preferencesPanel = new JPanel(preferencesPanelLayout);

	/**
	 * Start the applet (to be called by the appletviewer)
	 *
	 * @since ostermillerutils 1.02.00
	 */
	public void init() {
		getContentPane().removeAll();
		JMenuBar menuBar = new JMenuBar();
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.getAccessibleContext().setAccessibleDescription(
			"Change how passwords are generated."
		);
		JMenuItem prefsMenuItem = new JMenuItem("Preferences...", KeyEvent.VK_P);
		prefsMenuItem.getAccessibleContext().setAccessibleDescription(
			"Set password length and content."
		);
		preferencesPanelLayout.setHgap(5);
		Dimension d;
		d = passwordLengthField.getPreferredSize();
		d.setSize(70, d.getHeight());
		passwordLengthField.setPreferredSize(d);
		d = alphabetField.getPreferredSize();
		d.setSize(70, d.getHeight());
		alphabetField.setPreferredSize(d);
		d = alphabetFirstField.getPreferredSize();
		d.setSize(70, d.getHeight());
		alphabetFirstField.setPreferredSize(d);
		d = alphabetLastField.getPreferredSize();
		d.setSize(70, d.getHeight());
		alphabetLastField.setPreferredSize(d);
		preferencesPanel.add(new JLabel("Length:"));
		preferencesPanel.add(passwordLengthField);
		preferencesPanel.add(new JLabel("Alphabet:"));
		preferencesPanel.add(alphabetField);
		preferencesPanel.add(new JLabel("First Character Alphabet:"));
		preferencesPanel.add(alphabetFirstField);
		preferencesPanel.add(new JLabel("Last Character Alphabet:"));
		preferencesPanel.add(alphabetLastField);
		prefsMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				passwordLengthField.setText("" + passwordLength);
				alphabetField.setText(new String(passwordAlphabet));
				alphabetFirstField.setText(new String(passwordFirstAlphabet));
				alphabetLastField.setText(new String(passwordLastAlphabet));
				int preferencesResult = JOptionPane.showConfirmDialog(
					RandPassApplet.this,
					preferencesPanel,
					"Preferences",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE
				);
				if (preferencesResult == JOptionPane.OK_OPTION){
					try {
						int length = Integer.parseInt(passwordLengthField.getText());
						if (length >= 3 && length <=100){
							passwordLength = length;
						}
					} catch (NumberFormatException x){
					}
					String alphabetString = alphabetField.getText();
					passwordAlphabet = new char[alphabetString.length()];
					alphabetString.getChars(0, alphabetString.length(), passwordAlphabet, 0);
					randPass.setAlphabet(passwordAlphabet);
					String alphabetFirstString = alphabetFirstField.getText();
					passwordFirstAlphabet = new char[alphabetFirstString.length()];
					alphabetFirstString.getChars(0, alphabetFirstString.length(), passwordFirstAlphabet, 0);
					randPass.setFirstAlphabet(passwordFirstAlphabet);
					String alphabetLastString = alphabetLastField.getText();
					passwordLastAlphabet = new char[alphabetLastString.length()];
					alphabetLastString.getChars(0, alphabetLastString.length(), passwordLastAlphabet, 0);
					randPass.setLastAlphabet(passwordLastAlphabet);
				}
			}
		});
		editMenu.add(prefsMenuItem);
		menuBar.add(editMenu);
		setJMenuBar(menuBar);

		display.setEditable(false);
		display.setFont(new Font("Monospaced", Font.PLAIN, 12));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(display), BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				display.setText("");
			}
		});
		buttonPanel.add(clearButton);
		generateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				display.append(randPass.getPass(passwordLength) + '\n');
			}
		});
		buttonPanel.add(generateButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		display.setText(randPass.getPass(passwordLength) + '\n');
	}

	/**
	 * Get information such as the name of this applet, the author of
	 * this applet, and a description of this applet.
	 *
	 * @return a string with the information about this applet.
	 *
	 * @since ostermillerutils 1.02.00
	 */
	public String getAppletInfo() {
		return (
			"Title: Random Password Generator\n" +
			"Author: Stephen Ostermiller\n" +
			"http://ostermiller.org/contact.pl?regarding=Random+Password+Generator+Applet\n" +
			"Generates secure random passwords."
		);
	}
}
