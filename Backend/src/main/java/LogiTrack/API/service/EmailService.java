package LogiTrack.API.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarPasswordTemporal(String destinatario, String passwordTemporal) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("LogiTrack - Recuperación de Contraseña");
        mensaje.setText("Hola,\n\n" +
                "Tu nueva contraseña temporal es: " + passwordTemporal + "\n\n" +
                "Por favor, inicia sesión y cámbiala lo antes posible.\n\n" +
                "Atentamente,\nEl equipo de LogiTrack");

        mailSender.send(mensaje);
    }
}