import axios from "axios";

class pipelineRegister {
  craetePipelineDraft(requestBody) {
    return axios
      .post(`/pipelines/drafts`, requestBody)
      .then((response) => {
        return response;
      })
      .catch((error) => error);
  }
  getPipelineVo() {
    return axios
      .get(`/pipelines/drafts/create`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineDraft(id) {
    return axios
      .get(`/pipelines/drafts/${id}`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  postPipelineCompleted(id, requestBody) {
    return axios
      .post(`/pipelines/completed/${id}`, requestBody)
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  postPipelineDraft(requestBody) {
    return axios
      .post(`/pipelines/drafts`, requestBody)
      .then((response) => {
        return response;
      })
      .catch((error) => error);
  }
}

export default new pipelineRegister();
