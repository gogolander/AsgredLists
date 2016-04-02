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
    
    @Column(name="targetName")
    private String targetName;
    
    @Column(name="isEnabled")
    private boolean isEnabled = true;
    
    @Column(name="doPrered")
    private boolean doPrered = true;
    
    @Column(name="doWlcal")
    private boolean doWlcal = true;
    
    @Column(name="doFcal")
    private boolean doFcal = true;
    
    @Column(name="doBackground")
    private boolean doBackground = true;
    
    @Column(name="doApall")
    private boolean doApall = true;
    
    @Column(name="doScombine")
    private boolean doScombine = true;
    
    @Column(name="doImcopy")
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
