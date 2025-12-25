/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

public class gettersetter {

    private int sno;
    private String name;
    private String brand;
    private String ModelNo;
    private String carType;

    public gettersetter(int sno, String name, String brand, String ModelNo, String carType) {
        this.sno = sno;
        this.name = name;
        this.brand = brand;
        this.ModelNo = ModelNo;
        this.carType = carType;
    }

    // getters
    public int getSno() { return sno; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getPlateNo() { return ModelNo; }
    public String getCarType() { return carType; }

    // setters
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setPlateNo(String ModelNo) { this.ModelNo = ModelNo; }
    public void setCarType(String carType) { this.carType = carType; }

    // for JTable
    public Object[] toTableRow() {
        return new Object[]{sno, name, brand, ModelNo, carType};
    }

    public boolean toCSV() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
