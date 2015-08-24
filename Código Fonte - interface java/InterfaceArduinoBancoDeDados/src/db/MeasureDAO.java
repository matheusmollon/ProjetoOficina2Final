/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import entity.Measure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Felipe
 */
public class MeasureDAO {

    public void insert(Measure measure, Connection conn) throws SQLException {
        String sqlQuerry = "INSERT INTO Medida (temperatura,humidade,data,hora) VALUES(?,?,now(),(now()+interval 4 hour));";
        PreparedStatement preparedState = conn.prepareStatement(sqlQuerry);
        preparedState.setInt(1, measure.getTemperature());
        preparedState.setInt(2, measure.getHumidity());
        preparedState.executeUpdate();
    }
}
