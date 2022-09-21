package io.dtonic.dhubingestmodule.nifi.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdaptorVO {

    private String name;
    private boolean isCompleted;
    private List<NiFiComponentVO> NifiComponents = new ArrayList<>();
}
