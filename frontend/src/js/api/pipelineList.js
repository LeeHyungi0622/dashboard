import axios from "axios";

class pipelineList {
  getPipelineList() {
    return axios
      .get(`/pipelines/completed`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getTempPipelineList() {
    return axios
      .get(`/pipelines/drafts/list`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  putPipelineStatus(id, status) {
    return axios
      .put(`/pipelines/run-status/${id}`, null, { params : {status: status }})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  deletePipeline(id){
    return axios
    .delete(`/pipelines/completed/${id}`)
    .then((response) => {
      return response.data;
    })
    .catch((error) => error);
  }
  deleteTempPipeline(id){
    return axios
    .delete(`/pipelines/drafts/${id}`)
    .then((response) => {
      return response.data;
    })
    .catch((error) => error);
  }
}

export default new pipelineList();
