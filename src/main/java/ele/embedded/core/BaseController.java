package ele.embedded.core;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('ROLE_ADMIN')")
public abstract class BaseController<Entity extends BaseEntity, Repository extends BaseRepository<Entity>, RequestDTO , ResponseDTO , Service extends BaseService<Entity, RequestDTO, ResponseDTO, Repository>> {

  @Autowired
  protected Service service;

  @GetMapping("")
  public List<ResponseDTO> findAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public ResponseDTO findById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDTO create(@Valid @RequestBody RequestDTO requestDto) {
    return service.create(requestDto);
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseDTO update(@PathVariable UUID id, @Valid @RequestBody RequestDTO requestDto) {
    return service.update(id, requestDto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}