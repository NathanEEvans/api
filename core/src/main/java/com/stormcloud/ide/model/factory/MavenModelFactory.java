package com.stormcloud.ide.model.factory;

import com.stormcloud.ide.model.factory.exception.MavenModelFactoryException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.apache.maven.pom._4_0.Model;

/**
 *
 * @author martijn
 */
public class MavenModelFactory {

    /**
     * Translate a Pom.xml Maven porject file into the Maven Model object.
     *
     * @param file pom.xml file
     * @return Model
     * @throws MavenModelFactoryException
     */
    public static Model getProjectModel(File file)
            throws MavenModelFactoryException {

        Model pom = null;

        try {

            // the property placeholder map
            Map<String, String> tokens = new HashMap<String, String>(0);

            // get all properties from the pom file with stax
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            InputStream in = new FileInputStream(file);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            boolean isPropertiesSection = false;

            while (eventReader.hasNext()) {

                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()
                        && event.asStartElement().getName().getLocalPart().equals("properties")) {

                    // found the properties tag
                    isPropertiesSection = true;

                } else if (event.isStartElement() && isPropertiesSection) {

                    String token = event.asStartElement().getName().getLocalPart();

                    event = eventReader.nextEvent();
                    String value = event.asCharacters().getData();
                    tokens.put(token, value);

                } else if (event.isEndElement()
                        && event.asEndElement().getName().getLocalPart().equals("properties")) {

                    // end of the properties, stop here
                    break;
                }
            }

            ITokenResolver resolver = new MapTokenResolver(tokens);

            Reader source = new FileReader(file);

            Reader reader = new TokenReplacingReader(source, resolver);

            JAXBContext context =
                    JAXBContext.newInstance(
                    "org.apache.maven.pom._4_0");

            Unmarshaller u = context.createUnmarshaller();

            pom = (Model) ((JAXBElement) u.unmarshal(reader)).getValue();


        } catch (FileNotFoundException e) {
            throw new MavenModelFactoryException(e);
        } catch (JAXBException e) {
            throw new MavenModelFactoryException(e);
        } catch (XMLStreamException e) {
            throw new MavenModelFactoryException(e);
        }

        return pom;
    }
}
