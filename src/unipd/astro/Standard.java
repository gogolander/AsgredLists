/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unipd.astro;

/**
 *
 * @author Vincenzo Abate
 */
public class Standard {

    public Standard() {
    }

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
