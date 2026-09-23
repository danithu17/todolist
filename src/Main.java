import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
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

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> todoList = new JList<>(listModel);
        todoList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(todoList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setOpaque(false);

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Add බොත්තම නවීකරණය
        JButton addButton = new JButton("Add Task");
        addButton.setBackground(new Color(0, 120, 215));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setFocusPainted(false); // තිත් ඉර ඉවත් කිරීම
        addButton.setOpaque(true); // වර්ණය පෙන්වීමට ඉඩ දීම
        addButton.setBorderPainted(false); // Windows border එක ඉවත් කිරීම

        // Delete බොත්තම නවීකරණය
        JButton deleteButton = new JButton("Delete Selected Task");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);

        // Add බොත්තමට ක්‍රියාකාරීත්වයක් ලබා දීම
        addButton.addActionListener(e -> {
            String task = taskInput.getText();
            if (!task.trim().isEmpty()) {
                listModel.addElement(task);
                taskInput.setText("");
            }
        });

        // Delete බොත්තමට ක්‍රියාකාරීත්වයක් ලබා දීම
        deleteButton.addActionListener(e -> {
            int selectedIndex = todoList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(frame, "කරුණාකර මකා දැමීමට අවශ්‍ය කාර්යය ලැයිස්තුවෙන් තෝරන්න.", "දෝෂයකි", JOptionPane.WARNING_MESSAGE);
            }
        });

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        frame.add(deleteButton, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}