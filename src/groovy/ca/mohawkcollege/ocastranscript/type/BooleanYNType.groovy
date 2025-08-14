package ca.mohawkcollege.ocastranscript.type

import org.hibernate.HibernateException
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.usertype.UserType

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

class BooleanYNType implements UserType {
    static final String TRUE = "Y"
    static final String FALSE = "N"

    /**
     * @return the SQL types of the elements of this UserType
     */
    @Override
    int[] sqlTypes() {
        return [Types.VARCHAR]
    }

    /**
     * The lengths corresponding to each SQL type
     */
    int[] typeLengths() {
        return [1]
    }

    /**
     * The Groovy type of properties using this UserType
     */
    @Override
    Class returnedClass() {
        return Boolean
    }

    /**
     * Convert a database value to a Groovy object
     *
     * @param resultSet
     * @param names
     * @param sessionImplementor
     * @param o
     * @return
     * @throws HibernateException
     * @throws SQLException
     */
    @Override
    Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, Object o) throws HibernateException, SQLException {
        // Get the value as a String
        def value = resultSet.getObject(names[0], String)

        switch (value) {
            case TRUE: return Boolean.TRUE
            case FALSE: return Boolean.FALSE
            case null: return null
            default: throw new InvalidObjectException("Invalid value [$value] found in column [${names[0]}]")
        }
    }

    /**
     * Convert a Groovy object to a database value
     *
     * @param preparedStatement
     * @param o
     * @param index
     * @param sessionImplementor
     * @throws HibernateException* @throws SQLException
     */
    @Override
    void nullSafeSet(PreparedStatement preparedStatement, Object o, int index, SessionImplementor sessionImplementor) throws HibernateException, SQLException {
        // Check if the object or its value is null
        if (o == null) {
            preparedStatement.setNull(index, sqlTypes()[0])
        } else {
            // Add the data to the statement
            preparedStatement.setObject(index, (o ? TRUE : FALSE), sqlTypes()[0], typeLengths()[0])
        }
    }

    @Override
    Object deepCopy(Object o) throws HibernateException {
        return o
    }

    @Override
    boolean isMutable() {
        return false
    }

    @Override
    Serializable disassemble(Object o) throws HibernateException {
        return o as Serializable
    }

    @Override
    Object assemble(Serializable serializable, Object o) throws HibernateException {
        return serializable
    }

    @Override
    Object replace(Object o, Object o1, Object o2) throws HibernateException {
        return o
    }

    /**
     * Overridden equals method.
     */
    @Override
    boolean equals(Object object1, Object object2) {
        // Check if the objects are null or if they are not of the same class
        if (object1?.getClass() != object2?.getClass())
            return false

        // Check if the objects are the same object
        if (object1.is(object2))
            return true

        return object1.equals(object2)
    }

    /**
     * Overridden hashCode method.
     */
    @Override
    int hashCode(Object object) {
        return (object != null ? object.hashCode() : 0)
    }
}
