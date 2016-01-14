package unipd.astro.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import unipd.astro.entity.StandardImage;

@Repository("StandardRepository")
@Transactional(propagation = Propagation.REQUIRED)
public interface StandardRepository extends CrudRepository<StandardImage, Integer> {

	@Query("select standard"
			+ " from StandardImage standard"
			+ " where standard.image.fileName=?1")
	StandardImage findByFileName(String fileName);
}
