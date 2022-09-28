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
}

export default new collector();
