import axios from "axios";

class collector {
  getCollectorList() {
    return axios
      .get(`/pipelines/collectors`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineComplete(params) {
    return axios
      .get(`/pipelines/completed/properties`, { params: params })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineDraft(params) {
    return axios
      .get(`/pipelines/drafts/properties`, { params: params })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  postPipelineDraft(requestBody) {
    return axios
      .post(`/pipelines/drafts`, requestBody)
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new collector();
