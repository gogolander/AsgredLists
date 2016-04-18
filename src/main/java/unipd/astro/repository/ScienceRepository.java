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
	@Query("SELECT COUNT(s)" +
				" FROM ScienceImage s" +
				" WHERE s IN (" +
					" SELECT image" +
					" FROM ScienceImage image" +
					" WHERE (image.standard IS NULL OR image.lamp IS NULL))")
	int findConflicts();
	
	@Query("SELECT COUNT(s)" +
			" FROM ScienceImage s" +
			" WHERE s IN (" +
				" SELECT image" +
					" FROM ScienceImage image" +
					" WHERE image.image.targetName=?1 AND image.standard IS NULL" +
					" OR image.lamp IS NULL)")
	int findConflictsForTargetName(String targetName);
	
	@Query("SELECT COUNT(s)" +
			" FROM ScienceImage s" +
			" WHERE s IN (" +
				" SELECT image" +
				" FROM ScienceImage image" +
				" WHERE image.lamp IS NULL AND image.image.targetName=?1)")
	int getIsLampMissingForTargetName(String targetName);
	
	@Query("SELECT COUNT(s)" +
			" FROM ScienceImage s" +
			" WHERE s in (" +
				" SELECT image" +
				" FROM ScienceImage image" +
				" WHERE image.standard IS NULL AND image.image.targetName=?1)")
	int getIsStandardMissingForTargetName(String targetName);
	
	@Query("SELECT DISTINCT target" +
			" FROM ScienceImage target"+
				" WHERE target.image.targetName=?1 AND target.standard.image.fileName=?2")
	List<ScienceImage> getScienceImageByTargetNameAndStandardFileName(String targetName, String standardFileName);
	
	@Query("SELECT DISTINCT science.lamp.lampName" +
			" FROM ScienceImage science"+
				" WHERE science.observation.isEnabled = TRUE"+
				" AND science.image.enabled = TRUE"
				+ " ORDER BY science.lamp.lampName ASC")
	List<String> getLamps();
}
