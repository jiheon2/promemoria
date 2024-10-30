package kopo.analyzeservice.repository;

import kopo.analyzeservice.dto.MetaDTO;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaRepository extends ElasticsearchRepository<MetaDTO, String> {

    List<MetaDTO> findAllByUserIdAndDate(String userId, String date);
}
