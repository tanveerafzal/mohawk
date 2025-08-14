package jpl.hibernate.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.AbstractPostInsertGenerator;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.SequenceIdentityGenerator.NoCommentsInsert;
import org.hibernate.id.insert.AbstractReturningDelegate;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;

/**
 * A generator with immediate retrieval through JDBC3 {@link java.sql.Connection#prepareStatement(String, String[]) getGeneratedKeys}.
 * The value of the identity column must be set from a "before insert trigger"
 * <p/>
 * This generator only known to work with newer Oracle drivers compiled for
 * JDK 1.4 (JDBC3). The minimum version is 10.2.0.1
 * <p/>
 * Note: Due to a bug in Oracle drivers, sql comments on these insert statements
 * are completely disabled.
 *
 * @author Jean-Pol Landrain
 * see https://forum.hibernate.org/viewtopic.php?t=973262
 * and https://github.com/breath103/NewMadeleine/blob/master/src/jpl/hibernate/util/TriggerAssignedIdentityGenerator.java
 */
public class TriggerAssignedIdentityGenerator extends AbstractPostInsertGenerator {

    public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(PostInsertIdentityPersister persister, Dialect dialect, boolean isGetGeneratedKeysEnabled) throws HibernateException {
        return new Delegate(persister, dialect);
    }

    public static class Delegate extends AbstractReturningDelegate {
        private final Dialect dialect;

        private final String[] keyColumns;

        public Delegate(PostInsertIdentityPersister persister, Dialect dialect) {
            super(persister);
            this.dialect = dialect;
            this.keyColumns = getPersister().getRootTableKeyColumnNames();
            if (keyColumns.length > 1) {
                throw new HibernateException("trigger assigned identity generator cannot be used with multi-column keys");
            }
        }

        public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
            return new NoCommentsInsert(dialect);
        }

        protected PreparedStatement prepare(String insertSQL, SessionImplementor session) {
            return session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(insertSQL, this.keyColumns);
        }

        protected Serializable executeAndExtract(PreparedStatement insert, SessionImplementor session) throws SQLException {
            insert.executeUpdate();
            return IdentifierGeneratorHelper.getGeneratedIdentity(insert.getGeneratedKeys(), this.getPersister().getRootTableKeyColumnNames()[0], getPersister().getIdentifierType());
        }
    }
}