/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Felipe
 */
public class DBConnector {

    // Objeto de Connection
    private Connection conn = null;
    
    // Definições da conexão com o banco de dados
    private final String driver = "com.mysql.jdbc.Driver";
    private final String serverName = "p3plcpnl0206.prod.phx3.secureserver.net";
    private final String user = "apoo_endo";
    private final String password = "Aquaticchinchila1";
    
    // Flag que indica o status da conexão
    private Boolean status = false;

    public boolean isConnected() {
        return status;
    }

    public void openConnection() throws ClassNotFoundException, SQLException {        
        Class.forName(driver);
        String url = "jdbc:mysql://" + serverName + "/apoo";
        conn = DriverManager.getConnection(url, user, password);
        status = true;
    }

    public void closeConnection() throws ClassNotFoundException, SQLException {
        conn.close();
        status = false;
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
}
