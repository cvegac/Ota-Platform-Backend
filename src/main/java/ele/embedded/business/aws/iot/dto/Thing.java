package ele.embedded.business.aws.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Thing {
    String name;

    public Thing(String name){
        this.name = name;
    }
}
