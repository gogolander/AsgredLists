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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Enzo
 */
@Entity
@Table(name = "images")
public class ImageEntity {
    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getType() {
        return type;
    }

    public boolean isIsStandard() {
        return isStandard;
    }

    public float getExpTime() {
        return expTime;
    }
}
