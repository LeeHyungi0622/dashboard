import axios from "axios";

class pipelineUpdate {
  getPipelineListById(id) {
    return axios
      .get(`/pipeline/complete/${id}`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new pipelineUpdate();
