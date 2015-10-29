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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * ImageList stores all images and makes the necessary query.
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class ImageList extends ArrayList<Image> {
    private String firstLamp = "";

    /**
     * Constructor
     */
    public ImageList() {
    }

    /**
     * This method gets the file name of the first lamp taken in the current
     * list.
     *
     * @return Lamp file name, i.e. "IMA??????"
     */
    public String getFirstLamp() {
        return firstLamp;
    }

    /**
     * This method gets all the lamp images in this list.
     *
     * @return All the lamps images.
     */
    public Iterable<Image> getLamps() {
        ImageList temp = new ImageList();
        for (Image image : this) {
            if (image.getType().equals("LAMP")) {
                temp.add(image);
            }
            if (this.firstLamp.equals("")) {
                this.firstLamp = image.getFileName();
            }
        }
        return temp;
    }

    /**
     * This method gets all the images regarding to spectro-photometric standard
     * stars in this list.
     *
     * @return All the images of standard stars.
     */
    public Iterable<Image> getStandards() {
        ImageList temp = new ImageList();
        for (Image image : this) {
            if (image.isStandard()) {
                temp.add(image);
            }
        }
        return temp;
    }

    /**
     * This method gets all the images regardind flat fields in this list.
     *
     * @return All the images of flat field.
     */
    public Iterable<Image> getFlats() {
        ImageList temp = new ImageList();
        for (Image image : this) {
            if (image.getType().equals("FLATFIELD")) {
                temp.add(image);
            }
        }
        return temp;
    }

    /**
     * This method generates the list of the targets names in this list images.
     * The targets list contains both objects and standard stars, but it doesn't contain
     * lamps nor flat field.
     *
     * @return String list of images wich type is "IMAGE".
     */
    public Iterable<String> generateTargetsList() {
        ArrayList<String> targetList = new ArrayList<>();
        for (Image image : this) {
            if (image.getType().equals("IMAGE") && !targetList.contains(image.getTargetName())) {
                targetList.add(image.getTargetName());
            }
        }
        return targetList;
    }

    /**
     * This method enerates the file name list of objects having the given file
     * name as standard star. You have to provide the file name of standard instead of
     * the name of the standard beacause different images of the same standard
     * have to be treated as different objects.
     *
     * @param standardImageName The file name of the standard that has to be
     * used for the flux calibration of a object.
     * @return String list of file names which are to be calibrated with the
     * given standard image.
     */
    public Iterable<String> getImagesFileNameWhoseStandardIs(String standardImageName) {
        ArrayList<String> targetList = new ArrayList<>();
        for (Image image : this) {
            if (image.getStandardName().equals(standardImageName)) {
                targetList.add(image.getFileName());
            }
        }
        return targetList;
    }

    /**
     * This method generates the images list of objects having the given file
     * name as standard star.
     *
     * @param standardImageName The file name of the standard that has to be
     * used for the flux calibration of a object.
     * @return List of the images which are to be calibrated with the given
     * standard image.
     */
    public ImageList getImagesWhoseStandardIs(String standardImageName) {
        ImageList targetList = new ImageList();
        for (Image image : this) {
            if (image.getStandardName().equals(standardImageName)) {
                targetList.add(image);
            }
        }
        return targetList;
    }

    /**
     * This method generates the list of file name of images of the given
     * target.
     *
     * @param targetName The name of the target
     * @return List of file names of images with the given target name
     */
    public Iterable<String> getImagesFileNameWhoseTargetIs(String targetName) {
        ArrayList<String> targetList = new ArrayList<>();
        for (Image image : this) {
            if (image.getTargetName().equals(targetName)) {
                targetList.add(image.getFileName());
            }
        }
        return targetList;
    }

    /**
     * This method generates the list of images of the given target.
     *
     * @param targetName The name of the target.
     * @return List of the images for the given target.
     */
    public ImageList getImagesWhoseTargetIs(String targetName) {
        ImageList targetList = new ImageList();
        for (Image image : this) {
            if (image.getTargetName().equals(targetName)) {
                targetList.add(image);
            }
        }
        return targetList;
    }

    /**
     * This method gets the image with the given file name.
     *
     * @param fileName The name of the file you want to get.
     * @return The image having the given file name.
     */
    public Image getImageWhoseFileNameIs(String fileName) {
        for (Image image : this) {
            if (image.getFileName().equals(fileName)) {
                return image;
            }
        }
        return null;
    }

    /**
     * This method generates a string containing the file name of all the images
     * having type "FLATFIELD". File names are separated by a new-line.
     *
     * @return One string containing all the flat field images.
     */
    public String generateFlatList() {
        String temp = "";
        for (Image image : this) {
            if (image.getType().equals("FLATFIELD")) {
                temp += image.getFileName() + "\n";
            }
        }
        return temp.trim();
    }

    /**
     * This method gets images whose flux must be calibrated but haven't got a
     * standard star and tries to determine which standard star to use.
     * Tipically it uses the first standard star after the image that misses it.
     */
    @SuppressWarnings("empty-statement")
    public void fixStandards() {
        for (Image image : this) {
            if (image.getType().equals("IMAGE") && image.getStandardName().equals("")) {
                Iterator<Image> scrollList = this.iterator();
                //Scroll the list until the current image is found
                while (scrollList.hasNext() && !scrollList.next().equals(image));
                //Scroll the list once again to get the image next to the current one
                while (scrollList.hasNext()) {
                    Image standard = scrollList.next();
                    if (standard.getType().equals("IMAGE") && standard.isStandard()) {
                        image.setStandardName(standard.getFileName());
                        break;
                    }
                }
            }
        }
    }

    /**
     * This method gets images whose wavelenght must be calibrated but haven't
     * got a reference lamp and tries to determine which lamp to use. Tipically
     * it uses the first lamp after the image that misses it.
     */
    @SuppressWarnings("empty-statement")
    public void fixLamps() {
        for (Image image : this) {
            if (image.getType().equals("IMAGE") && image.getLampName().equals("")) {
                Iterator<Image> scrollList = this.iterator();
                //Scroll the list until the current image is found
                while (scrollList.hasNext() && !scrollList.next().equals(image));
                //Scroll the list once again to get the image next to the current one
                while (scrollList.hasNext()) {
                    Image lamp = scrollList.next();
                    if (lamp.getType().equals("LAMP")) {
                        image.setLampName(lamp.getFileName());
                        break;
                    }
                }
            }
        }
    }

    /**
     * This method returns all file name of the images in the list with the given extension append
     * to it, e.g. "IMA000001.*extension*\nIMA000002.*extension*\nIMA000003.*extension*".
     *
     * @param imageName
     * @param extension
     * @return
     */
    public String appendExtension(String imageName, String extension) {
        String temp = "";
        for (Image image : this) {
            if (image.getFileName().equals(imageName)) {
                if (extension.contains(".")) {
                    temp = image.getFileName() + extension;
                } else {
                    temp = image.getFileName() + "." + extension;
                }
            }
        }
        return temp.trim();
    }

    @Override
    public String toString() {
        String temp = "";
        while (this.iterator().hasNext()) {
            temp += this.iterator().next().getFileName() + "\n";
        }
        return temp.trim();
    }

    public Iterable<String> toStringArray() {
        ArrayList<String> temp = new ArrayList<>();
        while (this.iterator().hasNext()) {
            temp.add(this.iterator().next().getFileName());
        }
        return temp;
    }
    
    public boolean containsConflicts() {
        for(Image image : this) {
            if(image.isBroken())
                return true;
        }
        return false;
    }
    
    public boolean lampsMissing() {
        for(Image image : this) {
            if(image.lampMissing())
                return true;
        }
        return false;
    }
    
    public boolean standardsMissing() {
        for(Image image : this) {
            if(image.standardMissing())
                return true;
        }
        return false;
    }
}
