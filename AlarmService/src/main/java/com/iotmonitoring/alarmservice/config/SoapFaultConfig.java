package com.iotmonitoring.alarmservice.config;

import com.iotmonitoring.alarmservice.exception.AlarmNotFoundException;
import com.iotmonitoring.alarmservice.exception.AlarmValidationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.util.Properties;

@Configuration
public class SoapFaultConfig {

    private static final String NAMESPACE = "http://iotmonitoring.com/alarm";

    @Bean
    public SoapFaultMappingExceptionResolver soapFaultResolver() {
        SoapFaultMappingExceptionResolver resolver =
                new SoapFaultMappingExceptionResolver() {
                    @Override
                    protected void customizeFault(
                            Object endpoint, Exception exception, SoapFault fault) {
                        if (exception instanceof AlarmNotFoundException notFound) {
                            addDetail(fault, "ALARM_NOT_FOUND", notFound.getMessage());
                        } else if (exception instanceof AlarmValidationException validation) {
                            addDetail(fault, validation.getErrorCode(), validation.getMessage());
                        }
                    }

                    private void addDetail(SoapFault fault, String errorCode, String message) {
                            try {
                            var detail = fault.addFaultDetail()
                                .addFaultDetailElement(new QName(NAMESPACE, "AlarmFault"));
                            var document = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder().newDocument();
                            var root = document.createElementNS(NAMESPACE, "AlarmNotFoundFault");
                            root.appendChild(element(document, "errorCode", errorCode));
                            root.appendChild(element(document, "message", message));
                            document.appendChild(root);
                            TransformerFactory.newInstance().newTransformer()
                                .transform(new DOMSource(document), detail.getResult());
                            } catch (Exception detailException) {
                            throw new IllegalStateException(
                                "Unable to create AlarmNotFoundFault detail", detailException);
                            }
                    }

                        private org.w3c.dom.Element element(
                            org.w3c.dom.Document document, String name, String value) {
                        var element = document.createElementNS(NAMESPACE, name);
                        element.setTextContent(value);
                        return element;
                        }
                };

        Properties mappings = new Properties();
        mappings.setProperty(AlarmNotFoundException.class.getName(), "SERVER");
        mappings.setProperty(AlarmValidationException.class.getName(), "CLIENT");
        resolver.setExceptionMappings(mappings);
        resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return resolver;
    }
}