package se.nackademin.iot.alarm;

import java.util.Properties;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {

        MessageDispatcherServlet servlet =
                new MessageDispatcherServlet();

        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);

        return new ServletRegistrationBean<>(
                servlet,
                "/ws/*"
        );
    }

    @Bean(name = "alarm")
    public Wsdl11Definition alarmWsdl() {

        return new SimpleWsdl11Definition(
                new ClassPathResource(
                        "wsdl/alarm_v1.wsdl"
                )
        );
    }

    @Bean
    public SoapFaultMappingExceptionResolver exceptionResolver() {

        SoapFaultMappingExceptionResolver resolver =
                new SoapFaultMappingExceptionResolver();

        SoapFaultDefinition faultDefinition =
                new SoapFaultDefinition();

        faultDefinition.setFaultCode(
                SoapFaultDefinition.CLIENT
        );

        resolver.setDefaultFault(faultDefinition);

        Properties mappings = new Properties();

        mappings.setProperty(
                IllegalArgumentException.class.getName(),
                SoapFaultDefinition.CLIENT.toString()
        );

        resolver.setExceptionMappings(mappings);
        resolver.setOrder(1);

        return resolver;
    }
}