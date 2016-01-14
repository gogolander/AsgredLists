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
