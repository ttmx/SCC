package scc;

public class Env {
    static public final String BLOB_CONN_STRING = System.getenv("storageAccountConnectionString");
    static public final String DB_URI = System.getenv("mongoConnectionString");
    static public final String DB_NAME = System.getenv("DB_NAME");
    static public final String REDIS_HOSTNAME = System.getenv("REDIS_HOSTNAME");
    static public final String REDIS_KEY = System.getenv("REDIS_KEY");
}
