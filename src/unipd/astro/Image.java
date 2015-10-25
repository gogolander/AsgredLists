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

import java.io.IOException;

/**
 * Image class represent the images *.fits.
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class Image {

    public static StandardList standardList;

    /**
     * Constructor
     *
     * @param fileName name of the file "IMA******.fits"
     * @param targetName name of the target, read from the header
     * @param type type of the image. Can be: "FLATFIELD", "LAMP" or "IMAGE"
     * @param expTime exposure time, read from the header
     * @param lampName reference lamp image name
     * @param standardName reference standard star image name
     * @param isStandard is the target name a standard star? default must be:
     * false
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

    /**
     * Parse an Image from a given line, tipically read from the fits_list.
     * Lines wich starts with "#" are comments and hence will be skipped.
     *
     * @param image line that contains the image properties read from the image
     * header
     * @return
     * @throws IOException
     */
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
