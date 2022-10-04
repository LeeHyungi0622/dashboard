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
  putPipelineStatus(id, status) {
    return axios
      .put(`/pipeline/run-status/${id}`, { param: { status: status } })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new pipelineList();
