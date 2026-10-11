package com.mycompany.students;


import javax.swing.*;
import java.awt.*;

public class LoginSelection extends JFrame {

    public LoginSelection() {

        setTitle(
                "Student Monitoring and Management System"
        );

        setSize(620, 480);

        setLocationRelativeTo(null);
        UITheme.styleWindow(this);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLayout(new BorderLayout());

        JPanel hero = UITheme.createHeroPanel(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JLabel title =
                new JLabel("STUDENT PORTAL");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle =
                new JLabel("Monitoring & Management System");
        subtitle.setForeground(new Color(220, 241, 239));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel heroText = new JPanel(new GridLayout(2, 1, 0, 5));
        heroText.setOpaque(false);
        heroText.add(title);
        heroText.add(subtitle);
        hero.add(heroText, BorderLayout.CENTER);
        add(hero, BorderLayout.NORTH);

        JPanel panel =
                new JPanel(
                        new GridLayout(3, 1, 12, 12)
                );

        panel.setBorder(
                UITheme.cardBorder()
        );
        panel.setBackground(UITheme.SURFACE);

        JButton studentButton =
                new JButton("Login as Student");

        JButton teacherButton =
                new JButton("Login as Teacher");

        JLabel prompt = new JLabel(
                "Choose how you would like to continue",
                SwingConstants.CENTER
        );
        UITheme.styleSubtitle(prompt);

        UITheme.styleButton(studentButton, true);
        UITheme.styleButton(teacherButton, false);

        studentButton.addActionListener(e -> {

            dispose();

            new StudentLogin().setVisible(true);
        });

        teacherButton.addActionListener(e -> {

            dispose();

            new TeacherLogin().setVisible(true);
        });

        panel.add(prompt);
        panel.add(studentButton);
        panel.add(teacherButton);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(28, 72, 36, 72));
        content.setBackground(UITheme.BACKGROUND);
        content.add(panel);
        add(content, BorderLayout.CENTER);
    }
}
