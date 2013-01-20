package com.stormcloud.ide.model.factory;

import com.stormcloud.ide.model.factory.exception.MavenModelFactoryException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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

            JAXBContext context =
                    JAXBContext.newInstance(
                    "org.apache.maven.pom._4_0");

            Unmarshaller u = context.createUnmarshaller();

            Reader reader = new FileReader(file);

            pom = (Model) ((JAXBElement) u.unmarshal(reader)).getValue();

        } catch (FileNotFoundException e) {
            throw new MavenModelFactoryException(e);
        } catch (JAXBException e) {
            throw new MavenModelFactoryException(e);
        }

        return pom;
    }
}
