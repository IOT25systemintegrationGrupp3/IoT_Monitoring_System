package se.nackademin.iot.alarm.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import se.nackademin.iot.alarm.exception.AlarmServiceException;

@Configuration
public class SoapFaultConfig {

    @Bean
    public SoapFaultMappingExceptionResolver exceptionResolver() {

        SoapFaultMappingExceptionResolver resolver =
                new SoapFaultMappingExceptionResolver();

        SoapFaultDefinition defaultFault =
                new SoapFaultDefinition();

        defaultFault.setFaultCode(
                SoapFaultDefinition.SERVER
        );

        resolver.setDefaultFault(defaultFault);

        Properties mappings =
                new Properties();

        mappings.setProperty(
                IllegalArgumentException.class.getName(),
                SoapFaultDefinition.CLIENT.toString()
        );

        mappings.setProperty(
                AlarmServiceException.class.getName(),
                SoapFaultDefinition.SERVER.toString()
        );

        resolver.setExceptionMappings(mappings);
        resolver.setOrder(1);

        return resolver;
    }
}