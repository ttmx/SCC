package scc;

public class Env {
    static public final String DB_URI = System.getenv("mongoConnectionString");
    static public final String DB_NAME = System.getenv("DB_NAME");
    static public final String REDIS_HOSTNAME = System.getenv("REDIS_HOSTNAME");
    static public final String BLOB_PATH = System.getenv("BLOB_PATH");
}
