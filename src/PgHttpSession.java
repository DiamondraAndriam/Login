package session;

import java.util.*;
import java.sql.*;
import java.sql.Date;

import javax.servlet.http.HttpSession;

import util.Util;

public class PgHttpSession {
    private String id;
    private HashMap<String, Object> attributes = new HashMap<>();
    private HashMap<String, Object> changedAttributes = new HashMap<>();
    private List<String> removedAttributes = new ArrayList<>();
    private Date creationTime;
    private Date lastAccessedTime;
    private int maxInactiveInterval;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        try {
            this.saveAttributes(null);
            this.attributes = attributes;
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public HashMap<String, Object> getChangedAttributes() {
        return changedAttributes;
    }

    public void setChangedAttributes(HashMap<String, Object> changedAttributes) {
        try {
            this.updateAttributes(null);
            this.changedAttributes = changedAttributes;
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public List<String> getRemovedAttributes() {
        return removedAttributes;
    }

    public void setRemovedAttributes(List<String> removedAttributes) {
        this.removedAttributes = removedAttributes;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public Object getAttribute(String key) {
        if (this.attributes == null) {
            if (this.changedAttributes == null) {
                return null;
            }
            return changedAttributes.get(key);
        }
        Object value = attributes.get(key);
        if (value == null && changedAttributes != null) {
            return changedAttributes.get(key);
        }
        return value;
    }

    public void setAttribute(String key, Object value) throws Exception {
        Connection connection = null;
        try {
            connection = Util.connect();
            try {
                this.saveAttribute(key, value, connection);
            } catch (Exception e) {
                try {
                    this.updateAttribute(key, value, connection);
                } catch (Exception e1) {
                    throw e1;
                }
            }
            this.getAttributes().put(key, value);
        } catch (Exception e2) {
            System.out.println("Error: " + e2.getMessage());
            e2.printStackTrace(System.out);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public Object getChangedAttribute(String key) {
        if (changedAttributes != null) {
            return changedAttributes.get(key);
        }
        return null;
    }

    public PgHttpSession(HttpSession session) {
        this.setId(session.getId());
        this.setCreationTime(new Date(session.getCreationTime()));
        this.setLastAccessedTime(new Date(session.getLastAccessedTime()));
        this.setMaxInactiveInterval(session.getMaxInactiveInterval());
        try {
            this.save();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public PgHttpSession(String id) {
        this.setId(id);
        try {
            this.findById(id);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public PgHttpSession() {
    }

    public void save() throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Util.connect();
            statement = connection.prepareStatement(
                    "Insert into pgsession(creationTime,lastAccessedTime,maxInactiveInterval,id) values(?,?,?,?)");
            statement.setDate(1, this.getCreationTime());
            statement.setDate(2, this.getLastAccessedTime());
            statement.setInt(3, this.getMaxInactiveInterval());
            statement.setString(4, this.getId());
            statement.executeUpdate();
            this.saveAttributes(connection);
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    public PgHttpSession findById(String id) throws Exception {
        PgHttpSession session = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            connection = Util.connect();
            statement = connection.prepareStatement("Select * from Facture");
            result = statement.executeQuery();
            while (result.next()) {
                session = new PgHttpSession();
                session.setId(result.getString("id"));
                session.setCreationTime(result.getDate("creationTime"));
                session.setLastAccessedTime(result.getDate("lastAccessedTime"));
                session.setMaxInactiveInterval(result.getInt("maxInactiveInterval"));
            }
            session.findAttributes(connection);
        } catch (Exception e) {
            throw e;
        } finally {
            if (result != null) {
                result.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return session;
    }

    public void findAttributes(Connection connection) throws Exception {
        boolean newConnect = false;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            if (connection == null) {
                connection = Util.connect();
                newConnect = true;
            }
            statement = connection
                    .prepareStatement("Select attr_name,attr_value from pgsession_attribute where idsession = ?");
            statement.setString(1, this.getId());
            result = statement.executeQuery();
            while (result.next()) {
                this.getAttributes().put(result.getString("attr_name"), Util.tObject(result.getString("attr_value")));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (result != null) {
                result.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null && newConnect == true) {
                connection.close();
            }
        }
    }

    public void update() throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Util.connect();
            statement = connection.prepareStatement(
                    "Update pgsession creationTime = ? , lastAccessedTime = ?, maxInactiveInterval = ? where id = ? ");
            statement.setDate(1, this.getCreationTime());
            statement.setDate(2, this.getLastAccessedTime());
            statement.setInt(3, this.getMaxInactiveInterval());
            statement.setString(4, this.getId());
            this.updateAttributes(connection);
            statement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void destroy() throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Util.connect();
            statement = connection.prepareStatement("Delete from pgsession where id = ? cascade");
            statement.setString(1, this.getId());
            statement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    // si l'on n'utilise pas la fonction setAttribute mais setAttributes
    public void saveAttributes(Connection connection) throws Exception {
        boolean newConnect = false;
        try {
            if (connection == null) {
                connection = Util.connect();
                newConnect = true;
            }
            for (String key : this.getAttributes().keySet()) {
                saveAttribute(key, this.getAttribute(key), connection);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null && newConnect == true) {
                connection.close();
            }
        }
    }

    public void saveAttribute(String name, Object value, Connection connection) throws Exception {
        PreparedStatement statement = null;
        boolean newConnect = false;
        try {
            if (connection == null) {
                connection = Util.connect();
                newConnect = true;
            }
            statement = connection.prepareStatement(
                    "Insert into pgsession_attribute(idsession,attr_name,attr_value) values(?,?,?)");
            statement.setString(1, this.getId());
            statement.setString(2, name);
            statement.setString(3, Util.toJson(value));
            this.saveAttributes(connection);
            statement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    // si l'on n'utilise pas la fonction setAttribute mais setChangedAttributes
    public void updateAttributes(Connection connection) throws Exception {
        boolean newConnect = false;
        try {
            if (connection == null) {
                connection = Util.connect();
                newConnect = true;
            }
            for (String key : this.getChangedAttributes().keySet()) {
                updateAttribute(key, this.getChangedAttribute(key), connection);
            }
            for (String key : this.getRemovedAttributes()) {
                deleteAttribute(key, connection);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null && newConnect == true) {
                connection.close();
            }
        }
    }

    public void updateAttribute(String name, Object value, Connection connection) throws Exception {
        PreparedStatement statement = null;
        boolean newConnect = false;
        try {
            if (connection == null) {
                connection = Util.connect();
                newConnect = true;
            }
            statement = connection.prepareStatement(
                    "Update pgsession_attribute set attr_value = ? where id = ? and attr_name = ?");
            statement.setString(1, Util.toJson(value));
            statement.setString(2, this.getId());
            statement.setString(3, name);
            statement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null && newConnect == true) {
                connection.close();
            }
        }
    }

    public void deleteAttribute(String name, Connection connection) throws Exception {
        PreparedStatement statement = null;
        boolean newConnect = false;
        try {
            if (connection == null) {
                connection = Util.connect();
                newConnect = true;
            }
            statement = connection.prepareStatement(
                    "Delete from pgsession_attribute where id = ? and attr_name = ?");
            statement.setString(1, this.getId());
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null && newConnect == true) {
                connection.close();
            }
        }
    }
}