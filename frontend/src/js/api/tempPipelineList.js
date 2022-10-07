import axios from "axios";

class tempPipelineList {
  getTempPipelineList() {
    return axios
      .get(`/pipelines/drafts/list`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineList() {
    return axios
      .get(`/pipeline/complete/list`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }

}

export default new tempPipelineList();
