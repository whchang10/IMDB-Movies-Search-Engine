package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * Created by Wen-Han on 2/25/2017.
 */
public class Populate {

    private Connection conDB;
    private String path;

    Populate(){
        setPath();
    }

    public void setPath() {
        try {
            path = new File(".").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(path);
    }

    public void start() {
        try {
            conDB = connectDB();
            System.out.println("Success");
            insertMovies();
            insertMovieGenres();
            insertCountries();
            insertTags();
            insertMovieTags();
            insertFilmingContries();
        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
            closeConnection(conDB);
        }
    }

    private Connection connectDB() throws SQLException, ClassNotFoundException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        String host = "localhost";
        String port = "1521";
        String dbName = "xe";
        String userName = "COEN280";
        String password = "COEN280";
        String dbURL = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
        return DriverManager.getConnection(dbURL, userName, password);
    }

    private void closeConnection(Connection conDB) {
        try {
            conDB.close();
        } catch (SQLException e) {
            System.err.println("Cannot close connection: " + e.getMessage());
        }
    }

    private void deleteTable(String table) throws SQLException{
        Statement deleteStament = conDB.createStatement();
        System.out.println("Deleting previous tuples ...");
        deleteStament.executeUpdate("DELETE FROM " + table);
        deleteStament.close();
    }

    private void insertMovies() throws SQLException {

        try {
            BufferedReader br = new BufferedReader(new FileReader(path+"\\"+"movies.dat"));
            String [] colName;
            colName = br.readLine().split("\t");
            System.out.println(colName.length);
            deleteTable("MOVIES");

            System.out.println("Inserting Data ...");
            PreparedStatement preStmt = conDB.prepareStatement("INSERT INTO MOVIES VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            String rowline;
            while ((rowline = br.readLine()) != null) {
                System.out.println(rowline);
                String[] row = rowline.split("\t");
                for(int i = 0; i < colName.length; i++) {
                    if (row[i].contains("\\N")) {
                        preStmt.setString(i + 1, null);
                    }
                    else {
                        preStmt.setString(i + 1, row[i]);
                    }
                }
                preStmt.executeUpdate();
            }
            preStmt.close();
            System.out.println("Inserting Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertMovieGenres() throws SQLException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path+"\\"+"movie_genres.dat"));
            String [] colName;
            colName = br.readLine().split("\t");
            deleteTable("GENRES");

            System.out.println("Inserting Data ...");
            PreparedStatement preStmt = conDB.prepareStatement("INSERT INTO GENRES VALUES(?,?)");
            String rowline;
            while ((rowline = br.readLine()) != null) {
                System.out.println(rowline);
                String [] row = rowline.split("\t");
                for(int i = 0; i < colName.length; i++) {
                    preStmt.setString(i + 1, row[i]);
                }
                preStmt.executeUpdate();
            }
            preStmt.close();
            System.out.println("Inserting Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertCountries() throws SQLException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path+"\\"+"movie_countries.dat"));
            String [] colName;
            colName = br.readLine().split("\t");
            System.out.println(colName.length);
            deleteTable("COUNTRIES");

            System.out.println("Inserting Data ...");
            PreparedStatement prestmt = conDB.prepareStatement("INSERT INTO COUNTRIES VALUES(?,?)");
            String rowline;
            while ((rowline = br.readLine()) != null) {
                System.out.println(rowline);
                String[] row = rowline.split("\t");
                for(int i = 0; i < row.length; i++) {
                    prestmt.setString(i + 1, row[i]);
                }
                if(row.length < colName.length)
                    prestmt.setString(2, "None");
                prestmt.executeUpdate();
            }
            prestmt.close();
            System.out.println("Inserting Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertTags() throws SQLException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path+"\\"+"tags.dat"));
            String [] colName;
            colName = br.readLine().split("\t");
            System.out.println(colName.length);
            deleteTable("TAGS");

            System.out.println("Inserting Data ...");
            PreparedStatement preStmt = conDB.prepareStatement("INSERT INTO TAGS VALUES(?,?)");
            String rowLine;
            while ((rowLine = br.readLine()) != null) {
                System.out.println(rowLine);
                String[] row = rowLine.split("\t");
                for(int i = 0; i< colName.length; i++) {
                    preStmt.setString(i + 1, row[i]);
                }
                preStmt.executeUpdate();
            }
            preStmt.close();
            System.out.println("Inserting Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertMovieTags() throws SQLException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path+"\\"+"movie_tags.dat"));
            String [] colName;
            colName = br.readLine().split("\t");
            System.out.println(colName.length);
            deleteTable("MOVIE_TAGS");

            System.out.println("Inserting Data ...");
            PreparedStatement preStmt = conDB.prepareStatement("INSERT INTO MOVIE_TAGS VALUES(?,?,?)");
            String rowLine;
            while ((rowLine = br.readLine()) != null) {
                System.out.println(rowLine);
                String[] row = rowLine.split("\t");
                for(int i = 0; i < colName.length; i++) {
                    preStmt.setString(i + 1, row[i]);
                }
                preStmt.executeUpdate();
            }
            preStmt.close();
            System.out.println("Inserting Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertFilmingContries() throws SQLException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path+"\\"+"movie_locations.dat"));
            String [] colName;
            colName = br.readLine().split("\t");
            System.out.println(colName.length);
            deleteTable("FILMING_COUNTRIES");

            System.out.println("Inserting Data ...");
            String preMovieID = new String();
            String preCountry = new String();
            String none = "None";
            PreparedStatement preStmt = conDB.prepareStatement("INSERT INTO FILMING_COUNTRIES VALUES(?,?)");

            String rowLine;
            while ((rowLine = br.readLine()) != null) {
                System.out.println(rowLine);
                String[] row = rowLine.split("\t");
                String second = (row.length > 1)? row[1]: "None";
                if (!preMovieID.equals(row[0]) || !preCountry.equals(second)) {
                   preStmt.setString(1, row[0]);
                   preMovieID = row[0];
                   preStmt.setString(2, second);
                   preCountry = second;
                   preStmt.executeUpdate();
                }
            }
            preStmt.close();
            System.out.println("Inserting Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
