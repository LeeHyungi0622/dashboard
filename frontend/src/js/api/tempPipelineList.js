import axios from "axios";

class tempPipelineList {
  getTempPipelineList() {
    return axios
      .get(`/pipeline/drafts/list`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new tempPipelineList();
