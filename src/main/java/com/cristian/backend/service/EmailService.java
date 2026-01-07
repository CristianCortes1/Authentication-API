package com.cristian.backend.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    public void sendEmail(String to, String subject, String body, boolean isHtml) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hello@demomailtrap.co");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml); // 👈 CLAVE

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando email", e);
        }
    }



    private String buildVerificationEmailHtml(String username, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .container {
                            background-color: #f9f9f9;
                            border-radius: 10px;
                            padding: 30px;
                            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align: center;
                            color: #4CAF50;
                            margin-bottom: 30px;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            background-color: #4CAF50;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                        }
                        .button:hover {
                            background-color: #45a049;
                        }
                        .footer {
                            margin-top: 30px;
                            font-size: 12px;
                            color: #666;
                            text-align: center;
                        }
                        .token-box {
                            background-color: #e8f5e9;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 20px 0;
                            word-break: break-all;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Bienvenido a E-commerce App!</h1>
                        </div>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Gracias por registrarte en nuestra plataforma. Para completar tu registro y activar tu cuenta, por favor confirma tu dirección de email.</p>

                        <div style="text-align: center;">
                            <a href="%s" class="button">Confirmar mi Email</a>
                        </div>

                        <p>O copia y pega el siguiente enlace en tu navegador:</p>
                        <div class="token-box">
                            <code>%s</code>
                        </div>

                        <p><strong>Nota:</strong> Este enlace expirará en 24 horas por razones de seguridad.</p>

                        <div class="footer">
                            <p>Si no creaste esta cuenta, puedes ignorar este correo de forma segura.</p>
                            <p>&copy; 2026 E-commerce App. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username, verificationUrl, verificationUrl);
    }

    public void sendVerificationEmail(@NotBlank(message = "El email es requerido") @Email(message = "El email debe ser válido") String email, @NotBlank(message = "El nombre de usuario es requerido") String username, String verificationToken) {
        //cambiar la URL por la del frontend cuando esté en producción
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + verificationToken;
        String subject = "Confirma tu cuenta - E-commerce App";

        sendEmail(email, subject, buildVerificationEmailHtml(username, verificationUrl),true);


    }

    public void sendWelcomeEmail(@NotBlank(message = "El email es requerido") @Email(message = "El email debe ser válido") String email, @NotBlank(message = "El nombre de usuario es requerido") String username) {
        String subject = "¡Cuenta verificada exitosamente!";
        sendEmail(email, subject, buildWelcomeEmailHtml(username),true);

    }

    private String buildWelcomeEmailHtml(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .container {
                            background-color: #f9f9f9;
                            border-radius: 10px;
                            padding: 30px;
                            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align: center;
                            color: #4CAF50;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 ¡Cuenta Verificada!</h1>
                        </div>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Tu cuenta ha sido verificada exitosamente. Ya puedes disfrutar de todos los beneficios de nuestra plataforma.</p>
                        <p>¡Gracias por unirte a nosotros!</p>
                        <p style="margin-top: 30px;">Saludos,<br><strong>El equipo de E-commerce App</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }
}

