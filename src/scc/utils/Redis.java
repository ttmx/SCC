package scc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.entities.Session;
import scc.entities.exceptions.CacheException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Cookie;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static scc.Env.REDIS_HOSTNAME;
import static scc.Env.REDIS_KEY;

public class Redis {
    private static Redis redis;
    private static JedisPool instance;

    private final long COOKIE_EXPIRE = 3600;

    private static final String SESSION_PATH = "session/";
    private static final String TRENDING = "channels/trending/";


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
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            // TODO if cookie expires but user still sends it
            String a = jedis.get(SESSION_PATH + s);
            if (a == null) {
                throw new CacheException();
            }
            username = om.readValue(jedis.get(SESSION_PATH + s), String.class);
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
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            username = om.readValue(jedis.get(SESSION_PATH + sess.getValue()), String.class);
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
        if (!s.getUsername().equals(userId)) {
            throw new NotAuthorizedException("Inconsistent User : " + s.getUsername() + " " + userId);

        }

        try (Jedis jedis = Redis.getCachePool().getResource()) {
            jedis.expire(s.getUid(), COOKIE_EXPIRE);
        }

        return s;
    }

    public void addToTrendingList(String chanId) {
        ObjectMapper mapper = new ObjectMapper();
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            jedis.lpush(TRENDING, chanId);
            jedis.ltrim(TRENDING, 0, 100);
        }
    }

    public List<String> getTrending() {

        List<String> toReturn;
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            List<String> allChannels = jedis.lrange(TRENDING, 0, -1);
            toReturn = allChannels.stream()
                    .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingLong(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .limit(3)
                    .collect(Collectors.toList());
        }
        return toReturn;
    }
}
