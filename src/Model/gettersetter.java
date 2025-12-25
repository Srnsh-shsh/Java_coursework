/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;


public class gettersetter {
    private int sno;
    private String name;
    private String brand;
    private String plateNo;
    private String carType;

    // Constructors
    public gettersetter() {}  // Default constructor
    
    public gettersetter(int sno, String name, String brand, String plateNo, String carType) {
        this.sno = sno;
        this.name = name;
        this.brand = brand;
        this.plateNo = plateNo;
        this.carType = carType;
    }
    
    public gettersetter(String name, String brand, String plateNo, String carType) {
        this.name = name;
        this.brand = brand;
        this.plateNo = plateNo;
        this.carType = carType;
    }

    // Getters and Setters
    public int getSno() { return sno; }
    public void setSno(int sno) { this.sno = sno; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    
    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }

    // Convert to CSV for file saving
    public String toCSV() {
        return name + "," + brand + "," + plateNo + "," + carType;
    }
    
    // Convert to Object array for JTable
    public Object[] toTableRow() {
        return new Object[]{sno, name, brand, plateNo, carType};
    }
}