package unipd.astro.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: FlatfieldImage
 *
 */
@Entity
@Table(name="FLATFIELDS_IMAGES")
public class FlatfieldImage implements Serializable {	
	public void setImages(List<ImageEntity> images) {
		this.images = images;
	}

	private static final long serialVersionUID = 1L;

	public FlatfieldImage() {
		super();
	}
	
	@Id
	@GeneratedValue
	@Column(name="Flatfield_Id")
	private int id;

	@OneToMany(mappedBy="flat", cascade={CascadeType.REMOVE})
	List<ImageEntity> images;
	
	public List<ImageEntity> getImages() {
		return images;
	}

	public int getId() {
		return id;
	}
}
