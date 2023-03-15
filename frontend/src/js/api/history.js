import axios from "axios";

class history {
  getHistorytCmd(id) {
    return axios
      .get(`pipelines/hist/cmd/${id}`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  getHistorytask(id) {
    return axios
      .get(`pipelines/hist/task/${id}`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  
}

export default new history();