package service;

import dao.DbConnectivityClass;
import javafx.collections.ObservableList;
import model.Major;
import model.Person;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVService {

    private final DbConnectivityClass db;

    public CSVService() {
        this.db = new DbConnectivityClass();
    }

    /**
     * Export all users from TableView list to CSV file
     */
    public void exportToCSV(File file, ObservableList<Person> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            // Header
            writer.println("id,first_name,last_name,department,major,email,imageURL");

            // Data
            for (Person p : data) {
                writer.println(
                        p.getId() + "," +
                                escape(p.getFirstName()) + "," +
                                escape(p.getLastName()) + "," +
                                escape(p.getDepartment()) + "," +
                                p.getMajor() + "," +
                                escape(p.getEmail()) + "," +
                                escape(p.getImageURL())
                );
            }

            MyLogger.makeLog("CSV Export successful.");

        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.makeLog("CSV Export failed.");
        }
    }

    /**
     * Import users from CSV file into DB and return list
     */
    public List<Person> importFromCSV(File file) {
        List<Person> importedList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {

                // Skip header
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] tokens = parseCSVLine(line);

                if (tokens.length < 7) continue;

                try {
                    Person p = new Person(
                            tokens[1], // first_name
                            tokens[2], // last_name
                            tokens[3], // department
                            Major.valueOf(tokens[4]), // major
                            tokens[5], // email
                            tokens[6]  // imageURL
                    );

                    db.insertUser(p);
                    p.setId(db.retrieveId(p));

                    importedList.add(p);

                } catch (Exception e) {
                    // Skip bad rows instead of crashing
                    MyLogger.makeLog("Skipping invalid row: " + line);
                }
            }

            MyLogger.makeLog("CSV Import successful.");

        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.makeLog("CSV Import failed.");
        }

        return importedList;
    }

    /**
     * Handle commas inside values (basic CSV parsing)
     */
    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());

        return tokens.toArray(new String[0]);
    }

    /**
     * Escape commas and quotes in CSV values
     */
    private String escape(String value) {
        if (value == null) return "";

        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}