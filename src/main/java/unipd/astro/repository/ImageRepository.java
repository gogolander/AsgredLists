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
 * @author Vincenzo Abate
 */
@Repository("ImageRepository")
@Transactional(propagation = Propagation.REQUIRED)
public interface ImageRepository extends CrudRepository<ImageEntity, Integer> {
    List<ImageEntity> findByTargetName(String targetName);
    ImageEntity findByFileName(String fileName);
    List<ImageEntity> findByFileNameAndTargetName(String fileName, String targetName);
    List<ImageEntity> findByType(String type);
    List<ImageEntity> findByGrouped(boolean isGrouped);
    
    @Query("select fileName from ImageEntity image where type=?1")
    List<String> getFileNameByType(String type);
    
    @Query("select fileName from ImageEntity image where isStandard=?1")
    List<String> getFileNameByIsStandard(boolean isStandard);
    
	List<ImageEntity> findByIsStandard(boolean isStandard);
	
	@Query("select fileName from ImageEntity image where type='IMAGE' and enabled = TRUE")
	List<String> getTargetList();
	
	@Query("select distinct target.image.targetName,target.standard.image.fileName" +
			" from ScienceImage target"+
			" where target.image.type='IMAGE' and target.image.enabled = TRUE" +
			" order by target.standard.image.fileName")
	List<Object[]> getTargetNameAndStandardFileName();
	
	@Query("select fileName from ImageEntity image where isStandard=?1 and enabled = ?2")
	List<String> getFileNameByIsStandardAndIsEnabled(boolean isStandard, boolean isEnabled);

	@Query("select fileName from ImageEntity image where type=?1 and enabled = ?2")
	List<String> getFileNameByTypeAndIsEnabled(String type, boolean isEnabled);
	
	@Query("SELECT fileName FROM ImageEntity image WHERE image.grouped=?1 AND image.type LIKE 'LAMP'")
	List<String> getLampsByGrouped(boolean grouped);
}
