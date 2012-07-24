/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.test.integration.secondary;

import java.util.Arrays;
import java.util.Iterator;
import javax.persistence.EntityManager;

import org.junit.Test;

import org.hibernate.envers.test.BaseEnversJPAFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.hibernate.mapping.Join;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class BasicSecondary extends BaseEnversJPAFunctionalTestCase {
    private Integer id;

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { SecondaryTestEntity.class };
    }

    @Test
    @Priority(10)
    public void initData() {
        SecondaryTestEntity ste = new SecondaryTestEntity("a", "1");

        // Revision 1
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        em.persist(ste);

        em.getTransaction().commit();

        // Revision 2
        em.getTransaction().begin();

        ste = em.find(SecondaryTestEntity.class, ste.getId());
        ste.setS1("b");
        ste.setS2("2");

        em.getTransaction().commit();

        //

        id = ste.getId();
    }

    @Test
    public void testRevisionsCounts() {
        assert Arrays.asList(1, 2).equals(getAuditReader().getRevisions(SecondaryTestEntity.class, id));
    }

    @Test
    public void testHistoryOfId() {
        SecondaryTestEntity ver1 = new SecondaryTestEntity(id, "a", "1");
        SecondaryTestEntity ver2 = new SecondaryTestEntity(id, "b", "2");

        assert getAuditReader().find(SecondaryTestEntity.class, id, 1).equals(ver1);
        assert getAuditReader().find(SecondaryTestEntity.class, id, 2).equals(ver2);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testTableNames() {
        assert "secondary_AUD".equals(((Iterator<Join>)
                getCfg().getClassMapping("org.hibernate.envers.test.integration.secondary.SecondaryTestEntity_AUD")
                        .getJoinIterator())
                .next().getTable().getName());
    }
}