/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

/**
 * Main file
 * 
 * @author Detobel36
 */
public class OWL {

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        new Graph();
        // Create a plan for test
        System.out.println("Chargement du OF");
        Graph.getPlan("of");
        
        
    }
    
    
}
