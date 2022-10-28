import axios from "axios";
import { APIHandler } from './api-handler.js';
class userInfo {
  getUserInfo() {
    return axios
      .get(APIHandler.buildUrl(['user']))
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  sendLogOut() {
    return axios
      .get(APIHandler.buildUrl(['logout']))
      .then((response) => {
        return response;
      })
      .catch((error) => error);
  }
}

export default new userInfo();