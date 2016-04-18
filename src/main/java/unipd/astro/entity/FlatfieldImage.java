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

import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: FlatfieldImage
 *
 */
@Entity
@Table(name="FLATFIELDS_IMAGES")
public class FlatfieldImage {	
	@Id
	@GeneratedValue
	@Column(name="Flatfield_Id")
	private int id;

	@OneToMany(mappedBy="flat", cascade={CascadeType.REMOVE})
	List<ImageEntity> images;
		
	public void setImages(List<ImageEntity> images) {
		this.images = images;
	}
	
	public List<ImageEntity> getImages() {
		return images;
	}

	public int getId() {
		return id;
	}
}
