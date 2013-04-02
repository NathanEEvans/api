package com.stormcloud.ide.api.core.dao;

import com.stormcloud.ide.api.core.entity.*;
import com.stormcloud.ide.model.user.Coder;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author martijn
 */
@Transactional
public class StormCloudDao implements IStormCloudDao {

    private Logger LOG = Logger.getLogger(getClass());
    @PersistenceContext(unitName = "StormCloudPU")
    private EntityManager manager;

    @Override
    public List<Archetype> getCatalog() {

        Query query = manager.createQuery("Select a From Archetype a");

        @SuppressWarnings("unchecked")
        List<Archetype> archetypes = (List<Archetype>) query.getResultList();

        LOG.debug("Found " + archetypes.size() + " Archetypes");

        return archetypes;
    }

    @Override
    public Coder[] getCoders() {

        LOG.info("Get Coders");

        Query query = manager.createQuery("SELECT u FROM User u");

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) query.getResultList();

        List<Coder> coders = new LinkedList<Coder>();

        for (User user : users) {

            Coder coder = new Coder();
            coder.setCountry(user.getCountry());
            coder.setEmailAddress(user.getEmailAddress());
            coder.setFullName(user.getFullName());
            coder.setGravatar(user.getGravatar());
            coder.setHomeTown(user.getCity());
            coder.setJoined(user.getJoined());
            coder.setUserName(user.getUserName());

            coders.add(coder);
        }

        return coders.toArray(new Coder[users.size()]);
    }

    @Override
    public User getUser(String userName) {

        LOG.info("Get User for [" + userName + "]");

        User result;

        Query query = manager.createQuery("SELECT u FROM User u WHERE u.userName = :userName");

        query.setParameter("userName", userName);

        result = (User) query.getSingleResult();

        // add gravatar url
        result.setGravatar(createGravatarUrl(result.getEmailAddress()));

        return result;
    }

    @Override
    public void save(User user) {

        manager.persist(user);
    }

    @Override
    public void delete(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Classpath> searchJdkClasspath(String searchKey, int start, int count) {

        Query query = manager.createQuery("Select c From Classpath c Where c.javaClass Like :key Order by c.javaClass");

        String key = searchKey.replaceAll("\\*", "%");

        query.setParameter("key", key);
        query.setFirstResult(start);
        query.setMaxResults(count);

        @SuppressWarnings("unchecked")
        List<Classpath> result = (List<Classpath>) query.getResultList();

        return result;
    }

    private String createGravatarUrl(String email) {

        String url = "http://www.gravatar.com/avatar/" + md5Hex(email.toLowerCase());

        return url;
    }

    private String hex(byte[] array) {

        StringBuilder sb = new StringBuilder("");

        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1, 3));
        }

        return sb.toString();
    }

    private String md5Hex(String email) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("MD5");

            return hex(md.digest(email.getBytes("CP1252")));

        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);

        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }

        return null;
    }
}
