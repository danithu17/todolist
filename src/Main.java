import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final Color bgColor;
    private final String title;
    private String count;

    public WidgetPanel(String title, String count, Color bgColor) {
        this.bgColor = bgColor;
        this.title = title;
        this.count = count;
        setOpaque(false);
        setPreferredSize(new Dimension(100, 70));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(title, 12, getHeight() - 15);

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString(count, getWidth() - 35, 28);

        g2.dispose();
    }
}

// iOS Style Custom List Cell Renderer for Sidebar
class IOSListCellRenderer extends JPanel implements ListCellRenderer<String> {
    private final JLabel label = new JLabel();
    private boolean isSelected;
    private boolean isCellHasFocus;

    public IOSListCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(6, 12, 6, 12));
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(label, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        this.isSelected = isSelected;
        this.isCellHasFocus = cellHasFocus;
        label.setText(value);
        label.setForeground(isSelected ? new Color(0, 113, 227) : new Color(50, 50, 50));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {
            g2.setColor(new Color(220, 228, 245)); // iOS selection blue tint
            g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 10, 10);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

public class Main {
    private static final String FILE_NAME = "tasks.txt";
    private static final ArrayList<Task> tasksList = new ArrayList<>();
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
    private static String selectedPriority = "None";

    // Apple macOS System Colors
    private static final Color MAC_BG = new Color(255, 255, 255);
    private static final Color SIDEBAR_BG = new Color(245, 245, 247);
    private static final Color APPLE_BLUE = new Color(0, 122, 255);
    private static final Color APPLE_RED = new Color(255, 59, 48);
    private static final Color APPLE_ORANGE = new Color(255, 149, 0);
    private static final Color APPLE_GRAY = new Color(142, 142, 147);
    private static final Color APPLE_PINK = new Color(255, 45, 85);
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to set Look and Feel", e);
        }

        frame = new JFrame("Reminders");
        frame.setSize(950, 680);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        loadTasks();

        // 1. Sidebar Panel (Apple Style)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(225, 225, 228)));

        JPanel widgetsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        widgetsGrid.setBackground(SIDEBAR_BG);
        widgetsGrid.setBorder(new EmptyBorder(20, 15, 15, 15));

        allWidget = new WidgetPanel("All", "0", APPLE_GRAY);
        activeWidget = new WidgetPanel("Today", "0", APPLE_BLUE);
        urgentWidget = new WidgetPanel("Urgent", "0", APPLE_RED);
        completedWidget = new WidgetPanel("Done", "0", APPLE_PINK);

        // Widget Click Listeners
        allWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentFilter = "All Tasks";
                mainTitleLabel.setText("All Tasks");
                sideList.clearSelection();
                renderTasks();
            }
        });

        activeWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentFilter = "Today";
                mainTitleLabel.setText("Today");
                sideList.clearSelection();
                renderTasks();
            }
        });

        urgentWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentFilter = "Urgent";
                mainTitleLabel.setText("Urgent");
                sideList.clearSelection();
                renderTasks();
            }
        });

        completedWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentFilter = "Completed";
                mainTitleLabel.setText("Completed");
                sideList.clearSelection();
                renderTasks();
            }
        });

        widgetsGrid.add(allWidget);
        widgetsGrid.add(activeWidget);
        widgetsGrid.add(urgentWidget);
        widgetsGrid.add(completedWidget);

        sidebarListModel = new DefaultListModel<>();
        sideList = new JList<>(sidebarListModel);
        sideList.setBackground(SIDEBAR_BG);
        sideList.setCellRenderer(new IOSListCellRenderer()); // iOS Style Renderer applied here
        sideList.setOpaque(false);
        sideList.setBorder(new EmptyBorder(5, 8, 5, 8));
        sideList.setFixedCellHeight(36);

        sideList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && sideList.getSelectedValue() != null) {
                currentFilter = sideList.getSelectedValue().replace("🏷️ ", "").replace("📁 ", "").trim();
                mainTitleLabel.setText(currentFilter);
                renderTasks();
            }
        });

        JPanel sidebarTop = new JPanel(new BorderLayout());
        sidebarTop.setBackground(SIDEBAR_BG);
        sidebarTop.add(widgetsGrid, BorderLayout.NORTH);

        JScrollPane listScroll = new JScrollPane(sideList);
        listScroll.setBorder(null);
        listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);
        sidebarTop.add(listScroll, BorderLayout.CENTER);

        sidebar.add(sidebarTop, BorderLayout.CENTER);

        // 2. Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(MAC_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(MAC_BG);
        headerPanel.setBorder(new EmptyBorder(25, 30, 10, 30));

        mainTitleLabel = new JLabel(currentFilter);
        mainTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        mainTitleLabel.setForeground(APPLE_BLUE);

        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 215), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        searchField.setText("Search");
        searchField.setForeground(APPLE_GRAY);

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                if (searchField.getText().equals("Search")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent evt) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("Search");
                    searchField.setForeground(APPLE_GRAY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) { applySearch(); }
            public void removeUpdate(DocumentEvent evt) { applySearch(); }
            public void changedUpdate(DocumentEvent evt) { applySearch(); }

            private void applySearch() {
                String text = searchField.getText();
                searchQuery = text.equals("Search") ? "" : text.trim().toLowerCase();
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

        // Input Panel with Apple-Style Priority Buttons
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(MAC_BG);
        inputPanel.setBorder(new EmptyBorder(15, 30, 25, 30));

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font("SansSerif", Font.PLAIN, 15));
        taskInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 215), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        taskInput.setToolTipText("e.g. Finish physics homework #studies");

        JPanel priorityPickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        priorityPickerPanel.setBackground(MAC_BG);

        JToggleButton btnNone = createPriorityButton("None", APPLE_GRAY, true);
        JToggleButton btnLow = createPriorityButton("!", APPLE_BLUE, false);
        JToggleButton btnMed = createPriorityButton("!!", APPLE_ORANGE, false);
        JToggleButton btnHigh = createPriorityButton("!!!", APPLE_RED, false);

        ButtonGroup priorityGroup = new ButtonGroup();
        priorityGroup.add(btnNone);
        priorityGroup.add(btnLow);
        priorityGroup.add(btnMed);
        priorityGroup.add(btnHigh);

        btnNone.addActionListener(evt -> selectedPriority = "None");
        btnLow.addActionListener(evt -> selectedPriority = "Low");
        btnMed.addActionListener(evt -> selectedPriority = "Medium");
        btnHigh.addActionListener(evt -> selectedPriority = "High");

        priorityPickerPanel.add(btnNone);
        priorityPickerPanel.add(btnLow);
        priorityPickerPanel.add(btnMed);
        priorityPickerPanel.add(btnHigh);

        JButton addButton = new JButton("Add");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(APPLE_BLUE);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setBorder(new EmptyBorder(8, 16, 8, 16));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel rightInputControls = new JPanel(new BorderLayout(10, 0));
        rightInputControls.setBackground(MAC_BG);
        rightInputControls.add(priorityPickerPanel, BorderLayout.CENTER);
        rightInputControls.add(addButton, BorderLayout.EAST);

        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 8));
        bottomWrapper.setBackground(MAC_BG);
        bottomWrapper.add(taskInput, BorderLayout.NORTH);
        bottomWrapper.add(rightInputControls, BorderLayout.SOUTH);

        inputPanel.add(bottomWrapper, BorderLayout.CENTER);

        addButton.addActionListener(evt -> {
            String text = taskInput.getText();
            if (!text.trim().isEmpty()) {
                tasksList.add(new Task(text, false, selectedPriority));
                taskInput.setText("");
                selectedPriority = "None";
                btnNone.setSelected(true);
                updateDataAndUI();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
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
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JToggleButton createPriorityButton(String text, Color baseColor, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(42, 30));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(240, 240, 245));
        btn.setForeground(baseColor);
        btn.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 215), 1, true));
        return btn;
    }

    private static void updateDataAndUI() {
        int total = tasksList.size();
        int completed = 0;
        int urgent = 0;
        int activeToday = 0;
        HashSet<String> uniqueTags = new HashSet<>();

        for (Task task : tasksList) {
            if (task.completed) {
                completed++;
            } else {
                activeToday++;
            }
            if (task.title.toLowerCase().contains("#urgent") || "High".equalsIgnoreCase(task.priority)) urgent++;

            Matcher m = Pattern.compile("(#\\w+)").matcher(task.title);
            while (m.find()) {
                uniqueTags.add(m.group(1));
            }
        }

        allWidget.setCount(String.valueOf(total));
        completedWidget.setCount(String.valueOf(completed));
        urgentWidget.setCount(String.valueOf(urgent));
        activeWidget.setCount(String.valueOf(activeToday));

        String previousSelection = sideList.getSelectedValue();
        sidebarListModel.clear();
        sidebarListModel.addElement("📁 All Tasks");
        sidebarListModel.addElement("📁 Completed");

        for (String tag : uniqueTags) {
            sidebarListModel.addElement("🏷️ " + tag);
        }

        if (previousSelection != null && sidebarListModel.contains(previousSelection)) {
            sideList.setSelectedValue(previousSelection, true);
        }

        renderTasks();
    }

    private static String formatTitle(Task task) {
        String escaped = task.title.replace("<", "&lt;").replace(">", "&gt;");
        String formatted = escaped.replaceAll("(#\\w+)", "<font color='#007AFF'>$1</font>");

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
        return "<html>" + priorityBadge + "<font color='#1C1C1E'>" + formatted + "</font></html>";
    }

    private static JPanel createActionPanel(int index) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setBackground(MAC_BG);

        JButton deleteBtn = new JButton("×");
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 18));
        deleteBtn.setForeground(new Color(190, 190, 195));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteBtn.addActionListener(evt -> {
            tasksList.remove(index);
            updateDataAndUI();
        });

        actionPanel.add(deleteBtn);
        return actionPanel;
    }

    private static void renderTasks() {
        taskPanelContainer.removeAll();

        for (int i = 0; i < tasksList.size(); i++) {
            Task task = tasksList.get(i);
            final int index = i;

            boolean matchesCategory = false;
            if (currentFilter.equals("All Tasks")) {
                matchesCategory = true;
            } else if (currentFilter.equals("Today")) {
                matchesCategory = !task.completed;
            } else if (currentFilter.equals("Completed")) {
                matchesCategory = task.completed;
            } else if (currentFilter.equals("Urgent")) {
                matchesCategory = task.title.toLowerCase().contains("#urgent") || "High".equalsIgnoreCase(task.priority);
            } else if (currentFilter.startsWith("#")) {
                matchesCategory = task.title.contains(currentFilter);
            }

            boolean matchesSearch = searchQuery.isEmpty() || task.title.toLowerCase().contains(searchQuery);

            if (!matchesCategory || !matchesSearch) continue;

            JPanel rowPanel = new JPanel(new BorderLayout(15, 10));
            rowPanel.setBackground(MAC_BG);
            rowPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(238, 238, 242)),
                    new EmptyBorder(12, 5, 12, 5)));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

            JButton checkBtn = new JButton(task.completed ? "●" : "○");
            checkBtn.setFont(new Font("SansSerif", Font.PLAIN, 20));
            checkBtn.setForeground(task.completed ? APPLE_PINK : APPLE_GRAY);
            checkBtn.setContentAreaFilled(false);
            checkBtn.setBorderPainted(false);
            checkBtn.setFocusPainted(false);
            checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel titleLabel = new JLabel(formatTitle(task));
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));

            checkBtn.addActionListener(evt -> {
                task.completed = !task.completed;
                updateDataAndUI();
            });

            rowPanel.add(checkBtn, BorderLayout.WEST);
            rowPanel.add(titleLabel, BorderLayout.CENTER);
            rowPanel.add(createActionPanel(index), BorderLayout.EAST);

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
        } catch (StringIndexOutOfBoundsException | IOException ignored) {}
    }
}