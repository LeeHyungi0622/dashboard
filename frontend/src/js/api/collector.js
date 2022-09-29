import axios from "axios";

class collector {
  getCollectorList() {
    return axios
      .get(`/pipeline/complete/collectors`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineComplete(param) {
    return axios
      .get(`/pipeline/complete/properties`, { param: param })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getPipelineDraft(param) {
    return axios
      .get(`/pipeline/drafts/properties`, { param: param })
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new collector();
