/**
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU General Public License as published by
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
package unipd.astro.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import unipd.astro.service.DataService;

/**
 *
 * @author Enzo
 */
@Entity
@Table(name = "IMAGES")
public class ImageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public LampImage getLamp() {
		return lamp;
	}

	public void setLamp(LampImage lamp) {
		this.lamp = lamp;
	}

	public ScienceImage getScience() {
		return science;
	}

	public void setScience(ScienceImage science) {
		this.science = science;
	}

	public StandardImage getStandard() {
		return standard;
	}

	public void setStandard(StandardImage standard) {
		this.standard = standard;
	}

	public void setFlat(FlatfieldImage flat) {
		this.flat = flat;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ImageEntity_Id")
    private Long id;

    @Column(name = "FileName")
    private String fileName;

    @Column(name = "TargetName")
    private String targetName;

    @Column(name = "Type")
    private String type;

    @Column(name = "IsStandard")
    private boolean isStandard;

    @Column(name = "ExpTime")
    private float expTime;

    @ManyToOne
    @JoinColumn(name="Flat_Id")
    FlatfieldImage flat;
    
    @OneToOne(mappedBy="image")
    LampImage lamp;
    
    @OneToOne(mappedBy="image")
    ScienceImage science;
    
    @OneToOne(mappedBy="image")
    StandardImage standard;
    
    public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public FlatfieldImage getFlat() {
		return flat;
	}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isStandard() {
		return isStandard;
	}

	public void setStandard(boolean isStandard) {
		this.isStandard = isStandard;
	}

	public float getExpTime() {
		return expTime;
	}

	public void setExpTime(float expTime) {
		this.expTime = expTime;
	}

	public Long getId() {
        return id;
    }
    
    public static ImageEntity parseEntity(String image) {
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
        
        ImageEntity newEntity = new ImageEntity();
        newEntity.setFileName(params[0]);
        newEntity.setTargetName(params[1]);
        newEntity.setType(type);
        newEntity.setStandard((DataService.getInstance().getStandardAtlas().findByStandardName(params[1]) != null));
        newEntity.setExpTime(Float.parseFloat(params[3]));
        return newEntity;
    }
}
