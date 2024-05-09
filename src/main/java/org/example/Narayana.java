package org.example;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.springframework.transaction.jta.JtaTransactionManager;


import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Narayana {

    public static void main(String[] args) throws SQLException {
        DataSource dataSource = getDbcpDataSourceBean();

        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(new TransactionManagerImple());
        jtaTransactionManager.setUserTransaction(new UserTransactionImple());

        ((BasicManagedDataSource)dataSource).setTransactionManager(jtaTransactionManager.getTransactionManager());

        Connection conn = dataSource.getConnection();

        UserTransaction utx = jtaTransactionManager.getUserTransaction();
        try {
            utx.begin();
            Statement s1 = conn.createStatement();
            s1.executeUpdate("update JTA_INV set balance = 70 where productid = 'ABC'");
            s1.close();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("Select balance from JTA_INV where productid = 'ABC'");
            rs.next();
            System.out.println("result not commited:" + rs.getBigDecimal(1));

//            utx.commit();
            utx.rollback();
            conn.close();
        } catch (Exception ex) {
            System.err.println(ex);
        }


        conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("Select balance from JTA_INV where productid = 'ABC'");
        rs.next();
        System.out.println("result:" + rs.getBigDecimal(1));
        conn.close();

    }

    private static DataSource getDbcpDataSourceBean() {
        BasicDataSource ads = new BasicManagedDataSource();
        ads.setDriverClassName("oracle.jdbc.OracleDriver");
        ads.setUsername(System.getenv("JTA_USERNAME"));
        ads.setPassword(System.getenv("JTA_PASSWORD"));
        ads.setUrl(System.getenv("JTA_URL"));
        return ads;
    }

}
