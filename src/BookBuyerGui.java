package jadelab2;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class BookBuyerGui extends JFrame {	
	private BookBuyerAgent myAgent;
	
	private JTextField titleField;
	private JTextField budgetField;

	BookBuyerGui(BookBuyerAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));

		p.add(new JLabel("Title:"));
		titleField = new JTextField(15);
		p.add(titleField);

		p.add(new JLabel("Budget:"));
		budgetField = new JTextField(15);
		p.add(budgetField);

		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Search");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					int budget = Integer.valueOf(budgetField.getText().trim());

					myAgent.lookForTitle(title);
					myAgent.setBudget(budget);

					titleField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(BookBuyerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}	
}
