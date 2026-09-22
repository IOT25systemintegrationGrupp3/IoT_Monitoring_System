package com.iotmonitoring.integration_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import java.time.Duration;

@Configuration
public class SoapClientConfig {

    @Bean
    public Jaxb2Marshaller alarmMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.iotmonitoring.integration_service.alarm.ws");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate alarmWebServiceTemplate(Jaxb2Marshaller alarmMarshaller) {
        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofSeconds(5));
        sender.setReadTimeout(Duration.ofSeconds(10));

        WebServiceTemplate template = new WebServiceTemplate(alarmMarshaller);
        template.setMarshaller(alarmMarshaller);
        template.setUnmarshaller(alarmMarshaller);
        template.setMessageSender(sender);
        return template;
    }
}
