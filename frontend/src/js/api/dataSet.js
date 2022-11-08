import axios from "axios";

class dataSet {
  getDataSet() {
    return axios
      .get(`/datasets/list`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }

}

export default new dataSet();