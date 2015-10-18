/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unipd.astro;

import java.io.IOException;

/**
 *
 * @author Vincenzo Abate
 */
public class Image {

    public static StandardList standardList;

    /**
     *
     * @param fileName
     * @param targetName
     * @param expTime
     * @param lampName
     * @param standardName
     * @param isStandard
     */
    public Image(String fileName, String targetName, String type, float expTime, String lampName, String standardName, boolean isStandard) {
        this.fileName = fileName;
        this.targetName = targetName;
        this.type = type;
        this.expTime = expTime;
        this.lampName = lampName;
        this.standardName = standardName;
        this.isStandard = isStandard;
    }

    private String fileName;
    private String targetName;
    private float expTime;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    private String lampName;
    private String standardName;
    private boolean isStandard;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public float getExpTime() {
        return expTime;
    }

    public void setExpTime(float expTime) {
        this.expTime = expTime;
    }

    public String getLampName() {
        return lampName;
    }

    public void setLampName(String lampName) {
        this.lampName = lampName;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public boolean isStandard() {
        return isStandard;
    }

    public void setIsStandard(boolean isStandard) {
        this.isStandard = isStandard;
    }

    public static Image parseImage(String image) throws IOException {
        if (standardList == null) {
            standardList = new StandardList();
        }

        if (image.startsWith("#")) {
            return null;
        }
        /**
         * PARAMS MAP: file name, target name, image type, exposure time
         */
        String[] params = image.split("\t");
        String type = "IMAGE";
        if (params[2].equals("LAMP")) {
            type = "LAMP";
        } else if (params[1].toLowerCase().contains("flat")) {
            type = "FLATFIELD";
        }
        if (standardList.isStandard(params[1])) {
            return new Image(params[0], params[1], type, Float.parseFloat(params[3]), "", params[0], true);
        } else {
            return new Image(params[0], params[1], type, Float.parseFloat(params[3]), "", "", false);
        }
    }
}
