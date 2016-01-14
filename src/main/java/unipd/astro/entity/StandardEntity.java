/*
 * Copyright (C) 2015 Enzo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
import javax.persistence.Table;
/**
 * Entity for the spectro-photometric standard stars atlas
 * @author Enzo
 */
@Entity
@Table(name = "STANDARDS")
public class StandardEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "StandardName")
    private String standardName;
    
    @Column(name = "CatalogueName")
	private String catalogueName;
    
    @Column(name = "AliasName")
    private String aliasName;

    public Long getId() {
        return id;
    }

    public String getStandardName() {
		return standardName;
	}

	public void setStandardName(String standardName) {
		this.standardName = standardName;
	}

	public String getCatalogueName() {
		return catalogueName;
	}

	public void setCatalogueName(String catalogueName) {
		this.catalogueName = catalogueName;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getDatName() {
		if(!this.aliasName.isEmpty())
			return this.aliasName.toLowerCase();
		return this.standardName.toLowerCase();
	}
	
	public static StandardEntity parseEntity(String line) {
        String[] args = line.split(" ");
        if (line.startsWith("#") &&( args.length != 2 && args.length != 3)) {
            return null;
        } else if (args.length == 2) {
        	StandardEntity newEntity = new StandardEntity();
        	newEntity.setStandardName(args[0]);
        	newEntity.setCatalogueName(args[1]);
        	newEntity.setAliasName("");
            return newEntity;
        } else {
        	StandardEntity newEntity = new StandardEntity();
        	newEntity.setStandardName(args[0]);
        	newEntity.setCatalogueName(args[1]);
        	newEntity.setAliasName(args[2]);
            return newEntity;
        }
    }
}
