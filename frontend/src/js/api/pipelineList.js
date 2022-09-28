import axios from "axios";

class pipelineList {
  getPipelineList() {
    return axios
      .get(`/pipeline/complete/list`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new pipelineList();
