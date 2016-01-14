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

/**
 * This class represents the needed properties of standard stars
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class Standard {

    /**
     * Constructor
     */
    public Standard() {
    }

    /**
     * Constructor
     *
     * @param standardName name of the standard
     * @param catalogueName name of the onedspec$ directory wich contains the
     * standard data
     * @param aliasName name of the standard in the onedspec$ directory, if it
     * is other the standard name. Default value is "".
     */
    public Standard(String standardName, String catalogueName, String aliasName) {
        this.standardName = standardName;
        this.catalogueName = catalogueName;
        this.aliasName = aliasName;
    }

    private String standardName = "";
    private String catalogueName = "";
    private String aliasName = "";

    public String getStandardName() {
        return standardName;
    }

    public String getCatalogueName() {
        return catalogueName;
    }

    public String getAliasName() {
        return aliasName;
    }

    /**
     * Parse a Standard from a given line, tipically read from the standard.list
     * atlas. Lines wich starts with "#" are comments and hence will be skipped.
     *
     * @param line line that contains the image properties read from
     * standard.list
     * @return
     */
    public static Standard parseStandard(String line) {
        String[] args = line.split(" ");
        if (line.startsWith("#") &&( args.length != 2 && args.length != 3)) {
            return null;
        } else if (args.length == 2) {
            return new Standard(args[0], args[1], "");
        } else {
            return new Standard(args[0], args[1], args[2]);
        }
    }
}
