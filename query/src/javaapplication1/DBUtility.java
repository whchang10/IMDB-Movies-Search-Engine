package javaapplication1;
import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;
import java.util.Scanner;


public class DBUtility {
    
    public static Connection connectDB() throws SQLException, ClassNotFoundException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        String host = "localhost";
        String port = "1521";
        String dbName = "xe";
        String userName = "COEN280";
        String password = "COEN280";
        String dbURL = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
        return DriverManager.getConnection(dbURL, userName, password);
    }

    public static void closeConnection(Connection conDB) {
        try {
            conDB.close();
        } catch (SQLException e) {
            System.err.println("Cannot close connection: " + e.getMessage());
        }
    }


    public static ResultSet searchAllTuples(Connection con, String Query) throws SQLException {
        Statement stmt = con.createStatement();

        return stmt.executeQuery(Query);
    }

    public static void showMetaDataOfResultSet(ResultSet result) throws SQLException {
        ResultSetMetaData meta = result.getMetaData();
        for (int col = 1; col <= meta.getColumnCount(); col++) {
            System.out.println("Column: " + meta.getColumnName(col) + "\t, Type: " + meta.getColumnTypeName(col));
        }
    }

    public static void showResultSet(ResultSet result) throws SQLException {
        ResultSetMetaData meta = result.getMetaData();
        //int tupleCount = 1;
        while (result.next()) {
            //System.out.print("Tuple " + tupleCount++ + " : ");
            //System.out.print("Tuple " + tupleCount++ + " : ");
            for (int col = 1; col <= meta.getColumnCount(); col++) {

                System.out.print("\"" + result.getString(col) + "\",");
            }
            System.out.println();
         }
    }
}

