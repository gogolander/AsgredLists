package unipd.astro.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import unipd.astro.entity.StandardEntity;

@Repository("StandardAtlas")
@Transactional(propagation = Propagation.REQUIRED)
public interface StandardAtlas extends CrudRepository<StandardEntity, Integer> {
	StandardEntity findByStandardName(String standardName);
	StandardEntity findByAliasName(String aliasName);
	List<StandardEntity> findByCatalogueName(String catalogueName);
}
