package unipd.astro.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: ObservationEntity
 *
 */
@Entity
@Table(name = "OBSERVATIONS")
public class Observation implements Serializable {
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Observation_Id")
    private int id;
    
    @Column
    private String targetName;
    
    @Column
    private boolean isEnabled = true;
    
    @Column
    private boolean doPrered = true;
    
    @Column
    private boolean doWlcal = true;
    
    @Column
    private boolean doFcal = true;
    
    @Column
    private boolean doBackground = true;
    
    @Column
    private boolean doApall = true;
    
    @Column
    private boolean doScombine = true;
    
    @Column
    private boolean doImcopy = true;

	@OneToMany(mappedBy="observation", cascade={CascadeType.REMOVE})
    List<ScienceImage> scienceImages;
    
    @ManyToOne
    @JoinColumn(name="Standard_Id", nullable=true)
    StandardImage standard;
    
    public List<ScienceImage> getScienceImages() {
		return scienceImages;
	}

	public void setScienceImages(List<ScienceImage> scienceImages) {
		this.scienceImages = scienceImages;
		this.targetName = scienceImages.get(0).image.getTargetName();
		for(ScienceImage item : scienceImages)
			if(item.getLamp() == null) {
				this.doWlcal = false;
				break;
			}
		
		for(ScienceImage item : scienceImages)
			if(item.getStandard() == null) {
				this.doFcal = false;
				break;
			}
	}

	public StandardImage getStandard() {
		return standard;
	}

	public void setStandard(StandardImage standard) {
		this.standard = standard;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getId() {
		return id;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isDoPrered() {
		return doPrered;
	}

	public boolean isDoWlcal() {
		return doWlcal;
	}

	public boolean isDoFcal() {
		return doFcal;
	}

	public boolean isDoBackground() {
		return doBackground;
	}

	public boolean isDoApall() {
		return doApall;
	}

	public boolean isDoScombine() {
		return doScombine;
	}

	public boolean isDoImcopy() {
		return doImcopy;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setDoPrered(boolean doPrered) {
		this.doPrered = doPrered;
	}

	public void setDoWlcal(boolean doWlcal) {
		this.doWlcal = doWlcal;
	}

	public void setDoFcal(boolean doFcal) {
		this.doFcal = doFcal;
	}

	public void setDoBackground(boolean doBackground) {
		this.doBackground = doBackground;
	}

	public void setDoApall(boolean doApall) {
		this.doApall = doApall;
	}

	public void setDoScombine(boolean doScombine) {
		this.doScombine = doScombine;
	}

	public void setDoImcopy(boolean doImcopy) {
		this.doImcopy = doImcopy;
	}

	public String getTargetName() {
		return targetName;
	}
}
