import axios from "axios";

class userInfo {
  getUserInfo() {
    return axios
      .get(`/user`, {})
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
}

export default new userInfo();