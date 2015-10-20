/*
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * StandardList stores all the spectro-photometric standard stars in the Asiago
 * atlas and makes the necessary query.
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
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

    /**
     * This method determines if the given target name is relative to a standard
     * star.
     *
     * @param name Target name of the image.
     * @return
     */
    public boolean isStandard(String name) {
        for (Standard star : this) {
            if (star.getStandardName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method determines the name of the .dat file of the given standard
     * star.
     *
     * @param name Name of the standard star whose data you want to get.
     * @return
     */
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

    /**
     * This method determines the name of the catalogue in onedspec$ which
     * contains data of the given standard star.
     *
     * @param stdName Name of the standard star whose data you want to get.
     * @return
     */
    public String getCatalogue(String stdName) {
        for (Standard star : this) {
            if (star.getStandardName().equals(stdName) || (!star.getAliasName().isEmpty() && star.getAliasName().equals(stdName))) {
                return star.getCatalogueName();
            }
        }
        return "";
    }
}
