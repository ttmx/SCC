package scc;

public class Env {
    static public final String BLOB_CONN_STRING = System.getenv("storageAccountConnectionString");
    static public final String DB_URI = System.getenv("mongoConnectionString");
    static public final String DB_NAME = System.getenv("DB_NAME");
}
