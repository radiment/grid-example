package com.epam.learning;

import org.apache.ignite.Ignite;
import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.jetbrains.annotations.NotNull;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.io.Serializable;
import java.sql.*;

public class CacheJdbcClientStore extends CacheStoreAdapter<Integer, Client> implements Serializable {

    @IgniteInstanceResource
    private Ignite ignite;

    @Override
    public Client load(Integer key) throws CacheLoaderException {
        try (Connection conn = connection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * from CLIENTS WHERE ID=?1")) {
                st.setInt(1, key);
                try (ResultSet resultSet = st.executeQuery()) {
                    if (resultSet.next()) {
                        return getClient(resultSet);
                    } else {
                        return null;
                    }
                }

            }
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load value \"" + key + "\" from cache store.", e);
        }
    }

    @Override
    public void write(Cache.Entry<? extends Integer, ? extends Client> entry) throws CacheWriterException {
        try (Connection conn = connection()) {
            try (PreparedStatement st = conn.prepareStatement("MERGE INTO CLIENTS (ID, BALANCE, TYPE) VALUES (?1, ?2, ?3)")) {
                Client client = entry.getValue();
                st.setInt(1, client.getId());
                st.setInt(2, client.getBalance());
                st.setString(3, client.getType());
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load values from cache store.", e);
        }
    }

    @Override
    public void delete(Object key) throws CacheWriterException {
        try (Connection conn = connection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE from CLIENTS WHERE ID=?1")) {
                st.setInt(1, (Integer) key);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load values from cache store.", e);
        }
    }

    @Override
    public void loadCache(IgniteBiInClosure<Integer, Client> clo, Object... args) {
        if (args == null || args.length == 0 || args[0] == null)
            throw new CacheLoaderException("Expected entry count parameter is not provided.");
        final int entryCnt = (Integer) args[0];

        try (Connection conn = connection()) {
            try (PreparedStatement st = conn.prepareStatement("select * from CLIENTS")) {
                try (ResultSet rs = st.executeQuery()) {
                    int cnt = 0;

                    while (cnt < entryCnt && rs.next()) {
                        Client person = getClient(rs);

                        clo.apply(person.getId(), person);

                        cnt++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load values from cache store.", e);
        }
    }

    @NotNull
    private Client getClient(ResultSet rs) throws SQLException {
        return new Client(rs.getInt(1), rs.getInt(2), rs.getString(3));
    }

    private Connection connection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:~/.h2/test;MV_STORE=FALSE;MVCC=FALSE", "sa", "");
        connection.setAutoCommit(true);
        return connection;
    }

}
