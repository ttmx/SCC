package scc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.entities.Session;
import scc.entities.exceptions.CacheException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Cookie;

import static scc.Env.REDIS_HOSTNAME;
import static scc.Env.REDIS_KEY;

public class Redis {
    private static Redis redis;
    private static JedisPool instance;

    private final long COOKIE_EXPIRE = 3600;

    private static final String SESSION_PATH = "session/";

    private Redis() {

    }

    public synchronized static JedisPool getCachePool() {
        if (instance != null)
            return instance;
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        instance = new JedisPool(poolConfig, REDIS_HOSTNAME, 6380, 1000, REDIS_KEY, true);
        return instance;
    }

    public synchronized static Redis getInstance() {
        if (redis == null)
            redis = new Redis();
        return redis;
    }

    public void putSession(Session sess) {

        ObjectMapper mapper = new ObjectMapper();
        String k = SESSION_PATH + sess.getUid();
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            jedis.set(k, mapper.writeValueAsString(sess.getUsername()));
            jedis.expire(k, COOKIE_EXPIRE);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public Session getSession(String s) throws CacheException {
        String username = null;
        ObjectMapper om = new ObjectMapper();
        try(Jedis jedis = Redis.getCachePool().getResource()){

            username = om.readValue(jedis.get(SESSION_PATH+s), String.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (username == null)
            throw new CacheException();
        return new Session(s, username);
    }

    public String getUserfromCookie(Cookie sess) throws NotAuthorizedException {
        String username = null;
        ObjectMapper om = new ObjectMapper();
        try(Jedis jedis = Redis.getCachePool().getResource()){
            username = om.readValue(jedis.get(SESSION_PATH+sess.getValue()),String.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (username == null || username.length() == 0)
            throw new NotAuthorizedException("No valid session initialized");
        return username;
    }


    public Session checkCookieUser(Cookie sessUidCookie, String userId)
            throws NotAuthorizedException {
        if (sessUidCookie == null || sessUidCookie.getValue() == null)
            throw new NotAuthorizedException("No session initialized");
        Session s;
        try {
            s = Redis.getInstance().getSession(sessUidCookie.getValue());
        } catch (CacheException e) {
            throw new NotAuthorizedException("No valid session initialized");
        }
        if (s == null || s.getUsername() == null || s.getUsername().length() == 0)
            throw new NotAuthorizedException("No valid session initialized");
        if (!s.getUsername().equals(userId)){
            throw new NotAuthorizedException("Inconsistent User : " + s.getUsername() + " " + userId);

        }

        Redis.getCachePool().getResource()
                .expire(s.getUid(), COOKIE_EXPIRE);
        return s;
    }
}
