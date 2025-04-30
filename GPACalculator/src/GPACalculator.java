import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GPACalculator extends JFrame {
    private JTextField nameField, idField;
    private JComboBox<String> storageComboBox;
    private List<CoursePanel> coursePanels = new ArrayList<>();
    private JPanel coursesPanel;
    private JButton addCourseBtn, calculateBtn, saveBtn;
    private JTextArea reportArea;

    public GPACalculator() {
        setTitle("GPA Calculator");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JPanel studentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        studentPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        studentPanel.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        studentPanel.add(nameField);
        studentPanel.add(new JLabel("ID:"));
        idField = new JTextField(10);
        studentPanel.add(idField);
        
        JPanel storagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        storagePanel.setBorder(BorderFactory.createTitledBorder("Save Options"));
        storagePanel.add(new JLabel("Save to:"));
        String[] storageOptions = {"File (gpa_records.txt)", "Database (gpadb)"};
        storageComboBox = new JComboBox<>(storageOptions);
        storagePanel.add(storageComboBox);
        
        topPanel.add(studentPanel);
        topPanel.add(storagePanel);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        coursesPanel = new JPanel();
        coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.Y_AXIS));
        JScrollPane coursesScroll = new JScrollPane(coursesPanel);
        coursesScroll.setBorder(BorderFactory.createTitledBorder("Courses"));
        coursesScroll.setPreferredSize(new Dimension(600, 200));
        
        reportArea = new JTextArea(8, 50);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createTitledBorder("GPA Report"));
        
        centerPanel.add(coursesScroll, BorderLayout.CENTER);
        centerPanel.add(reportScroll, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addCourseBtn = new JButton("Add Course");
        calculateBtn = new JButton("Calculate GPA");
        saveBtn = new JButton("Save Data");
        saveBtn.setEnabled(false);
        
        buttonPanel.add(addCourseBtn);
        buttonPanel.add(calculateBtn);
        buttonPanel.add(saveBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        addCourseBtn.addActionListener(e -> addCoursePanel());
        calculateBtn.addActionListener(e -> calculateGPA());
        saveBtn.addActionListener(e -> saveData());

        add(mainPanel);
        addCoursePanel(); 
    }

    private void addCoursePanel() {
        CoursePanel coursePanel = new CoursePanel();
        coursePanels.add(coursePanel);
        coursesPanel.add(coursePanel);
        coursesPanel.revalidate();
    }

    private void calculateGPA() {
        try {
            if (nameField.getText().trim().isEmpty() || idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter student name and ID", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (coursePanels.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please add at least one course", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double totalPoints = 0;
            int totalCredits = 0;
            StringBuilder report = new StringBuilder();
            report.append("COURSE          CREDITS  GRADE  POINTS\n");
            report.append("--------------------------------------\n");

            for (CoursePanel panel : coursePanels) {
                try {
                    String course = panel.getCourseName();
                    if (course.isEmpty()) {
                        throw new IllegalArgumentException("Course name cannot be empty");
                    }
                    
                    int credits = Integer.parseInt(panel.getCreditHours());
                    if (credits <= 0) {
                        throw new IllegalArgumentException("Credits must be positive");
                    }
                    
                    String grade = panel.getGrade();
                    double points = getGradePoints(grade);
                    double coursePoints = points * credits;
                    
                    totalPoints += coursePoints;
                    totalCredits += credits;
                    
                    report.append(String.format("%-15s %5d    %-4s   %5.1f\n", 
                        course.length() > 14 ? course.substring(0, 11) + "..." : course,
                        credits, grade, coursePoints));
                        
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error in course: " + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double gpa = totalPoints / totalCredits;
            DecimalFormat df = new DecimalFormat("0.00");
            
            report.append("\nSUMMARY:\n");
            report.append(String.format("Total Courses: %d\n", coursePanels.size()));
            report.append(String.format("Total Credits: %d\n", totalCredits));
            report.append(String.format("GPA: %s\n", df.format(gpa)));
            report.append("Status: ").append(getGPAStatus(gpa));
            
            reportArea.setText(report.toString());
            saveBtn.setEnabled(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Calculation error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double getGradePoints(String grade) {
        switch (grade) {
            case "A": return 4.0;
            case "B+": return 3.5;
            case "B": return 3.0;
            case "C+": return 2.5;
            case "D": return 1.5;
            case "E": return 1.0;
            case "F": return 0.0;
            default: throw new IllegalArgumentException("Invalid grade");
        }
    }

    private String getGPAStatus(double gpa) {
        if (gpa >= 3.5) return "Excellent";
        if (gpa >= 3.0) return "Good";
        if (gpa >= 2.0) return "Satisfactory";
        return "Needs Improvement";
    }

    private void saveData() {
        String selectedOption = (String) storageComboBox.getSelectedItem();
        if (selectedOption.equals("File (gpa_records.txt)")) {
            saveToFile();
        } else {
            JOptionPane.showMessageDialog(this, "Database: Under Construction", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveToFile() {
        try (FileWriter writer = new FileWriter("gpa_records.txt", true)) {
            writer.write("\n=== GPA RECORD ===\n");
            writer.write("Student: " + nameField.getText() + "\n");
            writer.write("ID: " + idField.getText() + "\n");
            writer.write(reportArea.getText());
            writer.write("\n=================\n");
            
            JOptionPane.showMessageDialog(this, "Data saved to gpa_records.txt", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GPACalculator().setVisible(true));
    }
}

class CoursePanel extends JPanel {
    private JTextField courseField, creditsField;
    private JComboBox<String> gradeCombo;

    public CoursePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(BorderFactory.createEtchedBorder());
        
        courseField = new JTextField(12);
        add(new JLabel("Course:"));
        add(courseField);
        
        creditsField = new JTextField(3);
        add(new JLabel("Credits:"));
        add(creditsField);
        
        String[] grades = {"A", "B+", "B", "C+", "D", "E", "F"};
        gradeCombo = new JComboBox<>(grades);
        gradeCombo.setPreferredSize(new Dimension(50, 25));
        add(new JLabel("Grade:"));
        add(gradeCombo);
        
        JButton removeBtn = new JButton("×");
        removeBtn.setMargin(new Insets(0, 2, 0, 2));
        removeBtn.addActionListener(e -> {
            Container parent = getParent();
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        });
        add(removeBtn);
    }

    public String getCourseName() {
        return courseField.getText().trim();
    }

    public String getCreditHours() {
        return creditsField.getText().trim();
    }

    public String getGrade() {
        return (String) gradeCombo.getSelectedItem();
    }
}