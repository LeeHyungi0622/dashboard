import Vue from "vue";
import App from "./App.vue";
import Axios from "axios";
import router from "./router";
import vuetify from "@/plugins/vuetify.js";
import css from "@/assets/css/common.css";
import { store } from "./vuex/store";

let ws = null;
if(self.location.protocol == "http:") ws = new WebSocket("ws://"+self.location.hostname+ ":" + self.location.port+"/webpipeline");
else ws = new WebSocket("wss://"+self.location.hostname+ ":" + self.location.port+"/webpipeline");

Axios.defaults.baseURL = self.location.protocol + "//"+self.location.hostname + ":" + self.location.port;
// Axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';
// Axios.defaults.httpsAgent
Axios.defaults.headers.common['Authorization'] = 'testCode' // 수정예정
Axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8'
Axios.defaults.headers.timeout = 60000

Vue.config.productionTip = false;
Vue.prototype.$axios = Axios;
Vue.use(vuetify);
Vue.prototype.$ws = ws;

new Vue({
  css,
  router,
  vuetify,
  store,
  render: (h) => h(App),
}).$mount("#app");
