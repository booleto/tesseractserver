/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseractReader.model;

/**
 *
 * @author toanlh-10
 */
public enum ParamsDefault {
    PageSegMode("3");
    
    private final String defaultVal;
   
    ParamsDefault(String defaultVal) {
        this.defaultVal = defaultVal;
    }
    
    public String get() {
        return defaultVal;
    }
}
