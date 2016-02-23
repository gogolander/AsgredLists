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

import unipd.astro.entity.Observation;

@Repository("ObservationRepository")
@Transactional(propagation=Propagation.REQUIRED)
public interface ObservationRepository extends CrudRepository<Observation, Integer> {
	@Query("select observation" +
			" from Observation observation"+
				" where observation.targetName=?1 and observation.standard.image.fileName=?2")
	Observation findByTargetNameAndStandardFileName(String targetName, String standardFileName);
	
	List<Observation> findByIsEnabled(boolean isEnabled);
}
