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
					" where image.standard is null or image.lamp is null)")
	int findConflicts();
	
	@Query("select count(s)" +
			" from ScienceImage s" +
			" where s in (" +
				" select image" +
					" from ScienceImage image" +
					" where image.image.targetName=?1 and (image.standard is null or image.lamp is null))")
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
}
