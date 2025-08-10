package com.supplychain.scfapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 🔽 Décommente ces imports SEULEMENT si tu actives l'option EMAIL RÉEL
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // ============================================================
    // === OPTION 1 : FAKE EMAIL (LOG CONSOLE) — PAR DÉFAUT     ===
    // ============================================================
    // 👉 Laisse CE bloc décommenté, et commente le bloc "Option 2".
    // Aucun bean JavaMailSender requis.

    public void sendInvitationEmail(String to, String username, String password) {
        String subject = "SCF - Invitation";
        String body = "Bonjour,\n\nVotre compte a été créé.\n" +
                "Identifiant : " + username + "\n" +
                "Mot de passe temporaire : " + password + "\n\n" +
                "Veuillez vous connecter et changer votre mot de passe.\n";
        log.info("[FAKE EMAIL] To: {} | Subject: {} | Body:\n{}", to, subject, body);
    }

    public void sendResetEmail(String to, String resetLink) {
        String subject = "SCF - Réinitialisation de mot de passe";
        String body = "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" + resetLink;
        log.info("[FAKE EMAIL] To: {} | Subject: {} | Body:\n{}", to, subject, body);
    }

    // ============================================================
    // === OPTION 2 : EMAIL RÉEL (JavaMailSender) — MANUEL       ===
    // ============================================================
    // 👉 Pour activer l'envoi RÉEL :
    //  1) COMMANDE le bloc FAKE ci-dessus (les 2 méthodes).
    //  2) DÉCOMMANDE les imports, le champ mailSender et les méthodes ci-dessous.
    //  3) Ajoute spring-boot-starter-mail dans le pom.xml si absent.
    //  4) Configure spring.mail.* dans application.properties.

    // private final JavaMailSender mailSender;
    //
    // public EmailService(JavaMailSender mailSender) {
    //     this.mailSender = mailSender;
    // }
    //
    // public void sendInvitationEmail(String to, String username, String password) {
    //     String subject = "SCF - Invitation";
    //     String body = "Bonjour,\n\nVotre compte a été créé.\n" +
    //             "Identifiant : " + username + "\n" +
    //             "Mot de passe temporaire : " + password + "\n\n" +
    //             "Veuillez vous connecter et changer votre mot de passe.\n";
    //     SimpleMailMessage msg = new SimpleMailMessage();
    //     msg.setTo(to);
    //     msg.setSubject(subject);
    //     msg.setText(body);
    //     mailSender.send(msg);
    // }
    //
    // public void sendResetEmail(String to, String resetLink) {
    //     String subject = "SCF - Réinitialisation de mot de passe";
    //     String body = "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" + resetLink;
    //     SimpleMailMessage msg = new SimpleMailMessage();
    //     msg.setTo(to);
    //     msg.setSubject(subject);
    //     msg.setText(body);
    //     mailSender.send(msg);
    // }
}
