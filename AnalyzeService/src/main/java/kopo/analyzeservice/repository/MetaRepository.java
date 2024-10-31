package kopo.analyzeservice.repository;

import kopo.analyzeservice.dto.MetaDTO;
import kopo.analyzeservice.repository.document.Metadata;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends ElasticsearchRepository<Metadata, String> {
    List<MetaDTO> findAllByUploadIdentifier(String UploadIdentifier);
}
