package voting.system.example.demo.dto;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service("amazonSES")
public class AmazonSES {
    private final AmazonSimpleEmailService amazonSimpleEmailService;

    public AmazonSES(AmazonSimpleEmailService amazonSimpleEmailService) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
    }

    final String FROM = "tetteyabigail6@gmail.com";

    final String SUBJECT = "One last step to complete your registration";

    final String PASSWORD_RESET_REQUEST_SUBJECT = "Welcome to Leads Tracker - Reset Your Password to Get Started";

    //HTML for the body of the email
    final String HTMLBODY = "<h1>Please verify your email address</h1>"
            + "<p>Thank you for registering with us. To complete registration process and be able to log in, "
            + " click on the following link: "
            + "<a href=http://localhost:8080/verification-service/email-verification.html?token=$tokenValue>"
            + "Final step to complete your registration</a>" + "<br/><br/>"
            + "Thank you! And we are waiting for you inside!";

    final String TEXTBODY = "Please verify your email address. "
            + "Thank you registering with our mobile app. To complete registration process and be able to log in, "
            + " open then the following URL in your browser window: "
            + " http://localhost:8080/verification-service/email-verification.html?token=$tokenValue"
            + " Thank you! And we are waiting for you inside!";

    final String PASSWORD_RESET_REQUEST_HTMLBODY = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;\">" +
            "<h2 style=\"color: #2c3e50;\">Password Reset Request</h2>" +
            "<p>Hi <strong>$firstName</strong>,</p>" +

            "<p>We received a request to reset your password for your account on <strong>Voting System</strong>. " +
            "If you didn’t make this request, you can safely ignore this email.</p>" +

            "<p>If you did request a password reset, click the link below to set a new password:</p>" +

            "<p style=\"text-align: center;\">" +
            "<a href=\"http://localhost:4200/authentication/ResettingPasswordComponent\" " +
            "style=\"display: inline-block; padding: 12px 20px; background-color: #007bff; color: #fff; " +
            "text-decoration: none; border-radius: 5px;\">" +
            "Reset Password" +
            "</a>" +
            "</p>" +

            "<p>This link will expire in 15 minutes for your security.</p>" +

            "<p>If you have any questions, feel free to contact our support team.</p>" +

            "<p style=\"margin-top: 30px;\">Thank you,<br/>The Voting System Team</p>" +
            "</div>";

    //The email body for recipients with non-HTML email clients
    final String PASSWORD_RESET_REQUEST_TEXTBODY =  "A request to reset your password.\n\n" +
            "Hi, $firstName!\n\n" +
            "Someone has requested to reset your password for your account on Voting System. " +
            "If this wasn’t you, please ignore this message.\n\n" +
            "Otherwise, please open the link below in your browser to set a new password:\n\n" +
            "http://localhost:4200/authentication/ResettingPasswordComponent\n\n" +
            "This link will expire in 15 minutes for your security.\n\n" +
            "Thank you,\n" +
            "The Leads Tracker Team";

    // OTP Email Subject
    final String LOGIN_OTP_SUBJECT = "Your One-Time Password (OTP) for Voting System";

    // HTML body for OTP email
    final String LOGIN_OTP_HTMLBODY = "<h1 style=\"color: #2c3e50;\">Your Login OTP</h1>"
            + "<p>Hi, $firstName!</p>"
            + "<p>Your One-Time Password (OTP) for login is: <strong>$otp</strong></p>"
            + "<p>This OTP is valid for 3 minutes. Do not share it with anyone.</p>"
            + "<br/><p>Thank you,<br/>Voting System Team</p>";

    // Plain text body for OTP email
    final String LOGIN_OTP_TEXTBODY = "Your Login OTP\n\n"
            + "Hi, $firstName!\n\n"
            + "Your One-Time Password (OTP) for login is: $otp\n\n"
            + "This OTP is valid for 3 minutes. Do not share it with anyone.\n\n"
            + "Thank you,\nVoting System Team";


    public void sendPasswordResetRequest(String firstName, String email, String token) {
        boolean returnUser = false;

//        String htmlBodyWithToken = PASSWORD_RESET_REQUEST_HTMLBODY.replace("$tokenValue", token);
        String htmlBodyWithName = PASSWORD_RESET_REQUEST_HTMLBODY.replace("$firstName", firstName).replace("$tokenValue", token);
        String textBodyWithToken = PASSWORD_RESET_REQUEST_TEXTBODY.replace("$firstName", firstName).replace("$tokenValue", token);

        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(email))
                .withMessage(new Message().withBody(new Body().withHtml(new Content()
                                .withCharset("UTF-8").withData(htmlBodyWithName)).withText(new Content()
                                .withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8")
                                .withData(PASSWORD_RESET_REQUEST_SUBJECT))).withSource(FROM);

        SendEmailResult result = amazonSimpleEmailService.sendEmail(request);
        if (result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty())) {
            returnUser = true;
        }

    }


    public void sendLoginOtpEmail(String firstName, String email, String otp) {

        // Replacing placeholders
        String htmlBody = LOGIN_OTP_HTMLBODY.replace("$firstName", firstName).replace("$otp", otp);

        String textBody = LOGIN_OTP_TEXTBODY.replace("$firstName", firstName).replace("$otp", otp);

        // Build and send email
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(email)).withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8")
                                .withData(htmlBody)).withText(new Content()
                                .withCharset("UTF-8").withData(textBody)))
                        .withSubject(new Content()
                                .withCharset("UTF-8")
                                .withData(LOGIN_OTP_SUBJECT))).withSource(FROM);

        amazonSimpleEmailService.sendEmail(request);
        System.out.println("OTP sent to " + email);
    }


    public void sendSimpleEmail(String to, String subject, String body) {
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(to))
                .withMessage(new Message()
                        .withSubject(new Content().withCharset("UTF-8").withData(subject))
                        .withBody(new Body()
                                .withText(new Content().withCharset("UTF-8").withData(body))))
                .withSource(FROM);

        amazonSimpleEmailService.sendEmail(request);
    }


    public void sendOnboardingEmail(String email, String fullName) {
        String subject = "Welcome to Leads Tracker - Your Account Details";

        String body = String.format(
                "Hello %s,\n\n" +
                        "Welcome to the Voting system! Your account has been created successfully.\n\n" +
                        "You can log in using the following credentials:\n" +
                        "Email: %s\n" +
                        "Password: %s\n\n" +
                        "Please use the provided password to log in.\n\n" +
                        "Regards,\nVoting System Admin Team",
                fullName, email
        );

        sendSimpleEmail(email, subject, body);
    }


}
