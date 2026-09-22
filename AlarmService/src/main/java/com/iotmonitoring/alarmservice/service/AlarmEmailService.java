package com.iotmonitoring.alarmservice.service;

import com.iotmonitoring.alarmservice.model.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlarmEmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(AlarmEmailService.class);

    private final JavaMailSender mailSender;
    private final String mailUsername;
    private final String mailTo;

    public AlarmEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.to:}") String mailTo) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
        this.mailTo = mailTo;
    }

    public void sendAlarmNotification(Alarm alarm) {
        if (mailUsername.isBlank() || mailTo.isBlank()) {
            logger.error(
                    "Alarm email was not sent because MAIL_USERNAME or MAIL_TO is not configured. alarmId={}",
                    alarm.getAlarmId()
            );
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(mailTo);
        message.setSubject("IoT alarm created: " + alarm.getMessage());
        message.setText("Alarm ID: " + alarm.getAlarmId() + "\n"
                + "Device ID: " + alarm.getDeviceId() + "\n"
                + "Measurement type: " + alarm.getMeasurementType() + "\n"
                + "Measured value: " + alarm.getValue() + "\n"
                + "Unit: " + alarm.getUnit() + "\n"
                + "Timestamp: " + alarm.getTimestamp() + "\n"
                + "Status: " + alarm.getStatus() + "\n"
                + "Severity: " + alarm.getSeverity() + "\n"
                + "Message: " + alarm.getMessage());

        try {
            mailSender.send(message);
            logger.info("Alarm email sent successfully. alarmId={}, recipient={}",
                    alarm.getAlarmId(), mailTo);
        } catch (RuntimeException exception) {
            logger.error("Alarm email delivery failed. alarmId={}",
                    alarm.getAlarmId(), exception);
        }
    }
}