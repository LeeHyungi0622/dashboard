import axios from "axios";

class pipelineUpdate {
  getPipelineListById(id) {
    return axios
      .get(`/pipelines/completed/${id}`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new pipelineUpdate();
