package unipd.astro.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: StandardImage
 *
 */
@Entity
@Table(name="STANDARDS_IMAGES")
public class StandardImage implements Serializable {	
	private static final long serialVersionUID = 1L;

	public StandardImage() {
		super();
	}
	
	public List<Observation> getObservations() {
		return observations;
	}

	public void setObservation(List<Observation> observations) {
		this.observations = observations;
	}

	public void setLamp(LampImage lamp) {
		this.lamp = lamp;
	}

	public void setScienceImages(List<ScienceImage> scienceImages) {
		this.scienceImages = scienceImages;
	}

	public void setImage(ImageEntity image) {
		this.image = image;
	}

	@Id
	@GeneratedValue
	@Column(name="Standard_Id")
	int id;

	@ManyToOne
	@JoinColumn(name="Lamp_Id", nullable=true)
	LampImage lamp;
	
	@OneToOne
	@JoinColumn(name="Flatfield_Id", nullable=true)
	FlatfieldImage flat;
	
	@OneToMany(mappedBy="standard")
	List<ScienceImage> scienceImages;

	@OneToOne
	@JoinColumn(name="Image_Id")
	ImageEntity image;
	
	@OneToMany(mappedBy="standard")
//	@JoinColumn(name="Observation_Id", nullable=true)
	List<Observation> observations;
	
	public List<ScienceImage> getScienceImages() {
		return scienceImages;
	}
	
	public LampImage getLamp() {
		return lamp;
	}

	public FlatfieldImage getFlat() {
		return flat;
	}

	public void setFlat(FlatfieldImage flat) {
		this.flat = flat;
	}

	public ImageEntity getImage() {
		return image;
	}

	public int getId() {
		return id;
	}
}
