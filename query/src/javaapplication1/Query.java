/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Calendar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.eclipse.persistence.internal.oxm.Constants;

/**
 *
 * @author Wen-Han
 */
public class Query extends javax.swing.JFrame {
    private ArrayList<JCheckBox> genresCheckBox;
    private ArrayList<JCheckBox> countriesCheckBox;
    private ArrayList<JCheckBox> filmingCountriesCheckBox;
    private ArrayList<JCheckBox> tagValuseCheckBox;
    private String attributesRelation;
    
    public Query() {
        initComponents();
        genresCheckBox = new ArrayList<JCheckBox> ();
        countriesCheckBox = new ArrayList<JCheckBox> ();
        filmingCountriesCheckBox = new ArrayList<JCheckBox> ();
        tagValuseCheckBox = new ArrayList<JCheckBox> ();
        
        attributesRelation = "AND";
        loadInitialData();
    }
    
    private void loadInitialData(){
        Connection conDB = null;
        ResultSet result = null;
        try {
           conDB = DBUtility.connectDB();
           System.out.println("Success");
           String queryGenre = "SELECT DISTINCT GENRE\n" + "FROM GENRES\n";
           result = DBUtility.searchAllTuples(conDB, queryGenre);
           insertGenresCheckBox(result);
		
           String queryCountry = "SELECT DISTINCT COUNTRY\n" + "FROM COUNTRIES\n";
           result = DBUtility.searchAllTuples(conDB, queryCountry);
           insertCountriesCheckBox(result);
           
           String queryFilmingCountry = "SELECT DISTINCT COUNTRY\n" + "FROM FILMING_COUNTRIES\n";
           result = DBUtility.searchAllTuples(conDB, queryFilmingCountry);
           insertFilmingCountriesCheckBox(result);
        } catch (SQLException e) {
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch ( ClassNotFoundException e) {
            System.err.println("Cannot find the database driver");
        } finally {
           DBUtility.closeConnection(conDB);
        }
    }
    
    private void insertCheckBoxs(ResultSet result, JPanel panel, ArrayList<JCheckBox> checkBoxList, ActionListener actionListener) throws SQLException {
        panel.setLayout(new GridLayout(0, 1));
        while (result.next()) {
            JCheckBox cb = new JCheckBox(result.getString(1));
            cb.addActionListener(actionListener);
            
            checkBoxList.add(cb);
            panel.add(cb);
        }
        
        panel.revalidate();
        panel.repaint();
    }   
    
    private void insertGenresCheckBox(ResultSet result) throws SQLException {
        ActionListener actionListener = new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                generateCountriesCheckBox();
                                                generateFilmingCountriesCheckBox();
                                                generateTagValuesCheckBox();
                                            }
                                        };
        
        insertCheckBoxs(result, GenresPanel, genresCheckBox, actionListener);
    }   
    
    private void insertCountriesCheckBox(ResultSet result) throws SQLException {
        removeAllContries();
        
        ActionListener actionListener = new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                generateFilmingCountriesCheckBox();
                                                generateTagValuesCheckBox();
                                            }
                                        };
        insertCheckBoxs(result, CountriesPanel, countriesCheckBox, actionListener);
    }
    
    private void insertFilmingCountriesCheckBox(ResultSet result) throws SQLException {
        removeAllFilmingCountries();
        
        ActionListener actionListener = new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                generateTagValuesCheckBox();
                                            }
                                        };
        insertCheckBoxs(result, FilmingCountriesPanel, filmingCountriesCheckBox, actionListener);
    }
    
    private void insertTagValuesCheckBox(ResultSet result) throws SQLException {
        removeAllTagValues();
        
        ActionListener actionListener = null;
        
        insertCheckBoxs(result, TagValuesPanel, tagValuseCheckBox, actionListener);
    }
    
    private void removeAllCheckBox(ArrayList<JCheckBox> checkBoxList, JPanel panel) {
        for (int i = 0; i < checkBoxList.size(); i++) {
            checkBoxList.get(i).setVisible(false);
            panel.remove(checkBoxList.get(i));     
        }
        
        checkBoxList.clear();
        panel.updateUI(); 
    }
    
    private void removeAllContries() {
        removeAllCheckBox(countriesCheckBox, CountriesPanel);
    }
    private void removeAllFilmingCountries() {
        removeAllCheckBox(filmingCountriesCheckBox, FilmingCountriesPanel);
    }
    private void removeAllTagValues() {
        removeAllCheckBox(tagValuseCheckBox, TagValuesPanel);
    }

    private ArrayList<String> getSelectedCheckBox(ArrayList<JCheckBox> checkBoxs) {      
        ArrayList<String> selected = new ArrayList<String> ();
        
        for (int i = 0; i < checkBoxs.size(); i++) {
            if (checkBoxs.get(i).isSelected()) {
                selected.add(checkBoxs.get(i).getText());
            }
        }
        
        return selected;
    }
        
    private void generateCountriesCheckBox() {
        ArrayList<String> selectedGenres = getSelectedCheckBox(genresCheckBox);
        Connection conDB = null;
        ResultSet countries  = null;
        
        removeAllContries();

        StringBuilder queryCountries = new StringBuilder();
        queryCountries.append(
            "SELECT DISTINCT MC.COUNTRY\n" +
            "FROM countries MC,\n" +
            "(SELECT movieid, LISTAGG(GENRE, ',') WITHIN GROUP (ORDER BY GENRE) AS Genre\n" +
            "FROM GENRES\n" +
            "GROUP BY movieid" + 
            ") G\n" +
            "WHERE G.movieid = MC.movieid \n");
        
        if (selectedGenres.size() != 0) {  
            queryCountries.append("AND (\n");
            for (int i = 0; i < selectedGenres.size(); i++) {
                if (i != 0) {
                    queryCountries.append(" " + attributesRelation + "\n");
                }
                queryCountries.append("G.genre like " + "'%" + selectedGenres.get(i) + "%'");
            }
            queryCountries.append("\n)");
        }
        
        try {
            conDB = DBUtility.connectDB();
            String query = queryCountries.toString();
            System.out.println(query + "\n");
            countries = DBUtility.searchAllTuples(conDB, query);
            insertCountriesCheckBox(countries);
        } catch (SQLException e){ 
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e){
            System.err.println("Cannot find the database driver");
        } finally {
            DBUtility.closeConnection(conDB);
        }
    }  
    
    private void generateFilmingCountriesCheckBox() {
        ArrayList<String> selectedGenres = getSelectedCheckBox(genresCheckBox);
        ArrayList<String> selectedCountries = getSelectedCheckBox(countriesCheckBox);
        Connection conDB = null;
        ResultSet filmingCountries  = null;
        
        removeAllFilmingCountries();

        StringBuilder queryFilmingCountries = new StringBuilder();
        queryFilmingCountries.append(
            "SELECT DISTINCT MFC.COUNTRY\n" +
            "FROM COUNTRIES MC,\n" +
            "FILMING_COUNTRIES MFC,\n" +
            "(SELECT sG.movieid, LISTAGG(sG.GENRE, ',') WITHIN GROUP (ORDER BY sG.GENRE) AS GroupedGenre\n" +
            "FROM GENRES sG\n" +
            "GROUP BY sG.movieid" + 
            ") G\n" +
            "WHERE G.movieid = MC.movieid AND " + 
            "G.movieid = MFC.movieid \n");
        
        if (selectedGenres.size() != 0) {  
            queryFilmingCountries.append("AND (\n");
            for (int i = 0; i < selectedGenres.size(); i++) {
                if (i != 0) {
                    queryFilmingCountries.append(" " + attributesRelation + "\n");
                }
                queryFilmingCountries.append("G.GroupedGenre like " + "'%" + selectedGenres.get(i) + "%'");
            }
            queryFilmingCountries.append("\n)");
        }
        
        if (selectedCountries.size() != 0) {  
            queryFilmingCountries.append(" AND (\n");
            for (int i = 0; i < selectedCountries.size(); i++) {
                if (i != 0) {
                    queryFilmingCountries.append(" " + attributesRelation + "\n");
                }
                queryFilmingCountries.append("MC.COUNTRY like " + "'%" + selectedCountries.get(i) + "%'");
            }
            queryFilmingCountries.append("\n)");
        }
        
        try {
            conDB = DBUtility.connectDB();
            String query = queryFilmingCountries.toString();
            System.out.println(query + "\n");
            filmingCountries = DBUtility.searchAllTuples(conDB, query);
            insertFilmingCountriesCheckBox(filmingCountries);
        } catch (SQLException e){ 
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e){
            System.err.println("Cannot find the database driver");
        } finally {
            DBUtility.closeConnection(conDB);
        }
    }  
    
    private void generateTagValuesCheckBox() {        
        Connection conDB = null;
        ResultSet tagValues  = null;
        
        removeAllTagValues();
        
        StringBuilder queryTagValues = new StringBuilder();
        queryTagValues.append(    
            "SELECT DISTINCT T.TAGNAME\n" + 
            "FROM \n" + 
            "(SELECT sG.movieid, LISTAGG(sG.GENRE, ',') WITHIN GROUP (ORDER BY sG.GENRE) AS GroupedGenre \n" +
            "FROM GENRES sG \n" +
            "GROUP BY sG.movieid) G, \n" +
            "COUNTRIES MC, \n" +
            "(SELECT sMFC.movieid, LISTAGG(sMFC.COUNTRY, ',') WITHIN GROUP (ORDER BY sMFC.COUNTRY) AS GroupedFilimingCountries \n" +
            "FROM FILMING_COUNTRIES sMFC \n" +
            "GROUP BY sMFC.movieid) MFC, \n" +
            "MOVIES M, \n" +
            "MOVIE_TAGS MT, \n" +
            "TAGS T \n" +
            "WHERE M.movieid = G.movieid AND M.movieid = MC.movieid AND M.movieid = MFC.movieid AND M.movieid = MT.movieid AND MT.TAGID = T.TAGID \n");
  
        appendSelectedGenres(queryTagValues);

        appendSelectedCountries(queryTagValues);
        
        appendSelectedFilmingCountries(queryTagValues);   
        
        appendRatingsAndNumReviewsConstrain(queryTagValues);          
        
        appendYearConstrain(queryTagValues);        
        
        try {
            conDB = DBUtility.connectDB();
            String query = queryTagValues.toString();
            System.out.println(query + "\n");
            tagValues = DBUtility.searchAllTuples(conDB, query);
            insertTagValuesCheckBox(tagValues);
        } catch (SQLException e){ 
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e){
            System.err.println("Cannot find the database driver");
        } finally {
            DBUtility.closeConnection(conDB);
        }
    }
    
    private void appendSelectedGenres(StringBuilder queryBuilder) {
        ArrayList<String> selectedGenres = getSelectedCheckBox(genresCheckBox);
        if (selectedGenres.size() != 0) {  
            queryBuilder.append("AND (\n");
            for (int i = 0; i < selectedGenres.size(); i++) {
                if (i != 0) {
                    queryBuilder.append(" " + attributesRelation + "\n");
                }
                queryBuilder.append("G.GroupedGenre like " + "'%" + selectedGenres.get(i) + "%'");
            }
            queryBuilder.append("\n)");
        }
    }
    
    private void appendSelectedCountries(StringBuilder queryBuilder) {
        ArrayList<String> selectedCountries = getSelectedCheckBox(countriesCheckBox);
        if (selectedCountries.size() != 0) {  
            queryBuilder.append(" AND (\n");
            for (int i = 0; i < selectedCountries.size(); i++) {
                if (i != 0) {
                    queryBuilder.append(" " + attributesRelation + "\n");
                }
                queryBuilder.append("MC.COUNTRY like " + "'%" + selectedCountries.get(i) + "%'");
            }
            queryBuilder.append("\n)");
        }
    }
    
    private void appendSelectedFilmingCountries(StringBuilder queryBuilder) {
        ArrayList<String> selectedfilmingCountries = getSelectedCheckBox(filmingCountriesCheckBox);
        if (selectedfilmingCountries.size() != 0) {  
            queryBuilder.append(" AND (\n");
            for (int i = 0; i < selectedfilmingCountries.size(); i++) {
                if (i != 0) {
                    queryBuilder.append(" " + attributesRelation + "\n");
                }
                queryBuilder.append("MFC.GroupedFilimingCountries like " + "'%" + selectedfilmingCountries.get(i) + "%'");
            }
            queryBuilder.append("\n)");
        }
    }          
    
    private void appendRatingsAndNumReviewsConstrain(StringBuilder queryBuilder) {
        String ratingValue = ratingValueTextField.getText();
        String numReviews = numReviewsTextField.getText();
        if (!ratingValue.equals("") || !numReviews.equals("")) {
           queryBuilder.append(" AND (\n");
           if (!ratingValue.equals("")) {
               queryBuilder.append("M.RTALLCRITICSRATING " + ratingCondition.getSelectedItem().toString() + " " + ratingValue);
           }
           if (!ratingValue.equals("") && !numReviews.equals("")) {
               queryBuilder.append(" " + attributesRelation + "\n");
           }
           if (!numReviews.equals("")) {
               queryBuilder.append("M.RTALLCRITICSNUMREVIEWS " + numReviewsCondition.getSelectedItem().toString() + " " + numReviews);
           }
           queryBuilder.append("\n)");
        }
    }
    
    private void appendYearConstrain(StringBuilder queryBuilder) {
        String yearFrom = yearFromTextField.getText();
        String yearTo = yearToTextField.getText();
        if (!yearFrom.equals("") || !yearTo.equals("")) {
            queryBuilder.append(" AND (\n");
            if (!yearFrom.equals("")) {
                queryBuilder.append("M.Year >= " + yearFrom);
            }
            if (!yearFrom.equals("") && !yearTo.equals("")) {
                queryBuilder.append(" AND ");
            }
            if (!yearTo.equals("")) {
                queryBuilder.append("M.Year <= " + yearTo);
            }
            queryBuilder.append("\n)");          
        }
    }

    private void appendTagConstrain(StringBuilder queryBuilder) {
        ArrayList<String> selectedTagValues = getSelectedCheckBox(tagValuseCheckBox);
        String tagWeight = tagWeightInput.getText();
        
        if (selectedTagValues.size() != 0 || !tagWeight.equals("")) {
            queryBuilder.append(
                " AND ( \n" + 
                "M.movieid in \n" +
                "(SELECT sWMT.movieid \n" +
                "FROM MOVIE_TAGS sWMT, TAGS sWT \n" +
                "WHERE sWMT.TAGID = sWT.TAGID "
            );

            queryBuilder.append("AND (\n");
            if (selectedTagValues.size() != 0) {
                for (int i = 0; i < selectedTagValues.size(); i++) {
                    if (i != 0) {
                        queryBuilder.append(" " + attributesRelation + "\n");
                    }
                    queryBuilder.append("sWT.TAGNAME like " + "'%" + selectedTagValues.get(i) + "%'");
                }
            }
            if (selectedTagValues.size() != 0 && !tagWeight.equals("")) {
                queryBuilder.append(" " + attributesRelation + " \n");
            }
            if (!tagWeight.equals("")) {
                queryBuilder.append("sWMT.TAGWEIGHT " + tagWeightCondition.getSelectedItem().toString() + " " + tagWeight);
            }            
            queryBuilder.append(")\n");
         
            queryBuilder.append(")" + ")\n");
        }
    }
    
    private String generateFinalQueryStatement() {
        StringBuilder finalQuery = new StringBuilder();
        finalQuery.append(
            "SELECT M.movieid, M.TITLE, G.GroupedGenre AS Genres, M.YEAR, MC.COUNTRY, MFC.GroupedFilimingCountries AS FilimimgCountries, \n" +
	    "TRUNC((M.RTALLCRITICSRATING + M.RTTOPCRITICSRATING + M.RTTOPRTAUDIENCERATING) / 3, 2) AS Rating, \n" +
	    "TRUNC((M.RTALLCRITICSNUMREVIEWS + M.RTTOPCRITICSNUMREVIEWS + M.RTAUDIENCENUMRATINGS) / 3, 2) AS NumReviews \n" +
            "FROM \n" +
            "(SELECT sG.movieid, LISTAGG(sG.GENRE, ',') WITHIN GROUP (ORDER BY sG.GENRE) AS GroupedGenre \n" +
            "FROM GENRES sG \n" +
            "GROUP BY sG.movieid) G, \n" +
            "COUNTRIES MC, \n" +
            "(SELECT sMFC.movieid, LISTAGG(sMFC.COUNTRY, ',') WITHIN GROUP (ORDER BY sMFC.COUNTRY) AS GroupedFilimingCountries \n" +
            "FROM FILMING_COUNTRIES sMFC \n" +
            "GROUP BY sMFC.movieid) MFC, \n" +
            "MOVIES M \n" +
            "WHERE M.movieid = G.movieid AND M.movieid = MC.movieid AND M.movieid = MFC.movieid \n");

        appendSelectedGenres(finalQuery);
        
        appendSelectedCountries(finalQuery);
        
        appendSelectedFilmingCountries(finalQuery);
        
        appendRatingsAndNumReviewsConstrain(finalQuery);
        
        appendYearConstrain(finalQuery);
        
        appendTagConstrain(finalQuery);
        
        return finalQuery.toString();
    }
    
    private void displayFinalQuery(ResultSet queryResult) throws SQLException{
        
        java.sql.ResultSetMetaData rsmd = queryResult.getMetaData();
        int columnNumber = rsmd.getColumnCount();

        DefaultTableModel tableModel = new DefaultTableModel();
        resultTable.setModel(tableModel);

        for(int i = 1; i <= columnNumber; ++i){
           tableModel.addColumn(rsmd.getColumnLabel(i));
        }

        int count = 0;
        while(queryResult.next()){            
            Object[] row = new Object[columnNumber];

            for(int i = 0; i < columnNumber; ++i){
                row[i] = queryResult.getObject(i + 1);
            }
            tableModel.addRow(row);

            count++;
        }
        rowNums.setText(Integer.toString(count));
    }
    
    private void executeFinalQuery() {
        Connection conDB = null;
        ResultSet queryResult = null;
        
        try {
            conDB = DBUtility.connectDB();
            Statement stmt = conDB.createStatement();
            String query = generateFinalQueryStatement();
            queryStatementTextArea.setText(query);
            System.out.println(query + "\n");
            
            queryResult = stmt.executeQuery(query);

            displayFinalQuery(queryResult);
                    
            stmt.close();
        } catch (SQLException e){ 
            System.err.println("Errors occurs when communicating with the database server: " + e.getMessage());
        } catch (ClassNotFoundException e){
            System.err.println("Cannot find the database driver");
        } finally {
            DBUtility.closeConnection(conDB);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        MovieDatabase = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        AttributesRelationBox = new javax.swing.JComboBox<>();
        FlimingCountriesRootPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        FilmingCountriesPanel = new javax.swing.JPanel();
        CountriesRootPanel = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        CountriesPanel = new javax.swing.JPanel();
        GenersRootPanel = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        GenresPanel = new javax.swing.JPanel();
        RatingsRootPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        ratingCondition = new javax.swing.JComboBox<>();
        ratingValueTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        numReviewsCondition = new javax.swing.JComboBox<>();
        numReviewsTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        yearFromTextField = new javax.swing.JTextField();
        yearToTextField = new javax.swing.JTextField();
        TagVaulesRootPanel = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        tagWeightInput = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        tagWeightCondition = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        TagValuesPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        ResultRootPanel = new javax.swing.JScrollPane();
        ResulTablePane = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        QueryRootPanel = new javax.swing.JScrollPane();
        queryStatementTextArea = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        Excute = new javax.swing.JButton();
        rowNums = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Movie Database");

        AttributesRelationBox.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        AttributesRelationBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AND", "OR" }));
        AttributesRelationBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AttributesRelationBoxActionPerformed(evt);
            }
        });

        FlimingCountriesRootPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Filming Country");
        jLabel9.setToolTipText("");

        jScrollPane3.setBorder(null);

        javax.swing.GroupLayout FilmingCountriesPanelLayout = new javax.swing.GroupLayout(FilmingCountriesPanel);
        FilmingCountriesPanel.setLayout(FilmingCountriesPanelLayout);
        FilmingCountriesPanelLayout.setHorizontalGroup(
            FilmingCountriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 202, Short.MAX_VALUE)
        );
        FilmingCountriesPanelLayout.setVerticalGroup(
            FilmingCountriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 423, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(FilmingCountriesPanel);

        javax.swing.GroupLayout FlimingCountriesRootPanelLayout = new javax.swing.GroupLayout(FlimingCountriesRootPanel);
        FlimingCountriesRootPanel.setLayout(FlimingCountriesRootPanelLayout);
        FlimingCountriesRootPanelLayout.setHorizontalGroup(
            FlimingCountriesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FlimingCountriesRootPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addGap(62, 62, 62))
        );
        FlimingCountriesRootPanelLayout.setVerticalGroup(
            FlimingCountriesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FlimingCountriesRootPanelLayout.createSequentialGroup()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        CountriesRootPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Country");

        jScrollPane1.setBorder(null);

        javax.swing.GroupLayout CountriesPanelLayout = new javax.swing.GroupLayout(CountriesPanel);
        CountriesPanel.setLayout(CountriesPanelLayout);
        CountriesPanelLayout.setHorizontalGroup(
            CountriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 193, Short.MAX_VALUE)
        );
        CountriesPanelLayout.setVerticalGroup(
            CountriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 423, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(CountriesPanel);

        javax.swing.GroupLayout CountriesRootPanelLayout = new javax.swing.GroupLayout(CountriesRootPanel);
        CountriesRootPanel.setLayout(CountriesRootPanelLayout);
        CountriesRootPanelLayout.setHorizontalGroup(
            CountriesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CountriesRootPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CountriesRootPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(83, 83, 83))
        );
        CountriesRootPanelLayout.setVerticalGroup(
            CountriesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CountriesRootPanelLayout.createSequentialGroup()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59))
        );

        GenersRootPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Genres");

        jScrollPane2.setBorder(null);

        javax.swing.GroupLayout GenresPanelLayout = new javax.swing.GroupLayout(GenresPanel);
        GenresPanel.setLayout(GenresPanelLayout);
        GenresPanelLayout.setHorizontalGroup(
            GenresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 159, Short.MAX_VALUE)
        );
        GenresPanelLayout.setVerticalGroup(
            GenresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 406, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(GenresPanel);

        javax.swing.GroupLayout GenersRootPanelLayout = new javax.swing.GroupLayout(GenersRootPanel);
        GenersRootPanel.setLayout(GenersRootPanelLayout);
        GenersRootPanelLayout.setHorizontalGroup(
            GenersRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GenersRootPanelLayout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
        );
        GenersRootPanelLayout.setVerticalGroup(
            GenersRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GenersRootPanelLayout.createSequentialGroup()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        RatingsRootPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

        ratingCondition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", "<", ">", "<=", ">=" }));
        ratingCondition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratingConditionActionPerformed(evt);
            }
        });

        ratingValueTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ratingValueTextFieldFocusLost(evt);
            }
        });
        ratingValueTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratingValueTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Rating");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Num of Reviews");
        jLabel2.setToolTipText("");

        numReviewsCondition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", "<", ">", "<=", ">=" }));
        numReviewsCondition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numReviewsConditionActionPerformed(evt);
            }
        });

        numReviewsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                numReviewsTextFieldFocusLost(evt);
            }
        });
        numReviewsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numReviewsTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setText("Movie Year");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText("From");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel11.setText("To");

        yearFromTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                yearFromTextFieldFocusLost(evt);
            }
        });
        yearFromTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearFromTextFieldActionPerformed(evt);
            }
        });

        yearToTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                yearToTextFieldFocusLost(evt);
            }
        });
        yearToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearToTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yearToTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(yearFromTextField)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel11))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yearFromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yearToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ratingCondition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(numReviewsCondition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(ratingValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(numReviewsTextField)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ratingCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(ratingValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(5, 5, 5)
                .addComponent(numReviewsCondition, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numReviewsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addGap(2, 2, 2)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout RatingsRootPanelLayout = new javax.swing.GroupLayout(RatingsRootPanel);
        RatingsRootPanel.setLayout(RatingsRootPanelLayout);
        RatingsRootPanelLayout.setHorizontalGroup(
            RatingsRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RatingsRootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        RatingsRootPanelLayout.setVerticalGroup(
            RatingsRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RatingsRootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        TagVaulesRootPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Movie Tag");

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel18.setText("Tag Weight");

        tagWeightCondition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", "<", ">", "<=", ">=" }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tagWeightCondition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(tagWeightInput))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel18)
                .addGap(1, 1, 1)
                .addComponent(tagWeightCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagWeightInput, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane4.setBorder(null);

        javax.swing.GroupLayout TagValuesPanelLayout = new javax.swing.GroupLayout(TagValuesPanel);
        TagValuesPanel.setLayout(TagValuesPanelLayout);
        TagValuesPanelLayout.setHorizontalGroup(
            TagValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 202, Short.MAX_VALUE)
        );
        TagValuesPanelLayout.setVerticalGroup(
            TagValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 423, Short.MAX_VALUE)
        );

        jScrollPane4.setViewportView(TagValuesPanel);

        javax.swing.GroupLayout TagVaulesRootPanelLayout = new javax.swing.GroupLayout(TagVaulesRootPanel);
        TagVaulesRootPanel.setLayout(TagVaulesRootPanelLayout);
        TagVaulesRootPanelLayout.setHorizontalGroup(
            TagVaulesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TagVaulesRootPanelLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(TagVaulesRootPanelLayout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addComponent(jLabel15)
                .addContainerGap(87, Short.MAX_VALUE))
            .addGroup(TagVaulesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
        );
        TagVaulesRootPanelLayout.setVerticalGroup(
            TagVaulesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TagVaulesRootPanelLayout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(252, 252, 252)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(TagVaulesRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(TagVaulesRootPanelLayout.createSequentialGroup()
                    .addGap(19, 19, 19)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(105, Short.MAX_VALUE)))
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Result");

        ResultRootPanel.setToolTipText("");

        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Title", "Genre", "Year", "Country", "Filming Country", "Rating", "NumReviews"
            }
        ));
        ResulTablePane.setViewportView(resultTable);

        ResultRootPanel.setViewportView(ResulTablePane);

        queryStatementTextArea.setColumns(20);
        queryStatementTextArea.setRows(5);
        QueryRootPanel.setViewportView(queryStatementTextArea);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("Query");

        Excute.setText("Execute");
        Excute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExcuteActionPerformed(evt);
            }
        });

        rowNums.setText("                 ");

        jLabel19.setText("rows ");

        jCheckBox1.setText("jCheckBox1");

        javax.swing.GroupLayout MovieDatabaseLayout = new javax.swing.GroupLayout(MovieDatabase);
        MovieDatabase.setLayout(MovieDatabaseLayout);
        MovieDatabaseLayout.setHorizontalGroup(
            MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MovieDatabaseLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                        .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(MovieDatabaseLayout.createSequentialGroup()
                                .addComponent(QueryRootPanel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(AttributesRelationBox, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Excute, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(14, 14, 14))
                            .addGroup(MovieDatabaseLayout.createSequentialGroup()
                                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3)
                                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                                        .addComponent(GenersRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(CountriesRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(FlimingCountriesRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(4, 4, 4)))
                        .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MovieDatabaseLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ResultRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 488, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                                        .addComponent(rowNums, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
                            .addGroup(MovieDatabaseLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                                        .addComponent(RatingsRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(TagVaulesRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
        );
        MovieDatabaseLayout.setVerticalGroup(
            MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MovieDatabaseLayout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TagVaulesRootPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RatingsRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(FlimingCountriesRootPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CountriesRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(GenersRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 376, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(AttributesRelationBox, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Excute, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54))
                    .addGroup(MovieDatabaseLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(QueryRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ResultRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(MovieDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rowNums, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(MovieDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(MovieDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ExcuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExcuteActionPerformed
        executeFinalQuery();
    }//GEN-LAST:event_ExcuteActionPerformed
   
    private void AttributesRelationBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AttributesRelationBoxActionPerformed
            
        if (!attributesRelation.equals(AttributesRelationBox.getSelectedItem().toString())) {
            attributesRelation = AttributesRelationBox.getSelectedItem().toString();
            System.out.printf(attributesRelation + " Relation Selected\n");

            removeAllContries();
            removeAllFilmingCountries();
            removeAllTagValues();
            generateCountriesCheckBox();
            generateFilmingCountriesCheckBox();
            generateTagValuesCheckBox();
        }
    }//GEN-LAST:event_AttributesRelationBoxActionPerformed
    
    private void yearFromTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearFromTextFieldActionPerformed
        // TODO add your handling code here:
        generateTagValuesCheckBox();
    }//GEN-LAST:event_yearFromTextFieldActionPerformed

    private void ratingValueTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratingValueTextFieldActionPerformed
        // TODO add your handling code here:
        generateTagValuesCheckBox();
    }//GEN-LAST:event_ratingValueTextFieldActionPerformed

    private void yearToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearToTextFieldActionPerformed
        // TODO add your handling code here:
        generateTagValuesCheckBox();
    }//GEN-LAST:event_yearToTextFieldActionPerformed

    private void numReviewsTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numReviewsTextFieldActionPerformed
        // TODO add your handling code here:
        generateTagValuesCheckBox();
    }//GEN-LAST:event_numReviewsTextFieldActionPerformed

    private void numReviewsTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numReviewsTextFieldFocusLost
        // TODO add your handling code here:
        //generateTagValuesCheckBox();
    }//GEN-LAST:event_numReviewsTextFieldFocusLost

    private void yearFromTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yearFromTextFieldFocusLost
        // TODO add your handling code here:
        //generateTagValuesCheckBox();
    }//GEN-LAST:event_yearFromTextFieldFocusLost

    private void yearToTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yearToTextFieldFocusLost
        // TODO add your handling code here:
        //generateTagValuesCheckBox();
    }//GEN-LAST:event_yearToTextFieldFocusLost

    private void ratingValueTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ratingValueTextFieldFocusLost
        // TODO add your handling code here:
        //generateTagValuesCheckBox();
    }//GEN-LAST:event_ratingValueTextFieldFocusLost

    private void ratingConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratingConditionActionPerformed
        // TODO add your handling code here:
        generateTagValuesCheckBox();
    }//GEN-LAST:event_ratingConditionActionPerformed

    private void numReviewsConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numReviewsConditionActionPerformed
        // TODO add your handling code here:
        generateTagValuesCheckBox();
    }//GEN-LAST:event_numReviewsConditionActionPerformed
   
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Query.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Query.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Query.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Query.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
            
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Query().setVisible(true);
            }
        });
    }

    //private JPanel Panel1;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> AttributesRelationBox;
    private javax.swing.JPanel CountriesPanel;
    private javax.swing.JPanel CountriesRootPanel;
    private javax.swing.JButton Excute;
    private javax.swing.JPanel FilmingCountriesPanel;
    private javax.swing.JPanel FlimingCountriesRootPanel;
    private javax.swing.JPanel GenersRootPanel;
    private javax.swing.JPanel GenresPanel;
    private javax.swing.JPanel MovieDatabase;
    private javax.swing.JScrollPane QueryRootPanel;
    private javax.swing.JPanel RatingsRootPanel;
    private javax.swing.JScrollPane ResulTablePane;
    private javax.swing.JScrollPane ResultRootPanel;
    private javax.swing.JPanel TagValuesPanel;
    private javax.swing.JPanel TagVaulesRootPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JComboBox<String> numReviewsCondition;
    private javax.swing.JTextField numReviewsTextField;
    private javax.swing.JTextArea queryStatementTextArea;
    private javax.swing.JComboBox<String> ratingCondition;
    private javax.swing.JTextField ratingValueTextField;
    private javax.swing.JTable resultTable;
    private javax.swing.JTextField rowNums;
    private javax.swing.JComboBox<String> tagWeightCondition;
    private javax.swing.JTextField tagWeightInput;
    private javax.swing.JTextField yearFromTextField;
    private javax.swing.JTextField yearToTextField;
    // End of variables declaration//GEN-END:variables
}
