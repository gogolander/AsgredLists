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
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: LampImage
 *
 */
@Entity
@Table(name="LAMPS_IMAGES")
public class LampImage implements Serializable {

	
	public void setScienceImages(List<ScienceImage> scienceImages) {
		this.scienceImages = scienceImages;
	}

	public void setStandardImages(List<StandardImage> standardImages) {
		this.standardImages = standardImages;
	}

	private static final long serialVersionUID = 1L;

	public LampImage() {
		super();
	}
	
	@Id
	@GeneratedValue
	@Column(name="Lamp_Id")
	private int id;
   
	@OneToOne
	@JoinColumn(name="Image_Id", nullable=false)
	ImageEntity image;
	
	@ManyToOne
	@JoinColumn(name="Flatfield_Id", nullable=true)
	FlatfieldImage flat;
	
	@OneToMany(mappedBy="lamp")
	List<ScienceImage> scienceImages;
	
	@OneToMany(mappedBy="lamp")
	List<StandardImage> standardImages;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<StandardImage> getStandardImages() {
		return standardImages;
	}

	public List<ScienceImage> getScienceImages() {
		return scienceImages;
	}

	public ImageEntity getImage() {
		return image;
	}

	public void setImage(ImageEntity image) {
		this.image = image;
	}

	public FlatfieldImage getFlat() {
		return flat;
	}

	public void setFlat(FlatfieldImage flat) {
		this.flat = flat;
	}

	public int getId() {
		return id;
	}
}
