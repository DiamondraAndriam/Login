package util;

import java.sql.Connection;
import java.sql.DriverManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
    public static Connection connect() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/test_session", "test", "test");
    }

    public static String toJson(Object objet) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(objet);
    }

    public static Object tObject(String jSon) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(jSon, Object.class);
    }
}