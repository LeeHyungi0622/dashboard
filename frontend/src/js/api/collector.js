import axios from "axios";

class collector {
  getCollectorList() {
    return axios
      .get(`/pipeline/collectors`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineComplete(params) {
    return axios
      .get(`/pipeline/complete/properties`, { params: params })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineDraft(params) {
    return axios
      .get(`/pipeline/drafts/properties`, { params: params })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new collector();
