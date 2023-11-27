import Vue from "vue";
import App from "./App.vue";
import Axios from "axios";
import router from "./router";
import vuetify from "@/plugins/vuetify.js";
import css from "@/assets/css/common.css";
import { store } from "./vuex/store";

const NODE_ENV = process.env.NODE_ENV;
let servicePort = "";
let serviceProtocol = "";

servicePort = self.location.port;
serviceProtocol = self.location.protocol;
if (NODE_ENV === "development") {
  servicePort = "8099";
  serviceProtocol = "http://";
} 
let ws = null;
if(self.location.protocol.includes("https")) {
  ws = new WebSocket("wss://"+self.location.hostname+ ":" + servicePort+"/webpipeline");
  serviceProtocol = "https://";
}
else {
  ws = new WebSocket("ws://"+self.location.hostname+ ":" + servicePort+"/webpipeline");
  serviceProtocol = "http://";
}

Axios.defaults.baseURL = serviceProtocol + self.location.hostname + ":" + servicePort;


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
