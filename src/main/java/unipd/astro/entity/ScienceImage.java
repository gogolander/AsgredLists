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
import javax.persistence.*;

/**
 * Entity implementation class for Entity: ScienceImage
 *
 */
@Entity
@Table(name="SCIENCE_IMAGES")
public class ScienceImage implements Serializable {
	private static final long serialVersionUID = 1L;

	public ScienceImage() {
		super();
	}
	
	@Id
	@GeneratedValue
	@Column(name="Science_Id")
	private int id;
	
	@ManyToOne
	@JoinColumn(name="Flatfield_Id", nullable=true)
	FlatfieldImage flat; 
	
	@OneToOne
	@JoinColumn(name="Image_Id")
	ImageEntity image;
	
	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	@ManyToOne
	@JoinColumn(name="Lamp_Id", nullable=true)
	LampImage lamp;
	
	@ManyToOne
	@JoinColumn(name="Standard_Id", nullable=true)
	StandardImage standard;
	
	@ManyToOne
	@JoinColumn(name="Observation_Id", nullable=true)
	Observation observation;
	
	public LampImage getLamp() {
		return lamp;
	}

	public void setLamp(LampImage lamp) {
		this.lamp = lamp;
	}

	public FlatfieldImage getFlat() {
		return flat;
	}

	public void setFlat(FlatfieldImage flat) {
		this.flat = flat;
	}

	public StandardImage getStandard() {
		return standard;
	}

	public void setStandard(StandardImage standard) {
		this.standard = standard;
	}

	public ImageEntity getImage() {
		return image;
	}

	public void setImage(ImageEntity image) {
		this.image = image;
	}

	public int getId() {
		return id;
	}
}
