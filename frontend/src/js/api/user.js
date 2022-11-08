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
      .get(`/logout`, {headers:{ Authorization: 'Bearer '+ get_cookie('chaut')  }})
      .then((response) => {
        return response;
      })
      .catch((error) => error);
  }
  get_cookie(name) {
    var value = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return value? value[2] : null;
  }
}

export default new userInfo();