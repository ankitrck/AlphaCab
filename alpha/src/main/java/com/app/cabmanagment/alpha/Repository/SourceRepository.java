package com.app.cabmanagment.alpha.Repository;

import com.app.cabmanagment.alpha.Constants.KeyConstants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class SourceRepository {
    static int f_record = 0;

    private String resource_file_path = "./src/main/resources/vehdata.csv";
    private static final Logger LOGGER = LogManager.getLogger(SourceRepository.class);

    public Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:./src/main/resources/data.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    //Admin can register a new cab
    public boolean registerVehicle(String registration_no, String driver_name, long contact, String city) {

        boolean status = false;
        try
        {
            Connection c = connect();
            String sql = "Insert into vehdata values(?,?,?,?,?,?,?)";

            JSONArray alldata = new JSONArray();
            try (Connection conn = c;
                 PreparedStatement pstmt = conn.prepareStatement(sql)){

                pstmt.setString(1, registration_no);
                pstmt.setString(2, driver_name);
                pstmt.setInt(3, (int) contact);
                pstmt.setString(4, city);
                pstmt.setInt(5, 1);

                java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
                pstmt.setTimestamp(6, date);
                pstmt.setString(7, KeyConstants.IDLE);

                pstmt.executeUpdate();

                status=true;
            }
            sql = "Insert into reports (regnumber, tripstart, tripend, city) values(?,?,?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)){

                pstmt.setString(1, registration_no);
                java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
                pstmt.setTimestamp(2, date);
                pstmt.setTimestamp(3, date);
                pstmt.setString(4, city);
                pstmt.executeUpdate();
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return status;
    }

    //List all data for report or Admin
    public Object getallData() {
        Connection c = connect();
        String sql = "SELECT * from vehdata";

        JSONArray alldata = new JSONArray();
        try (Connection conn = c;
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){


            // loop through the result set
            while (rs.next()) {

                JSONObject rec = new JSONObject();
                rec.put("contact",rs.getInt("contact"));
                rec.put("registration_number",rs.getString("regnumber"));
                rec.put("name",rs.getString("name"));
                rec.put("city",rs.getString("city"));
                rec.put("in_service",rs.getInt("in_service"));

                //Timestamp ts=rs.getTimestamp("modified_date");
                //System.out.println(ts);
                //rec.put("last ON_TRIP", ts);

                alldata.put(rec);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return alldata;
    }

    //Admin functionality to change cab city
    public Object changeVehCity(String registration_no, String city) {

        JSONObject res_status = new JSONObject();
        Connection c = connect();
        try {
            String sql = "SELECT * from vehdata where regnumber LIKE ?";
            String stat = "";
            int in_serv = -1;
            try (Connection conn = c;
                 PreparedStatement pstmt = conn.prepareStatement(sql)){
                 pstmt.setString(1, registration_no);
                 ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    stat = rs.getString("cabstatus");
                    in_serv = rs.getInt("in_service");

                    if (stat.equalsIgnoreCase("ON_TRIP"))
                        res_status.put("message_st", "Cannot change CITY for a cab that has an ON GOING TRIP");
                    if (in_serv == 0)
                        res_status.put("message_inser", "CAB is not in service, Please activate the CAB first");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (in_serv == 1) {
                sql = "update vehdata set city = ? where regnumber = ? and in_service = 1 and cabstatus LIKE ?";

                JSONArray alldata = new JSONArray();
                try (Connection conn = connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, city);
                    pstmt.setString(2, registration_no);
                    pstmt.setString(3, KeyConstants.IDLE);

                    pstmt.executeUpdate();

                    res_status.put("message_transaction","Data updated successfully");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res_status;
    }

    //User to get availble cabs
    public Object getavailableVeh(String city) {
        JSONArray res_val = new JSONArray();
        Connection c = connect();
        String sql = "SELECT * from vehdata where city LIKE ? and in_service = 1 ORDER BY modified_date DESC LIMIT 2";
        String stat = "";
        int in_serv = -1;
        try (Connection conn = c;
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, city);
            ResultSet rs = pstmt.executeQuery();
            Timestamp assignedvalue = new Timestamp(System.currentTimeMillis());
            while (rs.next()) {
                JSONObject res_status =  new JSONObject();
                Timestamp ts=rs.getTimestamp("modified_date");
                //Find the most IDLE cab and book it.
                if(ts.before(assignedvalue)) {
                    assignedvalue = ts;
                    res_val.clear();
                    res_status.put("vehiclenumber",rs.getString("regnumber"));
                    res_status.put("Name",rs.getString("name"));
                    res_val.put(res_status);
                }
                System.out.println(assignedvalue);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return res_val;
    }

    //Book available CAB
    public Object bookavailcab(String regnumber) {
        JSONArray res_val = new JSONArray();
        boolean is_avail = false;
        String city = "";

        String sql = "SELECT * from vehdata where regnumber LIKE ? and in_service = 1 and cabstatus LIKE ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, regnumber);
            pstmt.setString(2, KeyConstants.IDLE);
            ResultSet rs = pstmt.executeQuery();
            Timestamp assignedvalue = new Timestamp(System.currentTimeMillis());
            while (rs.next()) {
                city = rs.getString("city");
                LOGGER.info("CAB number:" + rs.getString("regnumber"));
                is_avail = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (is_avail) {
            //Cab is still available
            String upsql = "update vehdata set modified_date = ?, cabstatus = ? where regnumber LIKE ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(upsql)) {

                java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
                pstmt.setTimestamp(1, date);
                pstmt.setString(2, KeyConstants.ON_TRIP);
                pstmt.setString(3, regnumber);

                System.out.println(pstmt.toString());
                pstmt.executeUpdate();

                res_val.put("CAB booked successfully. Will arrive shortly");
            } catch (Exception e) {
                e.printStackTrace();
            }

            sql = "Insert into reports (regnumber, tripstart, city) values(?,?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)){

                pstmt.setString(1, regnumber);
                java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
                pstmt.setTimestamp(2, date);
                pstmt.setString(3, city);
                pstmt.executeUpdate();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            res_val.put("CAB could not be booked. Please try again with available cabs");
        }
        return res_val;
    }

    //Endtrip
    public Object endtrip(String regnumber)
    {
        String city = "";
        JSONArray res_val = new JSONArray();
        String upsql = "update vehdata set modified_date = ?, cabstatus = ? where regnumber LIKE ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(upsql)) {

            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            pstmt.setTimestamp(1, date);
            pstmt.setString(2, KeyConstants.IDLE);
            pstmt.setString(3, regnumber);

            System.out.println(pstmt.toString());
            pstmt.executeUpdate();

            res_val.put("TRIP completed");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql = "select city from vehdata where regnumber LIKE ?";
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, regnumber);
            ResultSet rs = pstmt.executeQuery();
            Timestamp assignedvalue = new Timestamp(System.currentTimeMillis());
            while (rs.next()) {
                city = rs.getString("city");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        upsql = "Insert into reports (regnumber, tripend, city) values(?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(upsql)){

            pstmt.setString(1, regnumber);
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            pstmt.setTimestamp(2, date);
            pstmt.setString(3, city);
            pstmt.executeUpdate();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return res_val;
    }
}
