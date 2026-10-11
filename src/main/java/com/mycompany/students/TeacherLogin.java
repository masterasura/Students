package com.mycompany.students;


import javax.swing.*;
import java.awt.*;

public class TeacherLogin extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public TeacherLogin() {

        setTitle("Teacher Login");

        setSize(520, 430);

        setLocationRelativeTo(null);
        UITheme.styleWindow(this);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLayout(new BorderLayout());

        JPanel hero = UITheme.createHeroPanel(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));

        JLabel title =
                new JLabel("TEACHER LOGIN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));

        JLabel subtitle = new JLabel("Sign in to manage student records.");
        subtitle.setForeground(new Color(220, 241, 239));
        subtitle.setFont(UITheme.BODY_FONT);
        JPanel heroText = new JPanel(new GridLayout(2, 1, 0, 5));
        heroText.setOpaque(false);
        heroText.add(title);
        heroText.add(subtitle);
        hero.add(heroText, BorderLayout.CENTER);
        add(hero, BorderLayout.NORTH);

        JPanel panel =
                new JPanel(
                        new GridBagLayout()
                );
        UITheme.styleCard(panel);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(10, 10, 10, 10);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Enter your teacher credentials"), gbc);
        UITheme.styleSubtitle((JLabel) panel.getComponent(0));

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(
                new JLabel("Username:"),
                gbc
        );

        txtUsername =
                new JTextField(20);
        UITheme.styleField(txtUsername);

        gbc.gridx = 1;

        panel.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;

        panel.add(
                new JLabel("Password:"),
                gbc
        );

        txtPassword =
                new JPasswordField(20);
        UITheme.styleField(txtPassword);

        gbc.gridx = 1;

        panel.add(txtPassword, gbc);

        JButton loginButton =
                new JButton("Login");

        JButton backButton =
                new JButton("Back");

        UITheme.styleButton(loginButton, true);
        UITheme.styleButton(backButton, false);

        gbc.gridx = 0;
        gbc.gridy = 3;

        panel.add(loginButton, gbc);

        gbc.gridx = 1;

        panel.add(backButton, gbc);

        loginButton.addActionListener(e ->
                login()
        );

        backButton.addActionListener(e -> {

            dispose();

            new LoginSelection()
                    .setVisible(true);
        });

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UITheme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 42, 32, 42));
        content.add(panel);
        add(content, BorderLayout.CENTER);
    }

    private void login() {

        String username =
                txtUsername
                        .getText()
                        .trim();

        String password =
                new String(
                        txtPassword
                                .getPassword()
                );

        if (username.isEmpty()
                || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter username and password."
            );

            return;
        }

        String teacherName =
                StudentManager.loginTeacher(
                        username,
                        password
                );

        if (teacherName != null) {

            dispose();

            new TeacherDashboard(
                    teacherName
            ).setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid teacher username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
