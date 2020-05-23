package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends Thread implements ActionListener {
    private JButton fiveSecondTask = new JButton("5 Second Task");
    private JButton fifteenSecondTask = new JButton("15 Second Task");
    private JButton twentySecondTask = new JButton("20 Second Task");
    private JButton tenSecondTask = new JButton("10 Second Task");
    private JButton enter = new JButton("Enter");
    ServerSystem serverSystem;

    JFrame frame = new JFrame();
    JPanel panel = new JPanel();
    JLabel label = new JLabel("Select a task length: ");
    JLabel label2 = new JLabel("Or enter a custom number: ");
    JTextField textField = new JTextField();

    public GUI (ServerSystem serverSystem){
        this.serverSystem = serverSystem;
        panel.setLayout(new GridLayout());
        fiveSecondTask.setActionCommand("5");
        fiveSecondTask.addActionListener(this);
        tenSecondTask.setActionCommand("10");
        tenSecondTask.addActionListener(this);
        fifteenSecondTask.setActionCommand("15");
        fifteenSecondTask.addActionListener(this);
        twentySecondTask.setActionCommand("20");
        twentySecondTask.addActionListener(this);
        enter.addActionListener(this);

        panel.add(label);
        panel.add(fiveSecondTask);
        panel.add(tenSecondTask);
        panel.add(fifteenSecondTask);
        panel.add(twentySecondTask);
        panel.add(label2);
        panel.add(textField);
        panel.add(enter);
        Dimension background = new Dimension(500, 500);
        panel.setMinimumSize(background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void newWork(int duration){
        serverSystem.createNewWork(duration);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == enter) {
            if (textField.getText().isEmpty()) {
                textField.setBackground(Color.white);
                textField.setText("");
            }
            else if (Integer.parseInt(textField.getText().trim()) > 0 && Integer.parseInt(textField.getText().trim()) < 2147483647) {
                textField.setBackground(Color.green);
                newWork(Integer.parseInt(textField.getText().trim()));
                textField.setText("");
            } else {
                textField.setBackground(Color.red);
            }
        } else {
            Integer duration = Integer.parseInt(event.getActionCommand().trim());
            newWork(duration);
        }
    }
}
