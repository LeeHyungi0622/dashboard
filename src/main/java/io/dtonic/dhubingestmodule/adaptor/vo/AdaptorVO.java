package io.dtonic.dhubingestmodule.adaptor.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdaptorVO {

    private String name;
    private boolean completed;

    @JsonProperty("nifiComponents")
    private List<NiFiComponentVO> nifiComponents = new ArrayList<>();
}
