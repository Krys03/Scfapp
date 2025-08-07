package com.supplychain.scfapp.service;

// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // private final JavaMailSender mailSender;

    // public EmailService(JavaMailSender mailSender) {
    //     this.mailSender = mailSender;
    // }

    public void sendInvitationEmail(String to, String username, String password) {
        // --- VERSION FAKE ---
        System.out.println("=== INVITATION FAKE ===");
        System.out.println("Destinataire : " + to);
        System.out.println("Identifiant : " + username);
        System.out.println("Mot de passe : " + password);
        System.out.println("=======================");

        // --- VERSION REELLE ---
        /*
        String subject = "Votre accès à la plateforme SCF";
        String text = String.format(
            "Bonjour,\n\nVotre compte a été créé sur la plateforme SCF.\n\nIdentifiants de connexion :\nUsername: %s\nPassword: %s\n\nMerci de vous connecter et de changer votre mot de passe immédiatement.",
            username, password
        );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        */
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        // --- VERSION FAKE ---
        System.out.println("=== RESET PASSWORD FAKE ===");
        System.out.println("Destinataire : " + to);
        System.out.println("Lien : " + resetLink);
        System.out.println("===========================");

        // --- VERSION REELLE ---
        /*
        String subject = "Réinitialisation de votre mot de passe SCF";
        String text = String.format(
            "Bonjour,\n\nVous avez demandé la réinitialisation de votre mot de passe.\nCliquez sur ce lien pour choisir un nouveau mot de passe :\n%s\n\nSi vous n'avez pas fait cette demande, ignorez cet email.",
            resetLink
        );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        */
    }
}
