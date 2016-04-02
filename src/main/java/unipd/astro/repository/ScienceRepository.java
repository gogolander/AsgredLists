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

import unipd.astro.entity.ScienceImage;

@Repository("ScienceRepository")
@Transactional(propagation = Propagation.REQUIRED)
public interface ScienceRepository extends CrudRepository<ScienceImage, Integer> {
	@Query("select count(s)" +
				" from ScienceImage s" +
				" where s in (" +
					" select image" +
					" from ScienceImage image" +
					" where (image.standard is null or image.lamp is null))")
	int findConflicts();
	
	@Query("select count(s)" +
			" from ScienceImage s" +
			" where s in (" +
				" select image" +
					" from ScienceImage image" +
					" where image.image.targetName=?1 and image.standard is null" +
					" or image.lamp is null)")
	int findConflictsForTargetName(String targetName);
	
	@Query("select count(s)" +
			" from ScienceImage s" +
			" where s in (" +
				" select image" +
				" from ScienceImage image" +
				" where image.lamp is null and image.image.targetName=?1)")
	int getIsLampMissingForTargetName(String targetName);
	
	@Query("select count(s)" +
			" from ScienceImage s" +
			" where s in (" +
				" select image" +
				" from ScienceImage image" +
				" where image.standard is null and image.image.targetName=?1)")
	int getIsStandardMissingForTargetName(String targetName);
	
	@Query("select distinct target" +
			" from ScienceImage target"+
				" where target.image.targetName=?1 and target.standard.image.fileName=?2")
	List<ScienceImage> getScienceImageByTargetNameAndStandardFileName(String targetName, String standardFileName);
	
	@Query("select distinct science.lamp.image.fileName" +
			" from ScienceImage science"+
				" where science.observation.isEnabled = TRUE"+
				" and science.image.enabled = TRUE"
				+ " order by science.lamp.image.fileName asc")
	List<String> getLamps();
}
