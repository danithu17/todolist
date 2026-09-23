import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

class Task implements Serializable {
    String title;
    boolean completed;

    public Task(String title, boolean completed) {
        this.title = title;
        this.completed = completed;
    }
}

public class Main {
    private static final String FILE_NAME = "tasks.txt";
    private static ArrayList<Task> tasksList = new ArrayList<>();
    private static JPanel taskPanelContainer;
    private static JFrame frame;

    public static void main(String[] args) {
        try {
            // Nimbus LookAndFeel භාවිතා කිරීමෙන් වර්ණ ගැටළුව විසඳා ඇත
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("Error changing look and feel: " + e.getMessage());
        }

        frame = new JFrame("My To-Do List");
        frame.setSize(550, 600);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(240, 248, 255));

        loadTasks();

        taskPanelContainer = new JPanel();
        taskPanelContainer.setLayout(new BoxLayout(taskPanelContainer, BoxLayout.Y_AXIS));
        taskPanelContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(taskPanelContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setOpaque(false);

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton addButton = new JButton("Add Task");
        addButton.setBackground(new Color(0, 120, 215));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        topPanel.setOpaque(false);

        JButton clearButton = new JButton("Clear All Tasks");
        clearButton.setBackground(new Color(253, 126, 20));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        topPanel.add(clearButton);

        addButton.addActionListener(e -> {
            String text = taskInput.getText();
            if (!text.trim().isEmpty()) {
                tasksList.add(new Task(text, false));
                taskInput.setText("");
                renderTasks();
            }
        });

        clearButton.addActionListener(e -> {
            if (tasksList.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No tasks to clear.", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to clear all tasks?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    tasksList.clear();
                    renderTasks();
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
                System.exit(0);
            }
        });

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        renderTasks();
        frame.setVisible(true);
    }

    private static void renderTasks() {
        taskPanelContainer.removeAll();

        for (int i = 0; i < tasksList.size(); i++) {
            Task task = tasksList.get(i);
            final int index = i;

            JPanel rowPanel = new JPanel(new BorderLayout(5, 5));
            rowPanel.setBackground(Color.WHITE);
            rowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(task.completed);
            checkBox.setBackground(Color.WHITE);
            checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            if (task.completed) {
                checkBox.setText("<html><strike>" + task.title + "</strike></html>");
                checkBox.setForeground(Color.GRAY);
            } else {
                checkBox.setText(task.title);
                checkBox.setForeground(Color.BLACK);
            }

            checkBox.addActionListener(e -> {
                task.completed = checkBox.isSelected();
                renderTasks();
            });

            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actionPanel.setBackground(Color.WHITE);

            // සංකේත වෙනුවට 'Edit' සහ 'Delete' වචන යොදා ඇත
            JButton editBtn = new JButton("Edit");
            editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            editBtn.setMargin(new Insets(2, 5, 2, 5));
            editBtn.setToolTipText("Edit Task");

            editBtn.addActionListener(e -> {
                String newTask = JOptionPane.showInputDialog(frame, "Edit your task:", task.title);
                if (newTask != null && !newTask.trim().isEmpty()) {
                    task.title = newTask;
                    renderTasks();
                }
            });

            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            deleteBtn.setMargin(new Insets(2, 5, 2, 5));
            deleteBtn.setForeground(Color.RED);
            deleteBtn.setToolTipText("Delete Task");

            deleteBtn.addActionListener(e -> {
                tasksList.remove(index);
                renderTasks();
            });

            actionPanel.add(editBtn);
            actionPanel.add(deleteBtn);

            rowPanel.add(checkBox, BorderLayout.CENTER);
            rowPanel.add(actionPanel, BorderLayout.EAST);

            taskPanelContainer.add(rowPanel);
        }

        taskPanelContainer.revalidate();
        taskPanelContainer.repaint();
    }

    private static void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                boolean completed = line.startsWith("[X] ");
                String title = completed ? line.substring(4) : line;
                tasksList.add(new Task(title, completed));
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }

    private static void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasksList) {
                if (task.completed) {
                    writer.write("[X] " + task.title);
                } else {
                    writer.write(task.title);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }
}