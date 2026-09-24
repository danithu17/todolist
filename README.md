# 🍎 Apple Reminders Clone (Java Swing)

A modern desktop To-Do and Task Management application built using **Java Swing**, inspired by the clean **macOS Apple Reminders** interface.

---

## ✨ Features

- **macOS Inspired Design**: Minimalist desktop UI with custom sidebar, widget cards, and rounded containers.
- **Dynamic Status Widgets**: Live counters that automatically calculate task counts for **All**, **Active**, **Urgent**, and **Completed**.
- **Smart Tag Detection**: Automatically extracts hashtags (e.g., `#urgent`, `#zapier`) from task titles, displays them in iOS Blue, and adds dynamic sidebar filter categories.
- **Task Management**:
  - Add tasks with custom tags.
  - One-click completion toggle with circular indicators.
  - Delete individual tasks.
  - Strikethrough style for completed items.
- **File Persistence**: Seamlessly reads and writes tasks to a local `tasks.txt` file on app start and close.

---

## 🛠️ Tech Stack

- **Language**: Java (JDK 17+)
- **UI Framework**: Java Swing & AWT (Graphics2D custom rendering)
- **Storage**: Local File I/O (`BufferedReader` / `BufferedWriter`)

---

## 🚀 How to Run

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/your-username/apple-reminders-clone.git](https://github.com/your-username/apple-reminders-clone.git)
   cd apple-reminders-clone
