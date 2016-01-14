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
