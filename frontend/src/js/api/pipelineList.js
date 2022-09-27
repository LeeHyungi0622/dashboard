import axios from "axios";

class pipelineList {
  getPipelineList() {
    return axios
      .post(`/pipelineList`, {})
      .then((response) => {
        return response;
      })
      .catch((error) => error);
  }
}

export default new pipelineList();
