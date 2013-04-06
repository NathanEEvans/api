package com.stormcloud.ide.api.core.dao;

import com.stormcloud.ide.api.core.dao.exception.StormcloudDaoException;
import com.stormcloud.ide.api.core.entity.*;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.model.user.Coder;
import com.stormcloud.ide.model.user.UserInfo;
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
            coder.setCountry(user.getInfo(UserInfo.COUNTRY));
            coder.setEmailAddress(user.getInfo(UserInfo.EMAIL_ADDRESS));
            coder.setFullName(user.getInfo(UserInfo.FULL_NAME));
            coder.setGravatar(user.getInfo(UserInfo.GRAVATAR));
            coder.setHomeTown(user.getInfo(UserInfo.CITY));
            coder.setJoined(user.getInfo(UserInfo.JOINED));
            coder.setUserName(user.getUserName());
            coder.setLastSeen(user.getLastLogin());

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
        result.setInfo(UserInfo.GRAVATAR, createGravatarUrl(result.getInfo(UserInfo.EMAIL_ADDRESS)));

        return result;
    }

    @Override
    public void save(User user) {

        manager.merge(user);

    }

    @Override
    public void delete(User user) {

        manager.remove(user);
    }

    @Override
    public void savePreference(String key, String value) {

        Query query = manager.createQuery("Select p From Preference p Where p.user.id = :userId and p.key = :key");

        query.setParameter("userId", RemoteUser.get().getId());
        query.setParameter("key", key);

        Preference preference = (Preference) query.getSingleResult();

        preference.setValue(value);

        manager.merge(preference);
    }

    @Override
    public void saveInfo(String key, String value) {

        Query query = manager.createQuery("Select i From Info i Where i.user.id = :userId and i.key = :key");

        query.setParameter("userId", RemoteUser.get().getId());
        query.setParameter("key", key);

        Info info = (Info) query.getSingleResult();

        info.setValue(value);

        manager.merge(info);
    }

    @Override
    public String changePassword(String currentPassword, String newPassword)
            throws StormcloudDaoException {

        LOG.info("Change password for User [" + RemoteUser.get().getUserName() + "]");

        User result;

        Query query = manager.createQuery("SELECT u FROM User u WHERE u.userName = :userName");

        query.setParameter("userName", RemoteUser.get().getUserName());

        result = (User) query.getSingleResult();


        String currentHash = md5Hash(currentPassword);
        String newHash = md5Hash(newPassword);

        if (result.getPassword().equals(currentHash)) {
            result.setPassword(newHash);
        } else {
            return "Current password incorrect.";
        }

        return "0";
    }

    public String md5Hash(String input)
            throws StormcloudDaoException {

        String original = input;

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(original.getBytes());

            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder("");

            for (byte b : digest) {
                sb.append(Integer.toHexString(b & 0xff));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
            throw new StormcloudDaoException(e);
        }
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
