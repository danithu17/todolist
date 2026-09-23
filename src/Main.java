import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        // පෙනුම නවීන කිරීම (Warning එක මගහරවා ඇත)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("පෙනුම වෙනස් කිරීමේදී ගැටළුවක්: " + e.getMessage());
        }

        JFrame frame = new JFrame("මගේ To-Do List එක");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(240, 248, 255));

        // දත්ත පෙන්වන ලැයිස්තුව (List)
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> todoList = new JList<>(listModel);
        todoList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(todoList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input කොටස
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setOpaque(false);

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Add බොත්තම
        JButton addButton = new JButton("Add Task");
        addButton.setBackground(new Color(0, 120, 215));
        addButton.setForeground(Color.BLACK); // අකුරු පැහැදිලිව පෙනීමට කළු පැහැ කිරීම
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add බොත්තමට ක්‍රියාකාරීත්වයක් ලබා දීම (Action Listener)
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String task = taskInput.getText();
                if (!task.trim().isEmpty()) {
                    listModel.addElement(task); // ලැයිස්තුවට අලුත් දත්තය එකතු කිරීම
                    taskInput.setText("");      // දත්තය ඇතුළත් කළ පසු Text box එක හිස් කිරීම
                }
            }
        });

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}