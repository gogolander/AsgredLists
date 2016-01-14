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
 * An observation is relative to a given target and it is made by: 1. flat field
 * images; 2. lamps; 3. target images; 4. standard star. Observations of the
 * same target with different standard stars are different observations.
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class Observation {

    public Observation(ImageList flatfieldsImages, ImageList lampsImages, ImageList targetImages, Image standardImage) {
        this.flatfieldsImages = flatfieldsImages;
        this.lampsImages = lampsImages;
        this.targetImages = targetImages;
        this.standardImage = standardImage;
    }

    public Observation() {
        this.flatfieldsImages = new ImageList();
        this.lampsImages = new ImageList();
        this.targetImages = new ImageList();
    }

    private ImageList flatfieldsImages;
    private ImageList lampsImages;
    private ImageList targetImages;
    private Image standardImage;

    public ImageList getFlatfieldsImages() {
        return flatfieldsImages;
    }

    public void setFlatfieldsImages(ImageList flatfieldsImages) {
        this.flatfieldsImages = flatfieldsImages;
    }

    public ImageList getLampsImages() {
        return lampsImages;
    }

    public void setLampsImages(ImageList lampsImages) {
        this.lampsImages = lampsImages;
    }

    public ImageList getTargetImages() {
        return targetImages;
    }

    public void setTargetImages(ImageList targetImages) {
        this.targetImages = targetImages;
    }

//    public ImageList getStandardImage() {
//        return standardImage;
//    }
//
//    public void setStandardImage(ImageList standardImage) {
//        this.standardImage = standardImage;
//    }

}
