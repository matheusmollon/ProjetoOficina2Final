/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JDBC;

import Entitys.Measure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Felipe
 */
public class MeasureDAO {
    
    public void insert(Measure measure, Connection dataBaseConnection) throws SQLException{
       if( dataBaseConnection.isValid(1)){
        String sqlQuerry = "INSERT INTO Medida (temperatura,humidade,data,hora) VALUES(?,?,now(),(now()+interval 4 hour));";
        PreparedStatement preparedState = dataBaseConnection.prepareStatement(sqlQuerry);
        preparedState.setInt(1, measure.getTemperature());
        preparedState.setInt(2, measure.getHumidity());
        preparedState.executeUpdate();
        System.out.println("Dados salvos com sucesso!");
       }
       else{
           System.out.println("conexão caralhuda fechou!!!");
       }
    }

    public MeasureDAO() {
    }
    
    
}
