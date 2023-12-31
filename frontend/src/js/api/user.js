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
  getSecurityInfo() {
    return axios
      .get(APIHandler.buildUrl(['security']))
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  //TODO Server Info 불러오는 API 추가 (Security, SSL, TLS, HTTPS, HTTP, NiFI URL -> 추후 리다이렉트 페이지 기능 추가 예정)
  getServerInfo() {
    return axios
      .get(APIHandler.buildUrl(['security']))
      .then((response) => {
        return response.data;
      })
      .catch((error) => error);
  }
  get_cookie(name) {
    var value = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return value? value[2] : null;
  }
  sendLogOut() {
    return axios
      // .get(`/logout`, {headers:{ Authorization: 'Bearer '+ this.get_cookie('chaut')  }})
      .get(`/logout`)
      .then((response) => {
        return response;
      })
      .catch((error) => error);
  }
}

export default new userInfo();