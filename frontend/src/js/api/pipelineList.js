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
      .put(`/pipeline/run-status/${id}`, null, { params : {status: status }})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  deletePipeline(id){
    return axios
    .delete(`/pipeline/complete/${id}`)
    .then((response) => {
      return response.data;
    })
    .catch((error) => error);
  }
  deleteTempPipeline(id){
    return axios
    .delete(`/pipeline/drafts/${id}`)
    .then((response) => {
      return response.data;
    })
    .catch((error) => error);
  }
}

export default new pipelineList();
