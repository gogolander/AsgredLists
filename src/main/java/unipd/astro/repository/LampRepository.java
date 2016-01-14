package unipd.astro.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import unipd.astro.entity.LampImage;

@Repository("LampRepository")
@Transactional(propagation = Propagation.REQUIRED)
public interface LampRepository extends CrudRepository<LampImage, Integer> {
}
