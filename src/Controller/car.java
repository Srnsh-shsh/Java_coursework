package Controller;

import Model.gettersetter;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class car {
    private String name;
    private String brand;
    private String ModelNo;
    private String carType;
    
    private static final String FILE_NAME = "cars.txt";
    
    // Lists to track open tables for synchronization
    private static List<DefaultTableModel> customerTables = new ArrayList<>();
    private static List<DefaultTableModel> adminTables = new ArrayList<>();
    
    // ========== REGISTRATION METHODS ==========
    public static void registerCustomerTable(DefaultTableModel model) {
        if (!customerTables.contains(model)) customerTables.add(model);
    }
    
    public static void registerAdminTable(DefaultTableModel model) {
        if (!adminTables.contains(model)) adminTables.add(model);
    }
    
    public static void unregisterCustomerTable(DefaultTableModel model) {
        customerTables.remove(model);
    }
    
    public static void unregisterAdminTable(DefaultTableModel model) {
        adminTables.remove(model);
    }
    
    // ========== SYNCHRONIZATION ==========
    public static void syncAllTables() {
        for (DefaultTableModel adminModel : adminTables) {
            loadAllCars(adminModel);
        }
        for (DefaultTableModel customerModel : customerTables) {
            loadAllCarsForCustomer(customerModel);
        }
    }

    // ========== VALIDATION METHODS ==========
    
    /**
     * Refined Validation: Checks if the Model Number already exists in the table.
     * Enforces unique Model Number across the entire system.
     */
public static boolean isPlateExists(String ModelNo, DefaultTableModel model) {
    System.out.println("Checking for ModelNo: '" + ModelNo + "'");
    for (int i = 0; i < model.getRowCount(); i++) {
        String existingModel = model.getValueAt(i, 3).toString().trim();
        System.out.println("Existing: '" + existingModel + "'");
        if (existingModel.equalsIgnoreCase(ModelNo.trim())) {
            return true;
        }
    }
    return false;
}

    // ========== CRUD OPERATIONS ==========

    public static void addNewCar(String name, String brand, String ModelNo, String carType, DefaultTableModel model) {
        // 1. Empty Field Validation
        if (name.trim().isEmpty() || brand.trim().isEmpty() || ModelNo.trim().isEmpty() || carType.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 2. Unique Model Number Validation
        if (isPlateExists(ModelNo, model)) {
    JOptionPane.showMessageDialog(
        null,
        "Model Number already exists!",
        "Duplicate Entry",
        JOptionPane.ERROR_MESSAGE
    );
    return;
}
        
        int nextSNo = getNextSerialNumber();
        gettersetter newCar = new gettersetter(nextSNo, name.trim(), brand.trim(), ModelNo.trim(), carType.trim());
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(newCar.toCSV());
            syncAllTables();
            JOptionPane.showMessageDialog(null, "Car added and synchronized!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving to file: " + e.getMessage());
        }
    }

    public static void updateCar(int rowIndex, String name, String brand, String ModelNo, String carType, DefaultTableModel model) {
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "Please select a car to update!");
            return;
        }

        // Check if the modified ModelNo conflicts with ANOTHER row
        for (int i = 0; i < model.getRowCount(); i++) {
            if (i != rowIndex) {
                if (model.getValueAt(i, 3).toString().equalsIgnoreCase(ModelNo.trim())) {
                    JOptionPane.showMessageDialog(null, "Update failed: Model Number already exists!");
                    return;
                }
            }
        }

        model.setValueAt(name.trim(), rowIndex, 1);
        model.setValueAt(brand.trim(), rowIndex, 2);
        model.setValueAt(ModelNo.trim(), rowIndex, 3);
        model.setValueAt(carType.trim(), rowIndex, 4);
        
        saveAllCars(model);
        syncAllTables();
        JOptionPane.showMessageDialog(null, "Car updated successfully!");
    }

    public static void deleteCar(int rowIndex, DefaultTableModel model) {
        if (rowIndex >= 0) {
            int confirm = JOptionPane.showConfirmDialog(null, "Delete this car?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                model.removeRow(rowIndex);
                saveAllCars(model);
                syncAllTables();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a car to delete!");
        }
    }

    // ========== FILE I/O ==========

    public static void saveAllCars(DefaultTableModel model) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String line = String.join(",",
                    model.getValueAt(i, 1).toString(),
                    model.getValueAt(i, 2).toString(),
                    model.getValueAt(i, 3).toString(),
                    model.getValueAt(i, 4).toString()
                );
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("Save error: " + e.getMessage());
        }
    }

    public static void loadAllCars(DefaultTableModel model) {
        model.setRowCount(0);
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    model.addRow(new Object[]{sno++, data[0], data[1], data[2], data[3]});
                }
            }
        } catch (IOException e) {
            System.err.println("Load error: " + e.getMessage());
        }
    }

    public static void loadAllCarsForCustomer(DefaultTableModel model) {
        model.setRowCount(0);
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int sno = 1;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    // Customer view: Displays Name, Brand, and Type (often hides ID/ModelNo)
                    model.addRow(new Object[]{sno++, data[0], data[1], data[3]});
                }
            }
        } catch (IOException e) {
            System.err.println("Customer Load error: " + e.getMessage());
        }
    }

    private static int getNextSerialNumber() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return 1;
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) count++;
        } catch (IOException e) {}
        return count + 1;
    }

    // Constructor and Getters/Setters
    public car(String name, String brand, String ModelNo, String carType) {
        this.name = name;
        this.brand = brand;
        this.ModelNo = ModelNo;
        this.carType = carType;
    }
}