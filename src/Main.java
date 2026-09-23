import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        // පෙනුම නවීන කිරීම
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
        addButton.setForeground(Color.BLACK);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // අලුතින් එකතු කළ Delete බොත්තම
        JButton deleteButton = new JButton("Delete Selected Task");
        deleteButton.setBackground(new Color(220, 53, 69)); // රතු පැහැය
        deleteButton.setForeground(Color.WHITE); // අකුරු සුදු පැහැ කිරීම
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add බොත්තමට ක්‍රියාකාරීත්වයක් ලබා දීම
        addButton.addActionListener(e -> {
            String task = taskInput.getText();
            if (!task.trim().isEmpty()) {
                listModel.addElement(task);
                taskInput.setText("");
            }
        });

        // Delete බොත්තමට ක්‍රියාකාරීත්වයක් ලබා දීම
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ලැයිස්තුවෙන් තෝරාගෙන ඇති අයිතමයේ ස්ථානය (index) ලබා ගැනීම
                int selectedIndex = todoList.getSelectedIndex();

                if (selectedIndex != -1) {
                    listModel.remove(selectedIndex); // දත්තය ලැයිස්තුවෙන් ඉවත් කිරීම
                } else {
                    // කිසිවක් තෝරා නොමැති නම් පණිවිඩයක් පෙන්වීම
                    JOptionPane.showMessageDialog(frame, "කරුණාකර මකා දැමීමට අවශ්‍ය කාර්යය ලැයිස්තුවෙන් තෝරන්න.", "දෝෂයකි", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        // කොටස් සියල්ල ප්‍රධාන කවුළුවට එකතු කිරීම
        frame.add(deleteButton, BorderLayout.NORTH); // Delete බොත්තම ඉහළින් එකතු කිරීම
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}