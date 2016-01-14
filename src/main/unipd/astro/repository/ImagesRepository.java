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
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import unipd.astro.entity.ImageEntity;

/**
 *
 * @author Enzo
 */
@Repository("ImagesRepository")
@Transactional(propagation = Propagation.REQUIRED)
public interface ImagesRepository extends CrudRepository<ImageEntity, Long> {

    List<ImageEntity> findByTargetName(String targetName);

    List<ImageEntity> findByFileName(String fileName);

    List<ImageEntity> findByFileNameAndTargetName(String fileName, String targetName);

    List<ImageEntity> findByType(String type);

//    @Query("SELECT * FROM IMAGES WHERE TYPE LIKE 'LAMP'")
//    List<ImageEntity> findLamps();
//
//    @Query("SELECT * FROM IMAGES WHERE TYPE LIKE 'FLATFIELD")
//    List<ImageEntity> findFlats();
//
//    @Query("SELECT * FROM IMAGES WHERE TYPE LIKE 'IMAGE' AND NOT IsStandard")
//    List<ImageEntity> findScienceImages();
//
//    @Query("SELECT * FROM IMAGES WHERE TYPE LIKE 'IMAGE' AND IsStardard")
//    List<ImageEntity> findStandards();
//
//    @Query("SELECT targetName FROM IMAGES WHERE TYPE LIKE 'IMAGE'")
//    List<String> getTargetList();
}
