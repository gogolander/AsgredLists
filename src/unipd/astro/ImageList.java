/*
 * ImageList takes care of managing all the infos about the images collected
 * during the night.
 */
package unipd.astro;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Vincenzo Abate
 */
public class ImageList extends ArrayList<Image> {
    private String firstLamp = "";

    public ImageList() {
    }
    
    public String getFirstLamp() {
        return firstLamp;
    }

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

    public Iterable<Image> getStandards() {
        ImageList temp = new ImageList();
        for (Image image : this) {
            if (image.isStandard()) {
                temp.add(image);
            }
        }
        return temp;
    }

    public Iterable<Image> getFlats() {
        ImageList temp = new ImageList();
        for (Image image : this) {
            if (image.getType().equals("FLATFIELD")) {
                temp.add(image);
            }
        }
        return temp;
    }

    public Iterable<String> generateTargetsList() {
        ArrayList<String> targetList = new ArrayList<>();
        for (Image image : this) {
            if (image.getType().equals("IMAGE") && !targetList.contains(image.getTargetName())) {
                targetList.add(image.getTargetName());
            }
        }
        return targetList;
    }

    public Iterable<String> getImagesFileNameWhoseStandardIs(String standardImageName) {
        ArrayList<String> targetList = new ArrayList<>();
        for (Image image : this) {
            if (image.getStandardName().equals(standardImageName)) {
                targetList.add(image.getFileName());
            }
        }
        return targetList;
    }
    
    public ImageList getImagesWhoseStandardIs(String standardImageName) {
        ImageList targetList = new ImageList();
        for (Image image : this) {
            if (image.getStandardName().equals(standardImageName)) {
                targetList.add(image);
            }
        }
        return targetList;
    }

    public Iterable<String> getImagesFileNameWhoseTargetIs(String targetName) {
        ArrayList<String> targetList = new ArrayList<>();
        for (Image image : this) {
            if (image.getTargetName().equals(targetName)) {
                targetList.add(image.getFileName());
            }
        }
        return targetList;
    }
    
    public ImageList getImagesWhoseTargetIs(String targetName) {
        ImageList targetList = new ImageList();
        for (Image image : this) {
            if (image.getTargetName().equals(targetName)) {
                targetList.add(image);
            }
        }
        return targetList;
    }
    
    public Image getImageWhoseFileNameIs(String fileName) {
        for (Image image : this) {
            if (image.getFileName().equals(fileName)) {
                return image;
            }
        }
        return null;
    }
    
    public String generateFlatList() {
        String temp = "";
        for (Image image : this) {
            if (image.getType().equals("FLATFIELD")) {
                temp += image.getFileName() + "\n";
            }
        }
        return temp.trim();
    }

    public void fixStandards() {
        for (Image image : this) {
            if (image.getType().equals("IMAGE") && image.getStandardName().equals("")) {
                Iterator<Image> scrollList = this.iterator();
                //Scorri fino a trovare l'immagine corrente
                while (scrollList.hasNext() && !scrollList.next().equals(image));
                //Vai alla prima stella standard successiva all'immagine corrente
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

    public void fixLamps() {
        for (Image image : this) {
            if (image.getType().equals("IMAGE") && image.getLampName().equals("")) {
                Iterator<Image> scrollList = this.iterator();
                //Scorri fino a trovare l'immagine corrente
                while (scrollList.hasNext() && !scrollList.next().equals(image));
                //Vai alla prima lampada successiva all'immagine corrente
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
}
