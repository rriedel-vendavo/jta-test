package org.example;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Atomikos {
    public static void main(String[] args) throws SQLException, SystemException, NotSupportedException {
        System.out.println("Hello world!");


        DataSource dataSource = getAtomikosNonXADataSourceBean();

        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.init();
        userTransactionManager.setTransactionTimeout(300);
        userTransactionManager.setForceShutdown(true);


        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        jtaTransactionManager.setUserTransaction(userTransactionManager);

        UserTransaction utx = jtaTransactionManager.getUserTransaction();
        utx.begin();
        Connection conn = dataSource.getConnection();
        Statement s1 = conn.createStatement();
        s1.executeUpdate("update JTA_INV set balance = 40 where productid = 'ABC'");
        s1.close();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("Select balance from JTA_INV where productid = 'ABC'");
        rs.next();
        System.out.println("result not commited:" + rs.getBigDecimal(1));

        conn.close();
//        utx.commit();
        utx.rollback();

        utx = jtaTransactionManager.getUserTransaction();
        conn = dataSource.getConnection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("Select balance from JTA_INV where productid = 'ABC'");
        rs.next();
        System.out.println("result:" + rs.getBigDecimal(1));
        conn.close();
    }

    private static AtomikosNonXADataSourceBean getAtomikosNonXADataSourceBean() {
        AtomikosNonXADataSourceBean ads = new AtomikosNonXADataSourceBean();
        ads.setDriverClassName("oracle.jdbc.OracleDriver");
        ads.setUser(System.getenv("JTA_USERNAME"));
        ads.setPassword(System.getenv("JTA_PASSWORD"));
        ads.setUrl(System.getenv("JTA_URL"));
        ads.setUniqueResourceName("mydb");
        ads.setLocalTransactionMode(true);
        return ads;
    }
}