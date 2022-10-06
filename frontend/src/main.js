import Vue from "vue";
import App from "./App.vue";
import Axios from "axios";
import router from "./router";
import vuetify from "@/plugins/vuetify.js";
import css from "@/assets/css/common.css";
import { store } from "./vuex/store";

const ws = new WebSocket("ws://localhost:8099/webpipeline");

Axios.defaults.baseURL = "http://localhost:8099";
Axios.defaults.headers.post['Content-Type'] = 'application/json;charset=utf-8';
Axios.defaults.headers.post['Access-Control-Allow-Origin'] = '*';
Axios.defaults.headers.get['Access-Control-Allow-Origin'] = '*';

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
