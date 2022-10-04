import axios from "axios";

class pipelineRegister {
  craetePipelineDraft(requestBody) {
    return axios
      .post(`/pipeline/drafts`, requestBody)
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineDraft(id) {
    return axios
      .get(`/pipeline/drafts/${id}`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineVo() {
    return axios
      .get(`/pipeline/drafts/create`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new pipelineRegister();
