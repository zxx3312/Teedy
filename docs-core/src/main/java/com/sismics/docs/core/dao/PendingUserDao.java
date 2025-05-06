package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.PendingUser;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class PendingUserDao {

    public void create(PendingUser pendingUser) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(pendingUser);
    }

    public PendingUser getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(PendingUser.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<PendingUser> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.createQuery("SELECT pu FROM PendingUser pu", PendingUser.class).getResultList();
    }

    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        PendingUser pendingUser = getById(id);
        if (pendingUser != null) {
            em.remove(pendingUser);
        }
    }
}