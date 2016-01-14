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
package unipd.astro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import unipd.astro.entity.ImageEntity;

/**
 *
 * @author Enzo
 */
@Repository("ImageRepository")
@Transactional(propagation = Propagation.REQUIRED)
public interface ImageRepository extends CrudRepository<ImageEntity, Integer> {
    List<ImageEntity> findByTargetName(String targetName);
    ImageEntity findByFileName(String fileName);
    List<ImageEntity> findByFileNameAndTargetName(String fileName, String targetName);
    List<ImageEntity> findByType(String type);
    
    @Query("select fileName from ImageEntity image where type=?1")
    List<String> getFileNameByType(String type);
    
    @Query("select fileName from ImageEntity image where isStandard=?1")
    List<String> getFileNameByIsStandard(boolean isStandard);
    
	List<ImageEntity> findByIsStandard(boolean isStandard);
	
	@Query("select fileName from ImageEntity image where type='IMAGE'")
	List<String> getTargetList();
	
	@Query("select distinct target.image.targetName,target.standard.image.fileName" +
			" from ScienceImage target"+
			" where target.image.type='IMAGE'" +
			" order by target.standard.image.fileName")
	List<Object[]> getTargetNameAndStandardFileName();
}
