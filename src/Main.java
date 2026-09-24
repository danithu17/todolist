import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Task implements Serializable {
    String title;
    boolean completed;
    String priority; // None, Low, Medium, High

    public Task(String title, boolean completed, String priority) {
        this.title = title;
        this.completed = completed;
        this.priority = priority;
    }
}

class WidgetPanel extends JPanel {
    private Color bgColor;
    private String title;
    private String count;

    public WidgetPanel(String title, String count, Color bgColor) {
        this.bgColor = bgColor;
        this.title = title;
        this.count = count;
        setOpaque(false);
        setPreferredSize(new Dimension(100, 70));
    }

    public void setCount(String newCount) {
        this.count = newCount;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(title, 10, getHeight() - 15);

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString(count, getWidth() - 30, 25);

        g2.dispose();
    }
}

public class Main {
    private static final String FILE_NAME = "tasks.txt";
    private static ArrayList<Task> tasksList = new ArrayList<>();
    private static JPanel taskPanelContainer;
    private static JFrame frame;

    private static JLabel mainTitleLabel;
    private static DefaultListModel<String> sidebarListModel;
    private static JList<String> sideList;
    private static WidgetPanel allWidget;
    private static WidgetPanel completedWidget;
    private static WidgetPanel urgentWidget;
    private static WidgetPanel activeWidget;

    private static String currentFilter = "All Tasks";
    private static String searchQuery = "";

    private static final Color MAC_BG = new Color(255, 255, 255);
    private static final Color SIDEBAR_BG = new Color(242, 242, 247);

    private static final Color WIDGET_BLUE = new Color(44, 136, 255);
    private static final Color WIDGET_RED = new Color(255, 71, 71);
    private static final Color WIDGET_GRAY = new Color(99, 99, 102);
    private static final Color WIDGET_PINK = new Color(251, 73, 118);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Apple Reminders Clone");
        frame.setSize(900, 650);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        loadTasks();

        // 1. Sidebar Panel
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JPanel widgetsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        widgetsGrid.setBackground(SIDEBAR_BG);
        widgetsGrid.setBorder(new EmptyBorder(20, 15, 20, 15));

        allWidget = new WidgetPanel("All", "0", WIDGET_GRAY);
        activeWidget = new WidgetPanel("Active", "0", WIDGET_BLUE);
        urgentWidget = new WidgetPanel("Urgent", "0", WIDGET_RED);
        completedWidget = new WidgetPanel("Completed", "0", WIDGET_PINK);

        widgetsGrid.add(allWidget);
        widgetsGrid.add(activeWidget);
        widgetsGrid.add(urgentWidget);
        widgetsGrid.add(completedWidget);

        sidebarListModel = new DefaultListModel<>();
        sideList = new JList<>(sidebarListModel);
        sideList.setBackground(SIDEBAR_BG);
        sideList.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sideList.setForeground(Color.DARK_GRAY);
        sideList.setBorder(new EmptyBorder(0, 10, 0, 0));

        sideList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && sideList.getSelectedValue() != null) {
                currentFilter = sideList.getSelectedValue().replace("🏷️ ", "").replace("📋 ", "");
                mainTitleLabel.setText(currentFilter);
                renderTasks();
            }
        });

        JPanel sidebarTop = new JPanel(new BorderLayout());
        sidebarTop.setBackground(SIDEBAR_BG);
        sidebarTop.add(widgetsGrid, BorderLayout.NORTH);
        sidebarTop.add(new JScrollPane(sideList), BorderLayout.CENTER);

        sidebar.add(sidebarTop, BorderLayout.CENTER);

        // 2. Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(MAC_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(MAC_BG);
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));

        mainTitleLabel = new JLabel(currentFilter);
        mainTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        mainTitleLabel.setForeground(WIDGET_BLUE);

        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setText("Search...");
        searchField.setForeground(Color.GRAY);

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("Search...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySearch(); }
            public void removeUpdate(DocumentEvent e) { applySearch(); }
            public void changedUpdate(DocumentEvent e) { applySearch(); }

            private void applySearch() {
                String text = searchField.getText();
                searchQuery = text.equals("Search...") ? "" : text.trim().toLowerCase();
                renderTasks();
            }
        });

        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrapper.setBackground(MAC_BG);
        searchWrapper.add(searchField);

        headerPanel.add(mainTitleLabel, BorderLayout.WEST);
        headerPanel.add(searchWrapper, BorderLayout.EAST);

        taskPanelContainer = new JPanel();
        taskPanelContainer.setLayout(new BoxLayout(taskPanelContainer, BoxLayout.Y_AXIS));
        taskPanelContainer.setBackground(MAC_BG);
        taskPanelContainer.setBorder(new EmptyBorder(10, 30, 10, 30));

        JScrollPane scrollPane = new JScrollPane(taskPanelContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Input Panel (Priority Dropdown සහිතව)
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(MAC_BG);
        inputPanel.setBorder(new EmptyBorder(15, 30, 20, 30));

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font("SansSerif", Font.PLAIN, 16));
        taskInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        taskInput.setToolTipText("e.g. Finish physics homework #studies");

        // Priority Selector
        String[] priorities = {"None", "Low (!)", "Medium (!!)", "High (!!!)"};
        JComboBox<String> priorityBox = new JComboBox<>(priorities);
        priorityBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        priorityBox.setBackground(Color.WHITE);

        JButton addButton = new JButton("Add Task");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        addButton.setForeground(WIDGET_BLUE);
        addButton.setContentAreaFilled(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel rightInputControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightInputControls.setBackground(MAC_BG);
        rightInputControls.add(priorityBox);
        rightInputControls.add(addButton);

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(rightInputControls, BorderLayout.EAST);

        addButton.addActionListener(e -> {
            String text = taskInput.getText();
            if (!text.trim().isEmpty()) {
                String selectedPriority = (String) priorityBox.getSelectedItem();
                String cleanPriority = "None";
                if (selectedPriority.startsWith("High")) cleanPriority = "High";
                else if (selectedPriority.startsWith("Medium")) cleanPriority = "Medium";
                else if (selectedPriority.startsWith("Low")) cleanPriority = "Low";

                tasksList.add(new Task(text, false, cleanPriority));
                taskInput.setText("");
                priorityBox.setSelectedIndex(0);
                updateDataAndUI();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
                System.exit(0);
            }
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);

        updateDataAndUI();
        frame.setVisible(true);
    }

    private static void updateDataAndUI() {
        int total = tasksList.size();
        int completed = 0;
        int urgent = 0;
        HashSet<String> uniqueTags = new HashSet<>();

        for (Task task : tasksList) {
            if (task.completed) completed++;
            if (task.title.toLowerCase().contains("#urgent") || "High".equalsIgnoreCase(task.priority)) urgent++;

            Matcher m = Pattern.compile("(#\\w+)").matcher(task.title);
            while (m.find()) {
                uniqueTags.add(m.group(1));
            }
        }

        allWidget.setCount(String.valueOf(total));
        completedWidget.setCount(String.valueOf(completed));
        urgentWidget.setCount(String.valueOf(urgent));
        activeWidget.setCount(String.valueOf(total - completed));

        String previousSelection = sideList.getSelectedValue();
        sidebarListModel.clear();
        sidebarListModel.addElement("📋 All Tasks");
        sidebarListModel.addElement("📋 Completed");

        for (String tag : uniqueTags) {
            sidebarListModel.addElement("🏷️ " + tag);
        }

        if (previousSelection != null && sidebarListModel.contains(previousSelection)) {
            sideList.setSelectedValue(previousSelection, true);
        } else {
            sideList.setSelectedIndex(0);
        }

        renderTasks();
    }

    private static String formatTitle(Task task) {
        String escaped = task.title.replace("<", "&lt;").replace(">", "&gt;");
        String formatted = escaped.replaceAll("(#\\w+)", "<font color='#007AFF'>$1</font>");

        // Priority indicator (Apple Style)
        String priorityBadge = "";
        if ("High".equalsIgnoreCase(task.priority)) {
            priorityBadge = "<font color='#FF3B30'><b>!!! </b></font>";
        } else if ("Medium".equalsIgnoreCase(task.priority)) {
            priorityBadge = "<font color='#FF9500'><b>!! </b></font>";
        } else if ("Low".equalsIgnoreCase(task.priority)) {
            priorityBadge = "<font color='#007AFF'><b>! </b></font>";
        }

        if (task.completed) {
            return "<html><strike><font color='#8E8E93'>" + priorityBadge + formatted + "</font></strike></html>";
        }
        return "<html>" + priorityBadge + "<font color='#000000'>" + formatted + "</font></html>";
    }

    private static void renderTasks() {
        taskPanelContainer.removeAll();

        for (int i = 0; i < tasksList.size(); i++) {
            Task task = tasksList.get(i);
            final int index = i;

            boolean matchesCategory = false;
            if (currentFilter.equals("All Tasks")) {
                matchesCategory = true;
            } else if (currentFilter.equals("Completed")) {
                matchesCategory = task.completed;
            } else if (currentFilter.startsWith("#")) {
                matchesCategory = task.title.contains(currentFilter);
            }

            boolean matchesSearch = searchQuery.isEmpty() || task.title.toLowerCase().contains(searchQuery);

            if (!matchesCategory || !matchesSearch) continue;

            JPanel rowPanel = new JPanel(new BorderLayout(15, 10));
            rowPanel.setBackground(MAC_BG);
            rowPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    new EmptyBorder(15, 0, 15, 0)));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JButton checkBtn = new JButton(task.completed ? "🔘" : "⚪");
            checkBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            checkBtn.setForeground(task.completed ? WIDGET_BLUE : Color.LIGHT_GRAY);
            checkBtn.setContentAreaFilled(false);
            checkBtn.setBorderPainted(false);
            checkBtn.setFocusPainted(false);
            checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel titleLabel = new JLabel(formatTitle(task));
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

            checkBtn.addActionListener(e -> {
                task.completed = !task.completed;
                updateDataAndUI();
            });

            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            actionPanel.setBackground(MAC_BG);

            JButton deleteBtn = new JButton("🗑");
            deleteBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            deleteBtn.setForeground(Color.RED);
            deleteBtn.setContentAreaFilled(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            deleteBtn.addActionListener(e -> {
                tasksList.remove(index);
                updateDataAndUI();
            });

            actionPanel.add(deleteBtn);
            rowPanel.add(checkBtn, BorderLayout.WEST);
            rowPanel.add(titleLabel, BorderLayout.CENTER);
            rowPanel.add(actionPanel, BorderLayout.EAST);

            taskPanelContainer.add(rowPanel);
        }

        taskPanelContainer.add(Box.createVerticalGlue());
        taskPanelContainer.revalidate();
        taskPanelContainer.repaint();
    }

    private static void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                boolean completed = line.startsWith("[X] ");
                String raw = completed ? line.substring(4) : line;

                // Storage format: Priority|Title (e.g., High|Complete assignment)
                String priority = "None";
                String title = raw;
                if (raw.contains("|")) {
                    String[] parts = raw.split("\\|", 2);
                    priority = parts[0];
                    title = parts[1];
                }

                tasksList.add(new Task(title, completed, priority));
            }
        } catch (Exception ignored) {}
    }

    private static void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasksList) {
                String prefix = task.completed ? "[X] " : "";
                writer.write(prefix + task.priority + "|" + task.title);
                writer.newLine();
            }
        } catch (Exception ignored) {}
    }
}