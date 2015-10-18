/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unipd.astro;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vincenzo Abate
 */
public class StandardList extends ArrayList<Standard> {

    public StandardList() throws FileNotFoundException, IOException {
        String line = "";
        try {
            BufferedReader standardFile = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("unipd/astro/resources/standard.list")));
            do {
                line = standardFile.readLine();
                //Ignore comments, empty lines and EOF
                if (line != null && !line.startsWith("#") && !line.equals("")) {
                    Standard standard = Standard.parseStandard(line);
                    this.add(standard);
                }
            } while (line != null);
        } catch (Exception ex) {
            Logger.getLogger(AsgredLists.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isStandard(String name) {
        for (Standard star : this) {
            if (star.getStandardName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getDatName(String name) {
        for (Standard star : this) {
            if (star.getStandardName().equals(name) || (!star.getAliasName().isEmpty() && star.getAliasName().equals(name))) {
                if (star.getStandardName().equals(name)) {
                    if (star.getAliasName().isEmpty()) {
                        return star.getStandardName().toLowerCase();
                    } else {
                        return star.getAliasName().toLowerCase();
                    }
                }
            }
        }
        return "";
    }

    public String getCatalogue(String stdName) {
        for (Standard star : this) {
            if (star.getStandardName().equals(stdName) || (!star.getAliasName().isEmpty() && star.getAliasName().equals(stdName))) {
                return star.getCatalogueName();
            }
        }
        return "";
    }
}
