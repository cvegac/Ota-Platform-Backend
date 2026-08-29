package ele.embedded.core;

import ele.embedded.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseService<Entity, RequestDTO, ResponseDTO, Repository extends BaseRepository<Entity>> {

  @Autowired
  protected Repository repository;

  @Autowired
  protected ModelMapper modelMapper;

  public BaseService() {
    this.modelMapper = new ModelMapper();
  }

  protected abstract ResponseDTO convertToDTO(Entity entity);

  protected abstract Entity convertToEntity(RequestDTO requestDto);

  public List<ResponseDTO> getAll() {
    List<Entity> entities = repository.findAll();
    return entities.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
  }

  public ResponseDTO getById(UUID id) {
    Entity entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
    return convertToDTO(entity);
  }

  @Transactional
  public ResponseDTO create(RequestDTO requestDto) {
    Entity entity = convertToEntity(requestDto);
    entity = repository.save(entity);
    return convertToDTO(entity);
  }

  @Transactional
  public ResponseDTO update(UUID id, RequestDTO requestDto) {
    Entity entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
    updateEntityFields(entity, requestDto);
    entity = repository.save(entity);
    return convertToDTO(entity);
  }

  @Transactional
  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new ResourceNotFoundException("Entity not found with id: " + id);
    }
    repository.deleteById(id);
  }

  private void updateEntityFields(Entity entity, RequestDTO requestDto) {
    modelMapper.map(requestDto, entity);
  }

  @Transactional
  public Entity add(Entity entity){
    return repository.save(entity);
  }

  public Entity getIfEntityExistById(UUID entityId, String entityName) {
    return repository.findById(entityId)
            .orElseThrow(() -> new ResourceNotFoundException("Not found " + entityName + " with id = " + entityId));
  }

}
