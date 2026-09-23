import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class Main {
    private static final String FILE_NAME = "tasks.txt";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error changing look and feel: " + e.getMessage());
        }

        JFrame frame = new JFrame("My To-Do List");
        frame.setSize(450, 500);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(240, 248, 255));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> todoList = new JList<>(listModel);
        todoList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(todoList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        loadTasks(listModel);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setOpaque(false);

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton addButton = new JButton("Add Task");
        addButton.setBackground(new Color(0, 120, 215));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setOpaque(false);

        JButton deleteButton = new JButton("Delete Selected Task");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);

        JButton clearButton = new JButton("Clear All Tasks");
        clearButton.setBackground(new Color(253, 126, 20));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);

        addButton.addActionListener(e -> {
            String task = taskInput.getText();
            if (!task.trim().isEmpty()) {
                listModel.addElement(task);
                taskInput.setText("");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedIndex = todoList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a task to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Clear All බොත්තම සඳහා ඉංග්‍රීසි පණිවිඩය
        clearButton.addActionListener(e -> {
            if (listModel.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No tasks to clear.", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to clear all tasks?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    listModel.clear();
                }
            }
        });

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        topPanel.add(deleteButton);
        topPanel.add(clearButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks(listModel);
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    private static void loadTasks(DefaultListModel<String> listModel) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                listModel.addElement(line);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }

    private static void saveTasks(DefaultListModel<String> listModel) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < listModel.getSize(); i++) {
                writer.write(listModel.getElementAt(i));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }
}