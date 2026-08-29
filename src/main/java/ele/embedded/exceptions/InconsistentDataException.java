package ele.embedded.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class InconsistentDataException extends RuntimeException {
  public InconsistentDataException(String msg) {
    super(msg);
  }
}
