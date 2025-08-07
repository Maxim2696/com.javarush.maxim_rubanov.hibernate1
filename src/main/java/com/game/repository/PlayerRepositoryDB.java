package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .addProperties(getHibernateProperties())
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()){
            NativeQuery<Player> nativeQuery = session.createNativeQuery("select * from rpg.player OFFSET :pageNumber LIMIT :pageSize",  Player.class);
            nativeQuery.setParameter("pageNumber", pageNumber *  pageSize);
            nativeQuery.setParameter("pageSize", pageSize);
//            Query<Player> query = session.createQuery("select p FROM Player p", Player.class);
//            query.setFirstResult(pageNumber * pageSize);
//            query.setMaxResults(pageSize);
            return nativeQuery.getResultList();
        }
    }
    @Override
    public int getAllCount() {
        int result = 0;
        try (Session session = sessionFactory.openSession()){
            Query<Long> query = session.createQuery("select count(p) FROM Player p", Long.class);
            result = Math.toIntExact(query.getSingleResult());
        }
        return result;
    }

    @Override
    public Player save(Player player) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.persist(player);
            transaction.commit();
            return player;
        }
        catch (Exception e) {
            throw  new RuntimeException();
        }
    }

    @Override
    public Player update(Player player) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.update(player);
            transaction.commit();
            return player;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()){
            Player player = session.find(Player.class, id);
           return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.remove(player);
            session.flush();
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
        }
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.put(Environment.URL, "jdbc:p6spy:postgresql://localhost:5432/");
        properties.put(Environment.USER, "postgres");
        properties.put(Environment.PASS, "postgres");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.FORMAT_SQL, "true");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");

        return properties;
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}