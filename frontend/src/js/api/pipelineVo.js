import axios from "axios";

class pipelineVo {
  getPipelineDraft(param) {
    return axios
      .get(`/pipeline/complete/properties`, { param: param })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new pipelineVo();
